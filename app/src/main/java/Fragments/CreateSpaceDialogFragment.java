package Fragments;

import static Utils.Constants.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.SwitchCompat;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import API.SingleVolley;
import Utils.TokenValidator;
import Models.SpaceRule;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateSpaceDialogFragment extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private String imageBase64 = "";
    private Runnable onSpaceCreated;
    private CircleImageView imgProfile;
    private List<SpaceRule> spaceRuleList = new ArrayList<>();

    EditText editSpaceName, editCapacity;
    TextView txtChangePhoto;
    Spinner spinnerSpaceRules;
    SwitchCompat swAvailability;
    Button btnCreate,btnCancel;

    String spaceName, capacityStr;
    int selectedRuleId, capacity;
    boolean availability;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_space, null);

        initViews(view);
        setupSpaceRulesSpinner();

        return new AlertDialog.Builder(requireContext()).setView(view).setCancelable(false).create();
    }

    public void initViews(View view){
        editSpaceName = view.findViewById(R.id.editSpaceNameC);
        editCapacity = view.findViewById(R.id.editCapacityC);
        txtChangePhoto = view.findViewById(R.id.txtChangePhotoCreateSpace);
        imgProfile = view.findViewById(R.id.imgProfileCreateSpace);

        spinnerSpaceRules = view.findViewById(R.id.editSpinnerSpaceRules);
        swAvailability = view.findViewById(R.id.swAvailabilityC);

        btnCreate = view.findViewById(R.id.btnCreate);
        btnCancel = view.findViewById(R.id.btnCancel);

        imgProfile.setOnClickListener(v -> openGallery());
        txtChangePhoto.setOnClickListener(v -> openGallery());
        btnCreate.setOnClickListener(v -> {createSpace();});
        btnCancel.setOnClickListener(v -> dismiss());
    }

    public void setupSpaceRulesSpinner(){
        List<String> ruleNames = new ArrayList<>();
        for (SpaceRule rule : spaceRuleList) {
            ruleNames.add(rule.getRule());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ruleNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpaceRules.setAdapter(spinnerAdapter);
    }

    public void validateForm(){
        spaceName = editSpaceName.getText().toString().trim();
        capacityStr = editCapacity.getText().toString().trim();

        availability = swAvailability.isChecked();

        if (TextUtils.isEmpty(spaceName) || TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = spinnerSpaceRules.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= spaceRuleList.size()) {
            Toast.makeText(getContext(), "Please select a valid rule", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedRuleId = spaceRuleList.get(selectedIndex).getId();
    }

    public void createSpace(){
        validateForm();

        try {
            capacity = Integer.parseInt(capacityStr);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Capacity must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating space...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("SpaceName", spaceName);
            json.put("Capacity", capacity);
            json.put("spaceRuleId", selectedRuleId);
            json.put("Availability", availability);
            json.put("imageBase64", imageBase64);

            JsonObjectRequest request = getJsonObjectRequest(json, progressDialog);

            SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(LOG_TAG, "JSON exception: " + e.getMessage());
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(JSONObject jsonBody, ProgressDialog progressDialog) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL + SPACES_ENDPOINT, jsonBody,
                response -> {
                    Toast.makeText(getContext(), "Space created successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                    if (onSpaceCreated != null) {
                        onSpaceCreated.run();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error creating space", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return request;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 340, 200, true);

                imgProfile.setImageBitmap(resizedBitmap);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setSpaceRuleList(List<SpaceRule> spaceRuleList) {
        this.spaceRuleList = spaceRuleList;
    }

    public void setOnSpaceCreated(Runnable onSpaceCreated) {
        this.onSpaceCreated = onSpaceCreated;
    }

    @Override
    public void onResume() {
        super.onResume();
        TokenValidator.validateToken(requireContext());
    }
}
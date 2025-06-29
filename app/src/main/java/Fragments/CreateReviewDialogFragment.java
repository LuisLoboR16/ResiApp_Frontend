package Fragments;

import static Utils.Constants.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import API.SingleVolley;
import Utils.TokenValidator;
import Models.Space;
import Models.User;

public class CreateReviewDialogFragment extends DialogFragment {
    private User currentUser;
    private Runnable onReviewCreated;
    private List<Space> spaceList = new ArrayList<>();

    EditText editComment, editUser;
    Spinner spinnerSpace, spinnerRating;
    Button btnCreate, btnCancel;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_review, null);

        initViews(view);
        loadUserPreferences();
        setupRatingSpinner();
        setupSpaceSpinner();

        return new AlertDialog.Builder(requireContext()).setView(view).setCancelable(false).create();
    }

    public void initViews(View view){
        editComment = view.findViewById(R.id.editComment);
        editUser = view.findViewById(R.id.editUserReview);

        spinnerSpace = view.findViewById(R.id.editSpinnerSpace);
        spinnerRating = view.findViewById(R.id.editSpinnerRating);

        btnCreate = view.findViewById(R.id.btnCreateCReview);
        btnCancel = view.findViewById(R.id.btnCancelCReview);

        editUser.setEnabled(false);

        btnCreate.setOnClickListener(v -> {createReview();});
        btnCancel.setOnClickListener(v -> dismiss());
    }

    public void loadUserPreferences(){
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String residentName = prefs.getString("resident_name", null);
        int userId = prefs.getInt("user_id", -1);

        if (residentName != null && userId != -1) {
            currentUser = new User();
            currentUser.setId(userId);
            currentUser.setResidentName(residentName);
            editUser.setText(currentUser.getResidentName());
        }
    }

    private void setupRatingSpinner(){
        List<Integer> ratingOptions = Arrays.asList(1, 2, 3, 4, 5);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ratingOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(adapter);
    }

    private void setupSpaceSpinner(){
        List<String> spaceNames = new ArrayList<>();
        for (Space name : spaceList) {
            spaceNames.add(name.getSpaceName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spaceNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpace.setAdapter(spinnerAdapter);
    }

    public void createReview(){
        String comment = editComment.getText().toString().trim();

        int selectedIndex = spinnerSpace.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= spaceList.size()) {
            Toast.makeText(getContext(), "Please select a valid space", Toast.LENGTH_SHORT).show();
            return;
        }

        if(comment.isEmpty()){
            Toast.makeText(getContext(), "Please fill comments", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRating = (int) spinnerRating.getSelectedItem();

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating review...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("rating", selectedRating);
            json.put("comment", comment);
            json.put("residentId",currentUser.getId());
            json.put("spaceId", spaceList.get(selectedIndex).getId());

            JsonObjectRequest request = getJsonObjectRequest(json, progressDialog);
            SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(JSONObject jsonBody, ProgressDialog progressDialog) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL + REVIEWS_ENDPOINT, jsonBody,
                response -> {
                    Toast.makeText(getContext(), "Review created successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                    if (onReviewCreated != null) {
                        onReviewCreated.run();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error creating review", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public String getBodyContentType() {return "application/json; charset=utf-8";}
        };
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return request;
    }

    public void setSpaceList(List<Space> spaceList) {
        this.spaceList = spaceList;
    }

    public void setUser(User currentUser) {
        this.currentUser = currentUser;
    }
    public void setOnReviewCreated(Runnable onReviewCreated) {
        this.onReviewCreated = onReviewCreated;
    }

    @Override
    public void onResume() {
        super.onResume();
        TokenValidator.validateToken(requireContext());
    }
}
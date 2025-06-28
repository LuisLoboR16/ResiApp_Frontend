package Fragments;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import API.Constants;
import API.SingleVolley;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateUserDialogFragment extends DialogFragment {
    static final String URL = Constants.URL;
    static final String CREATE = Constants.USERS_ENDPOINT;
    static final String REGEX_EMAIL = Constants.REGEX_EMAIL;
    static final String LOG_TAG = Constants.LOG_TAG;
    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView imgProfile;
    private String imageBase64 = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_user, null);

        EditText editName = view.findViewById(R.id.editName);
        EditText editEmail = view.findViewById(R.id.editEmail);
        EditText editPassword = view.findViewById(R.id.editPassword);
        EditText editRePassword = view.findViewById(R.id.editRePassword);
        EditText editApartment = view.findViewById(R.id.editApartmentInformation);
        TextView txtChangePhoto = view.findViewById(R.id.txtChangePhotoCreateUser);

        imgProfile = view.findViewById(R.id.imgProfile);

        Button btnCreate = view.findViewById(R.id.btnCreate);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        imgProfile.setOnClickListener(v -> openGallery());
        txtChangePhoto.setOnClickListener(v -> openGallery());

        btnCreate.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String email = editEmail.getText().toString();
            String pass1 = editPassword.getText().toString();
            String pass2 = editRePassword.getText().toString();
            String apt = editApartment.getText().toString();
            String role = "Resident";

            if (!validateForm(name, email, pass1, pass2, apt)) return;

            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Creating user...");
            progressDialog.show();

            try {
                JSONObject jsonBody = createUserJson(name, email, pass1, apt, role, imageBase64);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        URL + CREATE,
                        jsonBody,
                        response -> {
                            Toast.makeText(getContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dismiss();
                        },
                        error -> {
                            progressDialog.dismiss();
                            handleServerError(error);
                        }
                )
                {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                };

                request.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));

                SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);

            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e(LOG_TAG, "JSON exception: " + e.getMessage());
                Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean validateForm(String name, String email, String pass1, String pass2, String apt){
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass1)
                || TextUtils.isEmpty(pass2) || TextUtils.isEmpty(apt)) {
             showToast("Please fill all fields.");
            return false;
        }
        if (!email.matches(REGEX_EMAIL)) {
            showToast("Please enter a valid email address.");
            return false;
        }
        if (!pass1.equals(pass2)) {
            showToast("Passwords do not match.");
            return false;
        }
        if (pass1.length() < 6) {
            showToast("Password must be 6 or more characters.");
            return false;
        }
        return true;
    }

    private JSONObject createUserJson(String name, String email, String pass1, String apt, String role, String imageBase64) throws Exception {
        JSONObject json = new JSONObject();
        json.put("residentName", name);
        json.put("email", email);
        json.put("password", pass1);
        json.put("apartmentInformation", apt);
        json.put("imageBase64", imageBase64);
        json.put("role", role);
        return json;
    }

    private void handleServerError(com.android.volley.VolleyError error) {
        String errorMessage = "Unexpected error";

        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Log.e(LOG_TAG, "Server response: " + body);

                JSONObject jsonError = new JSONObject(body);

                if (jsonError.has("errors")) {
                    JSONObject errors = jsonError.getJSONObject("errors");
                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < Objects.requireNonNull(errors.names()).length(); i++) {
                        String field = errors.names().getString(i);
                        JSONArray messages = errors.getJSONArray(field);

                        for (int j = 0; j < messages.length(); j++) {
                            builder.append("• ").append(messages.getString(j)).append("\n");
                        }
                    }
                    errorMessage = builder.toString().trim();

                } else if (jsonError.has("title")) {
                    errorMessage = jsonError.getString("title");
                }
            } else {
                errorMessage = "No response from server.";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing error response: " + e.getMessage());
            errorMessage = "Failed to parse server response.";
        }
        showToast(errorMessage);
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
}
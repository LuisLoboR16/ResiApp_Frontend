package Fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.resiapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import API.Constants;
import Models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateConfigurationsDialogFragment extends DialogFragment {
    static final String URL = Constants.URL;
    static final String GET = URL + Constants.FIND_BY_USER_ID_ENDPOINT;
    static final String UPDATE = URL + Constants.USERS_ENDPOINT + "/";
    static final String LOG_TAG = Constants.LOG_TAG;
    private static final int PICK_IMAGE_REQUEST = 1;

    private User currentUser;
    private String imageBase64 = "";
    private EditText etName, etEmail, etPass, etPass2, etApartmentInfo;
    private CircleImageView imgProfile;

    public void setUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_configurations, null);
        initializeViews(view);
        setupUserData();
        setupListeners(view);
        loadUserData();

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }

    private void initializeViews(View view) {
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPass = view.findViewById(R.id.etPass);
        etPass2 = view.findViewById(R.id.etPass2);
        etApartmentInfo = view.findViewById(R.id.etApartmentInformation);
        imgProfile = view.findViewById(R.id.imgProfileUpdating);
    }

    private void setupUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId != -1) {
            currentUser = new User();
            currentUser.setId(userId);
        }
    }

    private void setupListeners(View view) {
        TextView txtChangePhoto = view.findViewById(R.id.txtChangePhotoUpdating);
        View.OnClickListener imagePicker = v -> openGallery();
        imgProfile.setOnClickListener(imagePicker);
        txtChangePhoto.setOnClickListener(imagePicker);

        Button btnCancel = view.findViewById(R.id.btnCancelConfig);
        Button btnUpdate = view.findViewById(R.id.btnUpdateConfig);
        btnCancel.setOnClickListener(v -> dismiss());
        btnUpdate.setOnClickListener(v -> updateUser());
    }

    private void updateUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String pass2 = etPass2.getText().toString().trim();
        String aptInfo = etApartmentInfo.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(aptInfo)) {
            showToast("Please fill all fields");
            return;
        }
        if (!pass.equals(pass2)) {
            showToast("Passwords do not match");
            return;
        }

        ProgressDialog progressDialog = ProgressDialog.show(requireContext(), null, "Updating user...");

        try {
            JSONObject json = new JSONObject();
            json.put("residentName", name);
            json.put("email", email);
            json.put("password", pass);
            json.put("apartmentInformation", aptInfo);
            json.put("imageBase64", imageBase64);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    UPDATE + currentUser.getId(),
                    json,
                    response -> {
                        showToast("User updated successfully");
                        progressDialog.dismiss();
                        dismiss();
                    },
                    error -> {
                        progressDialog.dismiss();
                        showServerError(error);
                    }
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            Volley.newRequestQueue(requireContext()).add(request);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Execution JSON: " + e.getMessage());
            progressDialog.dismiss();
        }
    }

    private void loadUserData() {
        String url = GET + currentUser.getId();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            etName.setText(data.optString("residentName", ""));
                            etEmail.setText(data.optString("email", ""));
                            etApartmentInfo.setText(data.optString("apartmentInformation", ""));
                            setImage(data.optString("imageBase64", ""));
                        } else {
                            showToast("Failed to load info");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast("Error processing data");
                    }
                },
                error -> {
                    showToast("Error with request");
                    error.printStackTrace();
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showServerError(com.android.volley.VolleyError error) {
        StringBuilder errorMessage = new StringBuilder();

        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String errorBody = new String(error.networkResponse.data);
                JSONObject errorJson = new JSONObject(errorBody);
                if (errorJson.has("errors")) {
                    JSONObject errors = errorJson.getJSONObject("errors");
                    for (int i = 0; i < errors.names().length(); i++) {
                        String key = errors.names().getString(i);
                        JSONArray messages = errors.getJSONArray(key);
                        for (int j = 0; j < messages.length(); j++) {
                            errorMessage.append("\u2022 ").append(messages.getString(j)).append("\n");
                        }
                    }
                } else if (errorJson.has("title")) {
                    errorMessage.append(errorJson.getString("title"));
                } else {
                    errorMessage.append("Unexpected server error.");
                }
            } catch (Exception e) {
                errorMessage.append("An error occurred while parsing the error response.");
            }
        } else {
            errorMessage.append("Network error occurred. Please try again.");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Error updating user")
                .setMessage(errorMessage.toString())
                .setPositiveButton("Accept", null)
                .show();
    }

    private void setImage(String imageString) {
        if (imageString.startsWith("data:image")) {
            try {
                String encoded = imageString.split(",")[1];
                byte[] imageBytes = Base64.decode(encoded, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imgProfile.setImageBitmap(bitmap);
                imageBase64 = imageString;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error decoding image: " + e.getMessage());
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                imgProfile.setImageBitmap(bitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();
                imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error loading image: " + e.getMessage());
            }
        }
    }

    interface OnImageSelected {
        void onSelected(Bitmap image);
    }
}

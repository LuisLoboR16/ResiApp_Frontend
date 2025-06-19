package Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    static final String UPDATE = URL + Constants.USERS_ENDPOINT+"/";
    static final String LOG_TAG = Constants.LOG_TAG;
    private EditText etName, etEmail, etPass, etPass2, etApartmentInfo;
    private CircleImageView imgProfile;
    private User currentUser;
    public void setUser(User currentUser) {
        this.currentUser = currentUser;
    }
    private static final int PICK_IMAGE_REQUEST = 1;


    @SuppressLint("MissingInflatedId")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_configurations, null);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            currentUser = new User();
            currentUser.setId(userId);
        }

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPass = view.findViewById(R.id.etPass);
        etPass2 = view.findViewById(R.id.etPass2);
        etApartmentInfo = view.findViewById(R.id.etApartmentInformation);
        imgProfile = view.findViewById(R.id.imgProfile);

        Button btnCancel = view.findViewById(R.id.btnCancelConfig);
        Button btnUpdate = view.findViewById(R.id.btnUpdateConfig);
        TextView btnChangePhoto = view.findViewById(R.id.txtChangePhoto);

        btnChangePhoto.setOnClickListener(v -> openGallery());
        btnUpdate.setOnClickListener(v -> updateUser());

        btnCancel.setOnClickListener(v -> dismiss());

        loadUserData();

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                imgProfile.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String pass2 = etPass2.getText().toString().trim();
        String aptInfo = etApartmentInfo.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(aptInfo)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(pass2)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Updating user...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("residentName", etName.getText().toString().trim());
            json.put("email", etEmail.getText().toString().trim());
            json.put("password", pass);
            json.put("apartmentInformation", aptInfo);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                     UPDATE + currentUser.getId(),
                    json,
                    response -> {
                        Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        dismiss();
                    },
                    error -> {
                        Log.e(LOG_TAG, "Error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e(LOG_TAG, "Status code: " + error.networkResponse.statusCode);
                            if (error.networkResponse.data != null) {
                                String errorBody = new String(error.networkResponse.data);
                                Log.e(LOG_TAG, "Error body: " + errorBody);
                            }
                        }
                        Toast.makeText(getContext(), "Error updating user", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
            ) {
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

            Volley.newRequestQueue(requireContext()).add(request);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Execption JSON: " + e.getMessage());
            progressDialog.dismiss();
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("passwordHash", pass);
            jsonBody.put("apartmentInformation",aptInfo);
            /*
            if (selectedImageBitmap != null) {
                jsonBody.put("photo", convertirImagenBase64(selectedImageBitmap));
            }
             */
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

    }

    /*
    private String convertirImagenBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
     */

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

                        } else {
                            Toast.makeText(getContext(), "Failed to load info", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error processing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });
        Volley.newRequestQueue(requireContext()).add(request);
    }
}
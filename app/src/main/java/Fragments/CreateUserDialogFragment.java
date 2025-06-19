package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;

import org.json.JSONObject;

import API.Constants;
import API.SingleVolley;

public class CreateUserDialogFragment extends DialogFragment {
    static final String URL = Constants.URL;
    static final String CREATE = Constants.USERS_ENDPOINT;
    static final String LOG_TAG = Constants.LOG_TAG;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_user, null);

        EditText editName = view.findViewById(R.id.editName);
        EditText editEmail = view.findViewById(R.id.editEmail);
        EditText editPassword = view.findViewById(R.id.editPassword);
        EditText editRePassword = view.findViewById(R.id.editRePassword);
        EditText editApartment = view.findViewById(R.id.editApartmentInformation);
        Button btnCreate = view.findViewById(R.id.btnCreate);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnCreate.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String email = editEmail.getText().toString();
            String pass1 = editPassword.getText().toString();
            String pass2 = editRePassword.getText().toString();
            String apt = editApartment.getText().toString();
            String role = "Resident";

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass1) || TextUtils.isEmpty(pass2) || TextUtils.isEmpty(apt)) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass1.equals(pass2)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Creating user...");
            progressDialog.show();

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("residentName", name);
                jsonBody.put("email", email);
                jsonBody.put("password", pass1);
                jsonBody.put("apartmentInformation", apt);
                jsonBody.put("role", role);

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
                            String errorMessage = "Unexpected error.";

                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;

                                switch (statusCode) {
                                    case 400:
                                        errorMessage = "Verify your data, something is wrong.";
                                        break;
                                    case 401:
                                        errorMessage = "Not authorized, check your credentials.";
                                        break;
                                    case 409:
                                        errorMessage = "Email already exists.";
                                        break;
                                    case 500:
                                        errorMessage = "Server error, please try again later.";
                                        break;
                                    default:
                                        errorMessage = "Unknown error.";
                                }

                                Log.e(LOG_TAG, "Status code: " + statusCode);

                                if (error.networkResponse.data != null) {
                                    Log.e(LOG_TAG, "Body: " + new String(error.networkResponse.data));
                                }

                            } else {
                                errorMessage = "No network response. Check your internet connection or server URL.";
                                Log.e(LOG_TAG, "VolleyError: No network response (null)");
                            }

                            Log.e(LOG_TAG, "VolleyError: " + error.toString());
                            Toast.makeText(getContext(), "Error creating user: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e(LOG_TAG, "Volley error: " + error);
                            Log.e(LOG_TAG, "Status code: " + error.networkResponse.statusCode);
                            if (error.networkResponse.data != null) {
                                Log.e(LOG_TAG, "Body: " + new String(error.networkResponse.data));
                            }
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
}
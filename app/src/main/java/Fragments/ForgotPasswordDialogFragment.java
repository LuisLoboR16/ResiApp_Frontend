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
import com.android.volley.toolbox.Volley;
import com.example.resiapp.R;

import org.json.JSONObject;

import API.Constants;

public class ForgotPasswordDialogFragment extends DialogFragment {
    static final String URL = Constants.URL;
    static final String UPDATE = URL + Constants.FORGOT_PASSWORD_ENDPOINT;
    static final String LOG_TAG = Constants.LOG_TAG;

    EditText editEmail,editSecurityWord,editPassword,editRePassword;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_forgot_password, null);

         editEmail = view.findViewById(R.id.etEmailForgot);
         editSecurityWord = view.findViewById(R.id.etSecurityWord);
         editPassword = view.findViewById(R.id.etPassForgot);
         editRePassword = view.findViewById(R.id.etPass2Forgot);

        Button btnUpdate = view.findViewById(R.id.btnUpdateForgot);
        Button btnCancel = view.findViewById(R.id.btnCancelForgot);

        btnUpdate.setOnClickListener(v ->updateUser());
        btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }

    private void updateUser() {
        String email = editEmail.getText().toString();
        String securityWord = editSecurityWord.getText().toString();
        String pass = editPassword.getText().toString();
        String pass2 = editRePassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(pass2) || TextUtils.isEmpty(securityWord)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(pass2)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating password...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("email",email);
            json.put("newPassword", pass);
            json.put("securityWord",securityWord);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    UPDATE,
                    json,
                    response -> {
                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        dismiss();
                    },
                    error -> {
                        Log.e(LOG_TAG, "Error: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            Log.e(LOG_TAG, "Status code: " + statusCode);

                            if (error.networkResponse.data != null) {
                                String errorBody = new String(error.networkResponse.data);
                                Log.e(LOG_TAG, "Error body: " + errorBody);
                            }
                            switch (statusCode) {
                                case 400:
                                    Toast.makeText(getContext(), "Security word is incorrect", Toast.LENGTH_LONG).show();
                                    break;
                                case 401:
                                    Toast.makeText(getContext(), "Unauthorized request", Toast.LENGTH_LONG).show();
                                    break;
                                case 403:
                                    Toast.makeText(getContext(), "Access denied", Toast.LENGTH_LONG).show();
                                    break;
                                case 404:
                                    Toast.makeText(getContext(), "Resource not found", Toast.LENGTH_LONG).show();
                                    break;
                                case 500:
                                    Toast.makeText(getContext(), "Server error. Please try again later", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(getContext(), "Unexpected error occurred: " + statusCode, Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(getContext(), "No response from server. Check your connection.", Toast.LENGTH_LONG).show();
                        }
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
    }
}
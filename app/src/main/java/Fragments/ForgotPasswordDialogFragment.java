package Fragments;

import android.annotation.SuppressLint;
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
    static final String UPDATE = URL + Constants.USERS_ENDPOINT;
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

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnUpdate = view.findViewById(R.id.btnUpdateForgot);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnCancel = view.findViewById(R.id.btnCancelForgot);

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

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(pass2) || TextUtils.isEmpty(securityWord)) {
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
            json.put("password", pass);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    UPDATE + email,
                    json,
                    response -> {
                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Error updating password", Toast.LENGTH_LONG).show();
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
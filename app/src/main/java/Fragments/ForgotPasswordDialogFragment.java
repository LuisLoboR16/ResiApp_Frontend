package Fragments;

import static Utils.Constants.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

import Utils.Constants;

public class ForgotPasswordDialogFragment extends DialogFragment {
    static final String UPDATE = URL + Constants.FORGOT_PASSWORD_ENDPOINT;

    EditText editEmail;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_forgot_password, null);

        initViews(view);

        return new AlertDialog.Builder(requireContext()).setView(view).create();
    }

    public void initViews(View view){
        editEmail = view.findViewById(R.id.etEmailForgot);

        Button btnUpdate = view.findViewById(R.id.btnUpdateForgot);
        Button btnCancel = view.findViewById(R.id.btnCancelForgot);

        btnUpdate.setOnClickListener(v ->updateUser());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateUser() {
        String email = editEmail.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Please fill email", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating password...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("email",email);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, UPDATE, json,
                    response -> {
                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        dismiss();
                    },
                    error -> {
                            Toast.makeText(getContext(), "No response from server. Check your connection.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(requireContext()).add(request);

        } catch (Exception e) {
            progressDialog.dismiss();
        }
    }
}
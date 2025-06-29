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
import com.example.resiapp.R;

import org.json.JSONObject;

import API.SingleVolley;
import Utils.TokenValidator;

public class CreateSpaceRuleDialogFragment extends DialogFragment {
    private Runnable onSpaceCreated;

    EditText editSpaceRuleName;
    Button btnCreate,btnCancel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_space_rule, null);

        initViews(view);

        return new AlertDialog.Builder(requireContext()).setView(view).setCancelable(false).create();
    }

    public void initViews(View view){
        editSpaceRuleName = view.findViewById(R.id.editSpaceRuleName);

        btnCreate = view.findViewById(R.id.btnCreateSpaceRule);
        btnCancel = view.findViewById(R.id.btnCancelSpaceRule);

        btnCreate.setOnClickListener(v -> {createSpaceRule();});
        btnCancel.setOnClickListener(v -> dismiss());
    }

    public void createSpaceRule(){
        String spaceRuleName = editSpaceRuleName.getText().toString().trim();

        if (TextUtils.isEmpty(spaceRuleName)) {
            Toast.makeText(getContext(), "Please fill space rule name", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating space rule...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("rule", spaceRuleName);

            JsonObjectRequest request = getJsonObjectRequest(json, progressDialog);
            SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(JSONObject jsonBody, ProgressDialog progressDialog) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL + SPACE_RULES_ENDPOINT, jsonBody,
                response -> {
                    Toast.makeText(getContext(), "SpaceRule created successfully", Toast.LENGTH_SHORT).show();
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

    public void setOnSpaceCreated(Runnable onSpaceCreated) {
        this.onSpaceCreated = onSpaceCreated;
    }

    @Override
    public void onResume() {
        super.onResume();
        TokenValidator.validateToken(requireContext());
    }
}
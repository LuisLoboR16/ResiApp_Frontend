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
import androidx.appcompat.widget.SwitchCompat;
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

public class CreateSpaceDialogFragment extends DialogFragment {
    static final String URL = "http://10.0.2.2:5069/api/";
    static final String CREATE = "Space";
    static final String LOG_TAG = "ResiApp";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_space, null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editSpaceName = view.findViewById(R.id.editSpaceNameC);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editCapacity = view.findViewById(R.id.editCapacityC);
        SwitchCompat editAvailability = view.findViewById(R.id.swAvailabilityC);

        Button btnCreate = view.findViewById(R.id.btnCreate);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnCreate.setOnClickListener(v -> {
            String spaceName = editSpaceName.getText().toString().trim();
            String capacityStr = editCapacity.getText().toString().trim();
            boolean availability = editAvailability.isChecked();


            if (TextUtils.isEmpty(spaceName) || TextUtils.isEmpty(capacityStr)) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int capacity;
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
                json.put("Availability", availability);

                JsonObjectRequest request = getJsonObjectRequest(json, progressDialog);

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

    private Runnable onSpaceCreated;

    public void setOnSpaceCreated(Runnable onSpaceCreated) {
        this.onSpaceCreated = onSpaceCreated;
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(JSONObject jsonBody, ProgressDialog progressDialog) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL + CREATE,
                jsonBody,
                response -> {
                    Toast.makeText(getContext(), "Space created successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                    if (onSpaceCreated != null) {
                        onSpaceCreated.run();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "VolleyError: " + error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error creating space", Toast.LENGTH_LONG).show();
                    if (error.networkResponse != null) {
                        Log.e(LOG_TAG, "Status code: " + error.networkResponse.statusCode);
                        if (error.networkResponse.data != null) {
                            Log.e(LOG_TAG, "Body: " + new String(error.networkResponse.data));
                        }
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
        return request;
    }
}

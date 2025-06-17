package Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import API.Constants;
import API.SingleVolley;
import Models.Space;
import Models.User;

public class CreateReviewDialogFragment extends DialogFragment {
    static final String URL = Constants.URL;
    static final String CREATE = Constants.REVIEWS_ENDPOINT;
    static final String LOG_TAG = Constants.LOG_TAG;

    private List<Space> spaceList = new ArrayList<>();
    public void setSpaceList(List<Space> spaceList) {
        this.spaceList = spaceList;
    }
    private User currentUser;
    public void setUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_review, null);

        Spinner editRating = view.findViewById(R.id.editSpinnerRating);
        EditText editComment = view.findViewById(R.id.editComment);
        EditText editUser = view.findViewById(R.id.editUserReview);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Spinner editSpace = view.findViewById(R.id.editSpinnerSpace);

        Button btnCreate = view.findViewById(R.id.btnCreate);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        editUser.setEnabled(false);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String residentName = prefs.getString("resident_name", null);
        int userId = prefs.getInt("user_id", -1);

        if (residentName != null && userId != -1) {
            currentUser = new User();
            currentUser.setId(userId);
            currentUser.setResidentName(residentName);
        }

        if (currentUser != null && currentUser.getResidentName() != null) {
            editUser.setText(currentUser.getResidentName());
        }

        assert currentUser != null;
        editUser.setText(currentUser.getResidentName());

        List<Integer> ratingOptions = Arrays.asList(1, 2, 3, 4, 5);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ratingOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editRating.setAdapter(adapter);

        List<String> spaceNames = new ArrayList<>();
        for (Space name : spaceList) {
            spaceNames.add(name.getSpaceName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, spaceNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSpace.setAdapter(spinnerAdapter);

        btnCreate.setOnClickListener(v -> {
            String comment = editComment.getText().toString().trim();

            int selectedIndex = editSpace.getSelectedItemPosition();
            if (selectedIndex < 0 || selectedIndex >= spaceList.size()) {
                Toast.makeText(getContext(), "Please select a valid space", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedRating = (int) editRating.getSelectedItem();

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

    private Runnable onReviewCreated;

    public void setOnReviewCreated(Runnable onReviewCreated) {
        this.onReviewCreated = onReviewCreated;
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(JSONObject jsonBody, ProgressDialog progressDialog) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL + CREATE,
                jsonBody,
                response -> {
                    Toast.makeText(getContext(), "Review created successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                    if (onReviewCreated != null) {
                        onReviewCreated.run();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "VolleyError: " + error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error creating review", Toast.LENGTH_LONG).show();
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
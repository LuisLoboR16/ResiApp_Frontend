package Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import API.Constants;
import API.SingleVolley;

public class NotificationDialogFragment extends DialogFragment {
    static String email = "";
    static final String adminEmail = Constants.ADMIN_EMAIL;

    EditText edtEmail, edtSubject, edtComments;
    TextView txtSelectUser;
    Spinner spinnerUser;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_notification, null);

        String invokedClass = requireActivity().getClass().getSimpleName();

        edtEmail = view.findViewById(R.id.editToEmailNotification);
        edtSubject = view.findViewById(R.id.editSubject);
        edtComments = view.findViewById(R.id.editComment);
        txtSelectUser = view.findViewById(R.id.tvSelectUserNotification);
        spinnerUser = view.findViewById(R.id.editSpinnerUser);

        Button btnSend = view.findViewById(R.id.btnSend);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (invokedClass.trim().equals("ResidentDashboardActivity")) {
            edtEmail.setText(adminEmail);
            edtEmail.setEnabled(false);
            txtSelectUser.setVisibility(View.GONE);
            spinnerUser.setVisibility(View.GONE);

            email = adminEmail;

        } else {
            edtEmail.setVisibility(View.GONE);
            txtSelectUser.setVisibility(View.VISIBLE);
            spinnerUser.setVisibility(View.VISIBLE);

            loadUserEmails();
        }

        btnSend.setOnClickListener(v -> {
            String subject = edtSubject.getText().toString();
            String body = edtComments.getText().toString();

            if (!invokedClass.equals("ResidentDashboardActivity")) {
                email = spinnerUser.getSelectedItem() != null ? spinnerUser.getSelectedItem().toString() : "";
            }

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(subject) && !TextUtils.isEmpty(body)) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Select a client for emails"));
                dismiss();

            } else {
                Toast.makeText(getContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }

    private void loadUserEmails() {
        String url = Constants.URL + Constants.USERS_ENDPOINT;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");
                        List<String> emailList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject userJson = jsonArray.getJSONObject(i);
                            String userEmail = userJson.getString("email");
                            emailList.add(userEmail);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, emailList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerUser.setAdapter(adapter);

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error loading emails", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show()
        );

        SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);
    }
}
package Fragments;

import static android.content.Context.MODE_PRIVATE;

import static Utils.Constants.*;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Utils.Constants;
import API.SingleVolley;
import Utils.TokenValidator;

public class NotificationDialogFragment extends DialogFragment {
    static String email = "";

    String role, subject, body, emailCC,invokedClass;
    List<String> allUserEmails = new ArrayList<>();

    TextView txtSelectUser;
    Spinner spinnerUser;
    CheckBox checkSendToAll;
    Button btnSend,btnCancel;
    EditText edtEmail, edtSubject, edtComments;

    JSONObject jsonBody;
    JSONArray toArray, ccArrayResident;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_notification, null);
        invokedClass = requireActivity().getClass().getSimpleName();

        initViews(view);
        validateActivity();

        return new AlertDialog.Builder(requireContext()).setView(view).create();
    }

    public void initViews(View view){
        edtEmail = view.findViewById(R.id.editToEmailNotification);
        edtSubject = view.findViewById(R.id.editSubject);
        edtComments = view.findViewById(R.id.editComment);
        txtSelectUser = view.findViewById(R.id.tvSelectUserNotification);
        spinnerUser = view.findViewById(R.id.editSpinnerUser);
        checkSendToAll = view.findViewById(R.id.checkSendToAll);

        btnSend = view.findViewById(R.id.btnSend);
        btnCancel = view.findViewById(R.id.btnCancel);

        btnSend.setOnClickListener(v -> {sendEmail();});
        btnCancel.setOnClickListener(v -> dismiss());
    }

    public void validateActivity(){
        if (invokedClass.trim().equals("ResidentDashboardActivity")) {
            edtEmail.setText(ADMIN_EMAIL);
            edtEmail.setEnabled(false);
            txtSelectUser.setVisibility(View.GONE);
            spinnerUser.setVisibility(View.GONE);
            checkSendToAll.setVisibility(View.GONE);

            email = ADMIN_EMAIL;

        } else {
            edtEmail.setVisibility(View.GONE);
            txtSelectUser.setVisibility(View.VISIBLE);
            spinnerUser.setVisibility(View.VISIBLE);
            checkSendToAll.setVisibility(View.VISIBLE);

            loadUserEmails();
        }
    }

    private void loadUserEmails() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, URL + USERS_ENDPOINT, null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");
                        List<String> emailList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject userJson = jsonArray.getJSONObject(i);
                            String userEmail = userJson.getString("email");
                            emailList.add(userEmail);
                            allUserEmails.add(userEmail);
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

    public void sendEmail(){
        subject = edtSubject.getText().toString().trim();
        body = edtComments.getText().toString().trim();

        if (!invokedClass.equals("ResidentDashboardActivity")) {
            email = spinnerUser.getSelectedItem() != null ? spinnerUser.getSelectedItem().toString() : "";
        }

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(subject) && !TextUtils.isEmpty(body)) {
            try {
                jsonBody = new JSONObject();
                toArray = new JSONArray();
                toArray.put(email);

                role = getUserRoleFromPrefs();
                emailCC = getEmailFromPrefs();

                ccArrayResident = new JSONArray();
                ccArrayResident.put(emailCC);

                if(role.equals("Resident")){
                    jsonBody.put("to", toArray);
                    jsonBody.put("subject", subject);
                    jsonBody.put("body", body);
                    jsonBody.put("role", role);
                    jsonBody.put("cc", ccArrayResident);

                } else {
                    if (checkSendToAll.isChecked() && !allUserEmails.isEmpty()) {
                        for (String ccEmail : allUserEmails) {
                            if (!ccEmail.equals(email)) {
                                toArray.put(ccEmail);
                            }
                        }
                    }

                    jsonBody.put("to", toArray);
                    jsonBody.put("subject", subject);
                    jsonBody.put("body", body);
                    jsonBody.put("role", role);
                }

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST, Constants.URL + SEND_EMAIL, jsonBody,
                        response -> {
                            Toast.makeText(getContext(), "Email sent successfully", Toast.LENGTH_SHORT).show();
                            dismiss();
                        },
                        error -> {
                            Toast.makeText(getContext(), "Error sending email", Toast.LENGTH_LONG).show();
                        }
                );
                SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getUserRoleFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("role", "Resident");
    }

    private String getEmailFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("resident_email","");
    }
    @Override
    public void onResume() {
        super.onResume();
        TokenValidator.validateToken(requireContext());
    }
}
package Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.resiapp.R;

import API.Constants;

public class NotificationDialogFragment extends DialogFragment {
    static String email = "";
    static final String adminEmail = Constants.ADMIN_EMAIL;
    EditText txtEmail, txtSubject, txtComments;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_notification, null);

        String invokedClass = requireActivity().getClass().getSimpleName();

        txtEmail = view.findViewById(R.id.editEmail);
        txtSubject= view.findViewById(R.id.editSubject);
        txtComments = view.findViewById(R.id.editComment);
        Button btnSend = view.findViewById(R.id.btnSend);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (invokedClass.trim().equals("LoginActivity") || invokedClass.trim().equals("ResidentDashboardActivity")) {
            txtEmail.setText(adminEmail);
            txtEmail.setEnabled(false);
            email = adminEmail;
        } else {
            email = txtEmail.getText().toString();
        }

        btnSend.setOnClickListener(v -> {
            String subject = txtSubject.getText().toString();
            String body = txtComments.getText().toString();

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
}
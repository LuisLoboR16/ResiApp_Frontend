package com.example.resiapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class NotificationDialogFragment extends DialogFragment {

    EditText txtEmail, txtSubject, txtComents;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_notification, null);

        txtEmail = view.findViewById(R.id.editEmail);
        txtSubject= view.findViewById(R.id.editSubject);
        txtComents = view.findViewById(R.id.editComment);
        Button btnSend = view.findViewById(R.id.btnSend);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnSend.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            String subject = txtSubject.getText().toString();
            String comments = txtComents.getText().toString();

            if (!TextUtils.isEmpty(email)) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, comments);
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Select a client for emails"));
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}
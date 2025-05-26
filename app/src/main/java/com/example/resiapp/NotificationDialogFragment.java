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

    EditText txtEmail, txtAsunto, txtComentarios;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_notification, null);

        txtEmail = view.findViewById(R.id.editEmail);
        txtAsunto = view.findViewById(R.id.editSubject);
        txtComentarios = view.findViewById(R.id.editComment);
        Button btnEnviar = view.findViewById(R.id.btnSend);
        Button btnCancelar = view.findViewById(R.id.btnCancel);

        btnEnviar.setOnClickListener(v -> {
            String correo = txtEmail.getText().toString();
            String asunto = txtAsunto.getText().toString();
            String comentario = txtComentarios.getText().toString();

            if (!TextUtils.isEmpty(correo)) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
                intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
                intent.putExtra(Intent.EXTRA_TEXT, comentario);
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Selecciona un cliente de correo electrónico"));
                dismiss();
            }
        });

        btnCancelar.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}

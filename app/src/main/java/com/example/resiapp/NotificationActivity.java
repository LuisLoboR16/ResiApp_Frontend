package com.example.resiapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationActivity extends AppCompatActivity {

    TextView txtEmail, txtAsunto, txtComentarios;
    Button btnEnviarEmail, btnVolver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtEmail = findViewById(R.id.txtEmail);
        txtAsunto = findViewById(R.id.txtAsunto);
        txtComentarios = findViewById(R.id.txtComentarios);
        btnEnviarEmail = findViewById(R.id.btnEnviarEmail);
        btnVolver = findViewById(R.id.btnVolver);

        btnEnviarEmail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String enviarCorreo = txtEmail.getText().toString();
                String enviarAsunto = txtAsunto.getText().toString();
                String enviarComentario = txtComentarios.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { enviarCorreo });
                intent.putExtra(Intent.EXTRA_SUBJECT, enviarAsunto);
                intent.putExtra(Intent.EXTRA_TEXT, enviarComentario);

                intent.setType("message/rfc822");

                startActivity(Intent.createChooser(intent, "Selecciona un cliente de correo electronico: "));
                clearFieldsNotifications();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), AdminDashboardActivity.class));
                clearFieldsNotifications();
            }
        });

    }
    private void clearFieldsNotifications(){
        txtEmail.setText("");
        txtAsunto.setText("");
        txtComentarios.setText("");
    }
}
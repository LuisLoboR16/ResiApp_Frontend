package com.example.resiapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminDashboardActivity extends AppCompatActivity {

    LinearLayout layoutNotifications, layoutUsers;
    TextView txtNotifications, txtUsers;
    ImageView imgNotifications, imgUsers;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layoutNotifications = findViewById(R.id.layoutNotifications);
        layoutNotifications.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });


        txtNotifications = findViewById(R.id.txtNotifications);
        txtNotifications.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });

        imgNotifications = findViewById(R.id.imgNotifications);
        imgNotifications.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });




        layoutUsers = findViewById(R.id.layoutUsers);
        layoutUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
            }
        });


        txtUsers = findViewById(R.id.txtUsers);
        txtUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
            }
        });

        imgUsers = findViewById(R.id.imgUsers);
        imgUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
            }
        });
    }
}
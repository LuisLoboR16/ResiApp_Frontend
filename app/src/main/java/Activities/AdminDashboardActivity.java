package Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.resiapp.R;

import Fragments.NotificationDialogFragment;

public class AdminDashboardActivity extends AppCompatActivity {

    LinearLayout layoutNotifications, layoutUsers, layoutSpaces;
    TextView txtNotifications, txtUsers, txtSpaces;
    ImageView imgNotifications, imgUsers, imgSpaces;
    Button btnLogout;

    @SuppressLint({"MissingInflatedId", "ResourceType"})
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

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogLogout();
            }
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

        layoutSpaces = findViewById(R.id.layoutSpaces);
        layoutSpaces.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), SpaceActivity.class));
            }
        });

        txtSpaces = findViewById(R.id.txtSpaces);
        txtSpaces.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), SpaceActivity.class));
            }
        });

        imgSpaces = findViewById(R.id.imgSpaces);
        imgSpaces.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), SpaceActivity.class));
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDialogLogout();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit ResiApp")
                .setMessage("¿Are you sure you want to exit the application?")
                .setIcon(R.drawable.password_icon)
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }
}
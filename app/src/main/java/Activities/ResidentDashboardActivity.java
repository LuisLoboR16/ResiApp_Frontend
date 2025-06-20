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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.resiapp.R;

import Fragments.NotificationDialogFragment;
import Fragments.UpdateConfigurationsDialogFragment;


public class ResidentDashboardActivity extends AppCompatActivity {

    LinearLayout layoutNotifications, layoutConfigurations, layoutReviews, layoutReservations;
    TextView txtNotifications, txtConfigurations, txtReviews, txtReservations;
    ImageView imgNotifications, imgConfigurations, imgReviews, imgReservations;

    Button btnLogout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resident_dashboard);
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

        layoutNotifications = findViewById(R.id.layoutNotificationsResident);
        layoutNotifications.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });


        txtNotifications = findViewById(R.id.txtNotificationsResident);
        txtNotifications.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });

        imgNotifications = findViewById(R.id.imgNotificationsResident);
        imgNotifications.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });
        layoutConfigurations = findViewById(R.id.layoutConfigurationsResident);
        layoutConfigurations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                UpdateConfigurationsDialogFragment dialog = new UpdateConfigurationsDialogFragment();
                dialog.show(getSupportFragmentManager(), "UpdateConfigurationsDialogFragment");
            }
        });


        txtConfigurations = findViewById(R.id.txtConfigurationsResident);
        txtConfigurations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                UpdateConfigurationsDialogFragment dialog = new UpdateConfigurationsDialogFragment();
                dialog.show(getSupportFragmentManager(), "UpdateConfigurationsDialogFragment");
            }
        });

        imgConfigurations = findViewById(R.id.imgConfigurationsResident);
        imgConfigurations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                UpdateConfigurationsDialogFragment dialog = new UpdateConfigurationsDialogFragment();
                dialog.show(getSupportFragmentManager(), "UpdateConfigurationsDialogFragment");          }
        });

        layoutReviews = findViewById(R.id.layoutReviewsResident);
        layoutReviews.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ReviewActivity.class));
            }
        });


        txtReviews = findViewById(R.id.txtReviewsResident);
        txtReviews.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ReviewActivity.class));
            }
        });

        imgReviews = findViewById(R.id.imgReviewsResident);
        imgReviews.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ReviewActivity.class));
            }
        });

        layoutReservations = findViewById(R.id.layoutReservationsResident);
        layoutReservations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ReservationActivity.class));
            }
        });


        txtReservations = findViewById(R.id.txtReservationsResident);
        txtReservations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ReservationActivity.class));
            }
        });

        imgReservations = findViewById(R.id.imgReservationsResident);
        imgReservations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ReservationActivity.class));
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
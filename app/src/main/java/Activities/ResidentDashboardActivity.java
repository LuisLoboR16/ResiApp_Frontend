package Activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.example.resiapp.R;

import org.json.JSONObject;

import API.Constants;
import Fragments.NotificationDialogFragment;
import Fragments.UpdateConfigurationsDialogFragment;

public class ResidentDashboardActivity extends AppCompatActivity {
    static final String URL = Constants.URL;
    static final String FIND_BY_USER_ID = Constants.FIND_BY_USER_ID_ENDPOINT;

    LinearLayout layoutNotifications, layoutConfigurations, layoutReviews, layoutReservations;
    TextView txtNotifications, txtConfigurations, txtReviews, txtReservations;
    ImageView imgNotifications, imgConfigurations, imgReviews, imgReservations, imgProfileDashboard;

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

        initViews();
        setListeners();
        loadUserData();
    }

    private void initViews() {
        layoutNotifications = findViewById(R.id.layoutNotificationsResident);
        txtNotifications = findViewById(R.id.txtNotificationsResident);
        imgNotifications = findViewById(R.id.imgNotificationsResident);

        layoutConfigurations = findViewById(R.id.layoutConfigurationsResident);
        txtConfigurations = findViewById(R.id.txtConfigurationsResident);
        imgConfigurations = findViewById(R.id.imgConfigurationsResident);

        layoutReviews = findViewById(R.id.layoutReviewsResident);
        txtReviews = findViewById(R.id.txtReviewsResident);
        imgReviews = findViewById(R.id.imgReviewsResident);

        layoutReservations = findViewById(R.id.layoutReservationsResident);
        txtReservations = findViewById(R.id.txtReservationsResident);
        imgReservations = findViewById(R.id.imgReservationsResident);

        imgProfileDashboard = findViewById(R.id.imgProfileDashboard);
    }


    private void setListeners() {
        View.OnClickListener notificationListener = v -> {
            NotificationDialogFragment dialog = new NotificationDialogFragment();
            dialog.show(getSupportFragmentManager(), "NotificationDialog");
        };

        layoutNotifications.setOnClickListener(notificationListener);
        txtNotifications.setOnClickListener(notificationListener);
        imgNotifications.setOnClickListener(notificationListener);

        View.OnClickListener configListener = v -> {
            UpdateConfigurationsDialogFragment dialog = new UpdateConfigurationsDialogFragment();
            dialog.show(getSupportFragmentManager(), "UpdateConfigurationsDialogFragment");
        };

        layoutConfigurations.setOnClickListener(configListener);
        txtConfigurations.setOnClickListener(configListener);
        imgConfigurations.setOnClickListener(configListener);

        View.OnClickListener reviewListener = v -> startActivity(new Intent(getApplicationContext(), ReviewActivity.class));
        layoutReviews.setOnClickListener(reviewListener);
        txtReviews.setOnClickListener(reviewListener);
        imgReviews.setOnClickListener(reviewListener);

        View.OnClickListener reservationListener = v -> startActivity(new Intent(getApplicationContext(), ReservationActivity.class));
        layoutReservations.setOnClickListener(reservationListener);
        txtReservations.setOnClickListener(reservationListener);
        imgReservations.setOnClickListener(reservationListener);

        Button btnLogoutResident = findViewById(R.id.btnLogoutResident);
        btnLogoutResident.setOnClickListener(v -> showDialogLogout());
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDialogLogout();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialogLogout() {
        View view = LayoutInflater.from(this).inflate(R.layout.activity_exit, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setIcon(R.drawable.password_icon)
                .create();

        Button btnLogout = view.findViewById(R.id.btnLogoutExit);
        Button btnCancel = view.findViewById(R.id.btnCancelExit);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        String url = URL + FIND_BY_USER_ID + userId;

        RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this);

        StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject dataObject = json.getJSONObject("data");
                        String base64Image = dataObject.getString("imageBase64");

                        if (base64Image.startsWith("data:image")) {
                            String encoded = base64Image.split(",")[1];
                            byte[] imageBytes = Base64.decode(encoded, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            imgProfileDashboard.setImageBitmap(bitmap);
                        } else {
                            imgProfileDashboard.setImageResource(R.drawable.ic_resiapp_under_construction);
                        }
                    } catch (Exception e) {
                        imgProfileDashboard.setImageResource(R.drawable.ic_resiapp_under_construction);
                    }
                },
                error -> {
                    imgProfileDashboard.setImageResource(R.drawable.ic_resiapp_under_construction);
                }
        );
        queue.add(request);
    }
}
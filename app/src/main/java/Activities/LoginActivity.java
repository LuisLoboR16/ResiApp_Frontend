package Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;

import org.json.JSONObject;

import API.SingleVolley;
import Fragments.CreateUserDialogFragment;
import Fragments.NotificationDialogFragment;


public class LoginActivity extends AppCompatActivity {
    static final String URL = "http://10.0.2.2:5069/api/Auth/";
    static final String LOGIN = "login";
    static final String LOG_TAG = "ResiApp" ;
    Button btnLogin;
    EditText editEmail, editPassword;
    TextView txtJoinUs, txtForgotPassword;

    ImageView ivToggle;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.editEmailLogin);
        editPassword = findViewById(R.id.editPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        ivToggle = findViewById(R.id.ivTogglePassword);

        ivToggle.setOnClickListener(v -> {
            if (editPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggle.setImageResource(R.drawable.password_icon);
            } else {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggle.setImageResource(R.drawable.password_icon);
            }
            editPassword.setSelection(editPassword.getText().length());
        });

        btnLogin.setOnClickListener(v -> {

            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();

            try {
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        URL + LOGIN,
                        json,
                        response -> {
                            progressDialog.dismiss();
                            String token = response.optString("token");
                            String role = response.optString("role");
                            Toast.makeText(this, "Logging successfully: ", Toast.LENGTH_SHORT).show();
                            if(role.trim().equals("Resident")){
                                startActivity(new Intent(this, ResidentDashboardActivity.class));
                            } else{
                                startActivity(new Intent(this, AdminDashboardActivity.class));
                            }
                        },
                        error -> {
                            progressDialog.dismiss();
                            Log.e(LOG_TAG, "Volley error: " + error.toString());
                            Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                            error.printStackTrace();
                        }
                ) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                };

                SingleVolley.getInstance(this).getRequestQueue().add(request);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Volley error: " + e.toString());
                progressDialog.dismiss();
                Toast.makeText(this, "Error performing request", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        /*
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), AdminDashboardActivity.class));
            }
        });
         */

        txtJoinUs = findViewById(R.id.txtCreateUser);
        txtJoinUs.setOnClickListener(v -> {
            CreateUserDialogFragment dialog = new CreateUserDialogFragment();
            dialog.show(getSupportFragmentManager(), "CreateUserDialog");
        });

        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtForgotPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationDialogFragment dialog = new NotificationDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotificationDialog");            }
        });
    }
}
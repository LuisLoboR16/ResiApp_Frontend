package Utils;

import static com.android.volley.toolbox.Volley.newRequestQueue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import Activities.LoginActivity;

public class TokenValidator {
    private static final String URL = Constants.URL;
    private static final String VALIDATE_TOKEN = Constants.AUTH_VALIDATE_TOKEN_ENDPOINT;

    public static void validateToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            forceLogout(context, "Token not found");
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.GET,
                URL + VALIDATE_TOKEN,
                response -> {
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        forceLogout(context, "Session expired, please login again.");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        newRequestQueue(context).add(request);
    }

    private static void forceLogout(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

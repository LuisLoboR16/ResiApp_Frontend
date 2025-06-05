package Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import API.SingleVolley;
import Adapters.UserAdapter;
import Models.User;

public class UserActivity extends AppCompatActivity {
    static final String URL = "http://10.0.2.2:5069/api/";
    static final String GET = "User";
    static final String DELETE = "User/";
    static final String UPDATE = "User/";
    static final String LOG_TAG = "ResiApp" ;
    private RequestQueue requestQueues;
    Gson gson;
    private List<User> userList;
    private UserAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SingleVolley volley;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        RecyclerView recyclerView = findViewById(R.id.recyclerUsuarios);
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener(){
            @Override
            public void onUpdate(User user) {
                showUpdateForm(user);
            }

            @Override
            public void onDelete(User user) {
                AlertDialog dialog = new AlertDialog.Builder(UserActivity.this)
                        .setTitle("🗑 Confirm action")
                        .setMessage("¿Are you sure to delete" + "\n\n" + "*" + user.getResidentName() + "*?" + "\n" +"\nThis action can't be undone.")
                        .setPositiveButton("Delete", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    positive.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    negative.setTextColor(getResources().getColor(android.R.color.darker_gray));

                    positive.setOnClickListener(v -> {
                        deleteUser(user);
                        dialog.dismiss();
                    });

                    negative.setOnClickListener(v -> dialog.dismiss());
                });

                dialog.show();
            }

            public void deleteUser(User user) {
                String urlDelete = URL + DELETE + user.getId();
                ProgressDialog progressDialog = new ProgressDialog(UserActivity.this);
                progressDialog.setMessage("Deleting user...");
                progressDialog.show();

                JsonObjectRequest deleteRequest = new JsonObjectRequest(
                        Request.Method.DELETE,
                        urlDelete,
                        null,
                        response -> {
                            Toast.makeText(UserActivity.this, "User deleted sucessfully", Toast.LENGTH_SHORT).show();
                            userList.remove(user);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        },
                        error -> {
                            Toast.makeText(UserActivity.this, "Error deleting user: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                );

                deleteRequest.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));

                requestQueues.add(deleteRequest);
            }

            private void showUpdateForm(User user) {
                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_user, null);
                EditText editName = dialogView.findViewById(R.id.editName);
                EditText editEmail = dialogView.findViewById(R.id.editEmail);
                EditText editPassword = dialogView.findViewById(R.id.editPassword);
                EditText editApartmentInformation = dialogView.findViewById(R.id.editApartmentInformation);
                EditText editRole = dialogView.findViewById(R.id.editRole);

                Button btnUpdate= dialogView.findViewById(R.id.btnUpdate);
                Button btnCancel= dialogView.findViewById(R.id.btnCancel);

                editName.setText(user.getResidentName());
                editEmail.setText(user.getEmail());
                editPassword.setText("");
                editApartmentInformation.setText(user.getApartmentInformation());
                editRole.setText(user.getRole());

                AlertDialog dialog = new AlertDialog.Builder(UserActivity.this)
                        .setView(dialogView)
                        .create();

                btnUpdate.setOnClickListener(view -> {
                    user.setResidentName(editName.getText().toString());
                    user.setEmail(editEmail.getText().toString());

                    String newPassword = editPassword.getText().toString();
                    if(!newPassword.isEmpty()){
                        user.setPassword(newPassword);
                    }else{
                        user.setPassword(null);
                    }

                    user.setApartmentInformation(editApartmentInformation.getText().toString());
                    user.setRole(editRole.getText().toString());
                    updateUser(user);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(view -> {
                    dialog.dismiss();
                });

                dialog.show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        gson = new Gson();

        volley = SingleVolley.getInstance(getApplicationContext());
        requestQueues = volley.getRequestQueue();

        createUserRequest(URL + GET);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void updateUser(User user) {
        String urlUpdate = URL + UPDATE + user.getId();
        ProgressDialog progressDialog = new ProgressDialog(UserActivity.this);
        progressDialog.setMessage("Updating user...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("residentName", user.getResidentName());
            json.put("email", user.getEmail());
            json.put("password", user.getPassword());
            json.put("apartmentInformation", user.getApartmentInformation());
            json.put("role", user.getRole());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    urlUpdate,
                    json,
                    response -> {
                        Toast.makeText(this, "User updated sucessfully", Toast.LENGTH_SHORT).show();
                        createUserRequest(URL + GET);
                        progressDialog.dismiss();
                    },
                    error ->
                    { Log.e(LOG_TAG, "VolleyError: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e(LOG_TAG, "Status code: " + error.networkResponse.statusCode);
                            if (error.networkResponse.data != null) {
                                String errorBody = new String(error.networkResponse.data);
                                Log.e(LOG_TAG, "Error body: " + errorBody);
                            }
                        }
                        Toast.makeText(this, "Error updating", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueues.add(request);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Excepction JSON: " + e.getMessage());
            progressDialog.dismiss();
        }
    }

    /**
     * Genera una petición y la encola
     *
     * @param urlRequest URL del recurso solicitado
     */
    public void createUserRequest(String urlRequest) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading users...");
        pDialog.show();

        JsonObjectRequest newRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            userList.clear();
                            for (int c = 0; c < jsonArray.length(); c++) {
                                JSONObject userJson = jsonArray.getJSONObject(c);
                                User user = gson.fromJson(userJson.toString(), User.class);
                                userList.add(user);
                            }
                            adapter.notifyDataSetChanged();


                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error procesing response: " + e.getMessage());
                        } finally {
                            pDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, "Error with request: " + error.toString());
                        pDialog.dismiss();
                    }
                }
        );

        newRequest.setTag(LOG_TAG);
        newRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        requestQueues.add(newRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueues != null) {
            requestQueues.cancelAll(LOG_TAG);
        }
        Log.i(LOG_TAG, "onStop() - Requests canceled");
    }
}
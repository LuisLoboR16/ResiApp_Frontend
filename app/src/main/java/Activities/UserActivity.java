package Activities;

import static Utils.Constants.*;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import Utils.TokenValidator;

public class UserActivity extends AppCompatActivity {
    private RequestQueue requestQueues;
    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;
    Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initVolley();
        setupRecyclerView();
        loadUsers();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initVolley() {
        requestQueues = SingleVolley.getInstance(getApplicationContext()).getRequestQueue();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerUsuarios);
        adapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener(){
            @Override
            public void onUpdate(User user) {showUpdateForm(user);}

            public void onDelete(User user) {showDeleteDialog(user);}
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        gson = new Gson();
    }

    private ProgressDialog showProgress(String message){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
    }

    private void showDeleteDialog(User user){
        View dialogView = LayoutInflater.from(UserActivity.this).inflate(R.layout.activity_delete_user, null);

        TextView txtMessage = dialogView.findViewById(R.id.txtDeleteMessage);
        txtMessage.setText(user.getResidentName() + "\n\nThis action can't be undone.");

        AlertDialog dialog = new AlertDialog.Builder(UserActivity.this).setView(dialogView).setIcon(R.drawable.password_icon).create();

        Button btnDelete = dialogView.findViewById(R.id.btnConfirmDelete);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelDelete);

        btnDelete.setOnClickListener(v -> {deleteUser(user);dialog.dismiss();});
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void deleteUser(User user) {
        String urlDelete = URL + USERS_ENDPOINT+"/" + user.getId();
        ProgressDialog progressDialog = showProgress("Deleting user...");

        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, urlDelete, null,
                response -> {
                    Toast.makeText(UserActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    userList.remove(user);
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                },
                error -> {
                    Toast.makeText(UserActivity.this, "Error deleting user: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
        );
        deleteRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueues.add(deleteRequest);
    }

    private void showUpdateForm(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_update_user, null);

        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editEmail = dialogView.findViewById(R.id.editEmail);
        EditText editPassword = dialogView.findViewById(R.id.editPassword);
        EditText editApartmentInformation = dialogView.findViewById(R.id.editApartmentInformation);

        Spinner spinnerRole = dialogView.findViewById(R.id.editSpinnerRole);

        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        editName.setText(user.getResidentName());
        editEmail.setText(user.getEmail());
        editPassword.setText("");
        editApartmentInformation.setText(user.getApartmentInformation());

        ArrayAdapter<CharSequence> adapterRoles = ArrayAdapter.createFromResource(UserActivity.this, R.array.roles_array, android.R.layout.simple_spinner_item);
        adapterRoles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapterRoles);

        int rolePosition = adapterRoles.getPosition(user.getRole());
        if (rolePosition >= 0) {spinnerRole.setSelection(rolePosition);}

        AlertDialog dialog = new AlertDialog.Builder(UserActivity.this).setView(dialogView).create();

        btnUpdate.setOnClickListener(view -> {
            user.setResidentName(editName.getText().toString());
            user.setEmail(editEmail.getText().toString());

            String newPassword = editPassword.getText().toString();

            if (!newPassword.isEmpty()) {
                user.setPassword(newPassword);
            } else {
                user.setPassword(null);
            }

            user.setApartmentInformation(editApartmentInformation.getText().toString());
            user.setRole(spinnerRole.getSelectedItem().toString());

            updateUser(user);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    public void updateUser(User user) {
        String urlUpdate = URL + USERS_ENDPOINT+"/" + user.getId();
        ProgressDialog progressDialog = showProgress("Updating user...");

        try {
            JSONObject json = new JSONObject();
            json.put("residentName", user.getResidentName());
            json.put("email", user.getEmail());
            json.put("password", user.getPassword());
            json.put("apartmentInformation", user.getApartmentInformation());
            json.put("role", user.getRole());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, urlUpdate, json,
                    response -> {
                        Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        loadUsers();
                        progressDialog.dismiss();
                    },
                    error ->
                    { Log.e(LOG_TAG, "VolleyError: " + error.toString());
                        Toast.makeText(this, "Error updating", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueues.add(request);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception JSON: " + e.getMessage());
            progressDialog.dismiss();
        }
    }

    public void loadUsers() {
        ProgressDialog progressDialog = showProgress("Loading users...");
        JsonObjectRequest newRequest = new JsonObjectRequest(Request.Method.GET, URL + USERS_ENDPOINT, null,
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
                            Log.e(LOG_TAG, "Error processing response: " + e.getMessage());
                        } finally {
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, "Error with request: " + error.toString());
                        progressDialog.dismiss();
                    }
                }
        );
        newRequest.setTag(LOG_TAG);
        newRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
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
    @Override
    protected void onResume() {
        super.onResume();
        TokenValidator.validateToken(this);
    }
}
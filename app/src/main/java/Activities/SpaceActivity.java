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
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.resiapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import API.SingleVolley;
import Adapters.SpaceAdapter;
import Fragments.CreateSpaceDialogFragment;
import Fragments.CreateUserDialogFragment;
import Models.Space;

public class SpaceActivity extends AppCompatActivity {
    static final String URL = "http://10.0.2.2:5069/api/";
    static final String GET = "Space";
    static final String DELETE = "Space/";
    static final String UPDATE = "Space/";
    static final String LOG_TAG = "ResiApp";
    private RequestQueue requestQueues;
    Gson gson;
    private List<Space> spaceList;
    private SpaceAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SingleVolley volley;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space);

        FloatingActionButton btnCreateSpace = findViewById(R.id.btnCreateSpace);
        btnCreateSpace.setOnClickListener(v -> {
            CreateSpaceDialogFragment dialog = new CreateSpaceDialogFragment();
            dialog.setOnSpaceCreated(() -> createSpaceRequest(URL + GET)); // ✅ Este callback refresca la lista
            dialog.show(getSupportFragmentManager(), "CreateSpaceDialog");
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerSpaces);
        spaceList = new ArrayList<>();
        adapter = new SpaceAdapter(spaceList, new SpaceAdapter.onSpaceActionListener() {

            @Override
            public void onUpdate(Space space) {
                showUpdateForm(space);
            }

            @Override
            public void onDelete(Space space) {
                AlertDialog dialog = new AlertDialog.Builder(SpaceActivity.this)
                        .setTitle("\uD83D\uDDD1 Confirm action")
                        .setMessage("¿Are you sure to delete\n\n*" + space.getSpaceName() + "*?\n\nThis action can't be undone.")
                        .setPositiveButton("Delete", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    positive.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    negative.setTextColor(getResources().getColor(android.R.color.darker_gray));

                    positive.setOnClickListener(v -> {
                        deleteSpace(space);
                        dialog.dismiss();
                    });

                    negative.setOnClickListener(v -> dialog.dismiss());
                });

                dialog.show();
            }

            public void deleteSpace(Space space) {
                String urlDelete = URL + DELETE + space.getId();
                ProgressDialog progressDialog = new ProgressDialog(SpaceActivity.this);
                progressDialog.setMessage("Deleting space...");
                progressDialog.show();

                @SuppressLint("NotifyDataSetChanged") StringRequest deleteRequest = new StringRequest(
                        Request.Method.DELETE,
                        urlDelete,
                        response -> {
                            Toast.makeText(SpaceActivity.this, "Space deleted successfully", Toast.LENGTH_SHORT).show();
                            spaceList.remove(space);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        },
                        error -> {
                            Toast.makeText(SpaceActivity.this, "Error deleting space: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

            private void showUpdateForm(Space space) {
                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_space, null);
                EditText editSpaceName = dialogView.findViewById(R.id.editSpaceName);
                EditText editCapacity = dialogView.findViewById(R.id.editCapacity);
                SwitchCompat editSwAvailability = dialogView.findViewById(R.id.swAvailability);

                Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                editSpaceName.setText(space.getSpaceName());
                editCapacity.setText(String.valueOf(space.getCapacity()));
                editSwAvailability.setChecked(space.isAvailability());

                AlertDialog dialog = new AlertDialog.Builder(SpaceActivity.this)
                        .setView(dialogView)
                        .create();

                btnUpdate.setOnClickListener(view -> {
                    space.setSpaceName(editSpaceName.getText().toString());
                    try {
                        space.setCapacity(Integer.parseInt(editCapacity.getText().toString()));
                    } catch (NumberFormatException e) {
                        Toast.makeText(SpaceActivity.this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    space.setAvailability(editSwAvailability.isChecked());
                    updateSpace(space);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        gson = new Gson();

        volley = SingleVolley.getInstance(getApplicationContext());
        requestQueues = volley.getRequestQueue();

        createSpaceRequest(URL + GET);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void updateSpace(Space space) {
        String urlUpdate = URL + UPDATE + space.getId();
        ProgressDialog progressDialog = new ProgressDialog(SpaceActivity.this);
        progressDialog.setMessage("Updating space...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("spaceName", space.getSpaceName());
            json.put("capacity", space.getCapacity());
            json.put("availability", space.isAvailability());

            StringRequest request = new StringRequest(
                    Request.Method.PUT,
                    urlUpdate,
                    response -> {
                        Toast.makeText(this, "Space updated successfully", Toast.LENGTH_SHORT).show();
                        createSpaceRequest(URL + GET);
                        progressDialog.dismiss();
                    },
                    error -> {
                        Log.e(LOG_TAG, "VolleyError: " + error.toString());
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
                public byte[] getBody() {
                    return json.toString().getBytes();
                }

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
            Log.e(LOG_TAG, "Exception JSON: " + e.getMessage());
            progressDialog.dismiss();
        }
    }

    public void createSpaceRequest(String urlRequest) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading spaces...");
        pDialog.show();

        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest newRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        spaceList.clear();
                        for (int c = 0; c < response.length(); c++) {
                            JSONObject userJson = response.getJSONObject(c);
                            Space space = gson.fromJson(userJson.toString(), Space.class);
                            spaceList.add(space);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error processing response: " + e.getMessage());
                        Toast.makeText(SpaceActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    } finally {
                        pDialog.dismiss();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error with request: " + error.toString());
                    Toast.makeText(SpaceActivity.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                    pDialog.dismiss();
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
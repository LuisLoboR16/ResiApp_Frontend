package Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import API.Constants;
import API.SingleVolley;
import Adapters.SpaceRuleAdapter;
import Fragments.CreateSpaceRuleDialogFragment;
import Models.SpaceRule;

public class SpaceRuleActivity extends AppCompatActivity {
    static final String URL = Constants.URL;
    static final String GET = Constants.SPACE_RULES_ENDPOINT;
    static final String DELETE = Constants.SPACE_RULES_ENDPOINT+"/";
    static final String UPDATE = Constants.SPACE_RULES_ENDPOINT+"/";
    static final String LOG_TAG = Constants.LOG_TAG;
    private RequestQueue requestQueues;
    Gson gson;
    private List<SpaceRule> spaceRulesList;
    private SpaceRuleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SingleVolley volley;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_rules);

        FloatingActionButton btnCreateSpace = findViewById(R.id.btnCreateSpaceRule);
        btnCreateSpace.setOnClickListener(v -> {
            CreateSpaceRuleDialogFragment dialog = new CreateSpaceRuleDialogFragment();
            dialog.setOnSpaceCreated(() -> createSpaceRuleRequest(URL + GET));
            dialog.show(getSupportFragmentManager(), "CreateSpaceRuleDialog");
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerSpaceRules);
        spaceRulesList = new ArrayList<>();
        adapter = new SpaceRuleAdapter(spaceRulesList, new SpaceRuleAdapter.OnSpaceRuleActionListener(){
            @Override
            public void onUpdate(SpaceRule spaceRule) {
                showUpdateForm(spaceRule);
            }

            @SuppressLint("CutPasteId")
            @Override
            public void onDelete(SpaceRule spaceRule) {
                View dialogView = LayoutInflater.from(SpaceRuleActivity.this).inflate(R.layout.activity_delete_space_rule, null);

                AlertDialog dialog = new AlertDialog.Builder(SpaceRuleActivity.this)
                        .setView(dialogView)
                        .setIcon(R.drawable.password_icon)
                        .create();

                Button btnDelete = dialogView.findViewById(R.id.btnDeleteSpaceRule);
                Button btnCancel = dialogView.findViewById(R.id.btnCancelDeleteSpaceRule);

                btnDelete.setOnClickListener(v -> {
                    deleteSpaceRule(spaceRule);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }

            public void deleteSpaceRule(SpaceRule spaceRule) {
                String urlDelete = URL + DELETE + spaceRule.getId();
                ProgressDialog progressDialog = new ProgressDialog(SpaceRuleActivity.this);
                progressDialog.setMessage("Deleting space rule...");
                progressDialog.show();

                @SuppressLint("NotifyDataSetChanged") JsonObjectRequest deleteRequest = new JsonObjectRequest(
                        Request.Method.DELETE,
                        urlDelete,
                        null,
                        response -> {
                            Toast.makeText(SpaceRuleActivity.this, "Space rule deleted sucessfully", Toast.LENGTH_SHORT).show();
                            spaceRulesList.remove(spaceRule);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        },
                        error -> {
                            Log.e(LOG_TAG, "Error with request: " + error.toString());
                            int statusCode = error.networkResponse.statusCode;

                            if(statusCode == 500){
                                Toast.makeText(getApplicationContext(), "You must delete or change the Space which is using this SpaceRule.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SpaceRuleActivity.this, "Error deleting space rule: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
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

            private void showUpdateForm(SpaceRule spaceRule) {
                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_space_rule, null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editRule = dialogView.findViewById(R.id.editRule);

                Button btnUpdate= dialogView.findViewById(R.id.btnUpdate);
                Button btnCancel= dialogView.findViewById(R.id.btnCancel);

                editRule.setText(spaceRule.getRule());

                AlertDialog dialog = new AlertDialog.Builder(SpaceRuleActivity.this)
                        .setView(dialogView)
                        .create();

                btnUpdate.setOnClickListener(view -> {
                    spaceRule.setRule(editRule.getText().toString());

                    updateSpaceRule(spaceRule);
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

        createSpaceRuleRequest(URL + GET);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void updateSpaceRule(SpaceRule spaceRule) {
        String urlUpdate = URL + UPDATE + spaceRule.getId();
        ProgressDialog progressDialog = new ProgressDialog(SpaceRuleActivity.this);
        progressDialog.setMessage("Updating space rule...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("rule", spaceRule.getRule());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    urlUpdate,
                    json,
                    response -> {
                        Toast.makeText(this, "Space Rule updated sucessfully", Toast.LENGTH_SHORT).show();
                        createSpaceRuleRequest(URL + GET);
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
    public void createSpaceRuleRequest(String urlRequest) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading space rules...");
        pDialog.show();

        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest newRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        spaceRulesList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ruleJson = response.getJSONObject(i);
                            SpaceRule spaceRule = gson.fromJson(ruleJson.toString(), SpaceRule.class);
                            spaceRulesList.add(spaceRule);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error processing response: " + e.getMessage());
                    } finally {
                        pDialog.dismiss();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error with request: " + error.toString());
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
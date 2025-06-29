package Activities;

import static Utils.Constants.*;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import API.SingleVolley;
import Adapters.SpaceRuleAdapter;
import Fragments.CreateSpaceRuleDialogFragment;
import Models.SpaceRule;
import Utils.TokenValidator;

public class SpaceRuleActivity extends AppCompatActivity {
    private RequestQueue requestQueues;
    private List<SpaceRule> spaceRulesList;
    private SpaceRuleAdapter adapter;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_rules);

        initVolley();
        setupRecyclerView();
        createSpaceRuleDialog();
        loadSpaceRule();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initVolley() {
        requestQueues = SingleVolley.getInstance(getApplicationContext()).getRequestQueue();
    }

    private void setupRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recyclerSpaceRules);
        spaceRulesList = new ArrayList<>();
        adapter = new SpaceRuleAdapter(spaceRulesList, new SpaceRuleAdapter.OnSpaceRuleActionListener(){
            @Override
            public void onUpdate(SpaceRule spaceRule) {showUpdateForm(spaceRule);}
            @Override
            public void onDelete(SpaceRule spaceRule) {showDeleteDialog(spaceRule);}

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

    private void showDeleteDialog(SpaceRule spaceRule){
        View dialogView = LayoutInflater.from(SpaceRuleActivity.this).inflate(R.layout.activity_delete_space_rule, null);
        AlertDialog dialog = new AlertDialog.Builder(SpaceRuleActivity.this).setView(dialogView).setIcon(R.drawable.password_icon).create();

        Button btnDelete = dialogView.findViewById(R.id.btnDeleteSpaceRule);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelDeleteSpaceRule);

        btnDelete.setOnClickListener(v -> {deleteSpaceRule(spaceRule);dialog.dismiss();});
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void createSpaceRuleDialog(){
        FloatingActionButton btnCreateSpace = findViewById(R.id.btnCreateSpaceRule);
        btnCreateSpace.setOnClickListener(v -> {
            CreateSpaceRuleDialogFragment dialog = new CreateSpaceRuleDialogFragment();
            dialog.setOnSpaceCreated(() -> loadSpaceRule());
            dialog.show(getSupportFragmentManager(), "CreateSpaceRuleDialog");
        });
    }

    public void deleteSpaceRule(SpaceRule spaceRule) {
        String urlDelete = URL + SPACE_RULES_ENDPOINT+"/" + spaceRule.getId();
        ProgressDialog progressDialog = showProgress("Deleting space rule...");


        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, urlDelete, null,
                response -> {
                    Toast.makeText(SpaceRuleActivity.this, "Space rule deleted sucessfully", Toast.LENGTH_SHORT).show();
                    spaceRulesList.remove(spaceRule);
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                },
                error -> {
                    int statusCode = error.networkResponse.statusCode;

                    if(statusCode == 500){
                        Toast.makeText(getApplicationContext(), "You must delete or change the Space which is using this SpaceRule.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SpaceRuleActivity.this, "Error deleting space rule: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                }
        );

        deleteRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueues.add(deleteRequest);
    }

    private void showUpdateForm(SpaceRule spaceRule) {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_update_space_rule, null);
        EditText editRule = dialogView.findViewById(R.id.editRule);

        Button btnUpdate= dialogView.findViewById(R.id.btnUpdate);
        Button btnCancel= dialogView.findViewById(R.id.btnCancel);

        editRule.setText(spaceRule.getRule());

        AlertDialog dialog = new AlertDialog.Builder(SpaceRuleActivity.this).setView(dialogView).create();

        btnUpdate.setOnClickListener(view -> {spaceRule.setRule(editRule.getText().toString());
            updateSpaceRule(spaceRule);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> {dialog.dismiss();});

        dialog.show();
    }

    public void updateSpaceRule(SpaceRule spaceRule) {
        String urlUpdate = URL + SPACE_RULES_ENDPOINT+"/" + spaceRule.getId();
        ProgressDialog progressDialog = showProgress("Updating space rule...");

        try {
            JSONObject json = new JSONObject();
            json.put("rule", spaceRule.getRule());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, urlUpdate, json,
                    response -> {
                        Toast.makeText(this, "Space Rule updated successfully", Toast.LENGTH_SHORT).show();
                        loadSpaceRule();
                        progressDialog.dismiss();
                    },
                    error -> {
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

    public void loadSpaceRule() {
        ProgressDialog progressDialog = showProgress("Loading space rules...");

        JsonArrayRequest newRequest = new JsonArrayRequest(
                Request.Method.GET, URL + SPACE_RULES_ENDPOINT, null,
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
                        progressDialog.dismiss();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error with request: " + error.toString());
                    progressDialog.dismiss();
                }
        );
        newRequest.setTag(LOG_TAG);
        newRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
package Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Utils.Constants;
import API.SingleVolley;
import Adapters.SpaceAdapter;
import Fragments.CreateSpaceDialogFragment;
import Models.Space;
import Models.SpaceRule;
import Utils.TokenValidator;
import de.hdodenhof.circleimageview.CircleImageView;

public class SpaceActivity extends AppCompatActivity {
    static final String URL = Constants.URL;
    static final String GET = Constants.SPACES_ENDPOINT;
    static final String DELETE = Constants.SPACES_ENDPOINT+"/";
    static final String UPDATE = Constants.SPACES_ENDPOINT+"/";
    static final String GET_RULES = Constants.SPACE_RULES_ENDPOINT;
    static final String LOG_TAG = Constants.LOG_TAG;
    private static final int PICK_IMAGE_REQUEST = 1;

    private RequestQueue requestQueues;
    private List<Space> spaceList;
    private List<SpaceRule> spaceRuleList;
    private SpaceAdapter adapter;
    private CircleImageView imgProfile;
    private String imageBase64 = "";
    private Space spaceUpdated;
    private OnImageSelected onImageSelectedCallback;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space);

        FloatingActionButton btnCreateSpace = findViewById(R.id.btnCreateSpace);
        btnCreateSpace.setOnClickListener(v -> {
            CreateSpaceDialogFragment dialog = new CreateSpaceDialogFragment();
            dialog.setSpaceRuleList(spaceRuleList);
            dialog.setOnSpaceCreated(() -> createSpaceRequest(URL + GET));
            dialog.show(getSupportFragmentManager(), "CreateSpaceDialog");
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerSpaces);
        spaceList = new ArrayList<>();
        spaceRuleList = new ArrayList<>();
        gson = new Gson();

        SingleVolley volley = SingleVolley.getInstance(getApplicationContext());
        requestQueues = volley.getRequestQueue();


        createSpaceRulesRequest(URL + GET_RULES);
        adapter = new SpaceAdapter(spaceList, spaceRuleList,new SpaceAdapter.onSpaceActionListener() {

            @Override
            public void onUpdate(Space space, List<SpaceRule> spaceRulesList) {
                showUpdateForm(space, spaceRulesList);
            }

            @Override
            public void onDelete(Space space) {
                View dialogView = LayoutInflater.from(SpaceActivity.this).inflate(R.layout.activity_delete_space, null);

                TextView txtMessage = dialogView.findViewById(R.id.txtDeleteMessageSpace);
                txtMessage.setText(space.getSpaceName() + "\n\nThis action can't be undone.");

                AlertDialog dialog = new AlertDialog.Builder(SpaceActivity.this)
                        .setView(dialogView)
                        .setIcon(R.drawable.password_icon)
                        .create();

                Button btnDelete = dialogView.findViewById(R.id.btnDeleteSpace);
                Button btnCancel = dialogView.findViewById(R.id.btnCancelDeleteSpace);

                btnDelete.setOnClickListener(v -> {
                    deleteSpace(space);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }

            public void deleteSpace(Space space) {
                String urlDelete = URL + DELETE + space.getId();
                ProgressDialog progressDialog = new ProgressDialog(SpaceActivity.this);
                progressDialog.setMessage("Deleting space...");
                progressDialog.show();

                StringRequest deleteRequest = new StringRequest(
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

            private void showUpdateForm(Space space, List<SpaceRule> spaceRulesList) {
                spaceUpdated = space;

                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_space, null);
                EditText editSpaceName = dialogView.findViewById(R.id.editSpaceName);
                EditText editCapacity = dialogView.findViewById(R.id.editCapacity);
                Spinner spinnerSpaceRules = dialogView.findViewById(R.id.spinnerSpaceRules);
                SwitchCompat editSwAvailability = dialogView.findViewById(R.id.swAvailability);

                imgProfile = dialogView.findViewById(R.id.imgProfileUpdateSpace);
                TextView txtChangePhoto = dialogView.findViewById(R.id.txtChangePhotoUpdateSpace);

                if (space.getImage() != null && space.getImage().startsWith("data:image")) {

                    try {
                        String base64Image = space.getImage().split(",")[1];
                        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        imgProfile.setImageBitmap(decodedBitmap);
                        imageBase64 = space.getImage();

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error decoding image: " + e.getMessage());
                    }
                }

                View.OnClickListener imagePicker = v -> {
                    onImageSelectedCallback = image -> {
                        imgProfile.setImageBitmap(image);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        byte[] imageBytes = outputStream.toByteArray();
                        imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                    };
                    openGallery();
                };

                imgProfile.setOnClickListener(imagePicker);
                txtChangePhoto.setOnClickListener(imagePicker);

                txtChangePhoto.setOnClickListener(v -> imgProfile.performClick());

                Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                editSpaceName.setText(space.getSpaceName());
                editCapacity.setText(String.valueOf(space.getCapacity()));
                editSwAvailability.setChecked(space.isAvailability());

                List<String> ruleNames = new ArrayList<>();
                for (SpaceRule rule: spaceRulesList){
                    ruleNames.add(rule.getRule());
                }

                Log.d(LOG_TAG, "Loading spinner with " + ruleNames.size() + " rules");
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        SpaceActivity.this,
                        android.R.layout.simple_spinner_item,
                        ruleNames
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSpaceRules.setAdapter(spinnerAdapter);

                int selectedIndex = 0;
                    int currentRuleId = space.getSpaceRule().get(0).getId();
                    for (int i = 0; i < spaceRulesList.size(); i++) {
                        if (spaceRulesList.get(i).getId() == currentRuleId) {
                            selectedIndex = i;
                            break;
                    }
                }
                spinnerSpaceRules.setSelection(selectedIndex);

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

                    int selectedPosition = spinnerSpaceRules.getSelectedItemPosition();
                    space.setSpaceRules(Collections.singletonList(spaceRulesList.get(selectedPosition)));

                    updateSpace(space);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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

        Log.d(LOG_TAG, "Base64 image being sent: " + space.getImage());

        try {
            JSONObject json = new JSONObject();

            json.put("spaceName", space.getSpaceName());
            json.put("capacity", space.getCapacity());
            json.put("spaceRuleId", space.getSpaceRule().get(0).getId());
            json.put("availability", space.isAvailability());
            if (space.getImage() != null && !space.getImage().isEmpty()) {
                json.put("imageBase64", space.getImage());
            }

            Log.d(LOG_TAG, "Sending JSON: " + json);

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

    public void createSpaceRulesRequest(String urlRequest) {
        JsonArrayRequest rulesRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        spaceRuleList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ruleJson = response.getJSONObject(i);
                            SpaceRule rule = gson.fromJson(ruleJson.toString(), SpaceRule.class);
                            spaceRuleList.add(rule);
                        }
                        Log.d(LOG_TAG, "Space rules loaded: " + spaceRuleList.size());

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing space rules: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error fetching rules: " + error.toString());
                    Toast.makeText(this, "Failed to load rules", Toast.LENGTH_SHORT).show();
                }
        );
        rulesRequest.setTag(LOG_TAG);
        rulesRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueues.add(rulesRequest);
    }

    public void createSpaceRequest(String urlRequest) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading spaces...");
        pDialog.show();

        JsonArrayRequest newRequest = new JsonArrayRequest(
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 340, 200, true);

                if (imgProfile != null) {
                    imgProfile.setImageBitmap(resizedBitmap);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                if (spaceUpdated != null) {
                    spaceUpdated.setImage(imageBase64);
                    Log.d(LOG_TAG, "New image assigned");
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    interface OnImageSelected {
        void onSelected(Bitmap image);
    }
    @Override
    protected void onResume() {
        super.onResume();
        TokenValidator.validateToken(this);
    }
}
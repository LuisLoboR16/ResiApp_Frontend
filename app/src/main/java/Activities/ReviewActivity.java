package Activities;

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
import android.widget.Toast;

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
import Utils.Constants;
import Adapters.ReviewAdapter;
import Fragments.CreateReviewDialogFragment;
import Models.Review;
import Models.Space;
import Models.User;
import Utils.RoleRule;
import Utils.TokenValidator;

public class ReviewActivity extends RoleRule {
    static final String URL = Constants.URL;
    static final String GET = Constants.REVIEWS_ENDPOINT;
    static final String DELETE = Constants.REVIEWS_ENDPOINT+"/";
    static final String UPDATE = Constants.REVIEWS_ENDPOINT+"/";
    static final String GET_SPACE = Constants.SPACES_ENDPOINT;
    static final String LOG_TAG = Constants.LOG_TAG;

    private RequestQueue requestQueues;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private List<Space> spaceList;
    private List<User> userList;
    private User currentUser;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        roleRules(findViewById(android.R.id.content));

        FloatingActionButton btnCreateSpace = findViewById(R.id.btnCreateReview);
        btnCreateSpace.setOnClickListener(v -> {
            CreateReviewDialogFragment dialog = new CreateReviewDialogFragment();
            dialog.setUser(currentUser);
            dialog.setSpaceList(spaceList);
            dialog.setOnReviewCreated(() -> createReviewRequest(URL + GET));
            dialog.show(getSupportFragmentManager(), "CreateReviewDialog");
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerReviews);
        reviewList = new ArrayList<>();
        spaceList = new ArrayList<>();
        gson = new Gson();

        SingleVolley volley = SingleVolley.getInstance(getApplicationContext());
        requestQueues = volley.getRequestQueue();

        createSpaceRequest(URL + GET_SPACE);
        adapter = new ReviewAdapter(reviewList,userList,spaceList,new ReviewAdapter.onReviewActionListener() {

            @Override
            public void onUpdate(Review review, List<User> userList,List<Space> spaceList) {
                showUpdateForm(review,spaceList);
            }

            @Override
            public void onDelete(Review review) {
                View dialogView = LayoutInflater.from(ReviewActivity.this)
                        .inflate(R.layout.activity_delete_review, null);

                Button btnDelete = dialogView.findViewById(R.id.btnDeleteReview);
                Button btnCancel = dialogView.findViewById(R.id.btnCancelDeleteReview);

                AlertDialog dialog = new AlertDialog.Builder(ReviewActivity.this)
                        .setView(dialogView)
                        .create();

                btnDelete.setOnClickListener(v -> {
                    deleteReview(review);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }

            public void deleteReview(Review review) {
                String urlDelete = URL + DELETE + review.getId();
                ProgressDialog progressDialog = new ProgressDialog(ReviewActivity.this);
                progressDialog.setMessage("Deleting review...");
                progressDialog.show();

                StringRequest deleteRequest = new StringRequest(
                        Request.Method.DELETE,
                        urlDelete,
                        response -> {
                            Toast.makeText(ReviewActivity.this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                            reviewList.remove(review);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        },
                        error -> {
                            Toast.makeText(ReviewActivity.this, "Error deleting review: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

            private void showUpdateForm(Review review, List<Space> spaceList) {
                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_review, null);
                Spinner spinnerUpdateRating = dialogView.findViewById(R.id.editUpdateSpinnerRating);
                EditText editComment = dialogView.findViewById(R.id.editComment);
                Spinner spinnerSpaces = dialogView.findViewById(R.id.spinnerSpaces);

                Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                List<String> ratings = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    ratings.add(String.valueOf(i));
                }

                ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(
                        ReviewActivity.this,
                        android.R.layout.simple_spinner_item,
                        ratings
                );
                ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUpdateRating.setAdapter(ratingAdapter);

                spinnerUpdateRating.setSelection(review.getRating() - 1);

                editComment.setText(review.getComment());

                List<String> spaceNames = new ArrayList<>();
                for (Space spaces: spaceList){
                    spaceNames.add(spaces.getSpaceName());
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        ReviewActivity.this,
                        android.R.layout.simple_spinner_item,
                        spaceNames
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSpaces.setAdapter(spinnerAdapter);

                int selectedIndex = 0;
                int currentSpaceId = review.getSpace().getId();
                for (int i = 0; i < spaceList.size(); i++) {
                    if (spaceList.get(i).getId() == currentSpaceId) {
                        selectedIndex = i;
                        break;
                    }
                }
                spinnerSpaces.setSelection(selectedIndex);

                AlertDialog dialog = new AlertDialog.Builder(ReviewActivity.this)
                        .setView(dialogView)
                        .create();

                btnUpdate.setOnClickListener(view -> {
                    review.setComment(editComment.getText().toString());

                    int selectedPosition = spinnerSpaces.getSelectedItemPosition();
                    Space selectedSpace = spaceList.get(selectedPosition);
                    review.setSpace(selectedSpace);

                    int selectedRating = Integer.parseInt(spinnerUpdateRating.getSelectedItem().toString());
                    review.setRating(selectedRating);

                    updateReview(review);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
                roleRules(dialogView);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createReviewRequest(URL + GET);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void updateReview(Review review) {
        String urlUpdate = URL + UPDATE + review.getId();
        ProgressDialog progressDialog = new ProgressDialog(ReviewActivity.this);
        progressDialog.setMessage("Updating review...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("rating", review.getRating());
            json.put("comment", review.getComment());
            json.put("residentId", review.getUser().getId());
            json.put("spaceId", review.getSpace().getId());

            StringRequest request = new StringRequest(
                    Request.Method.PUT,
                    urlUpdate,
                    response -> {
                        Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show();
                        createReviewRequest(URL + GET);
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
        JsonArrayRequest rulesRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        spaceList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ruleJson = response.getJSONObject(i);
                            Space spaceName = gson.fromJson(ruleJson.toString(), Space.class);
                            spaceList.add(spaceName);
                        }
                        Log.d(LOG_TAG, "Space loaded: " + spaceList.size());

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing space: " + e.getMessage());
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

    public void createReviewRequest(String urlRequest) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading reviews...");
        pDialog.show();

        JsonArrayRequest newRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {

                        reviewList.clear();
                        for (int c = 0; c < response.length(); c++) {
                            JSONObject userJson = response.getJSONObject(c);
                            Review review = gson.fromJson(userJson.toString(), Review.class);

                            if (review.getUser() == null) {
                                Log.e(LOG_TAG, "¡Review con ID " + review.getId() + " no tiene usuario!");
                            }

                            reviewList.add(review);
                        }
                        Log.d(LOG_TAG, "Reviews loaded: " + reviewList.size());
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error processing response: " + e.getMessage());
                        Toast.makeText(ReviewActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    } finally {
                        pDialog.dismiss();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error with request: " + error.toString());
                    Toast.makeText(ReviewActivity.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
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
    @Override
    protected void onResume() {
        super.onResume();
        TokenValidator.validateToken(this);
    }
}
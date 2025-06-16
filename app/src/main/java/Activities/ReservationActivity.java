package Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.resiapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import API.SingleVolley;
import API.Constants;
import Adapters.ReservationAdapter;

import Models.Reservation;
import Models.Space;
import Models.User;

public class ReservationActivity extends AppCompatActivity {
    static final String URL = Constants.URL;
    static final String GET = Constants.RESERVATIONS_ENDPOINT;
    static final String DELETE = Constants.RESERVATIONS_ENDPOINT+"/";
    static final String UPDATE = Constants.RESERVATIONS_ENDPOINT+"/";
    static final String GET_SPACE = Constants.SPACES_ENDPOINT;
    static final String LOG_TAG = Constants.LOG_TAG;
    static String formattedStartTimeDate = "";
    static String formattedEndTimeDate = "";
    private RequestQueue requestQueues;
    Gson gson;
    private List<Reservation> reservationList;
    private List<Space> spaceList;
    private List<User> userList;
    private User currentUser;

    private ReservationAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        /*
        FloatingActionButton btnCreateSpace = findViewById(R.id.btnCreateReview);
        btnCreateSpace.setOnClickListener(v -> {
            CreateReviewDialogFragment dialog = new CreateReviewDialogFragment();
            dialog.setUser(currentUser);
            dialog.setSpaceList(spaceList);
            dialog.setOnReviewCreated(() -> createReviewRequest(URL + GET));
            dialog.show(getSupportFragmentManager(), "CreateReviewDialog");
        });

         */

        RecyclerView recyclerView = findViewById(R.id.recyclerReservation);
        reservationList = new ArrayList<>();
        spaceList = new ArrayList<>();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm")
                .create();

        SingleVolley volley = SingleVolley.getInstance(getApplicationContext());
        requestQueues = volley.getRequestQueue();

        createSpaceRequest(URL + GET_SPACE);
        adapter = new ReservationAdapter(reservationList,userList,spaceList,new ReservationAdapter.onReservationActionListener() {

            @Override
            public void onUpdate(Reservation reservation, List<User> userList,List<Space> spaceList) {
                showUpdateForm(reservation,spaceList);
            }

            @Override
            public void onDelete(Reservation reservation) {
                AlertDialog dialog = new AlertDialog.Builder(ReservationActivity.this)
                        .setTitle("\uD83D\uDDD1 Confirm action")
                        .setMessage("¿Are you sure to delete reservation for\n\n*" + reservation.getSpace() + "at: " + reservation.getStartTime() + " - " + reservation.getEndTime() + "*?\n\nThis action can't be undone.")
                        .setPositiveButton("Delete", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    positive.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    negative.setTextColor(getResources().getColor(android.R.color.darker_gray));

                    positive.setOnClickListener(v -> {
                        deleteReservation(reservation);
                        dialog.dismiss();
                    });

                    negative.setOnClickListener(v -> dialog.dismiss());
                });
                dialog.show();
            }

            public void deleteReservation(Reservation reservation) {
                String urlDelete = URL + DELETE + reservation.getId();
                ProgressDialog progressDialog = new ProgressDialog(ReservationActivity.this);
                progressDialog.setMessage("Deleting reservation...");
                progressDialog.show();

                @SuppressLint("NotifyDataSetChanged") StringRequest deleteRequest = new StringRequest(
                        Request.Method.DELETE,
                        urlDelete,
                        response -> {
                            Toast.makeText(ReservationActivity.this, "Reservation deleted successfully", Toast.LENGTH_SHORT).show();
                            reservationList.remove(reservation);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        },
                        error -> {
                            Toast.makeText(ReservationActivity.this, "Error deleting reservation: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

            private void showUpdateForm(Reservation reservation, List<Space> spaceList) {
                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_reservation, null);
                EditText editStartTime = dialogView.findViewById(R.id.editStartTime);
                EditText editEndTime = dialogView.findViewById(R.id.editEndTime);
                Spinner spinnerSpacesReservation = dialogView.findViewById(R.id.spinnerSpacesReservation);

                Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                editStartTime.setOnClickListener(v -> {
                    final Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            v.getContext(),
                            (view, year, month, dayOfMonth) -> {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(
                                        v.getContext(),
                                        (view1, hourOfDay, minute) -> {
                                            calendar.set(year, month, dayOfMonth, hourOfDay, minute);

                                            @SuppressLint("SimpleDateFormat")
                                            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                            editStartTime.setText(sdfDisplay.format(calendar.getTime()));

                                            SimpleDateFormat sdfSend = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                                            formattedStartTimeDate = sdfSend.format(calendar.getTime());
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                );
                                timePickerDialog.show();
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePickerDialog.show();
                });

                editEndTime.setOnClickListener(v -> {
                    final Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            v.getContext(),
                            (view, year, month, dayOfMonth) -> {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(
                                        v.getContext(),
                                        (view1, hourOfDay, minute) -> {
                                            calendar.set(year, month, dayOfMonth, hourOfDay, minute);

                                            @SuppressLint("SimpleDateFormat")
                                            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                            editEndTime.setText(sdfDisplay.format(calendar.getTime()));

                                            SimpleDateFormat sdfSend = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                                            formattedEndTimeDate = sdfSend.format(calendar.getTime());
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                );
                                timePickerDialog.show();
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePickerDialog.show();
                });

                List<String> spaceNames = new ArrayList<>();
                for (Space spaces: spaceList){
                    spaceNames.add(spaces.getSpaceName());
                }

                Log.d(LOG_TAG, "Loading spinner with " + spaceNames.size() + " spaces");
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        ReservationActivity.this,
                        android.R.layout.simple_spinner_item,
                        spaceNames
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSpacesReservation.setAdapter(spinnerAdapter);

                int selectedIndex = 0;
                int currentSpaceId = reservation.getSpace().getId();
                for (int i = 0; i < spaceList.size(); i++) {
                    if (spaceList.get(i).getId() == currentSpaceId) {
                        selectedIndex = i;
                        break;
                    }
                }
                spinnerSpacesReservation.setSelection(selectedIndex);

                AlertDialog dialog = new AlertDialog.Builder(ReservationActivity.this)
                        .setView(dialogView)
                        .create();

                btnUpdate.setOnClickListener(view -> {
                    int selectedPosition = spinnerSpacesReservation.getSelectedItemPosition();
                    Space selectedSpace = spaceList.get(selectedPosition);
                    Log.d(LOG_TAG, "Selected space: " + selectedSpace.getId());
                    reservation.setSpace(selectedSpace);

                    updateReservation(reservation);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createReservationRequest(URL + GET);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void updateReservation(Reservation reservation) {
        String urlUpdate = URL + UPDATE + reservation.getId();
        ProgressDialog progressDialog = new ProgressDialog(ReservationActivity.this);
        progressDialog.setMessage("Updating reservation...");
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("startTime", formattedStartTimeDate);
            json.put("endTime", formattedStartTimeDate);
            json.put("residentId", reservation.getUser().getId());
            json.put("spaceId", reservation.getSpace().getId());

            StringRequest request = new StringRequest(
                    Request.Method.PUT,
                    urlUpdate,
                    response -> {
                        Toast.makeText(this, "Reservation updated successfully", Toast.LENGTH_SHORT).show();
                        createReservationRequest(URL + GET);
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

    public void createReservationRequest(String urlRequest) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading reservations...");
        pDialog.show();

        JsonObjectRequest newRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        reservationList.clear();
                        JSONArray dataArray = response.getJSONArray("data");

                        for (int c = 0; c < dataArray.length(); c++) {
                            JSONObject itemJson = dataArray.getJSONObject(c);
                            Reservation reservation = gson.fromJson(itemJson.toString(), Reservation.class);

                            if (reservation.getUser() == null) {
                                Log.e(LOG_TAG, "¡Reservation con ID " + reservation.getId() + " no tiene usuario!");
                            }
                            reservationList.add(reservation);
                        }
                        Log.d(LOG_TAG, "Reservations loaded: " + reservationList.size());
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error processing response: " + e.getMessage());
                        Toast.makeText(ReservationActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    } finally {
                        pDialog.dismiss();
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error with request: " + error.toString());
                    Toast.makeText(ReservationActivity.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
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
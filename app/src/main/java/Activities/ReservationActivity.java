package Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.resiapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import API.SingleVolley;
import API.Constants;
import Adapters.ReservationAdapter;
import Fragments.CreateReservationDialogFragment;
import Models.Reservation;
import Models.Space;
import Models.User;

public class ReservationActivity extends RoleRuleActivity {
    static final String URL = Constants.URL;
    static final String GET = Constants.RESERVATIONS_ENDPOINT;
    static final String DELETE = Constants.RESERVATIONS_ENDPOINT+"/";
    static final String UPDATE = Constants.RESERVATIONS_ENDPOINT+"/";
    static final String GET_SPACE = Constants.SPACES_ENDPOINT;
    static final String GET_USERS = Constants.USERS_ENDPOINT;
    static final String DATE_FORMAT_LONG = Constants.DATE_FORMAT_LONG;
    static final String DATE_TIME_FORMAT = Constants.DATE_TIME_FORMAT;
    static final String DATE_INVERTED_FORMAT = Constants.DATE_INVERTED_FORMAT;
    static final String LOG_TAG = Constants.LOG_TAG;

    private RequestQueue requestQueues;
    private List<Reservation> reservationList;
    private List<Space> spaceList;
    private List<User> userList;
    private ReservationAdapter adapter;

    String formattedStartTimeDate = "";
    String formattedEndTimeDate = "";
    EditText editStartTime, editEndTime;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        roleRules(findViewById(android.R.id.content));

        FloatingActionButton btnCreateReservation = findViewById(R.id.btnCreatecReservation);
        btnCreateReservation.setOnClickListener(v -> {
            CreateReservationDialogFragment dialog = new CreateReservationDialogFragment();
            dialog.setSpaceList(spaceList);
            dialog.setOnReservationCreated(() -> createReservationRequest(getReservationUrlByRole()));
            dialog.show(getSupportFragmentManager(), "CreateReservationDialogFragment");
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerReservation);
        reservationList = new ArrayList<>();
        spaceList = new ArrayList<>();
        gson = new GsonBuilder().setDateFormat(DATE_FORMAT_LONG).create();

        SingleVolley volley = SingleVolley.getInstance(getApplicationContext());
        requestQueues = volley.getRequestQueue();

        createSpaceRequest(URL + GET_SPACE);
        createUserRequest(URL + GET_USERS);

        adapter = new ReservationAdapter(reservationList,userList,spaceList,new ReservationAdapter.onReservationActionListener() {

            @Override
            public void onUpdate(Reservation reservation, List<User> userList, List<Space> spaceList) {
                showUpdateForm(reservation,spaceList);
            }

            @Override
            public void onDelete(Reservation reservation) {
                View dialogView = LayoutInflater.from(ReservationActivity.this)
                        .inflate(R.layout.activity_delete_reservation, null);

                Button btnDelete = dialogView.findViewById(R.id.btnDeleteReservation);
                Button btnCancel = dialogView.findViewById(R.id.btnCancelDeleteReservation);

                AlertDialog dialog = new AlertDialog.Builder(ReservationActivity.this)
                        .setView(dialogView)
                        .create();

                btnDelete.setOnClickListener(v -> {
                    deleteReservation(reservation);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }

            public void deleteReservation(Reservation reservation) {
                String urlDelete = URL + DELETE + reservation.getId();
                ProgressDialog progressDialog = new ProgressDialog(ReservationActivity.this);
                progressDialog.setMessage("Deleting reservation...");
                progressDialog.show();

                StringRequest deleteRequest = new StringRequest(
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
                View view = getLayoutInflater().inflate(R.layout.activity_update_reservation, null);

                 editStartTime = view.findViewById(R.id.editStartTimeUpdateReservation);
                 editEndTime = view.findViewById(R.id.editEndTimeUpdateReservation);
                EditText editDate = view.findViewById(R.id.editDateUpdateReservation);
                Spinner editSpace = view.findViewById(R.id.editSpinnerSpaceUpdateReservation);
                Spinner editUser = view.findViewById(R.id.editSpinnerUserUpdateReservation);

                Button btnUpdate = view.findViewById(R.id.btnUpdateReservation);
                Button btnTimeBoard = view.findViewById(R.id.btnTimeBoardUpdateReservation);
                Button btnCancel = view.findViewById(R.id.btnCancelUpdateReservation);

                editStartTime.setEnabled(false);
                editEndTime.setEnabled(false);


                SimpleDateFormat displayFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat(DATE_INVERTED_FORMAT, Locale.getDefault());

                if (userList == null || userList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "User list not loaded yet. Try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Date start = reservation.getStartTime();
                    Date end = reservation.getEndTime();

                    if (start != null && end != null) {
                        SimpleDateFormat apiFormat = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.getDefault());
                        formattedStartTimeDate = apiFormat.format(start);
                        formattedEndTimeDate = apiFormat.format(end);

                        editStartTime.setText(displayFormat.format(start));
                        editEndTime.setText(displayFormat.format(end));
                        editDate.setText(dateOnlyFormat.format(start));
                    }

                    List<String> userNames = new ArrayList<>();
                    for (User user : userList) {
                        userNames.add(user.getResidentName());
                    }

                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                            ReservationActivity.this,
                            android.R.layout.simple_spinner_item,
                            userNames
                    );
                    userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    editUser.setAdapter(userAdapter);

                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getId() == reservation.getUser().getId()) {
                            editUser.setSelection(i);
                            break;
                        }
                    }

                    List<String> spaceNames = new ArrayList<>();
                    for (Space name : spaceList) {
                        spaceNames.add(name.getSpaceName());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            ReservationActivity.this,
                            android.R.layout.simple_spinner_item,
                            spaceNames
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    editSpace.setAdapter(spinnerAdapter);

                     boolean[] isInitialSelection = {true};

                    editSpace.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            if (isInitialSelection[0]) {
                                isInitialSelection[0] = false;
                                return;
                            }

                            editStartTime.setText("");
                            editEndTime.setText("");
                            formattedStartTimeDate = "";
                            formattedEndTimeDate = "";
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        }
                    });

                    for (int i = 0; i < spaceList.size(); i++) {
                        if (spaceList.get(i).getId() == reservation.getSpace().getId()) {
                            editSpace.setSelection(i);
                            break;
                        }
                    }

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error preloading data: " + e.getMessage());
                }

                editDate.setFocusable(false);
                editDate.setOnClickListener(view1 -> {

                    final Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            ReservationActivity.this,
                            (view2, year, month, day) -> {
                                calendar.set(year, month, day);
                                editDate.setText(dateOnlyFormat.format(calendar.getTime()));

                                editStartTime.setText("");
                                editEndTime.setText("");
                                formattedStartTimeDate = "";
                                formattedEndTimeDate = "";
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePickerDialog.show();
                });

                AlertDialog dialog = new AlertDialog.Builder(ReservationActivity.this)
                        .setView(view)
                        .create();

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                btnTimeBoard.setOnClickListener(v -> {
                            String selectedDate = editDate.getText().toString().trim();

                            if (selectedDate.isEmpty()) {
                                Toast.makeText(ReservationActivity.this, "Select a date first", Toast.LENGTH_SHORT).show();
                                return;
                            }

                    int selectedIndex = editSpace.getSelectedItemPosition();
                    if (selectedIndex < 0 || selectedIndex >= spaceList.size()) {
                        Toast.makeText(ReservationActivity.this, "Please select a valid space", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Space selectedSpace = spaceList.get(selectedIndex);
                    int spaceId = selectedSpace.getId();

                    String url = URL + Constants.FIND_RESERVATIONS_BY_AVAILABLE_SLOTS + spaceId + Constants.DATE_SLOTS + selectedDate + Constants.SLOT_MINUTES_SLOTS;

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            response -> {
                                try {
                                    JSONArray data = response.getJSONArray("data");
                                    if (data.length() == 0) {
                                        Toast.makeText(ReservationActivity.this, "No available time slots", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    List<String> labels = new ArrayList<>();
                                    List<String> startList = new ArrayList<>();
                                    List<String> endList = new ArrayList<>();

                                    SimpleDateFormat inFmt = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.getDefault());
                                    SimpleDateFormat outFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject obj = data.getJSONObject(i);
                                        String startTime = obj.getString("startTime");
                                        String endTime = obj.getString("endTime");

                                        Date start = inFmt.parse(startTime);
                                        Date end = inFmt.parse(endTime);

                                        assert start != null;
                                        assert end != null;
                                        labels.add(outFmt.format(start) + " - " + outFmt.format(end));
                                        startList.add(startTime);
                                        endList.add(endTime);
                                    }

                                    new AlertDialog.Builder(ReservationActivity.this)
                                            .setTitle("Select available time slot")
                                            .setItems(labels.toArray(new String[0]), (dialog3, which) -> {
                                                formattedStartTimeDate = startList.get(which);
                                                formattedEndTimeDate = endList.get(which);

                                                try {
                                                    SimpleDateFormat display = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.getDefault());
                                                    editStartTime.setText(display.format(Objects.requireNonNull(inFmt.parse(formattedStartTimeDate))));
                                                    editEndTime.setText(display.format(Objects.requireNonNull(inFmt.parse(formattedEndTimeDate))));
                                                } catch (Exception e) {
                                                    Log.e(LOG_TAG, "Date parsing error: " + e.getMessage());
                                                }
                                            })
                                            .show();

                                } catch (Exception e) {
                                    Toast.makeText(ReservationActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(ReservationActivity.this, "You can't select a previous date from today.", Toast.LENGTH_SHORT).show()
                    );

                    requestQueues.add(request);
                        });

                btnUpdate.setOnClickListener(view1 -> {
                    int selectedUserIndex = editUser.getSelectedItemPosition();
                    int selectedSpaceIndex = editSpace.getSelectedItemPosition();

                    if (selectedUserIndex < 0 || selectedSpaceIndex < 0) {
                        Toast.makeText(ReservationActivity.this, "Please select valid data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (formattedStartTimeDate == null || formattedStartTimeDate.trim().isEmpty() ||
                            formattedEndTimeDate == null || formattedEndTimeDate.trim().isEmpty()) {
                        Toast.makeText(ReservationActivity.this, "You must select a time range before updating.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    reservation.setUser(userList.get(selectedUserIndex));
                    reservation.setSpace(spaceList.get(selectedSpaceIndex));

                    updateReservation(reservation);
                    dialog.dismiss();
                });

                dialog.show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createReservationRequest(getReservationUrlByRole());

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
            JSONObject requestBody = new JSONObject();

            requestBody.put("startTime", formattedStartTimeDate);
            requestBody.put("endTime", formattedEndTimeDate);
            requestBody.put("spaceId", reservation.getSpace().getId());
            requestBody.put("residentId", reservation.getUser().getId());


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
                        Toast.makeText(this, "Error updating reservation", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
            ) {
                @Override
                public byte[] getBody() {
                    return requestBody.toString().getBytes(StandardCharsets.UTF_8);
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

    private String getUserRoleFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("role", "Resident");
    }

    private int getUserIdFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getInt("user_id", 1);
    }

    private String getReservationUrlByRole() {
        String role = getUserRoleFromPrefs();
        int residentId = getUserIdFromPrefs();
        if (role.equalsIgnoreCase("Resident")) {
            return URL + Constants.FIND_RESERVATIONS_BY_ID + residentId;
        } else {
            return URL + GET;
        }
    }

    public void createSpaceRequest(String urlRequest) {
        JsonArrayRequest spaceRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        spaceList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject spaceJson = response.getJSONObject(i);
                            Space space = gson.fromJson(spaceJson.toString(), Space.class);

                            if(space.isAvailability()){
                                spaceList.add(space);
                            }
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

        spaceRequest.setTag(LOG_TAG);
        spaceRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueues.add(spaceRequest);
    }

    public void createUserRequest(String urlRequest) {
        JsonObjectRequest userRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlRequest,
                null,
                response -> {
                    try {
                        userList = new ArrayList<>();
                        JSONArray usersArray = response.getJSONArray("data");

                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userJson = usersArray.getJSONObject(i);
                            User user = gson.fromJson(userJson.toString(), User.class);
                            userList.add(user);
                        }

                        Log.d(LOG_TAG, "Users loaded: " + userList.size());

                        if (adapter != null) {
                            adapter.setUserList(userList);
                            adapter.notifyDataSetChanged();
                        }

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing user list: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error fetching users: " + error.toString());
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
        );

        userRequest.setTag(LOG_TAG);
        userRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueues.add(userRequest);
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
                            JSONObject reservationJson = dataArray.getJSONObject(c);
                            Reservation reservation = gson.fromJson(reservationJson.toString(), Reservation.class);

                            if (reservation.getUser() == null) {
                                Log.e(LOG_TAG, "Reservation with ID: " + reservation.getId() + " doesn't has an User.");
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
                    showServerErrorMessage(error);
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

    private void showServerErrorMessage(com.android.volley.VolleyError error) {
        String errorMessage = "Unexpected error";
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Log.e(LOG_TAG, "Server response: " + body);

                JSONObject jsonError = new JSONObject(body);
                if (jsonError.has("message")) {
                    errorMessage = jsonError.getString("message");
                }
            } else {
                errorMessage = "No response from server.";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing server response: " + e.getMessage());
            errorMessage = "Failed to interpret server error.";
        }
        Toast.makeText(ReservationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
package Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.resiapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import API.Constants;
import API.SingleVolley;
import Models.Space;
import Models.User;

public class CreateReservationDialogFragment extends DialogFragment {
    static final String URL = Constants.URL;
    static final String CREATE = Constants.RESERVATIONS_ENDPOINT;
    static final String DATE_FORMAT_LONG = Constants.DATE_FORMAT_LONG;
    static final String DATE_TIME_FORMAT = Constants.DATE_TIME_FORMAT;
    static final String DATE_INVERTED_FORMAT = Constants.DATE_INVERTED_FORMAT;
    static final String DATE_FORMAT_SHORTEST = Constants.DATE_FORMAT_SHORTEST;
    static final String LOG_TAG = Constants.LOG_TAG;

    private RequestQueue requestQueues;
    private User currentUser;
    private List<Space> spaceList = new ArrayList<>();

    public void setSpaceList(List<Space> spaceList) {
        this.spaceList = spaceList;
    }
    public void setUser(User currentUser) {
        this.currentUser = currentUser;
    }

    String formattedStartTimeDate = "";
    String formattedEndTimeDate = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_create_reservation, null);

        EditText editStartTime = view.findViewById(R.id.editStartTimecReservation);
        EditText editEndTime = view.findViewById(R.id.editEndTimecReservation);
        EditText editDate = view.findViewById(R.id.editDateReservation);
        EditText editUser = view.findViewById(R.id.editUsercReservation);

        Spinner editSpace = view.findViewById(R.id.editSpinnerSpacecReservation);

        Button btnCreate = view.findViewById(R.id.btnCreatecReservation);
        Button btnTimeBoard = view.findViewById(R.id.btnTimeBoardcReservation);
        Button btnCancel = view.findViewById(R.id.btnCancelcReservation);

        editStartTime.setEnabled(false);
        editEndTime.setEnabled(false);
        editUser.setEnabled(false);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String residentName = prefs.getString("resident_name", null);
        int userId = prefs.getInt("user_id", -1);

        editDate.setFocusable(false);
        editDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view2, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_INVERTED_FORMAT, Locale.getDefault());
                        String formattedDate = sdf.format(calendar.getTime());
                        editDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        if (residentName != null && userId != -1) {
            currentUser = new User();
            currentUser.setId(userId);
            currentUser.setResidentName(residentName);
        }

        if (currentUser != null && currentUser.getResidentName() != null) {
            editUser.setText(currentUser.getResidentName());
        }

        List<String> spaceNames = new ArrayList<>();
        for (Space name : spaceList) {
            spaceNames.add(name.getSpaceName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, spaceNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSpace.setAdapter(spinnerAdapter);

        editSpace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                formattedStartTimeDate = "";
                formattedEndTimeDate = "";
                editStartTime.setText("");
                editEndTime.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnTimeBoard.setOnClickListener(v -> {
            String selectedDate = editDate.getText().toString().trim();
            if (selectedDate.isEmpty()) {
                Toast.makeText(getContext(), "Select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = editSpace.getSelectedItemPosition();
            Space selectedSpace = spaceList.get(selectedIndex);
            int spaceId = selectedSpace.getId();

            String url = URL + Constants.FIND_RESERVATIONS_BY_AVAILABLE_SLOTS + spaceId + Constants.DATE_SLOTS + selectedDate + Constants.SLOT_MINUTES_SLOTS;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray data = response.getJSONArray("data");
                            if (data.length() == 0) {
                                Toast.makeText(getContext(), "No available time slots", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            List<String> labels = new ArrayList<>();
                            List<String> startList = new ArrayList<>();
                            List<String> endList = new ArrayList<>();

                            SimpleDateFormat inFmt = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.getDefault());
                            SimpleDateFormat outFmt = new SimpleDateFormat(DATE_FORMAT_SHORTEST, Locale.getDefault());

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

                            new AlertDialog.Builder(getContext())
                                    .setTitle("Select available time slot")
                                    .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                                        formattedStartTimeDate = startList.get(which);
                                        formattedEndTimeDate = endList.get(which);

                                        try {
                                            SimpleDateFormat display = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
                                            editStartTime.setText(display.format(Objects.requireNonNull(inFmt.parse(formattedStartTimeDate))));
                                            editEndTime.setText(display.format(Objects.requireNonNull(inFmt.parse(formattedEndTimeDate))));

                                        } catch (Exception e) {
                                            Log.e(LOG_TAG, "Date parsing error: " + e.getMessage());
                                        }
                                    })
                                    .show();

                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(getContext(), "You can't select a previous date from today.", Toast.LENGTH_SHORT).show()
            );

            requestQueues.add(request);
        });

        btnCreate.setOnClickListener(view1 -> {
            String startTime = editStartTime.getText().toString().trim();
            String endTime = editEndTime.getText().toString().trim();

            int selectedIndex = editSpace.getSelectedItemPosition();
            if (selectedIndex < 0 || selectedIndex >= spaceList.size()) {
                Toast.makeText(getContext(), "Please select a valid space", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(getContext(), "Please select a range of time", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Creating reservation...");
            progressDialog.show();

            try {
                JSONObject json = new JSONObject();
                json.put("startTime", formattedStartTimeDate);
                json.put("endTime", formattedEndTimeDate);
                json.put("residentId", currentUser.getId());
                json.put("spaceId", spaceList.get(selectedIndex).getId());

                JsonObjectRequest request = getJsonObjectRequest(json, progressDialog);
                SingleVolley.getInstance(requireContext()).getRequestQueue().add(request);

            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e(LOG_TAG, "JSON exception: " + e.getMessage());
                Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        requestQueues = SingleVolley.getInstance(requireContext()).getRequestQueue();

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();
    }

    private Runnable onReservationCreated;

    public void setOnReservationCreated(Runnable onReviewCreated) {
        this.onReservationCreated = onReviewCreated;
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(JSONObject jsonBody, ProgressDialog progressDialog) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL + CREATE,
                jsonBody,
                response -> {
                    Toast.makeText(getContext(), "Reservation created successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                    if (onReservationCreated != null) {
                        onReservationCreated.run();
                    }
                },
                error -> {
                    progressDialog.dismiss();

                    int statusCode = -1;
                    String errorBody = "";

                    if (error.networkResponse != null) {
                        statusCode = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            errorBody = new String(error.networkResponse.data);
                            Log.e(LOG_TAG, "Error body: " + errorBody);

                        }
                        Log.e(LOG_TAG, "Status code: " + statusCode);
                    }
                    try {

                        JSONObject obj = new JSONObject(errorBody);

                        if (obj.has("message")) {
                            Log.e(LOG_TAG, obj.getString("message"));
                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing error body: " + e.getMessage());
                    }
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
        return request;
    }
}
package com.example.resiapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Adapters.UserAdapter;

public class UserActivity extends AppCompatActivity {
    static final String URL = "http://10.0.2.2:5069/api/Api/GetResident";
    static final String LOG_TAG = "ResiApp" ;
    private RequestQueue colaPeticiones;
    Gson gson;
    private List<Users> listaUsuarios;
    private UserAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SingleVolley volley;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        RecyclerView recyclerView = findViewById(R.id.recyclerUsuarios);
        listaUsuarios = new ArrayList<>();
        adapter = new UserAdapter(listaUsuarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        gson = new Gson();

        // acceder instancia volley y obtener cola peticiones
        volley = SingleVolley.getInstance(getApplicationContext());
        colaPeticiones = volley.getRequestQueue();

        hacerPeticionCentros(URL);

        // Establece las inserciones de recortes de pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Genera una petición y la encola
     *
     * @param urlRecurso URL del recurso solicitado
     */
    public void hacerPeticionCentros(String urlRecurso) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading users...");
        pDialog.show();

        JsonObjectRequest nuevaPeticion = new JsonObjectRequest(
                Request.Method.GET,
                urlRecurso,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            listaUsuarios.clear();
                            for (int c = 0; c < jsonArray.length(); c++) {
                                JSONObject userJson = jsonArray.getJSONObject(c);
                                Users user = gson.fromJson(userJson.toString(), Users.class);
                                listaUsuarios.add(user);
                            }
                            adapter.notifyDataSetChanged();  // Actualiza la vista


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

        nuevaPeticion.setTag(LOG_TAG);
        nuevaPeticion.setRetryPolicy(
                new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        colaPeticiones.add(nuevaPeticion);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (colaPeticiones != null) {
            colaPeticiones.cancelAll(LOG_TAG);
        }
        Log.i(LOG_TAG, "onStop() - Requests canceled");
    }

    /**
     * Encola la petición
     *
     * @param peticion petición JsonArrayRequest
     */
    public void encolarPeticion(JsonArrayRequest peticion) {
        if (peticion != null) {
            peticion.setTag(LOG_TAG);  // Tag for this request. Can be used to cancel all requests with this tag
            peticion.setRetryPolicy(
                    new DefaultRetryPolicy(
                            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,  // initial timeout for the policy.
                            3,      // maximum number of retries
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
            );
            colaPeticiones.add(peticion);
        }
    }
}
package com.example.maps;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maps.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private OkHttpClient client = new OkHttpClient();
    final String TAG = "test";

    private ArrayList<LatLng> path = new ArrayList<>();
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getRoute();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    /**
     * Get the Route Data
     */
    private void getRoute() {

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/map/route")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + Thread.currentThread().getId());

                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        Log.d(TAG, "onResponse: " + json);

                        title = json.getString("title");
                        Log.d(TAG, "onResponse: " + title);

                        JSONArray pathJson = json.getJSONArray("path");

                        for(int i = 0; i < pathJson.length(); i++) {
                            JSONObject pathJsonObject = pathJson.getJSONObject(i);

                            double latitude = pathJsonObject.getDouble("latitude");
                            double longitude = pathJsonObject.getDouble("longitude");
                            LatLng latLng = new LatLng(latitude, longitude);

                            path.add(latLng);
                        }
                        // Test to see if ArrayList was populated
                        Log.d(TAG, "onResponse: Array Size:" + path.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawRoute();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onResponse: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void drawRoute() {
        // Add a marker at the start of the path
        LatLng start = new LatLng(path.get(0).latitude, path.get(0).longitude);
        mMap.addMarker(new MarkerOptions().position(start).title("Start Location"));

        // Add a marker to the end of the path
        LatLng end = new LatLng(path.get(0).latitude, path.get(0).longitude);
        mMap.addMarker(new MarkerOptions().position(start).title("End Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));

        // PolyLine
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(path);

        Polygon polygon = mMap.addPolygon(polygonOptions);

        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (int i = 0; i < path.size(); i++) {
            builder.include(path.get(i));
        }
        LatLngBounds bounds = builder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25));
    }
}
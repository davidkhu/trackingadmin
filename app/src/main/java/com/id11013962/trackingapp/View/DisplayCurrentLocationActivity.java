package com.id11013962.trackingapp.View;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.id11013962.trackingapp.R;

/**
 * Handle the Display Current Location Activity
 * Shows Current Latitude, Longitude, Last updated location
 * Shows Location on Google Map.
 */
public class DisplayCurrentLocationActivity extends Activity
implements OnMapReadyCallback{

    private static final float DEFAULT_ZOOM = 15;
    private TextView mLat;
    private TextView mLng;
    private TextView mDateTimeUpdated;
    private MapView mMapView;
    private BroadcastReceiver mIntentReceiver;
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private String mDateTimeString;

    /**
     * Setup UI Widget
     * Setup Google Map
     * Get Data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_current_location);

        // UI Widget declaration
        mLat = (TextView) findViewById(R.id.latitude_value);
        mLng = (TextView) findViewById(R.id.longitude_value);
        mDateTimeUpdated = (TextView) findViewById(R.id.date_time_value);

        // Google map declaration and setup
        mMapView = (MapView) findViewById(R.id.display_google_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        // Get data from Intent.
        Intent intent = getIntent();
        mCurrentLocation = intent.getParcelableExtra(Constants.INTENT_LOCATION);
        mDateTimeString = intent.getStringExtra(Constants.INTENT_LAST_UPDATED_TIME);
    }


    /**
     * Update UI depending on the parameter input
     */
    private void updateUI(Location currentLocation, String dateTimeUpdated) {
        mLat.setText(String.valueOf(currentLocation.getLatitude()));
        mLng.setText(String.valueOf(currentLocation.getLongitude()));
        mDateTimeUpdated.setText(dateTimeUpdated);

        // Display Lat/Lng on Google Map
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate mapUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM);
        mMap.animateCamera(mapUpdate);
    }

    /**
     * Ensure that the receiver is active when app is on screen. to receive current location updates from the service.
     * Update UI upon currentLocation updates.
     */
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_FILTER_ID);
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCurrentLocation = intent.getParcelableExtra(Constants.INTENT_LOCATION);
                String dateTimeUpdated = intent.getStringExtra(Constants.INTENT_LAST_UPDATED_TIME);
                Log.d(Constants.BROADCAST_RECEIVER, Constants.BROADCAST_RECEIVED + mCurrentLocation.toString());

                // Update UI.
                updateUI(mCurrentLocation, dateTimeUpdated);
            }
        };
        this.registerReceiver(mIntentReceiver, intentFilter);

        mMapView.onResume();
    }

    /**
     * Stop Broadcast receiver.
     * Pause Map
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mIntentReceiver);
        mMapView.onPause();
    }

    /**
     * Destroy Map
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    /**
     * let map know its on Low Memory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /**
     * save map
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMapView.onSaveInstanceState(outState);
    }

    /**
     * Setup map when map is initialised.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // New Lat Lng to add marker, show position.
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("currentPosition"));

        // Animate camera on google map.
        CameraUpdate mapUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM);
        googleMap.animateCamera(mapUpdate);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Update the UI when map is initialised and ready.
        updateUI(mCurrentLocation, mDateTimeString);
    }
}

package com.id11013962.trackingapp.View;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.id11013962.trackingapp.MongoDB.MongoUpdateParcelToDeliverLocationAsyncTask;
import com.id11013962.trackingapp.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Location Tracking Background Services.
 */
public class MyServices extends Service
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /**
     * The desired interval for location updates. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static final String CUSTOM_INTENT = Constants.INTENT_FILTER_ID;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Start the service when called from Main Activity.
     * When the button is pressed this start command launch
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, R.string.start_location_updates, Toast.LENGTH_SHORT).show();
        Log.d(Constants.SERVICE_STARTED, Constants.SERVICE_STARTED);
        return START_STICKY;
    }

    /**
     * Stop the service when called from Main Activity.
     * When the button is pressed this stop command stop.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, R.string.service_destoryed, Toast.LENGTH_LONG).show();

        // Disconnect Google Api to stop updating.
        mGoogleApiClient.disconnect();
    }

    /**
     * Start Google Map Activity, establish connection to Google Map
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Check if Google Map API Services is Available
        if (servicesOK()) {
            Log.d(Constants.SERVICE_STARTED, Constants.GOOGLE_MAP_ONLINE);
            // build google map client and connect
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Update periodically starts
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Suppose to check Mobile device if permission to use location services is granted.
        } else {
            // Start Location Updates periodically
            com.google.android.gms.location.LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Build the google map service client to API service.
     */
    private void buildGoogleApiClient() {
        Log.d(Constants.SERVICE_CREATED, Constants.GOOGLE_MAP_BUILDING);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .build();
        Log.d(Constants.SERVICE_CREATED, Constants.GOOGLE_MAP_BUILT);
        createLocationRequest();
    }

    /**
     * Location request
     * Set interval how fast you want to update location.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.d(Constants.LOCATION_REQUEST, Constants.LOCATION_REQUEST);
    }

    /**
     * Check if google map services is online and available to use
     */
    private boolean servicesOK() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
            }
            return false;
        }
        return true;
    }

    /**
     * Upon Connected to Google Location services get the location first.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        } else {
            if (mCurrentLocation == null) {
                Log.d(Constants.GETTING_LOCATION, Constants.GETTING_LOCATION);
                mCurrentLocation = com.google.android.gms.location.LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                // Log test location on debug
                String test = mCurrentLocation.toString();
                Log.d(Constants.GETTING_LOCATION, Constants.GETTING_LOCATION + test);
                mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
            }
            // permission has been granted, continue as usual
            mCurrentLocation = com.google.android.gms.location.LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        // Start periodic updates
        startLocationUpdates();

        // Send broadcast to other activity.
        SendBroadcastLocation();
    }

    /**
     * Send broadcast on location update / location to other activity.
     */
    private void SendBroadcastLocation() {
        Intent locationBroadcast = new Intent();
        locationBroadcast.putExtra(Constants.INTENT_LOCATION, mCurrentLocation);
        locationBroadcast.putExtra(Constants.INTENT_LAST_UPDATED_TIME, mLastUpdateTime);
        Log.d(Constants.SENDING_BROADCAST, Constants.SENDING_BROADCAST);
        locationBroadcast.setAction(CUSTOM_INTENT);
        sendBroadcast(locationBroadcast);
    }

    /**
     * Upon Connected suspended. Log this Error and try to connect to Google Services again.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Constants.GOOGLE_MAP_ERROR_TAG, Constants.GOOGLE_MAP_ERROR);
        mGoogleApiClient.connect();
    }

    /**
     * Upon Location changed. Broadcast new location and update database on each parcel
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(Constants.UPDATING_LOCATION_TAG, Constants.UPDATING_LOCATION + location.toString());
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());

        SendBroadcastLocation();

        // Update Mongo database for each parcel in the database.
        MongoUpdateParcelToDeliverLocationAsyncTask update = new MongoUpdateParcelToDeliverLocationAsyncTask(mCurrentLocation, mLastUpdateTime);
        update.execute();
    }

    /**
     * If connection to google map service failed, Log into debug.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(Constants.GOOGLE_MAP_ERROR_TAG, Constants.GOOGLE_MAP_ERROR);
    }
}

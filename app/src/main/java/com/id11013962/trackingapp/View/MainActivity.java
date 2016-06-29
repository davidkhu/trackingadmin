package com.id11013962.trackingapp.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.id11013962.trackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.trackingapp.MongoDB.MongoSaveParcelToDeliverAsyncTask;
import com.id11013962.trackingapp.R;

/**
 * Display main screen of Admin Tracking.
 * Functions: add parcel to the list for deliver
 * track current location
 * start tracking , stop tracking.
 */
public class MainActivity extends Activity {
    private Location mCurrentLocation;
    private BroadcastReceiver mIntentReceiver;
    private String mDateTimeUpdated;
    private ProgressDialog mProgressDialog;

    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mTrackingStatus;
    protected EditText mParcelNumber;

    /**
     * Setup UI Widgets and Progress Dialog
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_tracking_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_tracking_button);
        mTrackingStatus = (TextView) findViewById(R.id.tracking_status_text);
        mParcelNumber = (EditText) findViewById(R.id.parcel_number_input);
        mParcelNumber.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        mProgressDialog = new ProgressDialog(this);
    }

    /**
     * Handle Start Button pressed.
     * Start tracking location service.
     * Set Tracking status to Active.
     */
    public void startTrackButtonPressed(View view) {
        if (isNetworkAvailable()){
            // Start location tracking service.
            startService(new Intent(getBaseContext(), MyServices.class));

            setButtonsEnabledState(true);
            mTrackingStatus.setText(R.string.active_status);
            mTrackingStatus.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
        }else{
            displayErrorDialog();

        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        stopService(new Intent(getBaseContext(), MyServices.class));
        setButtonsEnabledState(false);
        mTrackingStatus.setText(R.string.non_active_status);
        mTrackingStatus.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    /**
     * Start to next activity to display current location
     * Have to start starting for this to work.
     */
    public void displayCurrentLocationButtonHandler(View view) {
        if (isNetworkAvailable()){
            if (mCurrentLocation == null) {
                Toast.makeText(this, "Current Location is null",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, DisplayCurrentLocationActivity.class);
                intent.putExtra(Constants.INTENT_LOCATION, mCurrentLocation);
                intent.putExtra(Constants.INTENT_LAST_UPDATED_TIME, mDateTimeUpdated);
                startActivity(intent);
            }
        }else{
            displayErrorDialog();
        }

    }

    /**
     * Add Parcel Button Handler
     * Check for current location update, else error
     * Start Save Async task to mongo database.
     */
    public void addParcelButtonHandler(View view) {
        if (mCurrentLocation != null) {
            if (mParcelNumber.getText().toString().matches(getString(R.string.empty_string))) {
                Toast.makeText(this, R.string.parcel_number_not_in_database, Toast.LENGTH_SHORT).show();
            } else {
                // set data to be save in database
                DbParcelToDeliverDataModel newData = new DbParcelToDeliverDataModel();
                newData.latitude = mCurrentLocation.getLatitude();
                newData.longitude = mCurrentLocation.getLongitude();
                newData.parcelNumber = mParcelNumber.getText().toString();
                newData.dateTimeUpdated = mDateTimeUpdated;

                // Async call to save data
                MongoSaveParcelToDeliverAsyncTask saveAsyncTask = new MongoSaveParcelToDeliverAsyncTask(this);
                saveAsyncTask.execute(newData);
            }
        } else Toast.makeText(this, R.string.current_location_not_active, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display list of parcel to be delivered today
     * start next activity
     */
    public void listParcelButtonHandler(View view) {
        if (isNetworkAvailable()){
            mProgressDialog.setMessage(getString(R.string.fetching_parcel_info));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
            Intent intent = new Intent(this, ViewListOfParcelsActivity.class);
            startActivity(intent);
        }else{
            displayErrorDialog();
        }
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState(boolean isActive) {
        if (isActive) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Ensure that the receiver is active when app is on screen. to receive current location updates from the service.
     */
    @Override
    public void onResume() {
        super.onResume();
        mProgressDialog.dismiss();
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_FILTER_ID);
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCurrentLocation = intent.getParcelableExtra(Constants.INTENT_LOCATION);
                mDateTimeUpdated = intent.getStringExtra(Constants.INTENT_LAST_UPDATED_TIME);
                Log.d(Constants.BROADCAST_RECEIVER, Constants.BROADCAST_RECEIVED + mCurrentLocation.toString());
            }
        };
        this.registerReceiver(mIntentReceiver, intentFilter);
    }

    /**
     * Destroy receiver on pause. don't update location. no need.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mIntentReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Check if WIFI or Mobile Network is available
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // if no network is available networkInfo will be null
        // otherwise check if connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Error Dialog for No Network Connection
     */
    private void displayErrorDialog() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setMessage("No Network Connection");
        dlgAlert.setTitle("Error");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }
}

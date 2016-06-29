package com.id11013962.trackingapp.MongoDB;

import android.os.AsyncTask;
import android.util.Log;

import com.id11013962.trackingapp.View.Constants;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Updating the Parcel Information Database on Location changed.
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoUpdateParcelInfoStatusAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private String mParcelNumber;
    private String mStatus;
    private String mDateTimeDelivered;

    public MongoUpdateParcelInfoStatusAsyncTask(String parcelNumber, String status, String dateTime){
        this.mParcelNumber = parcelNumber;
        this.mStatus = status;
        this.mDateTimeDelivered = dateTime;
    }

    /**
     * Update status and date time delivered for parcel information.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_LOG_MSG);

            MongoParcelInfoQueryBuilder qb = new MongoParcelInfoQueryBuilder();
            URL url = new URL(qb.buildMongoDbSingleItemURL(mParcelNumber));
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod(Constants.PUT_REQUEST);
            connection.setDoOutput(true);
            connection.setRequestProperty(Constants.CONTENT_TYPE,
                    Constants.TO_JSON);
            connection.setRequestProperty(Constants.ACCEPT, Constants.TO_JSON);

            OutputStreamWriter osw = new OutputStreamWriter(
                    connection.getOutputStream());

            // Update database.
            osw.write(qb.updateParcelInfoStatus(mStatus, mDateTimeDelivered));
            osw.flush();
            osw.close();

            if(connection.getResponseCode() <205)
            {
                Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_SUCCESS);
                return true;
            }
            else
            {
                Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_FAILED);
                return false;
            }

        } catch (Exception e) {
            Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_FAILED + e.getMessage());
            return false;
        }
    }
}

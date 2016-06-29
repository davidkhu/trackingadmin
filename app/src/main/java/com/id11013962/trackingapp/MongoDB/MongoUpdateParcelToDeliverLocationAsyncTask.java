package com.id11013962.trackingapp.MongoDB;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.id11013962.trackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.trackingapp.View.Constants;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Updating the Parcel to Deliver Database
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoUpdateParcelToDeliverLocationAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private Location mUpdatedLocation;
    private String mLastUpdatedTime;
    private ArrayList<DbParcelToDeliverDataModel> mDbDatas = new ArrayList<>();

    public MongoUpdateParcelToDeliverLocationAsyncTask(Location updatedLocation, String lastUpdateTime){
        this.mUpdatedLocation = updatedLocation;
        this.mLastUpdatedTime = lastUpdateTime;
    }

    /**
     * Get all the data necessary to update first before updating.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Get your cloud data
        MongoGetAllParcelToDeliverAsyncTask task = new MongoGetAllParcelToDeliverAsyncTask();
        try {
            mDbDatas = task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update database with the new lat long attribute.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        int dbDataCount = mDbDatas.size();

        // don't update anything since there is no data in db to update.
        if (dbDataCount == 0) return false;

        for (int i = 0; i < dbDataCount; i++){
            // Update latitude and longitude of each data before query to update to cloud db.
            mDbDatas.get(i).setLatitude(mUpdatedLocation.getLatitude());
            mDbDatas.get(i).setLongitude(mUpdatedLocation.getLongitude());
            mDbDatas.get(i).setDateTimeUpdated(mLastUpdatedTime);

            try {
                MongoParcelToDeliverQueryBuilder qb = new MongoParcelToDeliverQueryBuilder();
                URL url = new URL(qb.buildMongoDbUpdateURL(mDbDatas.get(i).getParcelNumber()));
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setRequestMethod(Constants.PUT_REQUEST);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("Accept", "application/json");

                OutputStreamWriter osw = new OutputStreamWriter(
                        connection.getOutputStream());

                osw.write(qb.updateParcelData(mDbDatas.get(i)));
                osw.flush();
                osw.close();

                if(connection.getResponseCode() <205)
                {
                    continue;
                }
                else
                {
                    return false;
                }

            } catch (Exception e) {
                e.getMessage();
                return false;
            }
        }
        return true;
    }

    /**
     * Log to debug on success execution
     */
    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (isSuccess){
            Log.d(Constants.UPDATE_LOCATION_TAG, Constants.UPDATE_LOCATION_SUCCESS);
        }else{
            Log.d(Constants.UPDATE_LOCATION_TAG, Constants.UPDATE_LOCATION_FAILED);
        }
    }
}

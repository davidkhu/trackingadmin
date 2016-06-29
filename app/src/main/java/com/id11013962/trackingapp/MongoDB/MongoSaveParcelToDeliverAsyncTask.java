package com.id11013962.trackingapp.MongoDB;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.id11013962.trackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.trackingapp.R;
import com.id11013962.trackingapp.View.Constants;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

/**
 * Insert a new entry into parcel to deliver database and Update Parcel Info database status.
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoSaveParcelToDeliverAsyncTask extends AsyncTask<DbParcelToDeliverDataModel, Void, MongoSaveParcelToDeliverAsyncTask.result> {
    private Context mContext;
    private ProgressDialog mProgressDialog;

    public MongoSaveParcelToDeliverAsyncTask(Context context) {
        this.mContext = context;
    }

    /**
     * Show progress dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.saving_progress_dialog));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    /**
     * Save and update database in background.
     */
    @Override
    protected result doInBackground(DbParcelToDeliverDataModel... params) {
        try {
            DbParcelToDeliverDataModel data = params[0];

            // Update status in Parcel Info Status db
            updateStatus(data.getParcelNumber());

            // create new entry in parcel tracking db.
            MongoParcelToDeliverQueryBuilder parcelToDeliverQueryBuilder = new MongoParcelToDeliverQueryBuilder();

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(parcelToDeliverQueryBuilder.buildMongoDbURL());

            // Insert new data.
            StringEntity stringEntity = new StringEntity(parcelToDeliverQueryBuilder.createParcelData(data));
            request.addHeader(Constants.CONTENT_TYPE, Constants.TO_JSON);
            request.setEntity(stringEntity);
            HttpResponse response = httpClient.execute(request);

            // Return true is success status code.
            if (response.getStatusLine().getStatusCode() < 205) {
                return result.SUCCESS;
            } else {
                return result.FAIL;
            }
        } catch (Exception e) {
            return result.FAIL;
        }
    }

    /**
     * Updating the Parcel Information Database
     */
    private void updateStatus(String parcelNumber) {
        try {
            Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_LOG_MSG);

            MongoParcelInfoQueryBuilder qb = new MongoParcelInfoQueryBuilder();
            URL url = new URL(qb.buildMongoDbSingleItemURL(parcelNumber));
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
            osw.write(qb.updateParcelInfoStatus(Constants.IN_TRANSIT, Constants.EMPTY_STRING));
            osw.flush();
            osw.close();

            if (connection.getResponseCode() < 205) {
                Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_SUCCESS);
            } else {
                Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_FAILED);
            }

        } catch (Exception e) {
            Log.d(Constants.UPDATE_STATUS_LOG_TAG, Constants.UPDATE_STATUS_FAILED + e.getMessage());
        }
    }

    /**
     * Display toast upon success, fail, doest exist.
     */
    @Override
    protected void onPostExecute(result result) {
        super.onPostExecute(result);
        switch (result) {
            case SUCCESS:
                Toast.makeText(mContext, R.string.data_stored_success, Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                break;
            case FAIL:
                Toast.makeText(mContext, R.string.data_failed_to_store_in_db, Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                break;
            case NOT_EXIST:
                Toast.makeText(mContext, R.string.enter_valid_parcel_number, Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                break;
        }
    }

    /**
     * ENUM Class for result
     */
    public enum result {
        SUCCESS,
        FAIL,
        NOT_EXIST
    }
}

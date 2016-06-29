package com.id11013962.trackingapp.MongoDB;

import android.os.AsyncTask;

import com.id11013962.trackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.trackingapp.View.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Async Task to Mongo MLab Cloud database to get Parcel Information.
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoGetParcelInfoAsyncTask extends AsyncTask<Void, Void, DbParcelInfoDataModel> {
    private String mParcelNumber;
    static String server_output = null;
    static String temp_output = null;

    public MongoGetParcelInfoAsyncTask(String parcelNumber) {
        this.mParcelNumber = parcelNumber;
    }

    @Override
    protected DbParcelInfoDataModel doInBackground(Void... params) {
        DbParcelInfoDataModel parcelInfoDataModel = new DbParcelInfoDataModel();

        try {
            // build query URL and request via the web To GET data back.
            MongoParcelInfoQueryBuilder qb = new MongoParcelInfoQueryBuilder();
            URL url = new URL(qb.buildMongoDbSingleItemURL(mParcelNumber));
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setRequestMethod(Constants.GET_REQUEST);
            conn.setRequestProperty(Constants.ACCEPT, Constants.TO_JSON);

            // Handle exception if error
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException(Constants.RUN_TIME_EXCEPTION
                        + conn.getResponseCode());
            }

            // buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
            // Reads text from a character-input stream
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            while ((temp_output = br.readLine()) != null) {
                server_output = temp_output;
            }

            // create a basic db list parse to JSON object.
            String mongoarray = "{ artificial_basicdb_list: " + server_output + "}";
            Object object = com.mongodb.util.JSON.parse(mongoarray);

            DBObject dbObj = (DBObject) object;
            BasicDBObject parcelToDeliver = (BasicDBObject) dbObj.get(Constants.ARTIFICIAL_DB_LIST);

            // set data fetched to data model
            parcelInfoDataModel.setParcelNumber(parcelToDeliver.get(Constants.GET_ID).toString());
            parcelInfoDataModel.setFullName(parcelToDeliver.get(Constants.GET_FULLNAME).toString());
            parcelInfoDataModel.setAddress(parcelToDeliver.get(Constants.GET_ADDRESS).toString());
            parcelInfoDataModel.setSuburb(parcelToDeliver.get(Constants.GET_SUBURB).toString());
            parcelInfoDataModel.setState(parcelToDeliver.get(Constants.GET_STATE).toString());
            parcelInfoDataModel.setCity(parcelToDeliver.get(Constants.GET_CITY).toString());
            parcelInfoDataModel.setPostcode(parcelToDeliver.get(Constants.GET_POSTCODE).toString());
            parcelInfoDataModel.setStatus(parcelToDeliver.get(Constants.GET_STATUS).toString());
            parcelInfoDataModel.setDateTimeDelivered(parcelToDeliver.get(Constants.GET_DATE_TIME_DELIVERED).toString());
        } catch (ProtocolException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parcelInfoDataModel;
    }
}

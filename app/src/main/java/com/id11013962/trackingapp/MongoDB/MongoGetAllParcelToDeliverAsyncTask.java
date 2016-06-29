package com.id11013962.trackingapp.MongoDB;

import android.os.AsyncTask;

import com.id11013962.trackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.trackingapp.View.Constants;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Async Task to Mongo MLab Cloud database to get Parcel to Deliver Information.
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoGetAllParcelToDeliverAsyncTask extends AsyncTask<DbParcelToDeliverDataModel, Void, ArrayList<DbParcelToDeliverDataModel>> {
    static String server_output = null;
    static String temp_output = null;

    @Override
    protected ArrayList<DbParcelToDeliverDataModel> doInBackground(DbParcelToDeliverDataModel... params) {
        ArrayList<DbParcelToDeliverDataModel> dbDatas = new ArrayList<>();
        try {

            // build query URL and request via the web To GET data back.
            MongoParcelToDeliverQueryBuilder qb = new MongoParcelToDeliverQueryBuilder();
            URL url = new URL(qb.buildMongoDbURL());
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

            // create a basic db list
            String mongoarray = "{ artificial_basicdb_list: " + server_output + "}";
            Object o = com.mongodb.util.JSON.parse(mongoarray);

            DBObject dbObj = (DBObject) o;
            BasicDBList parcelToDeliver = (BasicDBList) dbObj.get(Constants.ARTIFICIAL_DB_LIST);

            // iterate all the objects
            for (Object obj : parcelToDeliver) {
                DBObject userObj = (DBObject) obj;

                // set data fetched to data model
                DbParcelToDeliverDataModel temp = new DbParcelToDeliverDataModel();
                temp.setParcelNumber(userObj.get(Constants.GET_ID).toString());
                temp.setLatitude(Double.parseDouble(userObj.get(Constants.GET_LATITUDE).toString()));
                temp.setLongitude(Double.parseDouble(userObj.get(Constants.GET_LONGITUDE).toString()));

                // add to list
                dbDatas.add(temp);
            }
        } catch (ProtocolException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dbDatas;
    }
}

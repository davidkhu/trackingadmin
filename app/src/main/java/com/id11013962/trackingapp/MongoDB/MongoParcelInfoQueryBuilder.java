package com.id11013962.trackingapp.MongoDB;

/**
 * URL Query Builder to Access Mongo Cloud Service Database.
 * Access the Parcel Info Database
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoParcelInfoQueryBuilder {
    /**
     * database name
     */
    public String getDatabaseName() {
        return "trackingdb";
    }

    /**
     * MongoLab API
     */
    public String getApiKey() {
        return "IxYQY25eyAM8wGhNZHJKwfC-YUCDOSt_";
    }

    /**
     * This constructs the URL that allows you to manage your database,
     * collections and documents
     */
    public String getBaseUrl()
    {
        return "https://api.mongolab.com/api/1/databases/"+getDatabaseName()+"/collections/";
    }

    /**
     * Completes the formatting of your URL and adds your API key at the end
     */
    public String docApiKeyUrl()
    {
        return "?apiKey="+getApiKey();
    }

    /**
     * Get a specified document
     * @param docId
     */
    public String docApiKeyUrl(String docId)
    {
        return "/"+docId+"?apiKey="+getApiKey();
    }

    /**
     * Returns the collection
     * @return
     */
    public String documentRequest()
    {
        return "parcel_details";
    }

    /**
     * Builds a complete URL using the methods specified above
     * @return
     */
    public String buildMongoDbURL()
    {
        return getBaseUrl()+documentRequest()+docApiKeyUrl();
    }

    /**
     * Get a Mongodb document that corresponds to the given object id
     * @param doc_id
     * @return
     */
    public String buildMongoDbSingleItemURL(String doc_id)
    {
        return getBaseUrl()+documentRequest()+docApiKeyUrl(doc_id);
    }

    /**
     * Update a given contact record
     * @param status
     * @return
     */
    public String updateParcelInfoStatus(String status, String dateTimeDelivered) {
        return String.format("{ \"$set\" : "
                        + "{\"status\" : \"%s\", "
                        + "\"dateTimeDelivered\" : \"%s\" }"
                        + "}",
                status, dateTimeDelivered);
    }
}

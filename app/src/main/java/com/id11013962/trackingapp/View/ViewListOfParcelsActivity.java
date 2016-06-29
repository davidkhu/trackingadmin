package com.id11013962.trackingapp.View;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.id11013962.trackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.trackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.trackingapp.MongoDB.MongoGetAllParcelToDeliverAsyncTask;
import com.id11013962.trackingapp.MongoDB.MongoGetParcelInfoAsyncTask;
import com.id11013962.trackingapp.MongoDB.MongoUpdateParcelInfoStatusAsyncTask;
import com.id11013962.trackingapp.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * View Parcel List To be Delivered Activity
 * Use: RecyclerView
 */
public class ViewListOfParcelsActivity extends Activity {
    private ArrayList<DbParcelToDeliverDataModel> mDbDatas = new ArrayList<>();
    private ArrayList<DbParcelInfoDataModel> mParcelInfoList = new ArrayList<>();
    private Integer mPosition;
    private ParcelAdapter mParcelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_parcels);
    }

    /**
     * Get Only Those Parcels that are registered in the "to be delivered" list
     * use recyclerview to display.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // get cloud data.
        getParcelData();

        // Setup the Recycler View.
        RecyclerView mListOfParcels = (RecyclerView) findViewById(R.id.parcel_recycler_view);
        mParcelAdapter = new ParcelAdapter(this, mParcelInfoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mListOfParcels.setLayoutManager(mLayoutManager);
        mListOfParcels.addItemDecoration(new ViewListDividerDecoration(this, LinearLayoutManager.VERTICAL));
        mListOfParcels.setAdapter(mParcelAdapter);

        // Recycler OnTouchListener Implementation
        mListOfParcels.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mListOfParcels, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                mPosition = position;
                DbParcelInfoDataModel mSelectedItem = mParcelInfoList.get(position);
                Toast.makeText(getApplicationContext(), mSelectedItem.getParcelNumber() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    /**
     * Get Parcel Data in cloud database.
     */
    private void getParcelData() {
        //Get your cloud contacts
        MongoGetAllParcelToDeliverAsyncTask task = new MongoGetAllParcelToDeliverAsyncTask();
        try {
            mDbDatas = task.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Only get parcel information of those parcels that are registered in the parcel list to be delivered.
        for (DbParcelToDeliverDataModel data : mDbDatas){
            MongoGetParcelInfoAsyncTask parcelInfo = new MongoGetParcelInfoAsyncTask(data.getParcelNumber());
            try {
                DbParcelInfoDataModel mParcelInfo = parcelInfo.execute().get();
                mParcelInfoList.add(mParcelInfo);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Chosen parcel set to delivered and update database.
     */
    public void DeliveredParcelButtonHandler(View view){
        DbParcelInfoDataModel parcelSelected = mParcelInfoList.get(mPosition);
        String dateNow = DateFormat.getDateTimeInstance().format(new Date());

        parcelSelected.setStatus(getString(R.string.delivered));
        parcelSelected.setDateTimeDelivered(dateNow);
        mParcelAdapter.notifyDataSetChanged();

        MongoUpdateParcelInfoStatusAsyncTask updateStatus = new MongoUpdateParcelInfoStatusAsyncTask(parcelSelected.getParcelNumber(), getString(R.string.delivered), dateNow);
        updateStatus.execute();
    }

    /**
     * Chosen parcel set to attempted delivery and update database.
     */
    public void NotDeliveredParcelButtonHandler(View view){
        DbParcelInfoDataModel parcelSelected = mParcelInfoList.get(mPosition);
        parcelSelected.setStatus(getString(R.string.attempted_delivery));
        parcelSelected.setDateTimeDelivered(getString(R.string.empty_string));
        mParcelAdapter.notifyDataSetChanged();

        MongoUpdateParcelInfoStatusAsyncTask updateStatus =
                new MongoUpdateParcelInfoStatusAsyncTask(parcelSelected.getParcelNumber(), getString(R.string.attempted_delivery), getString(R.string.empty_string));
        updateStatus.execute();
    }

    /**
     * Recycler View Touch Interface
     * Code this followed from tutorial android hive
     * Link; - http://www.androidhive.info/2016/01/android-working-with-recycler-view/
     */
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    /**
     * Recycler View Touch Listener class.
     * Code this followed from tutorial android hive
     * Link; - http://www.androidhive.info/2016/01/android-working-with-recycler-view/
     */
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ViewListOfParcelsActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ViewListOfParcelsActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}

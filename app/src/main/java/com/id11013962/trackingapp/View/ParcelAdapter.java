package com.id11013962.trackingapp.View;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.id11013962.trackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.trackingapp.R;

import java.util.ArrayList;

/**
 * Adapter for the Recycler View. Each Parcel view binds
 */
public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<DbParcelInfoDataModel> mParcelList;

    /**
     * Gets the context and parcelList.
     */
    public ParcelAdapter (Context context, ArrayList parcelList){
        this.mContext = context;
        this.mParcelList = parcelList;
    }

    /**
     * inflate each adapter parcel item layout
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.adapter_parcel_item, parent, false);
        return new ViewHolder(itemView);
    }

    /**
     * Binds each item to be displayed on the recycler view
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DbParcelInfoDataModel dbData = mParcelList.get(position);
        holder.mParcelNumber.setText(dbData.getParcelNumber());
        holder.mFullName.setText(String.valueOf(dbData.getFullName()));

        // Format string full address using String Format
        String fullAddress = String.format("%s , %s %s %s %s",
                                            dbData.getAddress(),
                                            dbData.getSuburb(),
                                            dbData.getState(),
                                            dbData.getCity(),
                                            dbData.getPostcode());
        holder.mAddress.setText(fullAddress);
        holder.mParcelStatus.setText(dbData.getStatus());
        holder.mDateTimeDelivered.setText(dbData.getDateTimeDelivered());
    }

    @Override
    public int getItemCount() {
        return mParcelList.size();
    }

    /**
     * Custom ViewHolder gets all the required attributes
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mParcelNumber;
        public TextView mFullName;
        public TextView mAddress;

        public TextView mParcelStatus;
        public TextView mDateTimeDelivered;

        public ViewHolder(View view) {
            super(view);
            mParcelNumber = (TextView) view.findViewById(R.id.adapter_parcel_number);
            mFullName = (TextView) view.findViewById(R.id.adapter_parcel_full_name);
            mAddress = (TextView) view.findViewById(R.id.adapter_parcel_address);
            mParcelStatus = (TextView) view.findViewById(R.id.adapter_parcel_status);
            mDateTimeDelivered = (TextView) view.findViewById(R.id.adapter_parcel_dateTimeDelivered);
        }
    }
}

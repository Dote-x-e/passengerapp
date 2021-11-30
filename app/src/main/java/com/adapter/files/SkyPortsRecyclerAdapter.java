package com.adapter.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.levaeu.passenger.R;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by tarwindersingh on 06/01/18.
 */

public class SkyPortsRecyclerAdapter extends RecyclerView.Adapter<SkyPortsRecyclerAdapter.ViewHolder> {

    ArrayList<HashMap<String, String>> listData;
    Context mContext;
    OnSelectListener onSelectListener;
    int pos=-1;



    public SkyPortsRecyclerAdapter(ArrayList<HashMap<String, String>> listData, Context mContext) {
        this.listData = listData;
        this.mContext = mContext;
    }

    public void setSelectedSkyPort(Date selectedDate) {
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_choose_skyports, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    public void setSelectedListener(OnSelectListener OnSelectListener) {
        this.onSelectListener = OnSelectListener;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final HashMap<String, String> item = listData.get(position);

        holder.skyPortTitle.setText((String) item.get("skyPortTitle"));
        holder.skyPortAddress.setText((String) item.get("skyPortAddress"));
        holder.skyPortKm.setText((String) item.get("skyPortKm"));
        holder.skyPortHTxt.setText((String) item.get("LBL_AWAY_TXT"));


        if (pos!=-1 && pos==position)
        {
            holder.containerView.setBackgroundColor(mContext.getResources().getColor(R.color.selectedColor));
            holder.skyPortHTxt.setVisibility(View.VISIBLE);
        }else
        {
            holder.containerView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.skyPortHTxt.setVisibility(View.INVISIBLE);

        }


        holder.containerView.setOnClickListener(view -> {
            if (onSelectListener != null) {
                pos=position;
                notifyDataSetChanged();
                onSelectListener.onDataSelect(position);
            }
        });

    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listData.size();
    }


    public interface OnSelectListener {
        void onDataSelect(int position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {


        private  View containerView;
        private MTextView skyPortAddress;
        private MTextView skyPortKm;
        private MTextView skyPortTitle;
        private MTextView skyPortHTxt;

        public ViewHolder(View view) {
            super(view);

            skyPortKm = (MTextView) view.findViewById(R.id.skyPortKm);
            skyPortTitle = (MTextView) view.findViewById(R.id.skyPortTitle);
            skyPortAddress = (MTextView) view.findViewById(R.id.skyPortAddress);
            skyPortHTxt = (MTextView) view.findViewById(R.id.skyPortHTxt);
            containerView = view;

        }
    }
}

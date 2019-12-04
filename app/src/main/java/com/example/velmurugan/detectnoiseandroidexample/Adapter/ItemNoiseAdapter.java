package com.example.velmurugan.detectnoiseandroidexample.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.velmurugan.detectnoiseandroidexample.Model.ItemNoise;
import com.example.velmurugan.detectnoiseandroidexample.R;

import java.util.ArrayList;

public class ItemNoiseAdapter extends   RecyclerView.Adapter<ItemNoiseAdapter.ViewHolder> {

    private ArrayList<ItemNoise> itemsList;
    private Activity activity;
    public ItemNoiseAdapter(Activity activity, ArrayList<ItemNoise> itemsList){
        this.itemsList = itemsList;
        this.activity = activity;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_noise_data, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.dbCount.setText(itemsList.get(position).getDbCOunt());

        if (itemsList.get(position).getStatus().equals("Good")){
            holder.status.setTextColor(activity.getResources().getColor(R.color.greenAccent));
        }
        else if (itemsList.get(position).getStatus().equals("Normal")){
            holder.status.setTextColor(activity.getResources().getColor(R.color.myYellow));
        }
        else{
            holder.status.setTextColor(activity.getResources().getColor(R.color.redAccent));
        }

        holder.status.setText (itemsList.get(position).getStatus());
        holder.duration.setText(itemsList.get(position).getDuration());
        holder.startTime.setText (itemsList.get(position).getStartTime());
        holder.endTime.setText(itemsList.get(position).getEndTime());
        holder.latitude.setText (itemsList.get(position).getLat());
        holder.longitude.setText("View on Map");

        holder.longitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q="+ (itemsList.get(position).getLat())+","+ (itemsList.get(position).getLong())+"(label)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(mapIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView dbCount;
        TextView status;
        TextView duration;
        TextView startTime;
        TextView endTime;
        TextView latitude;
        TextView longitude;

        ViewHolder(View view) {
            super(view);
            dbCount = view.findViewById(R.id.db_count);
            status = view.findViewById(R.id.status);
            duration = view.findViewById(R.id.duration);
            startTime = view.findViewById(R.id.start_time);
            endTime = view.findViewById(R.id.end_time);
            latitude = view.findViewById(R.id.latitude);
            longitude = view.findViewById(R.id.longitude);

        }

    }




}
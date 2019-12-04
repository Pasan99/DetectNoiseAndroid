package com.example.velmurugan.detectnoiseandroidexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.velmurugan.detectnoiseandroidexample.Adapter.ItemNoiseAdapter;
import com.example.velmurugan.detectnoiseandroidexample.Model.ItemNoise;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class NoiseListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView itemRecycleView;
    private ArrayList<ItemNoise> noiseArrayList = new ArrayList<ItemNoise>();
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noise_list);

        itemRecycleView = findViewById(R.id.noise_recycle_view);
        itemRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeRefresh = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefresh.setOnRefreshListener(this);
        getData();

    }

    private void getData(){
        noiseArrayList = new ArrayList<>();
        mSwipeRefresh.setRefreshing(true);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("noise_data")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()){
                            ItemNoise noise = new ItemNoise(
                                    (snap.child("db_count").getValue()).toString(),
                                    (snap.child("duration").getValue()).toString(),
                                    (snap.child("start_time").getValue()).toString(),
                                    (snap.child("end_time").getValue()).toString(),
                                    (snap.child("latitude").getValue()).toString(),
                                    (snap.child("longitude").getValue()).toString()
                            );
                            noiseArrayList.add(noise);
                        }
                        startRecycleView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void startRecycleView() {
        ItemNoiseAdapter noiseAdapter = new ItemNoiseAdapter(this, noiseArrayList);
        itemRecycleView.setAdapter(noiseAdapter);
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        getData();
    }
}

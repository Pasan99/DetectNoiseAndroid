package com.example.velmurugan.detectnoiseandroidexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shinelw.library.ColorArcProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    /* constants */
    private static final int POLL_INTERVAL = 300;
    private static final String NOISE_ALERT = "NoiseAlert:";

    /** running state **/
    private boolean mRunning = false;

    /** config state **/
    private int mThreshold;

    int RECORD_AUDIO = 0;
    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mStatusView, tv_noice, mNoiseStatus;
    private MaterialButton mRecordBtn;
    private boolean isRecording = false;
    private ArrayList<Double> mListOfNoises = new ArrayList<>();
    private Timer mTimer = new Timer();
    private int recordedTime;

    private double mLongitude;
    private double mLatitude;
    private String mLastTime;
    private String mStartTime;
    private String mEndTime;
    private String mLastDbCount;

    private MaterialButton mViewBtn;


    /* sound data source */
    private DetectNoise mSensor;
    ColorArcProgressBar bar;


    /****************** Define runnable thread again and again detect noise *********/
    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");
            start();
        }
    };

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            updateDisplay("Monitoring Voice...", amp);

            if ((amp > mThreshold)) {
                callForHelp(amp);
                //Log.i("Noise", "==== onCreate ===");
            }
            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

    /** Called when the activity is first created. */
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Defined SoundLevelView in main.xml file
        setContentView(R.layout.activity_main);

        initViews();
        initRecorder();
    }

    private void initViews() {
        mStatusView = findViewById(R.id.status);
        tv_noice = findViewById(R.id.tv_noice);
        mNoiseStatus = findViewById(R.id.noise_status);
        bar = findViewById(R.id.progressBar1);
        mRecordBtn = findViewById(R.id.record_btn);
        mViewBtn = findViewById(R.id.viewBtn);

        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                    isRecording = true;
                } else {
                    stopRecording();
                    isRecording = false;
                }
            }
        });

        mViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NoiseListActivity.class));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void startRecording() {
        // views
        mRecordBtn.setBackgroundColor(getResources().getColor(R.color.redAccent));
        mRecordBtn.setText("Stop");
        mStatusView.setTextColor(getResources().getColor(R.color.redAccent));

        mStartTime = getTime();
        startTimer();

    }

    @SuppressLint("SetTextI18n")
    private void stopRecording() {
        // views
        mRecordBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mRecordBtn.setText("Record");
        mStatusView.setTextColor(getResources().getColor(R.color.dark));

        // data

        if (mListOfNoises.size() > 0) {
            double total = 0;
            for (double item : mListOfNoises) {
                total += item;
            }
            total = total / mListOfNoises.size();

            mEndTime = getTime();
            mLastDbCount = String.valueOf(total);
            mLastTime = String.valueOf(recordedTime);
            showDialog(String.valueOf(total));

        }
        mListOfNoises.clear();
        recordedTime = 0;
        mTimer.cancel();
    }

    private void saveData() {
        Snackbar.make(mStatusView, "Saving...", Snackbar.LENGTH_LONG).show();
        Map<String, String> map = new HashMap<String, String>();
        map.put("db_count", mLastDbCount);
        map.put("duration", mLastTime);
        map.put("start_time", String.valueOf(mStartTime));
        map.put("end_time", String.valueOf(mEndTime));
        map.put("longitude", String.valueOf(mLongitude));
        map.put("latitude", String.valueOf(mLatitude));

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("noise_data")
                .child(getDocId())
                .setValue(map);
    }

    private String getDocId() {
        long date = (3574164722L) - Timestamp.now().getSeconds();
        return String.valueOf(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        assert lm != null;
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        assert location != null;
        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();

        saveData();

//        final LocationListener locationListener = new LocationListener() {
//            public void onLocationChanged(Location location) {
//                mLongitude = location.getLongitude();
//                mLatitude = location.getLatitude();
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//        };
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordedTime++;
                    }
                });
            }
        }, 1000, 1000);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showDialog(String dbCount) {
        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.dialog_record_complete)
                .show();

        TextView dbCountText = dialog.findViewById(R.id.db_count);
        TextView time = dialog.findViewById(R.id.time);

        assert dbCountText != null;
        dbCountText.setText(dbCount.substring(0, 7) + " dB");
        assert time != null;
        time.setText(recordedTime + " sec");

        Objects.requireNonNull(dialog.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Objects.requireNonNull(dialog.findViewById(R.id.save_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getLocation();
                }
                dialog.dismiss();
            }
        });
    }


    private void initRecorder() {
        // Used to record voice
        mSensor = new DetectNoise();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, NOISE_ALERT);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeApplicationConstants();
        if (!mRunning) {
            mRunning = true;
            start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");
        //Stop noise monitoring
        stop();
    }

    private void start() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }

        //Log.i("Noise", "==== start ===");
        mSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        Log.d("Noise", "==== Stop Noise Monitoring===");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        bar.setCurrentValues(0);
        updateDisplay("stopped...", 0.0);
        mRunning = false;

    }


    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 13;

    }

    @SuppressLint("SetTextI18n")
    private void updateDisplay(String status, double signalEMA) {
        if (!isRecording) {
            mStatusView.setText(status);
            if (signalEMA > -14) {
                mListOfNoises.add(signalEMA);
            }
        } else {
            mStatusView.setText("Recording...");
        }
        //
        if (signalEMA < 1) {
            mNoiseStatus.setText("Good");
            mNoiseStatus.setTextColor(Color.GREEN);
        } else {

            mNoiseStatus.setText("Normal");
            mNoiseStatus.setTextColor(getResources().getColor(R.color.myYellow));
        }
        bar.setCurrentValues((int) signalEMA);
        Log.d("SONUND", String.valueOf(signalEMA));
        tv_noice.setText(signalEMA + "dB");
    }


    private String getTime(){
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH)+1;
        int date = Calendar.getInstance().get(Calendar.DATE);
        int hours = Calendar.getInstance().get(Calendar.HOUR);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        if (Calendar.getInstance().get(Calendar.AM_PM) == 0){
            return hours+":"+min+" AM    "+year+"/"+month+"/"+date;
        }
         return hours+":"+min+" PM    "+year+"/"+month+"/"+date;
    }

    @SuppressLint("SetTextI18n")
    private void callForHelp(double signalEMA) {
        if (!isRecording) {
            if (signalEMA > -14) {
                mListOfNoises.add(signalEMA);
            }
        }
        // alert
        mNoiseStatus.setText("Noisy");
        mNoiseStatus.setTextColor(Color.RED);
        Log.d("SONUND", String.valueOf(signalEMA));
        tv_noice.setText(signalEMA + "dB");
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                assert lm != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                assert location != null;
                mLongitude = location.getLongitude();
                mLatitude = location.getLatitude();

                saveData();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}

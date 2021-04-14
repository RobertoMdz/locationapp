package com.coopera.locationapp;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationJobService extends JobService {
    public static final String TAG = "MY-JOB-LOCATION";
    private boolean isJobCancelled = false;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job is started");
        String phoneImei = getImeiFromPreferences();
        Log.d(TAG, "Phone IMEI: " +  phoneImei);
        runTaskInBackground(params);
        return true;
    }

    private String getImeiFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("phoneInfo", Context.MODE_PRIVATE);
        return sharedPreferences.getString("phoneImei", "null");
    }

    private void runTaskInBackground(JobParameters params) {
        new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                locationProviderClient.getLastLocation().addOnCompleteListener( new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(isJobCancelled) {
                            return;
                        }
                        Location location = task.getResult();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d(TAG, "Latitude: " + latitude + " Longitude: " + longitude);
                        } else {
                            Log.d(TAG, "isLocationNull");
                        }
                        Log.d(TAG, "Job finished");
                        jobFinished(params, false);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job has been canceled");
        isJobCancelled = false;
        return false;
    }
}

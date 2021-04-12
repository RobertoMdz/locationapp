package com.coopera.locationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 200;
    private static final int JOB_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void requestDeviceLocation(View view) {
        if(isGpsEnabled()) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                configureJobRestriccions();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showRationaleOrEnableGpsDialogMessage(
                            getString(R.string.location_request_text),
                            getString(R.string.location_request_message),
                            getString(R.string.get_request_btn_text),
                            true
                    );
                } else {
                    requestPermissions();
                }
            }
        } else {
            showRationaleOrEnableGpsDialogMessage(
                    getString(R.string.gps_title_text),
                    getString(R.string.gps_message_text),
                    getString(R.string.accept_btn_text),
                    false
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                configureJobRestriccions();
            } else {
                Toast.makeText(this, "La aplicación podría no funcionar correctamente", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showRationaleOrEnableGpsDialogMessage(String title, String message, String positiveBtnText, boolean isRationaleDialog) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog .setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isRationaleDialog) {
                    requestPermissions();
                } else {
                    dialog.cancel();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel_btn_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.create();
        dialog.show();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, LOCATION_REQUEST_CODE);
        }
    }

    public void configureJobRestriccions() {
        ComponentName componentName = new ComponentName(this, LocationJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                .setRequiresCharging(false)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();
        setJobToJobScheduler(jobInfo);
    }

    private void setJobToJobScheduler(JobInfo jobInfo) {
        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Main Activity", "Job scheduled success");
        } else {
            Log.d("Main Activity", "Job scheduled failed");
        }
    }

    public void cancelJob(View view) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
        Log.d("Main Activity", "Job scheduled canceled");
    }
}
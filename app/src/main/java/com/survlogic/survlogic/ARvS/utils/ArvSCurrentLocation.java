package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.survlogic.survlogic.ARvS.interf.ArvSOnLocationChangeListener;
import com.survlogic.survlogic.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by chrisfillmore on 3/17/2018.
 */

public class ArvSCurrentLocation  {
    private static final String TAG = "ArvSCurrentLocation";

    private Context mContext;
    private Activity mActivity;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;

    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private ArvSOnLocationChangeListener onLocationChangeListener;

    private Boolean mRequestingLocationUpdates = false;
    private String mLastUpdateTime;

    private static final int GPS_INTERVAL = 10 * 1000;  // 10 SECONDS, in milliseconds
    private static final int GPS_FASTEST_INTERVAL = 1 * 1000; //1 second, in milliseconds

    //----------------------------------------------------------------------------------------------//
    public ArvSCurrentLocation(ArvSOnLocationChangeListener onLocationChangeListener){
        Log.d(TAG, "MyCurrentLocation: Started");

        this.onLocationChangeListener = onLocationChangeListener;


    }

    public synchronized void buildGoogleApiClient(Context context){
        Log.d(TAG, "buildGoogleApiClient: Started");

        mContext = context;
        mActivity = (Activity) mContext;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();


    }

    public void startGPS(){
        Log.d(TAG, "startGPS: Started");
        if(!mRequestingLocationUpdates){
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }

    }

    public void stopGPS(){
        Log.d(TAG, "stopGPS: Started");
        stopLocationUpdates();

    }

    //----------------------------------------------------------------------------------------------//
    private void createLocationCallback(){
        Log.d(TAG, "createLocationCallback: Started");

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationListener();
            }
        };

    }

    private void createLocationRequest(){
        Log.d(TAG, "createLocationRequest: Started");

        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(GPS_INTERVAL);
        mLocationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void buildLocationSettingsRequest(){
        Log.d(TAG, "buildLocationSettingsRequest: Started");

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();


    }

    //----------------------------------------------------------------------------------------------//
    private void updateLocationListener() {
        Log.d(TAG, "updateLocationListener: Started");

        if(mCurrentLocation !=null) {
            onLocationChangeListener.onLocationChanged(mCurrentLocation);
            Log.d(TAG, "updateLocationListener: Location: " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        }else{
            showToast("Location Not found Yet", true);
        }


    }

    private void startLocationUpdates(){
        Log.d(TAG, "startLocationUpdates: Started");

        try {
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "All Location Settings are satisfied.");
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());

                            updateLocationListener();

                        }
                    })
                    .addOnFailureListener(mActivity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    break;

                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }

                            updateLocationListener();
                        }
                    });

        }catch (SecurityException ex) {
            Log.e(TAG, "startLocationUpdates: " + ex);
        }

    }

    private void stopLocationUpdates(){
        Log.d(TAG, "stopLocationUpdates: Started");

        if(!mRequestingLocationUpdates){
            Log.d(TAG, "stopLocationUpdates: updated never requested, no operation");
            return;
        }

        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(mActivity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });

    }

    //----------------------------------------------------------------------------------------------//
    private void showToast(String data, boolean isShortTime) {

        if (isShortTime) {
            Toast.makeText(mActivity, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mActivity, data, Toast.LENGTH_LONG).show();

        }
    }


}

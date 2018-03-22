package com.survlogic.survlogic.ARvS.utils;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.survlogic.survlogic.ARvS.interf.ArvSOnAzimuthChangeListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chrisfillmore on 3/18/2018.
 */

public class ArvSCurrentAzimuth implements SensorEventListener {

    private static final String TAG = "ArvSCurrentAzimuth";
    private Context mContext;

    private static SensorManager mSensorManager;
    private Sensor mSensor;

    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;
    private static GeomagneticField gmf = null;
    private AtomicBoolean computing = new AtomicBoolean(false);
    private static double floatBearing = 0;

    private static final float grav[] = new float[3];
    private static final float mag[] = new float[3];
    private static final float rotation[] = new float[9];
    private static final float orientation[] = new float[3];
    private static float smoothed[] = new float[3];

    private int azimuthFrom = 0;
    private int azimuthTo = 0;

    private boolean isSimpleSensor;

    private ArvSOnAzimuthChangeListener mAzimuthListener;

    public ArvSCurrentAzimuth(Context mContext, ArvSOnAzimuthChangeListener mAzimuthListener, boolean isSimpleSensor ) {
        this.mContext = mContext;
        this.mAzimuthListener = mAzimuthListener;
        this.isSimpleSensor = isSimpleSensor;
    }

    public void startAzimuth(){
        Log.d(TAG, "startAzimuth: Started");

        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);

        if(isSimpleSensor){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() >0) {
                Log.d(TAG, "startAzimuth: Senosr Size Acc:" + sensors.size());
                sensorGrav = sensors.get(0);
            }
            sensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() >0) {
                Log.d(TAG, "startAzimuth: Senosr Size Acc:" + sensors.size());
                sensorMag = sensors.get(0);
            }

            mSensorManager.registerListener(this,sensorGrav,SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this,sensorMag,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void stopAzimith(){
        Log.d(TAG, "stopAzimith: Started");


        if(isSimpleSensor){
            mSensorManager.unregisterListener(this);

        }else{
            mSensorManager.unregisterListener(this,sensorGrav);
            mSensorManager.unregisterListener(this,sensorMag);
        }

        mSensorManager = null;

    }

    public void setOnShakeListener(ArvSOnAzimuthChangeListener listener){
        Log.d(TAG, "setOnShakeListener: Started");

        mAzimuthListener = listener;

    }

    //----------------------------------------------------------------------------------------------//
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged- Started");

        if(isSimpleSensor){
            onSimpleSensorChange(event);
        }else{
            onAdvanceSensorChange(event);
        }


    }


    private void onSimpleSensorChange(SensorEvent event) {
        Log.d(TAG, "OnSimpleSensorChange: Started");

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            azimuthFrom = azimuthTo;

            float[] orientation = new float[3];
            float[] rMat = new float[9];
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            azimuthTo = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;

            mAzimuthListener.onAzimuthChanged(azimuthFrom, azimuthTo);
        }

    }

    private void onAdvanceSensorChange(SensorEvent event) {
        Log.d(TAG, "OnAdvanceSensorChange: Started");

        if (!computing.compareAndSet(false,true )){
            Log.d(TAG, "onSensorChanged: Returning...");
            return;
        }

        azimuthFrom = azimuthTo;

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            Log.d(TAG, "onSensorChanged: Sensor is Accelerometer");

            smoothed = ArvSLowPassFilter.filter(event.values,grav);

            grav[0] = smoothed[0];
            grav[1] = smoothed[1];
            grav[2] = smoothed[2];

        }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            Log.d(TAG, "onSensorChanged: Sensor is Magnetic Field");

            smoothed = ArvSLowPassFilter.filter(event.values, mag);

            mag[0] = smoothed[0];
            mag[1] = smoothed[1];
            mag[2] = smoothed[2];
        }

        SensorManager.getRotationMatrix(rotation,null,grav,mag);
        SensorManager.getOrientation(rotation,orientation);
        floatBearing = orientation[0];

        floatBearing = Math.toDegrees(floatBearing);
        Log.d(TAG, "onSensorChanged: Floating Bearing: " + floatBearing);

        if (gmf !=null){
            floatBearing += gmf.getDeclination();
        }

        if (floatBearing <0) {
            floatBearing += 360;
        }

        azimuthTo = (int) floatBearing;

        Log.d(TAG, "onSensorChanged: Azimuth From: " + azimuthFrom + ", To:" + azimuthTo );

        mAzimuthListener.onAzimuthChanged(azimuthFrom, azimuthTo);

        computing.set(false);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

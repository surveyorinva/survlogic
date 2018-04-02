package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

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
    private GeomagneticField gmf = null;
    private AtomicBoolean computing = new AtomicBoolean(false);
    private static double floatBearing = 0;

    private static final float grav[] = new float[3];
    private static final float mag[] = new float[3];
    private static final float rotation[] = new float[9];
    private static final float orientation[] = new float[3];
    private static float smoothed[] = new float[3];

    private static final float vector_orientation[] = new float[3];
    private static final float vector_rotation[] = new float[9];
    private static float vector_orientation_smoothed[] = new float[3];

    private final WindowManager mWindowManager;

    private static float cameraRotation[]= new float[9];
    private float cameraOrientation[] = new float[3];


    private int azimuthFrom = 0;
    private int azimuthTo = 0;

    private boolean isSimpleSensor;

    private ArvSOnAzimuthChangeListener mAzimuthListener;

    public ArvSCurrentAzimuth(Context mContext, ArvSOnAzimuthChangeListener mAzimuthListener, boolean isSimpleSensor ) {
        this.mContext = mContext;

        Activity mActivity = (Activity) mContext;
        this.mWindowManager = mActivity.getWindow().getWindowManager();

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
                Log.d(TAG, "startAzimuth: Sensor Size Acc:" + sensors.size());
                sensorGrav = sensors.get(0);
            }
            sensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() >0) {
                Log.d(TAG, "startAzimuth: Sensor Size Acc:" + sensors.size());
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


    public void setGmf(GeomagneticField gmf){
        this.gmf = gmf;
    }

    //----------------------------------------------------------------------------------------------//
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged- Started");

        if(isSimpleSensor){
            //onSimpleSensorChange(event);
            onSimpleSensorChangeSmoothedWithPitch(event);

        }else{
            onAdvanceSensorChange(event);
        }

    }

    private void onSimpleSensorChangeSmoothed(SensorEvent event){
        Log.d(TAG, "onSimpleSensorChangeOriginal: Started");

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            azimuthFrom = azimuthTo;

            float[] orientation = new float[3];
            float[] rMat = new float[9];
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            SensorManager.getOrientation(rMat,orientation);

            int azimuthTo_Original = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
            Log.d(TAG, "onSimpleSensorChangeSmoothed: Original Orientation: " + azimuthTo_Original);
            Log.d(TAG, "onSimpleSensorChangeSmoothed: Orientation: " + Math.toDegrees(orientation[0]));
            mAzimuthListener.onAzimuthChanged(azimuthFrom, azimuthTo_Original, orientation);
        }
    }

    private void onSimpleSensorChangeSmoothedWithPitch(SensorEvent event){
        Log.d(TAG, "onSimpleSensorChangeOriginal: Started");

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            azimuthFrom = azimuthTo;

            float[] orientation = new float[3];
            float[] rMat = new float[9];

            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            final int worldAxisForDeviceAxisX;
            final int worldAxisForDeviceAxisY;

            // Remap the axes as if the device screen was the instrument panel,
            // and adjust the rotation matrix for the device orientation.
            switch (mWindowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                default:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                    break;
                case Surface.ROTATION_90:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                    break;
                case Surface.ROTATION_180:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                    break;
                case Surface.ROTATION_270:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                    break;
            }

            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rMat, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);

            // Transform rotation matrix into azimuth/pitch/roll
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);

            int azimuthTo_Original = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
            Log.d(TAG, "onSimpleSensorChangeSmoothed: Original Orientation: " + azimuthTo_Original);
            Log.d(TAG, "onSimpleSensorChangeSmoothed: Orientation: " + Math.toDegrees(orientation[0]));

            mAzimuthListener.onAzimuthChanged(azimuthFrom, azimuthTo_Original, orientation);
        }
    }



    private void onSimpleSensorChangeOriginal(SensorEvent event){
        Log.d(TAG, "onSimpleSensorChangeOriginal: Started");

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            azimuthFrom = azimuthTo;

            float[] orientation = new float[3];
            float[] rMat = new float[9];
            SensorManager.getRotationMatrixFromVector(rMat, event.values);

            azimuthTo = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;

            Log.d(TAG, "onSimpleSensorChangeOriginal: Correct: " + azimuthTo);
            mAzimuthListener.onAzimuthChanged(azimuthFrom, azimuthTo, orientation);
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

        //------------------------------------------------------------------------------------------//

        float compOrientation[] = new float[3];
        float[] compRotationMatrix = new float[9];

        SensorManager.getRotationMatrix(compRotationMatrix,null,grav,mag);
        SensorManager.getOrientation(compRotationMatrix,compOrientation);

        float cameraRotation[] = new float[9];
        SensorManager.remapCoordinateSystem(compRotationMatrix,SensorManager.AXIS_X,SensorManager.AXIS_Y,cameraRotation);

        float cameraOrientation[] = new float[3];
        SensorManager.getOrientation(cameraRotation,cameraOrientation);


        //------------------------------------------------------------------------------------------//
        floatBearing = orientation[0];

        floatBearing = Math.toDegrees(floatBearing);
        Log.d(TAG, "onSensorChanged: Floating Bearing: " + floatBearing);


        if (floatBearing <0) {
            floatBearing += 360;
        }

        azimuthTo = (int) floatBearing;

        Log.d(TAG, "onSensorChanged: Azimuth From: " + azimuthFrom + ", To:" + azimuthTo );

        mAzimuthListener.onAzimuthChanged(azimuthFrom, azimuthTo, orientation);
        mAzimuthListener.onDeviceSensorChange(event);

        computing.set(false);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class SensorService extends Service implements SensorEventListener {

    private static final String TAG = "SensorService";

    private WindowManager mWindowManager;

    private final SensorServiceBinder binder = new SensorServiceBinder();

    //Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticSensor;
    private Sensor mOrientationSensor;
    private Sensor mRotationVectorSensor;

    //Sensor Data
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float mOrientation[] = new float[3];

    //Method variables
    private boolean useFusedSensor = false;
    private boolean isSensorRunning = false;

    //Sensor Variables
    private AtomicBoolean computing = new AtomicBoolean(false);
    private static final float grav[] = new float[3];
    private static final float mag[] = new float[3];
    private static final float rotation[] = new float[9];
    private static final float orientation[] = new float[3];
    private static float smoothed[] = new float[3];

    //Filter Variables
    private float filterAlpha;

    public SensorService() {

    }

    //----------------------------------------------------------------------------------------------Class Methods
    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Started");
        return binder;

    }

    @Override
    public void onRebind(Intent intent) {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        this.stopSensor();
        stopSelf();
    }

    //----------------------------------------------------------------------------------------------Binder Class

    public class SensorServiceBinder extends Binder {
        public SensorService getService()
        {
            return SensorService.this;
        }
    }

    //----------------------------------------------------------------------------------------------Public Methods

    public void setupSensor(Context context, boolean useFusedSensor){
        Log.d(TAG, "setupSensor: Started");

        initSensorSettings(context, useFusedSensor);

    }

    public void startSensor(){
        Log.d(TAG, "startSensor: Started");

        startUpdatingSensor();
    }

    public void stopSensor(){
        Log.d(TAG, "stopSensor: Started");

        stopUpdatingSensor();

    }

    public boolean isUseFusedSensor() {
        return useFusedSensor;
    }

    public void setUseFusedSensor(boolean useFusedSensor) {
        this.useFusedSensor = useFusedSensor;
    }

    public float getFilterAlpha() {
        return filterAlpha;
    }

    public void setFilterAlpha(float filterAlpha) {
        this.filterAlpha = filterAlpha;
    }

    //----------------------------------------------------------------------------------------------Sensor Methods Start Up/Stop

    private void initSensorSettings(Context context, boolean useFusedSensor){
        Log.d(TAG, "initSensorSettings: Started");

        Activity mActivity = (Activity) context;
        this.mWindowManager = mActivity.getWindow().getWindowManager();

        this.useFusedSensor = useFusedSensor;

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    private void startUpdatingSensor(){
        Log.d(TAG, "startUpdatingSensor: Started");
        Log.d(TAG, "startUpdatingSensor: Fused Sensor: " + useFusedSensor);

        if(!isSensorRunning){

            if(useFusedSensor){
                mSensorManager.registerListener(this,mRotationVectorSensor,SensorManager.SENSOR_DELAY_NORMAL);

            }else{
                mSensorManager.registerListener(this,mAccelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(this,mMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
            }

            isSensorRunning = true;
        }

    }

    private void stopUpdatingSensor(){
        Log.d(TAG, "stopUpdatingSensor: Started");

        if(isSensorRunning){

            mSensorManager.unregisterListener(this);

            isSensorRunning = false;
        }

    }

    //----------------------------------------------------------------------------------------------Sensor Methods

    private void sensorTypeRotationVector(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] orientation = new float[3];
            float[] rMat = new float[9];

            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            final int worldAxisForDeviceAxisX;
            final int worldAxisForDeviceAxisY;

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

            int azimuth = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
            float pitch = (float) Math.toDegrees(orientation[1]);
            float roll = (float) Math.toDegrees(orientation[2]);

            Log.i(TAG, "sensorTypeRotationVector: Azimuth: " + azimuth);
            Log.i(TAG, "sensorTypeRotationVector: Pitch: " + pitch);
            Log.i(TAG, "sensorTypeRotationVector: Roll: " + roll);

            Intent intent = new Intent("SensorData");
            intent.putExtra("azimuth",azimuth);
            intent.putExtra("pitch",pitch);
            intent.putExtra("roll",roll);
            LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

        }
    }

    private void sensorTypeMultipleSensors(SensorEvent event){
        Log.d(TAG, "sensorTypeMultipleSensors: Started");

        if (!computing.compareAndSet(false,true )){
            Log.d(TAG, "onSensorChangedFiltered: Returning...");
            return;
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            Log.d(TAG, "sensorTypeMultipleSensors: Sensor is Accelerometer");

            smoothed = ArvSLowPassFilter.filter(event.values,grav, filterAlpha);

            grav[0] = smoothed[0];
            grav[1] = smoothed[1];
            grav[2] = smoothed[2];

        }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            Log.d(TAG, "sensorTypeMultipleSensors: Sensor is Magnetic Field");

            smoothed = ArvSLowPassFilter.filter(event.values, mag, filterAlpha);

            mag[0] = smoothed[0];
            mag[1] = smoothed[1];
            mag[2] = smoothed[2];
        }

        SensorManager.getRotationMatrix(rotation,null,grav,mag);

        //------------------------------------------------------------------------------------------//

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

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
        SensorManager.remapCoordinateSystem(rotation, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transform rotation matrix into azimuth/pitch/roll
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        //------------------------------------------------------------------------------------------//

        int azimuth = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
        float pitch = (float) Math.toDegrees(orientation[1]);
        float roll = (float) Math.toDegrees(orientation[2]);


        Intent intent = new Intent("SensorData");
        intent.putExtra("azimuth",azimuth);
        intent.putExtra("pitch",pitch);
        intent.putExtra("roll",roll);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);


        computing.set(false);
    }



    //----------------------------------------------------------------------------------------------Sensor Event Listeners

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(useFusedSensor){
            sensorTypeRotationVector(sensorEvent);
        }else{
            sensorTypeMultipleSensors(sensorEvent);
        }
        
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String sensorText = String.valueOf(sensor);

        Intent intent = new Intent("SensorAccuracy");
        intent.putExtra("sensor",sensorText);
        intent.putExtra("accuracy",accuracy);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

    }
}

package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
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
    private int angleOfDevice = 45;

    //Filter Variables
    private float filterAlpha;

    //GMF Compensation
    private GeomagneticField gmf = null;
    private boolean hasLocation = false;
    private Location mCurrentLocation;

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


    public void setUseFusedSensor(boolean useFusedSensor) {
        this.useFusedSensor = useFusedSensor;
    }


    public void setFilterAlpha(float filterAlpha) {
        this.filterAlpha = filterAlpha;
    }

    public void setHasLocation(boolean hasLocation){
        this.hasLocation = hasLocation;
    }

    public void setSensorLocation(Location currentLocation){
        this.mCurrentLocation = currentLocation;
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
            float[] adjustedRotationMatrix = new float[9];

            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            final int worldAxisForDeviceAxisX;
            final int worldAxisForDeviceAxisY;

            int windowsOrientation = mWindowManager.getDefaultDisplay().getRotation();

            //--------------------------------------------------------------------------------------//
            switch (windowsOrientation) {
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
            //--------------------------------------------------------------------------------------//
            SensorManager.remapCoordinateSystem(rMat, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);


            //--------------------------------------------------------------------------------------//
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

            //------------------------------------------------------------------------------------------//
            getGmfSensorCompensation(orientation);

        }
    }

    private void sensorTypeMultipleSensors(SensorEvent event){
        Log.d(TAG, "sensorTypeMultipleSensors: Started");
        float[] adjustedRotationMatrix = new float[9];

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
        int windowsOrientation = mWindowManager.getDefaultDisplay().getRotation();
        switch (windowsOrientation) {
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

        //------------------------------------------------------------------------------------------//
        SensorManager.remapCoordinateSystem(rotation, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);

        //------------------------------------------------------------------------------------------//
        // Transform rotation matrix into azimuth/pitch/roll
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        int azimuth = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
        float pitch = (float) Math.toDegrees(orientation[1]);
        float roll = (float) Math.toDegrees(orientation[2]);

        Log.i(TAG, "sensorTypeMultipleSensors: Azimuth: " + azimuth);
        Log.i(TAG, "sensorTypeMultipleSensors: Pitch: " + pitch);
        Log.i(TAG, "sensorTypeMultipleSensors: Roll: " + roll);

        Intent intent = new Intent("SensorData");
        intent.putExtra("azimuth",azimuth);
        intent.putExtra("pitch",pitch);
        intent.putExtra("roll",roll);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

        computing.set(false);

        //------------------------------------------------------------------------------------------//
        getGmfSensorCompensation(orientation);


    }

    //---------------------------------------------------------------------------------------------- GMF Compensation

    private void getGmfSensorCompensation(float[] orientation){
        Log.d(TAG, "getGmfSensorCompensation: Started");
        if(hasLocation){
            setGmfForSensor();

            if (gmf !=null){
                Log.d(TAG, "onSensorChangedFiltered: Declination: " + gmf.getDeclination());

                double declination = Math.toRadians(gmf.getDeclination());

                if(declination < 0){
                    orientation[0] += Math.abs(declination);
                }else{
                    orientation[0] += declination;
                }

                int azimuth = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
                float pitch = (float) Math.toDegrees(orientation[1]);
                float roll = (float) Math.toDegrees(orientation[2]);

                Log.i(TAG, "getGmfSensorCompensation: Azimuth: " + azimuth);
                Log.i(TAG, "getGmfSensorCompensation: Pitch: " + pitch);
                Log.i(TAG, "getGmfSensorCompensation: Roll: " + roll);

                
                Intent intent = new Intent("SensorGMFData");
                intent.putExtra("azimuth",azimuth);
                intent.putExtra("pitch",pitch);
                intent.putExtra("roll",roll);
                LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

            }
        }
    }

    private synchronized void setGmfForSensor(){
        Log.d(TAG, "setGmfForSensor: Started");

            gmf = new GeomagneticField(
                (float) mCurrentLocation.getLatitude(),
                (float) mCurrentLocation.getLongitude(),
                (float) mCurrentLocation.getAltitude(),
                System.currentTimeMillis());

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
        boolean sensorWarning = false;

        switch (accuracy){
            case SensorManager.SENSOR_STATUS_UNRELIABLE: case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                Log.i(TAG, "evaluateSensorAccuracy: Unreliable/Low Sensor Reading on " + sensor);
                sensorWarning = true;
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                Log.i(TAG, "evaluateSensorAccuracy: Medium Sensor Reading on " + sensor);
                sensorWarning = false;
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                Log.i(TAG, "evaluateSensorAccuracy: High Sensor Reading on " + sensor);
                sensorWarning = false;
                break;

        }


        Intent intent = new Intent("SensorAccuracy");
        intent.putExtra("sensorWarning",sensorWarning);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

    }
}

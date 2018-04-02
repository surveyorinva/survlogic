package com.survlogic.survlogic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Range;
import android.view.View;

import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.ARvS.utils.ArvSLowPassFilter;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogProjectDescriptionAdd;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chrisfillmore on 3/24/2018.
 */

public class CameraOverlayView extends View {
    private static final String TAG = "Started";

    private Paint mTextPaint, mMainLinePaint, mSecondaryLinePaint, mMarkerPaint, mMarkerPulsePaint;
    private Path pathMarker;

    private int mTextColor, mBackgroundColor, mLineColor, mSecLineColor, mMarkerColor;
    private float mTextSize;

    private static int screenWidth, screenHeight;

    private SensorEvent mSensorEvent;
    private String mSensorMetaData, mRawVectorData, mRawAccelData, mRawCompassData, mRawGyroData;
    private double mAzimuthObserved = 0;
    private double mAzimuthTheoretical = 0;

    private ArvSLocationPOI mTargePoi;
    private Location mCurrentLocation;
    private float mOrientation[] = new float[3];

    private GeomagneticField gmf = null;

    private boolean isSensorDataAvailable = false, isLocationDataAvailable = false;

    private double mAzimuthRange = 5;

    private float mVerticalFOV, mHorizontalFOV;

    private boolean isDebugMode = false;
    private boolean useGMF = false;

    private boolean showTargetMarker = false;
    private boolean isPoiInRange = false;

    private float mRadiusPOI = 20.0F, mRadiusPOIMax = 75.0F, mRadiusPOICurrent = mRadiusPOI;


    private float ANGLE_UPPER_RANGE = 10, ANGLE_LOWER_RANGE = 10;
    private static final float POI_MAPPING_SCALE = 0.5F;


    public CameraOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CompassView, 0, 0);

        mBackgroundColor = a.getColor(R.styleable.CompassView_backgroundColor, Color.BLACK);
        mMarkerColor = a.getColor(R.styleable.CompassView_markerColor, Color.RED);
        mLineColor = a.getColor(R.styleable.CompassView_lineColor, Color.WHITE);
        mSecLineColor = a.getColor(R.styleable.CompassView_lineColor, Color.BLUE);
        mTextColor = a.getColor(R.styleable.CompassView_textColor, Color.WHITE);
        mTextSize = a.getDimension(R.styleable.CompassView_textSize, 15 * getResources().getDisplayMetrics().scaledDensity);

        a.recycle();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;

        init();

    }

    private void init(){
        Log.d(TAG, "init: Started");

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mMainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainLinePaint.setStrokeWidth(8f);
        mMainLinePaint.setColor(mLineColor);

        mSecondaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondaryLinePaint.setStrokeWidth(6f);
        mSecondaryLinePaint.setColor(mSecLineColor);

        mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setStyle(Paint.Style.FILL);
        mMarkerPaint.setColor(mMarkerColor);

        mMarkerPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPulsePaint.setStyle(Paint.Style.FILL);
        mMarkerPulsePaint.setColor(mTextColor);


    }

    //----------------------------------------------------------------------------------------------//

    public void setCameraParamsFOV(float verticalFOV, float horizontalFOV){
        Log.d(TAG, "setCameraParams: Started");

        this.mVerticalFOV = verticalFOV;
        this.mHorizontalFOV = horizontalFOV;

        Log.d(TAG, "setCameraParamsFOV: Camera Param:" + mHorizontalFOV + ", " + mVerticalFOV);

    }


    public void setMSensorEvent(SensorEvent event){
        Log.d(TAG, "setMSensorEvent: Sensor event");

        this.mSensorEvent = event;
        parseEventToMetadata();

    }

    public void setDebugSensorData(boolean showMetaData){
        Log.d(TAG, "debugSensorData: Started");

        this.isDebugMode = showMetaData;

    }

    public void setCurrentCameraSensorData(Location location, float azimuthFrom, float azimuthTo, float[] orientation){
        Log.d(TAG, "setCurrentCameraSensorData: Started");

        //Location
        this.mCurrentLocation = location;

        //Sensor
        this.mAzimuthObserved = azimuthTo;
        this.mAzimuthTheoretical = calculateTheoreticalAzimuth();
        this.mOrientation = orientation;
        isSensorDataAvailable = true;

        //Check and verify location before updating any of the UI
        if(mCurrentLocation !=null){
            isLocationDataAvailable = true;


            if(useGMF){
                setGmfForSensor();

                if (gmf !=null){
                    Log.d(TAG, "onSensorChanged: Declination: " + gmf.getDeclination());
                    mAzimuthObserved += gmf.getDeclination();
                    orientation[0] += Math.toRadians(gmf.getDeclination());
                }
            }
        }
        setPoiInRange();

        invalidate();

    }

    public void setTarget(ArvSLocationPOI poi){
        Log.d(TAG, "setTargetLocation: Started");

        this.mTargePoi = poi;
        
    }


    public void setAngleRange(float lowerRange, float upperRange){
        Log.d(TAG, "setAngleRange: Started");

        this.ANGLE_LOWER_RANGE = lowerRange;
        this.ANGLE_UPPER_RANGE = upperRange;

    }

    public void setUseGMF(boolean useCorrection){
        Log.d(TAG, "setUseGMF: Started");

        this.useGMF = useCorrection;


    }


    //----------------------------------------------------------------------------------------------//

    private void setPoiInRange(){
        Log.d(TAG, "setPoiInRange: Started");
        Log.i(TAG, "sensorWorx: orientation In: " + mOrientation[0]);
        float azimuth, pitch, bearing;
        Range<Float> azimuthRange, pitchRange;

        Location targetLocation = new Location(mTargePoi.getName());
        targetLocation.setLatitude(mTargePoi.getLatitude());
        targetLocation.setLongitude(mTargePoi.getLongitude());
        targetLocation.setAltitude(mTargePoi.getAltitude());

        bearing = (mCurrentLocation.bearingTo(targetLocation) + 360) % 360;

        azimuth = (float) Math.toDegrees(mOrientation[0]);
        pitch = (float) Math.toDegrees(mOrientation[1]);

        Log.i(TAG, "sensorWorx: setPoiInRange: Bearing: " + bearing);
        Log.i(TAG, "sensorWorx: setPoiInRange: Azimuth: " + mAzimuthObserved);
        Log.i(TAG, "sensorWorx: setPoiInRange: Pitch: " + pitch);
        Log.i(TAG, "sensorWorx: setPoiInRange: Called Azimuth: " + azimuth);

        azimuthRange = new Range<>(bearing - ANGLE_LOWER_RANGE, bearing + ANGLE_UPPER_RANGE);
        pitchRange = new Range<>(-90.0f, +90.0f);

        if(azimuthRange.contains((float) mAzimuthObserved) && pitchRange.contains(pitch)){
            Log.i(TAG, "sensorWorx: setPoiInRange: True");
            isPoiInRange = true;
        }else{
            Log.i(TAG, "sensorWorx: setPoiInRange: False");
            isPoiInRange = false;
        }


    }


    private String solveForTargetDistance(){
        Log.d(TAG, "setTargetDistance: Started");

        return String.valueOf(distanceToTarget(mTargePoi,mCurrentLocation));

    }

    //----------------------------------------------------------------------------------------------//

    /**
     * Location Methods
     */

    private double distanceToTarget(ArvSLocationPOI target, Location current){
        Log.d(TAG, "distanceToTarget: Started");

        Location targetLocation = new Location(target.getName());
        targetLocation.setLatitude(target.getLatitude());
        targetLocation.setLongitude(target.getLongitude());
        targetLocation.setAltitude(target.getAltitude());

        double distanceMetric = current.distanceTo(targetLocation);

        Log.d(TAG, "distanceToTarget: " + distanceMetric);
        return distanceMetric;

    }

    private float bearingToTarget(ArvSLocationPOI target, Location current){
        Log.d(TAG, "bearingToTarget: Started");

        Location targetLocation = new Location(target.getName());
        targetLocation.setLatitude(target.getLatitude());
        targetLocation.setLongitude(target.getLongitude());
        targetLocation.setAltitude(target.getAltitude());

        float bearing = current.bearingTo(targetLocation);

        Log.d(TAG, "bearingToTarget: " + bearing);
        return bearing;

    }


    //----------------------------------------------------------------------------------------------//
    /**
     * Sensor Methods
     */


    private void parseEventToMetadata(){
        Log.d(TAG, "parseEventToMetadata: Started");

        StringBuilder msg = new StringBuilder(mSensorEvent.sensor.getName()).append(" ");
        for(float value: mSensorEvent.values)
        {
            msg.append("[").append(value).append("]");
        }

        switch(mSensorEvent.sensor.getType())
        {
            case Sensor.TYPE_ROTATION_VECTOR:
                mRawVectorData = msg.toString();

            case Sensor.TYPE_ACCELEROMETER:
                mRawAccelData = msg.toString();
                break;
            case Sensor.TYPE_GYROSCOPE:
                mRawGyroData = msg.toString();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mRawCompassData = msg.toString();
                break;
        }

        updateSensorMetaData();

    }

    private void updateSensorMetaData(){
        Log.d(TAG, "updateSensorMetaData: Started");

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        mSensorMetaData = "Sensor Data as of " + date + "\n" + ":";

        if(!StringUtilityHelper.isStringNull(mRawVectorData)){
            mSensorMetaData = "VECTOR: " + mRawVectorData + ". " + "\n";
        }

        if(!StringUtilityHelper.isStringNull(mRawAccelData)){
            mSensorMetaData = "ACCEL: " + mRawAccelData + ". " + "\n";
        }

        if(!StringUtilityHelper.isStringNull(mRawGyroData)){
            mSensorMetaData = "GYRO: " + mRawGyroData + ". " + "\n";
        }

        if(!StringUtilityHelper.isStringNull(mRawCompassData)){
            mSensorMetaData = "COMPASS: " + mRawCompassData + ". " + "\n";
        }

        invalidate();

    }

    //----------------------------------------------------------------------------------------------//
    public double calculateTheoreticalAzimuth(){
        Log.d(TAG, "calculateTheoreticalAzimuth: Started");

        double currentLatitude = mCurrentLocation.getLatitude();
        double currentLongitude = mCurrentLocation.getLongitude();

        double dX = mTargePoi.getLatitude() - currentLatitude;
        double dY = mTargePoi.getLongitude() - currentLongitude;

        double phiAngle;
        double tanPhi;
        double azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quater
            return azimuth = phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            return azimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            return azimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            return azimuth = 360 - phiAngle;
        }

        return azimuth;

    }

    private List<Double> calculateAzimuthAccuracy(double azimuth) {
        Log.d(TAG, "calculateAzimuthAccuracy: Started");
        double minAngle = azimuth - mAzimuthRange;
        double maxAngle = azimuth + mAzimuthRange;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        Log.d(TAG, "isBetween: Started");
        if (minAngle > maxAngle) {
            if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
                return true;
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true;
        }
        return false;
    }


    //----------------------------------------------------------------------------------------------//
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Drawing");
        super.onDraw(canvas);

        canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, mMainLinePaint);

        canvas.save();

        if(showTargetMarker){
            canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 8.0f, mMarkerPaint);
        }
        
        drawTargetInRange(canvas);

        canvas.restore();
    }

    //----------------------------------------------------------------------------------------------//

    private void drawTargetInRange(Canvas c){
        Log.d(TAG, "drawTargetInRange: Started");

        if(isLocationDataAvailable){
            float currentBearingToTarget = bearingToTarget(mTargePoi,mCurrentLocation);
            float currentOnScreenAzimuth = (float) mAzimuthObserved;

            float dx = (float) ((c.getWidth()/mHorizontalFOV) * (currentOnScreenAzimuth-currentBearingToTarget))/(ANGLE_UPPER_RANGE / POI_MAPPING_SCALE);

            if(isPoiInRange){
                c.translate(0.0f-dx,0.0f);
                drawMarker(c,c.getWidth()/2,c.getHeight()/2,true);
            }

        } else{
            mSensorMetaData = "No GPS Location Available";
            c.drawText(mSensorMetaData,(c.getWidth()/4)*2,(c.getHeight()/10)*2,mTextPaint);
        }

    }

    private void drawMarker(Canvas c, float mx, float my, boolean animate){
        Log.d(TAG, "drawMarker: Started");

        c.drawCircle(mx,my,mRadiusPOI,mMarkerPaint);


    }

    //----------------------------------------------------------------------------------------------//
    private synchronized void setGmfForSensor(){
        Log.d(TAG, "setGmfForSensor: Started");

        GeomagneticField geoField = new GeomagneticField(
                (float) mCurrentLocation.getLatitude(),
                (float) mCurrentLocation.getLongitude(),
                (float) mCurrentLocation.getAltitude(),
                System.currentTimeMillis());

        this.gmf = geoField;

    }

    public double roundToDecimals(double d, int c) {
        int temp=(int)((d*Math.pow(10,c)));
        return (((double)temp)/Math.pow(10,c));
    }

}
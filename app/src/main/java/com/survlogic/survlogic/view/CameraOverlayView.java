package com.survlogic.survlogic.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.GeomagneticField;
import android.hardware.SensorEvent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.util.Range;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 3/24/2018.
 */

public class CameraOverlayView extends View {
    private static final String TAG = "Started";

    private Context mContext;

    private Paint mTextPaint, mMainLinePaint, mSecondaryLinePaint, mMarkerPaint, mMarkerPulsePaint, mMarkerPulseMaskPaint;
    private TextPaint mDebugTextPaint;
    private Path pathMarker;

    private int mTextColor, mBackgroundColor, mLineColor, mSecLineColor, mMarkerColor;
    private float mTextSize;

    private static int screenWidth, screenHeight;

    private SensorEvent mSensorEvent;
    private String mSensorMetaData;
    private double mAzimuthObserved = 0;

    private ArvSLocationPOI mTargetPoi;
    private float mTargetPoiFalseAltitudeMsl = 0.00F;

    private Location mCurrentLocation;
    private float mOrientation[] = new float[3];
    private float mCurrentAltitudeMsl = 0.00F;
    private float mCurrentFalseAltitudeMsl = 0.00F;

    private GeomagneticField gmf = null;

    private boolean isSensorDataAvailable = false, isLocationDataAvailable = false;

    private float mVerticalFOV, mHorizontalFOV;

    private boolean isDebugMode = false;
    private boolean useGMF = false;
    private boolean useDTMModelTarget = false, useDTMModelCurrent = false;

    private boolean isPoiInRange = false;

    private float ANGLE_UPPER_RANGE = 10, ANGLE_LOWER_RANGE = 10;
    private float PITCH_UPPER_RANGE = 25, PITCH_LOWER_RANGE = -25;
    private static final float POI_MAPPING_SCALE = 0.5F;

    private float outerCircleRadiusProgress = 0f;
    private float innerCircleRadiusProgress = 0f;

    private float mRadiusPOI = 20.0F;
    private int mRadiusPOIPulse = 80;

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private static final int START_COLOR = 0xFFFF5722;
    private static final int END_COLOR = 0xFFFFC107;
    private boolean isAnimatingPulse = false;

    public CameraOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

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

        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),"fonts/Roboto-ThinItalic.ttf");

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

        mMarkerPulseMaskPaint = new Paint();
        mMarkerPulseMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mDebugTextPaint = new TextPaint();
        mDebugTextPaint.setAntiAlias(true);
        mDebugTextPaint.setTextSize(mTextSize/2);
        mDebugTextPaint.setColor(mTextColor);
        mDebugTextPaint.setTypeface(typeface);

        setLayerType(LAYER_TYPE_HARDWARE,null);

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

    }

    public void setDebugSensorData(boolean showMetaData){
        Log.d(TAG, "debugSensorData: Started");

        this.isDebugMode = showMetaData;

    }

    public void setCurrentCameraSensorData(Location location, float azimuthTo, float[] orientation, float altitudeMsl){
        Log.d(TAG, "setCurrentCameraSensorData: Started");

        //Location
        this.mCurrentLocation = location;
        this.mCurrentAltitudeMsl = altitudeMsl;
        
        //Sensor
        this.mAzimuthObserved = azimuthTo;
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

    public void useDTMModelCurrent(boolean useDTM){
        Log.d(TAG, "useDTMModelCurrent: Started");

        this.useDTMModelCurrent = useDTM;

    }

    public void setDTMModelCurrent(float currentFalseElevation){
        this.mCurrentFalseAltitudeMsl = currentFalseElevation;
    }


    public void setTarget(ArvSLocationPOI poi){
        Log.d(TAG, "setTargetLocation: Started");

        this.mTargetPoi = poi;
        
    }

    public void useDTMModelTarget(boolean useDTM){
        Log.d(TAG, "setUseDTMModelTarget: ");
        this.useDTMModelTarget = useDTM;

    }

    public void setTargetPoiFalseElevation(float mTargetPoiFalseElevation) {
        this.mTargetPoiFalseAltitudeMsl = mTargetPoiFalseElevation;
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
        float azimuth, pitch, bearing, zenith;
        Range<Float> azimuthRange, pitchRange;

        Location targetLocation = convertPOIToLocation();

        bearing = (mCurrentLocation.bearingTo(targetLocation) + 360) % 360;
        zenith = pitchToTarget(targetLocation,mCurrentLocation);

        Log.d(TAG, "setPoiInRange: zenith: " + zenith);

        azimuth = (float) Math.toDegrees(mOrientation[0]);
        pitch = (float) Math.toDegrees(mOrientation[1]);


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



    //----------------------------------------------------------------------------------------------//

    /**
     * Location Methods
     */

    private Location convertPOIToLocation(){
        Log.d(TAG, "convertPOIToLocation: Started");

        Location targetLocation = new Location(mTargetPoi.getName());
        targetLocation.setLatitude(mTargetPoi.getLatitude());
        targetLocation.setLongitude(mTargetPoi.getLongitude());
        targetLocation.setAltitude(mTargetPoi.getAltitude());

        return targetLocation;

    }

    private float distanceToTarget(Location target, Location current){
        Log.d(TAG, "distanceToTarget: Started");

        float distanceMetric = current.distanceTo(target);

        Log.d(TAG, "distanceToTarget: " + distanceMetric);
        return distanceMetric;

    }

    private float bearingToTarget(ArvSLocationPOI target, Location current){
        Log.d(TAG, "bearingToTarget: Started");

        Location targetLocation = convertPOIToLocation();

        float bearing = current.bearingTo(targetLocation);

        Log.d(TAG, "bearingToTarget: " + bearing);
        return bearing;

    }

    private float pitchToTarget(Location target, Location current){
        Log.d(TAG, "pitchToTarget: Started");

        double distance = current.distanceTo(target);

        double elevationTarget = 0.00d, elevationCurrent = 0.00d;

        elevationCurrent = getCurrentLocationElevation(current);
        elevationTarget = getTargetLocationElevation(target);

        double dElevation = elevationTarget - elevationCurrent;

        double vAngle = (dElevation/distance);

        vAngle = 0.00F - (Math.asin(vAngle));

        return (float) Math.toDegrees(vAngle);

    }

    private double getCurrentLocationElevation(Location current){
        Log.d(TAG, "getTargetElevation: Started");

        if(useDTMModelCurrent){
            return mCurrentFalseAltitudeMsl;
        }else{
            return current.getAltitude();
        }

    }

    private double getTargetLocationElevation(Location target){
        Log.d(TAG, "getTargetLocationElevation: Started");

        if(useDTMModelTarget){
            return mTargetPoiFalseAltitudeMsl;
        }else{
            return target.getAltitude();
        }


    }


    //----------------------------------------------------------------------------------------------//
    /**
     * Sensor Methods
     */






    //----------------------------------------------------------------------------------------------//
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Drawing");
        super.onDraw(canvas);

        canvas.drawLine(0f - canvas.getWidth(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, mMainLinePaint);

        if(isDebugMode){
            drawSmartWorxData(canvas);
        }

        drawTargetInRange(canvas);

    }

    //----------------------------------------------------------------------------------------------//

    private void drawSmartWorxData(Canvas c){
        Log.d(TAG, "drawSmartWorxData: Started");
       int padding = c.getWidth()/100;
       int paddingStart = padding * 2;
       int paddingEnd = padding * 2;

        DecimalFormat df = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(4);

       if(mCurrentLocation !=null){
           Location targetLocation = convertPOIToLocation();
           float distance = distanceToTarget(targetLocation,mCurrentLocation);
           float bearing = (mCurrentLocation.bearingTo(targetLocation) + 360) % 360;
           float zenith = pitchToTarget(targetLocation,mCurrentLocation);
           float azimuth = (float) Math.toDegrees(mOrientation[0]);
           float pitch = (float) Math.toDegrees(mOrientation[1]);

           float currentBearingToTarget = bearingToTarget(mTargetPoi,mCurrentLocation);
           float currentOnScreenAzimuth = (float) mAzimuthObserved;

           double elevationCurrent = getCurrentLocationElevation(mCurrentLocation);
           double elevationTarget = getTargetLocationElevation(targetLocation);

           float dx = (float) ((c.getWidth()/mHorizontalFOV) * (currentOnScreenAzimuth-currentBearingToTarget))/(ANGLE_UPPER_RANGE / POI_MAPPING_SCALE);
           float dy = (float) ((c.getHeight()/mVerticalFOV) * (pitch - zenith))/(PITCH_UPPER_RANGE);


           String debug_location = getResources().getString(R.string.cameraoverlayview_debug_location_value,
                   df.format(mCurrentLocation.getLatitude()), df.format(mCurrentLocation.getLongitude()),df.format(mCurrentLocation.getAltitude()),
                   df.format(targetLocation.getLatitude()),df.format(targetLocation.getLongitude()),df.format(targetLocation.getAltitude()),df.format(distance));

           String debug_sensor_orientation = getResources().getString(R.string.cameraoverlayview_debug_sensor_orientation_values,
                   df.format(bearing),df.format(mAzimuthObserved),df.format(azimuth));

           String debug_sensor_pitch = getResources().getString(R.string.cameraoverlayview_debug_sensor_pitch_values,
                   df.format(zenith),df.format(pitch));

           String debug_dtm = getResources().getString(R.string.cameraoverlayview_debug_dtm_values,
                   df.format(mCurrentAltitudeMsl),df.format(mTargetPoiFalseAltitudeMsl));

           String debug_metadata_gps = getResources().getString(R.string.cameraoverlayview_debug_metadata_gps,
                   df.format(mCurrentLocation.getAccuracy()),String.valueOf(useGMF),String.valueOf(useDTMModelTarget),df.format(elevationTarget),String.valueOf(useDTMModelCurrent),df.format(elevationCurrent));

           String debug_metadata_canvas = getResources().getString(R.string.cameraoverlayview_debug_metadata_canvas,
                   df.format(dx),df.format(dy), df.format(c.getWidth()), df.format(c.getHeight()));


           String combined = debug_location + debug_sensor_orientation + debug_sensor_pitch + debug_dtm + debug_metadata_gps + debug_metadata_canvas;

           int debug_width = (int) c.getWidth()/2 - paddingStart - paddingEnd;
           DynamicLayout mDl_location = new DynamicLayout(combined, mDebugTextPaint, debug_width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);

           int dX = c.getWidth()/2;
           int dY = (c.getHeight()/100)*10;

           c.save();

           c.translate(dX, dY);
           mDl_location.draw(c);

           c.restore();
       }

    }


    private void drawTargetInRange(Canvas c){
        Log.d(TAG, "drawTargetInRange: Started");

        c.save();

        if(isLocationDataAvailable){
            float currentBearingToTarget = bearingToTarget(mTargetPoi,mCurrentLocation);
            float currentOnScreenAzimuth = (float) mAzimuthObserved;

            Location targetLocation = convertPOIToLocation();
            float distance = distanceToTarget(targetLocation,mCurrentLocation);
            float zenith = pitchToTarget(targetLocation,mCurrentLocation);
            float pitch = (float) Math.toDegrees(mOrientation[1]);

            float dx = (float) ((c.getWidth()/mHorizontalFOV) * (currentOnScreenAzimuth-currentBearingToTarget))/(ANGLE_UPPER_RANGE / POI_MAPPING_SCALE);
            float dy = (float) ((c.getHeight()/mVerticalFOV) * (pitch - zenith))/(PITCH_UPPER_RANGE);

            Log.i(TAG, "drawTargetInRange: dx:" + dx);
            Log.i(TAG, "drawTargetInRange: dy:" + (c.getHeight()/mVerticalFOV));

            if(isPoiInRange){
                c.translate(0.0f-dx,0.0f-dy);
                drawMarker(c,c.getWidth()/2,c.getHeight()/2,true);
            }

        } else{
            mSensorMetaData = "No GPS Location Available";
            c.drawText(mSensorMetaData,(c.getWidth()/4)*2,(c.getHeight()/10)*2,mTextPaint);
            isAnimatingPulse = false;
        }

        c.restore();

    }

    private void drawMarker(Canvas c, float mx, float my, boolean animate){
        Log.d(TAG, "drawMarker: Started");

        if(animate){
            drawPulse(c,mx,my);

            if(!isAnimatingPulse){
                isAnimatingPulse = true;
                animatePulse();
            }
        }

        c.drawCircle(mx,my,mRadiusPOI,mMarkerPaint);

    }

    private void drawPulse(Canvas c, float mx, float my){
        c.drawCircle(mx,my,outerCircleRadiusProgress * mRadiusPOIPulse, mMarkerPulsePaint);
        c.drawCircle(mx,my,innerCircleRadiusProgress * mRadiusPOIPulse, mMarkerPulseMaskPaint);

    }


    private void animatePulse(){
        Log.d(TAG, "animatePulse: Started");

        setInnerCircleRadiusProgress(0);
        setOuterCircleRadiusProgress(0);

        final AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(this,CameraOverlayView.OUTER_CIRCLE_RADIUS_PROGRESS,0.1f,1f);
        outerCircleAnimator.setDuration(550);
        outerCircleAnimator.setInterpolator(new DecelerateInterpolator());


        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(this,CameraOverlayView.INNER_CIRCLE_RADIUS_PROGRESS,0.1f,1f);
        innerCircleAnimator.setDuration(500);
        innerCircleAnimator.setStartDelay(500);
        innerCircleAnimator.setInterpolator(new DecelerateInterpolator());

        animatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                setInnerCircleRadiusProgress(0);
                setOuterCircleRadiusProgress(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animatorSet.start();
            }
        });

        animatorSet.start();

    }

    private void progressOuterCircle(float value){
        setOuterCircleRadiusProgress(value);
    }

    private void progressInnerCircle(float value){
        setInnerCircleRadiusProgress(value);

    }

    private void setInnerCircleRadiusProgress(float innerCircleRadiusProgress){
        this.innerCircleRadiusProgress = innerCircleRadiusProgress;
        postInvalidate();
    }

    private float getInnerCircleRadiusProgress(){
        return innerCircleRadiusProgress;
    }

    private void setOuterCircleRadiusProgress(float outerCircleRadiusProgress){
        this.outerCircleRadiusProgress = outerCircleRadiusProgress;
        updateCircleColor();
        postInvalidate();
    }

    public float getOuterCircleRadiusProgress() {
        return outerCircleRadiusProgress;
    }

    private void updateCircleColor(){
        float colorProgress = (float) clamp(outerCircleRadiusProgress,0.5,1);
        colorProgress = (float) mapValueFromRangeToRange(colorProgress,0.5f,1f,0f,1f);
        this.mMarkerPulsePaint.setColor((Integer) argbEvaluator.evaluate(colorProgress,START_COLOR,END_COLOR));
    }


    public static final Property<CameraOverlayView, Float> INNER_CIRCLE_RADIUS_PROGRESS =
            new Property<CameraOverlayView, Float>(Float.class, "innerCircleRadiusProgress") {
                @Override
                public Float get(CameraOverlayView object) {
                    return object.getInnerCircleRadiusProgress();
                }

                @Override
                public void set(CameraOverlayView object, Float value) {
                    object.setInnerCircleRadiusProgress(value);
                }
            };

    public static final Property<CameraOverlayView, Float> OUTER_CIRCLE_RADIUS_PROGRESS =
            new Property<CameraOverlayView, Float>(Float.class, "outerCircleRadiusProgress") {
                @Override
                public Float get(CameraOverlayView object) {
                    return object.getOuterCircleRadiusProgress();
                }

                @Override
                public void set(CameraOverlayView object, Float value) {
                    object.setOuterCircleRadiusProgress(value);
                }
            };

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


    //----------------------------------------------------------------------------------------------//

    /**
     * @return text height
     */
    private float getTextHeight(String text, Paint paint) {

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private static double mapValueFromRangeToRange(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
        return toLow + ((value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow));
    }

    private static double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }


}


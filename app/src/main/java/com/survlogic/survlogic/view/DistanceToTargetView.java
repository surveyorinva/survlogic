package com.survlogic.survlogic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.location.Location;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.GPSLocationConverter;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;


public class DistanceToTargetView extends View {

    private static final String TAG = "DistanceToTargetView";
    private Context mContext;

    private Paint mBackgroundPaint, mTextPaint, mMainLinePaint, mSecondaryLinePaint, mMarkerPaint, mMarkerPulsePaint, mMarkerPulseMaskPaint;

    private int mTextColor, mBackgroundColor, mLineColor, mSecLineColor, mMarkerColor;
    private float mTextSize;

    private static int screenWidth, screenHeight;

    private String mDistance = "0";
    private Location mLocationTarget, mLocationCurrent;
    private int mUnitsToDisplay = UNITS_METRIC;
    private boolean mHaveLocation = false, mIsTracking = false;

    public static final int UNITS_METRIC = 3, UNITS_INTFEET = 2, UNITS_USFEET = 1;

    public DistanceToTargetView(Context context, @Nullable AttributeSet attrs) {
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

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(R.color.semi_transparent));
        mBackgroundPaint.setStrokeWidth(1);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        RadialGradient radialGradient = new RadialGradient(screenWidth / 2, screenHeight /2,
                getHeight() / 3, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);

        mBackgroundPaint.setShader(radialGradient);

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


    }

    //----------------------------------------------------------------------------------------------//

    public void setLocationTarget(Location mLocationTarget) {
        this.mLocationTarget = mLocationTarget;
    }

    public void setLocationCurrent(Location mLocationCurrent) {
        this.mLocationCurrent = mLocationCurrent;
    }

    public void setUnitsToDisplay(int mUnitsToDisplay) {
        this.mUnitsToDisplay = mUnitsToDisplay;
    }

    public boolean isHaveLocation() {
        return mHaveLocation;

    }

    public void setHaveLocation(boolean mHaveLocation) {
        this.mHaveLocation = mHaveLocation;
        showDistance();
    }

    public boolean isTracking() {
        return mIsTracking;
    }

    public void setIsTracking(boolean mIsTracking) {
        this.mIsTracking = mIsTracking;
    }

    //----------------------------------------------------------------------------------------------//

    private void showDistance(){
        Log.d(TAG, "showDistance: Started");

        DecimalFormat df = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(0);

        mDistance = df.format(calculateDistance());

        invalidate();
    }

    private double calculateDistance(){
        Log.d(TAG, "calculateDistance: Started");
        double distance=  mLocationCurrent.distanceTo(mLocationTarget);

        switch (mUnitsToDisplay){
            case UNITS_USFEET:
                distance = GPSLocationConverter.convertMetersToValue(distance,UNITS_USFEET);
                break;

            case UNITS_INTFEET:
                distance = GPSLocationConverter.convertMetersToValue(distance,UNITS_INTFEET);
                break;

            case UNITS_METRIC:
                distance = GPSLocationConverter.convertMetersToValue(distance,UNITS_METRIC);
                break;
        }

        return distance;
    }

    //----------------------------------------------------------------------------------------------//


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawDistance(canvas);

    }

    private void drawDistance(Canvas c){
        Log.d(TAG, "drawDistance: Started");
        int center = getWidth() / 2;
        int radius = center - 8;

        if(mLocationCurrent !=null && mIsTracking){

            c.drawCircle(center,center,radius,mBackgroundPaint);

            c.save();

            c.drawText(mDistance,center,center,mTextPaint);

            c.restore();

        }


    }
}

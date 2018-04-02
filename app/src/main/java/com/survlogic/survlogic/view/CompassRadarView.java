package com.survlogic.survlogic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 3/21/2018.
 */

public class CompassRadarView extends View {
    private static final String TAG = "CompassRadarView";

    private Context mContext;

    private static final long RETAIN_GPS_MILLIS = 10000L;

    private Paint mGridPaint;

    private Paint mBackgroundPaint;

    private Paint mErasePaint;

    private float mOrientation;

    private double mTargetLat;

    private double mTargetLon;

    private double mMyLocationLat;

    private double mMyLocationLon;

    private static int screenWidth, screenHeight;


    private int mLastScale = -1;
    private String[] mDistanceScale = new String[4];

    private static float KM_PER_METERS = 0.001f;
    private static float METERS_PER_KM = 1000f;

    /**
     * These are the list of choices for the radius of the outer circle on the screen when using metric units. All items
     * are in kilometers. This array is used to choose the scale of the radar display.
     */
    private static double mMetricScaleChoices[] = { 100 * KM_PER_METERS, 200 * KM_PER_METERS, 400 * KM_PER_METERS, 1,
            2, 4, 8, 20, 40, 100, 200, 400, 1000, 2000, 4000, 10000, 20000, 40000, 80000 };

    /**
     * Once the scale is chosen, this array is used to convert the number of kilometers on the screen to an integer.
     * (Note that for short distances we use meters, so we multiply the distance by {@link #METERS_PER_KM}. (This array
     * is for metric measurements.)
     */
    private static float mMetricDisplayUnitsPerKm[] = { METERS_PER_KM, METERS_PER_KM, METERS_PER_KM, METERS_PER_KM,
            METERS_PER_KM, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };

    /**
     * This array holds the formatting string used to display the distance to the target. (This array is for metric
     * measurements.)
     */
    private static String mMetricDisplayFormats[] = { "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.1fkm", "%.1fkm",
            "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm",
            "%.0fkm", "%.0fkm" };

    /**
     * This array holds the formatting string used to display the distance on each ring of the radar screen. (This array
     * is for metric measurements.)
     */
    private static String mMetricScaleFormats[] = { "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.0fkm", "%.0fkm",
            "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm",
            "%.0fkm", "%.0fkm", "%.0fkm" };

    private static float KM_PER_YARDS = 0.0009144f;
    private static float KM_PER_MILES = 1.609344f;
    private static float YARDS_PER_KM = 1093.6133f;
    private static float MILES_PER_KM = 0.621371192f;

    /**
     * These are the list of choices for the radius of the outer circle on the screen when using standard units. All
     * items are in kilometers. This array is used to choose the scale of the radar display.
     */
    private static double mEnglishScaleChoices[] = { 100 * KM_PER_YARDS, 200 * KM_PER_YARDS, 400 * KM_PER_YARDS,
            1000 * KM_PER_YARDS, 1 * KM_PER_MILES, 2 * KM_PER_MILES, 4 * KM_PER_MILES, 8 * KM_PER_MILES,
            20 * KM_PER_MILES, 40 * KM_PER_MILES, 100 * KM_PER_MILES, 200 * KM_PER_MILES, 400 * KM_PER_MILES,
            1000 * KM_PER_MILES, 2000 * KM_PER_MILES, 4000 * KM_PER_MILES, 10000 * KM_PER_MILES, 20000 * KM_PER_MILES,
            40000 * KM_PER_MILES, 80000 * KM_PER_MILES };

    /**
     * Once the scale is chosen, this array is used to convert the number of kilometers on the screen to an integer.
     * (Note that for short distances we use meters, so we multiply the distance by {@link #YARDS_PER_KM}. (This array
     * is for standard measurements.)
     */
    private static float mEnglishDisplayUnitsPerKm[] = { YARDS_PER_KM, YARDS_PER_KM, YARDS_PER_KM, YARDS_PER_KM,
            MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM,
            MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM,
            MILES_PER_KM, MILES_PER_KM };

    /**
     * This array holds the formatting string used to display the distance to the target. (This array is for standard
     * measurements.)
     */
    private static String mEnglishDisplayFormats[] = { "%.0fyd", "%.0fyd", "%.0fyd", "%.0fyd", "%.1fmi", "%.1fmi",
            "%.1fmi", "%.1fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi",
            "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi" };

    /**
     * This array holds the formatting string used to display the distance on each ring of the radar screen. (This array
     * is for standard measurements.)
     */
    private static String mEnglishScaleFormats[] = { "%.0fyd", "%.0fyd", "%.0fyd", "%.0fyd", "%.2fmi", "%.1fmi",
            "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi",
            "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi" };

    /**
     * True when we have know our own location
     */
    private boolean mHaveLocation = false;

    /**
     * The view that will display the distance text
     */
    private TextView mDistanceView;

    /**
     * Distance to target, in KM
     */
    private double mDistance;

    /**
     * Bearing to target, in degrees
     */
    private double mBearing;

    /**
     * Ratio of the distance to the target to the radius of the outermost ring on the radar screen
     */
    private float mDistanceRatio;

    /**
     * Utility rect for calculating the ring labels
     */
    private Rect mTextBounds = new Rect();

    /**
     * The bitmap used to draw the target
     */
    private Bitmap mBlip;

    /**
     * Used to draw the animated ring that sweeps out from the center
     */
    private Paint mSweepPaint0;

    /**
     * Used to draw the animated ring that sweeps out from the center
     */
    private Paint mSweepPaint1;

    /**
     * Used to draw the animated ring that sweeps out from the center
     */
    private Paint mSweepPaint2;

    /**
     * Time in millis when the most recent sweep began
     */
    private long mSweepTime;

    /**
     * True if the sweep has not yet intersected the blip
     */
    private boolean mSweepBefore;

    /**
     * Time in millis when the sweep last crossed the blip
     */
    private long mBlipTime;

    /**
     * True if the display should use metric units; false if the display should use standard units
     */
    private boolean mUseMetric;

    /**
     * True if the display should show the distances of each radar circle
     */
    private boolean isShowRadarCircleText;


    private RadarLines lrl, rrl;
    private float rx = 10, ry = 20;

    private int mTextColor, mBackgroundColor, mLineColor;

    private float mTextSize;


    public CompassRadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CompassView, 0, 0);

        mBackgroundColor = a.getColor(R.styleable.CompassView_backgroundColor, Color.BLACK);
        mLineColor = a.getColor(R.styleable.CompassView_lineColor, Color.WHITE);
        mTextColor = a.getColor(R.styleable.CompassView_textColor, Color.WHITE);
        mTextSize = a.getDimension(R.styleable.CompassView_textSize, 15 * getResources().getDisplayMetrics().scaledDensity);
        isShowRadarCircleText = a.getBoolean(R.styleable.CompassView_showRadarCircleText,false);
        a.recycle();


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;

        init();
        initRadarLines();

    }

    private void init(){
        Log.d(TAG, "init: Started");

        // Paint used for the rings and ring text
        mGridPaint = new Paint();
        mGridPaint.setColor(mLineColor);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Style.STROKE);
        mGridPaint.setStrokeWidth(1.0f);
        mGridPaint.setTextSize(10.0f);
        mGridPaint.setTextAlign(Align.CENTER);

        // Paint used to erase the rectangle behind the ring text
        mErasePaint = new Paint();
        mErasePaint.setColor(0xFF191919);
        mErasePaint.setStyle(Style.FILL);

        // Paint applied to the back circle to emphasize over textureview
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(R.color.semi_transparent));
        mBackgroundPaint.setStrokeWidth(1);
        mBackgroundPaint.setStyle(Style.FILL_AND_STROKE);
        RadialGradient radialGradient = new RadialGradient(screenWidth / 2, screenHeight /2,
                getHeight() / 3, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);

        mBackgroundPaint.setShader(radialGradient);

        // Outer ring of the sweep
        mSweepPaint0 = new Paint();
        mSweepPaint0.setColor(Color.TRANSPARENT);
        mSweepPaint0.setAntiAlias(true);
        mSweepPaint0.setStyle(Style.STROKE);
        mSweepPaint0.setStrokeWidth(2f);

        // Middle ring of the sweep
        mSweepPaint1 = new Paint();
        mSweepPaint1.setColor(Color.TRANSPARENT);
        mSweepPaint1.setAntiAlias(true);
        mSweepPaint1.setStyle(Style.STROKE);
        mSweepPaint1.setStrokeWidth(2f);

        // Inner ring of the sweep
        mSweepPaint2 = new Paint();
        mSweepPaint2.setColor(Color.TRANSPARENT);
        mSweepPaint2.setAntiAlias(true);
        mSweepPaint2.setStyle(Style.STROKE);
        mSweepPaint2.setStrokeWidth(2f);

        mBlip = ((BitmapDrawable) getResources().getDrawable(R.drawable.radar_blip)).getBitmap();
    }


    private void initRadarLines(){
        int center = getWidth() / 2;
        int radius = center - 8;

        lrl = new RadarLines();
        rrl = new RadarLines();

        lrl.set(0,-radius);
        lrl.rotate(Math.toRadians(45));
        lrl.add(radius, radius);

        rrl.set(0,-radius);
        rrl.rotate(-Math.toRadians(45));
        rrl.add(radius, radius);

    }

    //----------------------------------------------------------------------------------------------//
    public void setTarget(ArvSLocationPOI poi) {
        mTargetLat = poi.getLatitude();
        mTargetLon = poi.getLongitude();
    }

    public void setOrientation(float mOrientation){
        Log.d(TAG, "setOrientation: Started");

        this.mOrientation = mOrientation;
        postInvalidate();
    }

    public void setCurrentLocation(Location location){
        Log.d(TAG, "setmCurrentLocation: Started");

        if (!mHaveLocation) {
            mHaveLocation = true;
        }

        if(location != null){
            mMyLocationLat = location.getLatitude();
            mMyLocationLon = location.getLongitude();

            float[] results = new float[2];
            Location.distanceBetween(mMyLocationLat, mMyLocationLon, mTargetLat, mTargetLon, results);
            mDistance = results[0] / 1000;

            mBearing = results[1];

            updateDistance(mDistance);
        }

    }

    public void setUseMetric(boolean useMetric) {
        mUseMetric = useMetric;
        mLastScale = -1;
        if (mHaveLocation) {
            updateDistance(mDistance);
        }
        invalidate();
    }

    public void setShowRadarCircleText(boolean showRadarCircleText){
        isShowRadarCircleText = showRadarCircleText;
        invalidate();
    }

    //----------------------------------------------------------------------------------------------//
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int center = getWidth() / 2;
        int radius = center - 8;

        //Draw Background
        final Paint backgroundPaint = mBackgroundPaint;

        canvas.drawCircle(center,center,radius,backgroundPaint);

        // Draw the rings
        final Paint gridPaint = mGridPaint;
        canvas.drawCircle(center, center, radius, gridPaint);
        canvas.drawCircle(center, center, radius * 3 / 4, gridPaint);
        canvas.drawCircle(center, center, radius >> 1, gridPaint);
        canvas.drawCircle(center, center, radius >> 2, gridPaint);

        int blipRadius = (int) (mDistanceRatio * radius);

        final long now = SystemClock.uptimeMillis();
        if (mSweepTime > 0 && mHaveLocation) {
            // Draw the sweep. Radius is determined by how long ago it started
            long sweepDifference = now - mSweepTime;
            if (sweepDifference < 512L) {
                int sweepRadius = (int) (((radius + 6) * sweepDifference) >> 9);
                canvas.drawCircle(center, center, sweepRadius, mSweepPaint0);
                canvas.drawCircle(center, center, sweepRadius - 2, mSweepPaint1);
                canvas.drawCircle(center, center, sweepRadius - 4, mSweepPaint2);

                // Note when the sweep has passed the blip
                boolean before = sweepRadius < blipRadius;
                if (!before && mSweepBefore) {
                    mSweepBefore = false;
                    mBlipTime = now;
                }
            } else {
                mSweepTime = now + 1000;
                mSweepBefore = true;
            }
            postInvalidate();
        }

        // Draw horizontal and vertical lines

        //canvas.drawLine(center, center - (radius >> 2) + 6, center, center - radius - 6, gridPaint); - North Line Removed for radar fov
        canvas.drawLine(center, center + (radius >> 2) - 6, center, center + radius + 6, gridPaint);
        canvas.drawLine(center - (radius >> 2) + 6, center, center - radius - 6, center, gridPaint);
        canvas.drawLine(center + (radius >> 2) - 6, center, center + radius + 6, center, gridPaint);

        // Draw X in the center of the screen
        canvas.drawLine(center - 4, center - 4, center + 4, center + 4, gridPaint);
        canvas.drawLine(center - 4, center + 4, center + 4, center - 4, gridPaint);

        //Draw radar field of view
        canvas.drawLine(lrl.x,lrl.y,center,center,gridPaint);
        canvas.drawLine(rrl.x,rrl.y,center,center,gridPaint);



        if (mHaveLocation) {
            double bearingToTarget = mBearing - mOrientation;
            double drawingAngle = Math.toRadians(bearingToTarget) - (Math.PI / 2);

            float cos = (float) Math.cos(drawingAngle);
            float sin = (float) Math.sin(drawingAngle);

            // Draw the text for the rings
            final String[] distanceScale = mDistanceScale;

            if(isShowRadarCircleText){
                addText(canvas, distanceScale[0], center, center + (radius >> 2));
                addText(canvas, distanceScale[1], center, center + (radius >> 1));
                addText(canvas, distanceScale[2], center, center + radius * 3 / 4);
                addText(canvas, distanceScale[3], center, center + radius);
            }

            // Draw the blip. Alpha is based on how long ago the sweep crossed the blip
            long blipDifference = now - mBlipTime;
            gridPaint.setAlpha(255 - (int) ((128 * blipDifference) >> 10));
            canvas.drawBitmap(mBlip, center + (cos * blipRadius) - 8, center + (sin * blipRadius) - 8, gridPaint);
            gridPaint.setAlpha(255);
        }
    }

    private void addText(Canvas canvas, String str, int x, int y) {

        mGridPaint.getTextBounds(str, 0, str.length(), mTextBounds);
        mTextBounds.offset(x - (mTextBounds.width() >> 1), y);
        mTextBounds.inset(-2, -2);
        canvas.drawRect(mTextBounds, mErasePaint);
        canvas.drawText(str, x, y, mGridPaint);
    }


    //----------------------------------------------------------------------------------------------//


    /**
     * Update our state to reflect a new distance to the target. This may require choosing a new scale for the radar
     * rings.
     *
     * @param distanceKm The new distance to the target
     */
    private void updateDistance(double distanceKm) {
        final double[] scaleChoices;
        final float[] displayUnitsPerKm;
        final String[] displayFormats;
        final String[] scaleFormats;
        String distanceStr = null;
        if (mUseMetric) {
            scaleChoices = mMetricScaleChoices;
            displayUnitsPerKm = mMetricDisplayUnitsPerKm;
            displayFormats = mMetricDisplayFormats;
            scaleFormats = mMetricScaleFormats;
        } else {
            scaleChoices = mEnglishScaleChoices;
            displayUnitsPerKm = mEnglishDisplayUnitsPerKm;
            displayFormats = mEnglishDisplayFormats;
            scaleFormats = mEnglishScaleFormats;
        }

        int count = scaleChoices.length;
        for (int i = 0; i < count; i++) {
            if (distanceKm < scaleChoices[i] || i == (count - 1)) {
                String format = displayFormats[i];
                double distanceDisplay = distanceKm * displayUnitsPerKm[i];
                if (mLastScale != i) {
                    mLastScale = i;
                    String scaleFormat = scaleFormats[i];
                    float scaleDistance = (float) (scaleChoices[i] * displayUnitsPerKm[i]);
                    mDistanceScale[0] = String.format(scaleFormat, (scaleDistance / 4));
                    mDistanceScale[1] = String.format(scaleFormat, (scaleDistance / 2));
                    mDistanceScale[2] = String.format(scaleFormat, (scaleDistance * 3 / 4));
                    mDistanceScale[3] = String.format(scaleFormat, scaleDistance);
                }
                mDistanceRatio = (float) (mDistance / scaleChoices[mLastScale]);
                distanceStr = String.format(format, distanceDisplay);
                break;
            }
        }

    }

    /**
     * Turn on the sweep animation starting with the next draw
     */
    public void startSweep() {
        mSweepTime = SystemClock.uptimeMillis();
        mSweepBefore = true;
    }

    /**
     * Turn off the sweep animation
     */
    public void stopSweep() {
        mSweepTime = 0L;
    }

    public class RadarLines {
        public float x, y;

        public RadarLines() {
            set(0, 0);
        }

        public RadarLines(float x, float y) {
            set(x, y);
        }

        public void set(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void rotate(double t) {
            float xp = (float) Math.cos(t) * x - (float) Math.sin(t) * y;
            float yp = (float) Math.sin(t) * x + (float) Math.cos(t) * y;

            x = xp;
            y = yp;
        }

        public void add(float x, float y) {
            this.x += x;
            this.y += y;
        }
    }


}

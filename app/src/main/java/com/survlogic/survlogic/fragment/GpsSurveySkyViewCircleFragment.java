package com.survlogic.survlogic.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.GpsSurveyActivity;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.utils.GnssType;
import com.survlogic.survlogic.utils.GpsHelper;

import java.util.Iterator;

import static android.R.attr.digits;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class GpsSurveySkyViewCircleFragment extends Fragment implements GpsSurveyListener {

    //    Dimensions to make compass fit screen
    public static int mHeight, mWidth;

    //    Inner class that generates the skyview
    private GpsSkyView mSkyView;

    private static final String TAG = "GpsSurveySkyViewCircleFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mSkyView = new GpsSkyView(getActivity());
        GpsSurveyActivity.getInstance().addListener(this);

        // Get the proper height and width of this view, to ensure the compass draws onscreen
        mSkyView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @SuppressLint("NewApi")
                    @Override
                    public void onGlobalLayout() {
                        final View v = getView();
                        mHeight = v.getHeight();
                        mWidth = v.getWidth();

                        if (v.getViewTreeObserver().isAlive()) {
                            // remove this layout listener
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    }
                }
        );

        return mSkyView;

    }

    @Override
    public void gpsStart() {

    }

    @Override
    public void gpsStop() {

    }

    @Override
    public void onGpsStatusChanged(int event, GpsStatus status) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                mSkyView.setStarted();
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                mSkyView.setStopped();
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                mSkyView.setSats(status);
                break;
        }
    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
        mSkyView.setGnssStatus(status);
    }

    @Override
    public void onGnssStarted() {
        mSkyView.setStarted();
    }

    @Override
    public void onGnssStopped() {
        mSkyView.setStopped();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        mSkyView.setGnssMeasurementEvent(event);
    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {
        // For performance reasons, only proceed if this fragment is visible
        if (!getUserVisibleHint()) {
            return;
        }

        if (mSkyView != null) {
            mSkyView.onOrientationChanged(orientation, tilt);
        }
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private static class GpsSkyView extends View implements GpsSurveyListener {

        private static final String TAG = "GpsSkyView";

        Context mContext;
        WindowManager mWindowManager;


//        Drawable Options
        private int skyViewStartAngle = 0;
        private int skyViewTotalSegments = 12;
        private int sweepAngle = 360/skyViewTotalSegments;

        private double mOrientation = 0.0;
        private static final float PRN_TEXT_SCALE = 0.7f;
        private boolean mUseSnr = false;

        private static int SAT_RADIUS;
        private final float mSnrThresholds[], mCn0Thresholds[];
        private final int mSnrColors[], mCn0Colors[];

        private int[] circleSegments = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        private int circleFontSize = 0;
        private int radius = 0;

//        Drawing Paint
        private Paint mHorizonActiveFillPaint, mHorizonInactiveFillPaint, mHorizonStrokePaint,
                mGridStrokePaint, mGridStrokeDashedPaint,
                mSatelliteFillPaint, mSatelliteStrokePaint, mSatelliteUsedStrokePaint,
                mNorthPaint, mNorthFillPaint,
                mPrnIdPaint, mPrnIdPaintLocked, mPrnBoxFillPaint, mPrnBoxStrokePaint, mPrnBoxStrokePaintLock, mPrnBoxFillPaintLock,
                mNotInViewPaint, mCompassAnglePaint;


        private RectF rectPrnBox = new RectF();
        private RectF rectF = new RectF();

//        Fragment Status indicator
        private boolean mSkyViewStarted;  //was mStarted

        //    Satellite Metadata
        private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
        private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
        private String mSnrCn0Title;
        private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];


        public GpsSkyView(Context context) {
            super(context);

            mContext = context;
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            SAT_RADIUS = GpsHelper.dpToPixels(context, 5);


//            Skyview Background - When GPS Lock Acquired
            mHorizonActiveFillPaint = new Paint();
            mHorizonActiveFillPaint.setColor(ContextCompat.getColor(context, R.color.skyview_background));
            mHorizonActiveFillPaint.setStyle(Paint.Style.FILL);
            mHorizonActiveFillPaint.setAntiAlias(true);

            //Skyview Background = When GPS Lock not yet Available
            mHorizonInactiveFillPaint = new Paint();
            mHorizonInactiveFillPaint.setColor(Color.LTGRAY);
            mHorizonInactiveFillPaint.setStyle(Paint.Style.FILL);
            mHorizonInactiveFillPaint.setAntiAlias(true);

//            Outer Circle Frame
            mHorizonStrokePaint = new Paint();
            mHorizonStrokePaint.setColor(Color.BLACK);
            mHorizonStrokePaint.setStyle(Paint.Style.STROKE);
            mHorizonStrokePaint.setStrokeWidth(3.0f);
            mHorizonStrokePaint.setAntiAlias(true);

//            Grid lines - Solid
            mGridStrokePaint = new Paint();
            mGridStrokePaint.setColor(Color.GRAY);
            mGridStrokePaint.setStyle(Paint.Style.STROKE);
            mGridStrokePaint.setAntiAlias(true);

//            Grid Line - Dashed
            mGridStrokeDashedPaint = new Paint();
            mGridStrokeDashedPaint.setColor(Color.WHITE);
            mGridStrokeDashedPaint.setStyle(Paint.Style.STROKE);
            mGridStrokeDashedPaint.setPathEffect(new DashPathEffect(new float [] {10,20},0));
            mGridStrokeDashedPaint.setAntiAlias(true);

            mSatelliteFillPaint = new Paint();
            mSatelliteFillPaint.setColor(Color.YELLOW);
            mSatelliteFillPaint.setStyle(Paint.Style.FILL);
            mSatelliteFillPaint.setAntiAlias(true);

//            Outer Frame of Satellites without Lock
            mSatelliteStrokePaint = new Paint();
            mSatelliteStrokePaint.setColor(Color.WHITE);
            mSatelliteStrokePaint.setStyle(Paint.Style.STROKE);
            mSatelliteStrokePaint.setStrokeWidth(2.0f);
            mSatelliteStrokePaint.setAntiAlias(true);

//            Outer Frame of Satellites with Lock
            mSatelliteUsedStrokePaint = new Paint();
            mSatelliteUsedStrokePaint.setColor(Color.WHITE);
            mSatelliteUsedStrokePaint.setStyle(Paint.Style.STROKE);
            mSatelliteUsedStrokePaint.setStrokeWidth(4.0f);
            mSatelliteUsedStrokePaint.setAntiAlias(true);

            mSnrThresholds = new float[]{0.0f, 10.0f, 20.0f, 30.0f};
            mSnrColors = new int[]{Color.GRAY, Color.RED, Color.YELLOW, Color.GREEN};

            mCn0Thresholds = new float[]{0.0f, 16.6f, 33.3f, 50.0f};
            mCn0Colors = new int[]{Color.GRAY, Color.RED, Color.YELLOW, Color.GREEN};

            mNorthPaint = new Paint();
            mNorthPaint.setColor(Color.BLACK);
            mNorthPaint.setStyle(Paint.Style.STROKE);
            mNorthPaint.setStrokeWidth(4.0f);
            mNorthPaint.setAntiAlias(true);

            mNorthFillPaint = new Paint();
            mNorthFillPaint.setColor(Color.WHITE);
            mNorthFillPaint.setStyle(Paint.Style.FILL);
            mNorthFillPaint.setStrokeWidth(4.0f);
            mNorthFillPaint.setAntiAlias(true);


//            Text Color of Satellites - Not Locked
            mPrnIdPaint = new Paint();
            mPrnIdPaint.setColor(Color.WHITE);
            mPrnIdPaint.setStyle(Paint.Style.STROKE);
            mPrnIdPaint.setTextSize(GpsHelper.dpToPixels(getContext(), SAT_RADIUS * PRN_TEXT_SCALE));
            mPrnIdPaint.setAntiAlias(true);

//            Text Color of Satellites - Locked
            mPrnIdPaintLocked = new Paint();
            mPrnIdPaintLocked.setColor(Color.BLACK);
            mPrnIdPaintLocked.setStyle(Paint.Style.STROKE);
            mPrnIdPaintLocked.setTextSize(GpsHelper.dpToPixels(getContext(), SAT_RADIUS * PRN_TEXT_SCALE));
            mPrnIdPaintLocked.setAntiAlias(true);


//            Background of Text
            // No Lock
            mPrnBoxFillPaint = new Paint();
            mPrnBoxFillPaint.setStyle(Paint.Style.FILL);
            mPrnBoxFillPaint.setColor(Color.BLACK);

            //No Lock
            mPrnBoxStrokePaint = new Paint();
            mPrnBoxStrokePaint.setStyle(Paint.Style.STROKE);
            mPrnBoxStrokePaint.setColor(Color.WHITE);
            mPrnBoxStrokePaint.setStrokeWidth(1.0f);
            mPrnBoxStrokePaint.setAntiAlias(true);

            // Lock
            mPrnBoxFillPaintLock = new Paint();
            mPrnBoxFillPaintLock.setStyle(Paint.Style.FILL);
            mPrnBoxFillPaintLock.setColor(Color.WHITE);

            //Lock
            mPrnBoxStrokePaintLock = new Paint();
            mPrnBoxStrokePaintLock.setStyle(Paint.Style.STROKE);
            mPrnBoxStrokePaintLock.setColor(Color.BLACK);
            mPrnBoxStrokePaintLock.setStrokeWidth(1.0f);
            mPrnBoxStrokePaintLock.setAntiAlias(true);

            //Prn Not in View
            mNotInViewPaint = new Paint();
            mNotInViewPaint.setColor(ContextCompat.getColor(context, R.color.sat_invisible));
            mNotInViewPaint.setStyle(Paint.Style.FILL);
            mNotInViewPaint.setStrokeWidth(4.0f);
            mNotInViewPaint.setAntiAlias(true);

            // Azimuths and Angle Text
            mCompassAnglePaint = new Paint();
            mCompassAnglePaint.setColor(Color.WHITE);
            mCompassAnglePaint.setStyle(Paint.Style.STROKE);
            mCompassAnglePaint.setTextSize((GpsHelper.dpToPixels(getContext(), SAT_RADIUS * PRN_TEXT_SCALE) / 2));
            mCompassAnglePaint.setAntiAlias(true);

            setFocusable(true);

        }

        public void setStarted(){
            mSkyViewStarted = true;
            invalidate();
        }

        public void setStopped(){
            mSkyViewStarted = false;
            mSvCount = 0;
            invalidate();

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void setGnssStatus(GnssStatus status) {
            mUseSnr = false;

            if (mPrns == null) {
                final int MAX_LENGTH = 255;
                mPrns = new int[MAX_LENGTH];
                mSnrCn0s = new float[MAX_LENGTH];
                mSvElevations = new float[MAX_LENGTH];
                mSvAzimuths = new float[MAX_LENGTH];
                mConstellationType = new int[MAX_LENGTH];
                mHasEphemeris = new boolean[MAX_LENGTH];
                mHasAlmanac = new boolean[MAX_LENGTH];
                mUsedInFix = new boolean[MAX_LENGTH];
            }

            int length = status.getSatelliteCount();
            mSvCount = 0;
            while (mSvCount < length) {
                mSnrCn0s[mSvCount] = status.getCn0DbHz(mSvCount);
                mSvElevations[mSvCount] = status.getElevationDegrees(mSvCount);
                mSvAzimuths[mSvCount] = status.getAzimuthDegrees(mSvCount);
                mPrns[mSvCount] = status.getSvid(mSvCount);
                mConstellationType[mSvCount] = status.getConstellationType(mSvCount);
                mHasEphemeris[mSvCount] = status.hasEphemerisData(mSvCount);
                mHasAlmanac[mSvCount] = status.hasAlmanacData(mSvCount);
                mUsedInFix[mSvCount] = status.usedInFix(mSvCount);
                mSvCount++;
            }

            mSkyViewStarted = true;
            invalidate();

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void setGnssMeasurementEvent(GnssMeasurementsEvent event) {
            // No-op
        }

        @Deprecated
        public void setSats(GpsStatus status) {
            // Use SNR instead of C/N0 - see #65
            mUseSnr = true;

            Iterator<GpsSatellite> satellites = status.getSatellites().iterator();

            if (mSnrCn0s == null) {
                int length = status.getMaxSatellites();
                mSnrCn0s = new float[length];
                mSvElevations = new float[length];
                mSvAzimuths = new float[length];
                mPrns = new int[length];
                mHasEphemeris = new boolean[length];
                mHasAlmanac = new boolean[length];
                mUsedInFix = new boolean[length];
                // Constellation type isn't used, but instantiate it to avoid NPE in legacy devices
                mConstellationType = new int[length];
            }

            mSvCount = 0;
            while (satellites.hasNext()) {
                GpsSatellite satellite = satellites.next();
                mSnrCn0s[mSvCount] = satellite.getSnr();
                mSvElevations[mSvCount] = satellite.getElevation();
                mSvAzimuths[mSvCount] = satellite.getAzimuth();
                mPrns[mSvCount] = satellite.getPrn();
                mHasEphemeris[mSvCount] = satellite.hasEphemeris();
                mHasAlmanac[mSvCount] = satellite.hasAlmanac();
                mUsedInFix[mSvCount] = satellite.usedInFix();
                mSvCount++;
            }

            mSkyViewStarted = true;
            invalidate();
        }


        private void drawLine(Canvas c, float x1, float y1, float x2, float y2) {
            // rotate the line based on orientation
            double angle = Math.toRadians(-mOrientation);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float centerX = (x1 + x2) / 2.0f;
            float centerY = (y1 + y2) / 2.0f;
            x1 -= centerX;
            y1 = centerY - y1;
            x2 -= centerX;
            y2 = centerY - y2;

            float X1 = cos * x1 + sin * y1 + centerX;
            float Y1 = -(-sin * x1 + cos * y1) + centerY;
            float X2 = cos * x2 + sin * y2 + centerX;
            float Y2 = -(-sin * x2 + cos * y2) + centerY;

            c.drawLine(X1, Y1, X2, Y2, mGridStrokePaint);
        }

        private void drawHorizon(Canvas c, int s) {
            float radius = s / 2;

            c.drawCircle(radius, radius, radius,mSkyViewStarted ? mHorizonActiveFillPaint : mHorizonInactiveFillPaint);

//            Segmented
            int paddingLeft = 50;
            int paddingTop = 50;
            int paddingRight = 50;
            int paddingBottom = 50;

            int left = 0 + paddingLeft;
            int top = 0 + paddingTop;
            int right = mWidth - paddingRight;
            int bottom = mWidth - paddingBottom;

            rectF = new RectF(left, top, right, bottom);

            for (int i = 0; i < skyViewTotalSegments; i++){
                c.drawArc(rectF,skyViewStartAngle,sweepAngle,true,mGridStrokeDashedPaint);
                skyViewStartAngle -=sweepAngle;
            }

            //Azimuths
            c.drawCircle(radius, radius, elevationToRadius(s, 60.0f), mGridStrokeDashedPaint);
            c.drawCircle(radius, radius, elevationToRadius(s, 40.0f), mGridStrokeDashedPaint);
            c.drawCircle(radius, radius, elevationToRadius(s, 20.0f), mGridStrokeDashedPaint);
            //Borders
            //c.drawCircle(radius, radius, elevationToRadius(s, 5.0f), mGridStrokeDashedPaint);
            c.drawCircle(radius, radius, radius, mHorizonStrokePaint);
        }

        private void drawCompass(Canvas c, int s){
            float radius = (s / 2);
            int compassAngle = 0;
            int compassAngleBufferSmall = 1;
            int compassAngleBufferLarge = 2;
            Path unitPath0, unitPath30,unitPath60,unitPath90, unitPath120, unitPath150, unitPath180,
                    unitPath210, unitPath240, unitPath270, unitPath300, unitPath330;

            int paddingLeft = 40;
            int paddingTop = 40;
            int paddingRight = 40;
            int paddingBottom = 40;

            int left = 0 + paddingLeft;
            int top = 0 + paddingTop;
            int right = mWidth - paddingRight;
            int bottom = mWidth - paddingBottom;

            rectF = new RectF(left, top, right, bottom);

            for (int  number : circleSegments){
                switch (number){
                    case 1:
                        compassAngle = 30;

                        unitPath30 = new Path();
                        unitPath30.addArc(rectF,compassAngle-90-compassAngleBufferSmall,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath30,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 2:
                        compassAngle = 60;

                        unitPath60 = new Path();
                        unitPath60.addArc(rectF,compassAngle-90-compassAngleBufferSmall,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath60,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 3:
                        compassAngle = 90;

                        unitPath90 = new Path();
                        unitPath90.addArc(rectF,compassAngle-90-compassAngleBufferSmall,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath90,0.0f,0.0f,mCompassAnglePaint);

                        break;
                    case 4:
                        compassAngle = 120;

                        unitPath120 = new Path();
                        unitPath120.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath120,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 5:
                        compassAngle = 150;

                        unitPath150 = new Path();
                        unitPath150.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath150,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 6:
                        compassAngle = 180;

                        unitPath180 = new Path();
                        unitPath180.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath180,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 7:
                        compassAngle = 210;

                        unitPath210 = new Path();
                        unitPath210.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath210,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 8:
                        compassAngle = 240;

                        unitPath240 = new Path();
                        unitPath240.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath240,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 9:
                        compassAngle = 270;

                        unitPath270 = new Path();
                        unitPath270.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath270,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 10:
                        compassAngle = 300;

                        unitPath300 = new Path();
                        unitPath300.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath300,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 11:
                        compassAngle = 330;

                        unitPath330 = new Path();
                        unitPath330.addArc(rectF,compassAngle-90-compassAngleBufferLarge,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath330,0.0f,0.0f,mCompassAnglePaint);
                        break;
                    case 12:
                        compassAngle = 0;

                        unitPath0 = new Path();
                        unitPath0.addArc(rectF,compassAngle-90-compassAngleBufferSmall,sweepAngle);
                        c.drawTextOnPath(String.valueOf(compassAngle),unitPath0,0.0f,0.0f,mCompassAnglePaint);
                        break;

                }

            }

        }

        private void drawNorthIndicator(Canvas c, int s) {
            float radius = s / 2;
            double angle = Math.toRadians(-mOrientation);
            final float ARROW_HEIGHT_SCALE = 0.05f;
            final float ARROW_WIDTH_SCALE = 0.1f;

            float x1, y1;  // Tip of arrow
            x1 = radius;
            y1 = elevationToRadius(s, 90.0f);

            float x2, y2;
            x2 = x1 + radius * ARROW_HEIGHT_SCALE;
            y2 = y1 + radius * ARROW_WIDTH_SCALE;

            float x3, y3;
            x3 = x1 - radius * ARROW_HEIGHT_SCALE;
            y3 = y1 + radius * ARROW_WIDTH_SCALE;

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
            path.lineTo(x3, y3);
            path.lineTo(x1, y1);
            path.close();

            // Rotate arrow around center point
            Matrix matrix = new Matrix();
            matrix.postRotate((float) -mOrientation, radius, radius);
            path.transform(matrix);

            c.drawPath(path, mNorthPaint);
            c.drawPath(path, mNorthFillPaint);
        }

        private void drawAzimuthText(Canvas c, int s){
            float radius = s / 2;
            float x1, y1, y2, y3, y4;  // Tip of 0

            x1 = radius;
            y1 = elevationToRadius(s, 90.0f);

            y2 = elevationToRadius(s, 30.0f);
            y3 = elevationToRadius(s, 50.0f);
            y4 = elevationToRadius(s, 70.0f);

            c.drawText("60",x1,y2,mCompassAnglePaint);
            c.drawText("40",x1,y3,mCompassAnglePaint);
            c.drawText("20",x1,y4,mCompassAnglePaint);

        }

        private void drawSatellite(Canvas c, int s, float elev, float azim, float snrCn0, int prn,
                                   int constellationType, boolean usedInFix) {
            double radius, angle;
            float x, y;
            // Place PRN text slightly below drawn satellite
            final double PRN_X_SCALE = 1.4;
            final double PRN_Y_SCALE = 3.8;

            Paint fillPaint;
            if (snrCn0 == 0.0f) {
                // Satellite can't be seen
                fillPaint = mNotInViewPaint;
            } else {
                // Calculate fill color based on signal strength
                fillPaint = getSatellitePaint(mSatelliteFillPaint, snrCn0);
            }

            Paint strokePaint, prnBoxStrokePaint, prnBoxFillPaint, prnTextPaint;

            if (usedInFix) {
                strokePaint = mSatelliteUsedStrokePaint;


                prnTextPaint = mPrnIdPaintLocked;
                prnBoxStrokePaint = mPrnBoxStrokePaintLock;
                prnBoxFillPaint = mPrnBoxFillPaintLock;

            } else {
                strokePaint = mSatelliteStrokePaint;

                prnTextPaint = mPrnIdPaint;
                prnBoxStrokePaint = mPrnBoxStrokePaint;
                prnBoxFillPaint = mPrnBoxFillPaint;
            }

            radius = elevationToRadius(s, elev);
            azim -= mOrientation;
            angle = (float) Math.toRadians(azim);

            x = (float) ((s / 2) + (radius * Math.sin(angle)));
            y = (float) ((s / 2) - (radius * Math.cos(angle)));

            // Change shape based on satellite operator
            GnssType operator;
            if (GpsHelper.isGnssStatusListenerSupported()) {
                operator = GpsHelper.getGnssConstellationType(constellationType);
            } else {
                operator = GpsHelper.getGnssType(prn);
            }
            switch (operator) {
                case NAVSTAR:
                    c.drawCircle(x, y, SAT_RADIUS, fillPaint);
                    c.drawCircle(x, y, SAT_RADIUS, strokePaint);
                    break;
                case GLONASS:
                    c.drawRect(x - SAT_RADIUS, y - SAT_RADIUS, x + SAT_RADIUS, y + SAT_RADIUS,
                            fillPaint);
                    c.drawRect(x - SAT_RADIUS, y - SAT_RADIUS, x + SAT_RADIUS, y + SAT_RADIUS,
                            strokePaint);
                    break;
                case QZSS:
                    drawTriangle(c, x, y, fillPaint, strokePaint);
                    break;
                case BEIDOU:
                    drawPentagon(c, x, y, fillPaint, strokePaint);
                    break;
                case GALILEO:
                    // We're running out of shapes - QZSS should be regional to Japan, so re-use triangle
                    drawTriangle(c, x, y, fillPaint, strokePaint);
                    break;
            }

            int marginBig = 10;
            int marginSmall = 5;
            String results = String.format("%02d",prn);


            float x1Rect = x - (int) (SAT_RADIUS * PRN_X_SCALE);
            float y1Rect = y + (int) (SAT_RADIUS * PRN_Y_SCALE);
            float x2Rect = x1Rect + mPrnIdPaint.measureText(String.valueOf(results));
            float y2Rect = y1Rect - mPrnIdPaint.getTextSize();


            rectPrnBox = new RectF(x1Rect-marginSmall,y1Rect + marginBig, x2Rect + marginSmall,y2Rect + marginBig);

            int cornerRadius = 10;

            c.drawRoundRect(rectPrnBox,cornerRadius,cornerRadius,prnBoxFillPaint);
            c.drawRoundRect(rectPrnBox,cornerRadius,cornerRadius,prnBoxStrokePaint);

            c.drawText(results, x1Rect,y1Rect, prnTextPaint);

        }

        private float elevationToRadius(int s, float elev) {
            return ((s / 2) - SAT_RADIUS) * (1.0f - (elev / 90.0f));
        }

        private void drawTriangle(Canvas c, float x, float y, Paint fillPaint, Paint strokePaint) {
            float x1, y1;  // Top
            x1 = x;
            y1 = y - SAT_RADIUS;

            float x2, y2; // Lower left
            x2 = x - SAT_RADIUS;
            y2 = y + SAT_RADIUS;

            float x3, y3; // Lower right
            x3 = x + SAT_RADIUS;
            y3 = y + SAT_RADIUS;

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
            path.lineTo(x3, y3);
            path.lineTo(x1, y1);
            path.close();

            c.drawPath(path, fillPaint);
            c.drawPath(path, strokePaint);
        }

        private void drawPentagon(Canvas c, float x, float y, Paint fillPaint, Paint strokePaint) {
            Path path = new Path();
            path.moveTo(x, y - SAT_RADIUS);
            path.lineTo(x - SAT_RADIUS, y - (SAT_RADIUS / 3));
            path.lineTo(x - 2 * (SAT_RADIUS / 3), y + SAT_RADIUS);
            path.lineTo(x + 2 * (SAT_RADIUS / 3), y + SAT_RADIUS);
            path.lineTo(x + SAT_RADIUS, y - (SAT_RADIUS / 3));
            path.close();

            c.drawPath(path, fillPaint);
            c.drawPath(path, strokePaint);
        }

        /**
         * Gets the paint color for a satellite based on provided SNR or C/N0
         *
         * @param base   the base paint color to be changed
         * @param snrCn0 the SNR to use (if mUseSnr is true) or the C/N0 to use (if mUseSnr is
         *               false)
         *               to generate the satellite color based on signal quality
         * @return the paint color for a satellite based on provided SNR or C/N0
         */
        private Paint getSatellitePaint(Paint base, float snrCn0) {
            Paint newPaint;
            newPaint = new Paint(base);

            int numSteps;
            final float thresholds[];
            final int colors[];

            if (mUseSnr) {
                // Use SNR
                numSteps = mSnrThresholds.length;
                thresholds = mSnrThresholds;
                colors = mSnrColors;
            } else {
                // Use C/N0
                numSteps = mCn0Thresholds.length;
                thresholds = mCn0Thresholds;
                colors = mCn0Colors;
            }

            if (snrCn0 <= thresholds[0]) {
                newPaint.setColor(colors[0]);
                return newPaint;
            }

            if (snrCn0 >= thresholds[numSteps - 1]) {
                newPaint.setColor(colors[numSteps - 1]);
                return newPaint;
            }

            for (int i = 0; i < numSteps - 1; i++) {
                float threshold = thresholds[i];
                float nextThreshold = thresholds[i + 1];
                if (snrCn0 >= threshold && snrCn0 <= nextThreshold) {
                    int c1, r1, g1, b1, c2, r2, g2, b2, c3, r3, g3, b3;
                    float f;

                    c1 = colors[i];
                    r1 = Color.red(c1);
                    g1 = Color.green(c1);
                    b1 = Color.blue(c1);

                    c2 = colors[i + 1];
                    r2 = Color.red(c2);
                    g2 = Color.green(c2);
                    b2 = Color.blue(c2);

                    f = (snrCn0 - threshold) / (nextThreshold - threshold);

                    r3 = (int) (r2 * f + r1 * (1.0f - f));
                    g3 = (int) (g2 * f + g1 * (1.0f - f));
                    b3 = (int) (b2 * f + b1 * (1.0f - f));
                    c3 = Color.rgb(r3, g3, b3);

                    newPaint.setColor(c3);

                    return newPaint;
                }
            }

            newPaint.setColor(Color.MAGENTA);

            return newPaint;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int minScreenDimen;

            minScreenDimen = (GpsSurveySkyViewCircleFragment.mWidth < GpsSurveySkyViewCircleFragment.mHeight)
                    ? GpsSurveySkyViewCircleFragment.mWidth : GpsSurveySkyViewCircleFragment.mHeight;


            canvas.drawColor(ContextCompat.getColor(mContext, R.color.app_background));

            drawHorizon(canvas, minScreenDimen);

            drawCompass(canvas,minScreenDimen);

            drawAzimuthText(canvas,minScreenDimen);

            //drawNorthIndicator(canvas, minScreenDimen);

            if (mSvElevations != null) {
                int numSats = mSvCount;

                for (int i = 0; i < numSats; i++) {
                    if (mSvElevations[i] != 0.0f || mSvAzimuths[i] != 0.0f) {
                        drawSatellite(canvas, minScreenDimen, mSvElevations[i], mSvAzimuths[i], mSnrCn0s[i],
                                mPrns[i], mConstellationType[i], mUsedInFix[i]);
                    }
                }
            }
        }

        @Override
        public void onOrientationChanged(double orientation, double tilt) {
            mOrientation = orientation;
            invalidate();
        }

        @Override
        public void gpsStart() {

        }

        @Override
        public void gpsStop() {

        }

        @Override
        public void onGpsStatusChanged(int event, GpsStatus status) {

        }

        @Override
        public void onGnssFirstFix(int ttffMillis) {

        }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {

        }

        @Override
        public void onGnssStarted() {

        }

        @Override
        public void onGnssStopped() {

        }

        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

        }


        @Override
        public void onNmeaMessage(String message, long timestamp) {

        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}

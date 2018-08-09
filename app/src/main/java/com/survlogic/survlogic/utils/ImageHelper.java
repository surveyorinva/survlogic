package com.survlogic.survlogic.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by chrisfillmore on 7/19/2017.
 */

public class ImageHelper {
    private static final String TAG = "ImageHelper";

    static Context mContext;

    public ImageHelper(Context mContext) {
        this.mContext = mContext;

    }

    /**
     * Convert bitmap to bytes
     * @param b
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public byte[] convertImageToByte(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }

    /**
     * Convert byte Array to Bitmap
     * @param b
     * @return
     */
    public Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

    public Bitmap convertFileURLToBitmap(String mPathURL){
        Log.d(TAG, "convertFileURLToBitmap: Converting Bitmap for:" + mPathURL);
        File imgFile = new File(mPathURL);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        return myBitmap;

    }

    /**
     * Creates a smaller overlay box at top of image and single row of text for detailing.
     * @param src
     * @param watermark
     * @param createOverlay
     * @return
     */


    public Bitmap setWatermarkAtTop(Bitmap src, String watermark, Boolean createOverlay){
        Log.d(TAG, "setWatermarkAtBottom: Starting method");

        Paint rectBlackStroke,rectBlackFill;
        Rect rectWatermarkBounds = new Rect(), rectTitleBounds = new Rect();
        RectF rectOverlay;

        int mTextSize = 40,  mTextAlpha = 255;
        int mTextHeaderSize = 20, mTextHeaderAlpha = 230;

        int topMargin = 30;
        int topPadding = 10, leftPadding = 20;
        float mLineWidth = 3;

        int w = src.getWidth();
        int h = src.getHeight();

        int hOverlay = 10;

        Bitmap result = Bitmap.createBitmap(w,h,src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src,0,0,null);


//        Text

        //Point Title
        String titlePoint = "Point ";
        Paint paintTitle = new Paint();
        paintTitle.setColor(Color.WHITE);
        paintTitle.setAlpha(mTextHeaderAlpha);
        paintTitle.setTextSize(mTextHeaderSize);
        paintTitle.setStyle(Paint.Style.FILL);
        paintTitle.setAntiAlias(true);

        paintTitle.getTextBounds(titlePoint,0, titlePoint.length(), rectTitleBounds);
        int titleHeight = rectTitleBounds.height();
        int titleWidth = rectTitleBounds.width();

        int startTitleTextX = 1 + leftPadding;
        int startTitleTextY = topMargin;

        // Point No
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(mTextAlpha);
        paint.setTextSize(mTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint.getTextBounds(watermark,0, watermark.length(),rectWatermarkBounds);
        int headerHeight = rectWatermarkBounds.height();
        int headerWidth = rectWatermarkBounds.width();

        int intStartTextX = 1 + leftPadding;
        int intStartTextY = headerHeight + topMargin + topPadding;

//        Overlay
        if (createOverlay) {
            rectBlackFill = new Paint();
            rectBlackFill.setStyle(Paint.Style.FILL);
            rectBlackFill.setColor(Color.BLACK);
            rectBlackFill.setAlpha(180);

            rectBlackStroke = new Paint();
            rectBlackStroke.setStyle(Paint.Style.STROKE);
            rectBlackStroke.setColor(Color.BLACK);
            rectBlackStroke.setStrokeWidth(mLineWidth);
            rectBlackStroke.setAntiAlias(true);

            int intStartBoxX = 0;
            int StartBoxY = 0;
            int EndBoxY = intStartTextY + headerHeight + hOverlay;

            rectOverlay = new RectF(intStartBoxX, StartBoxY, w, EndBoxY);
            canvas.drawRect(rectOverlay, rectBlackFill);

        }

        // Draw after overlay
        canvas.drawText(titlePoint,startTitleTextX,startTitleTextY,paintTitle);
        canvas.drawText(watermark, intStartTextX, intStartTextY, paint);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        Log.d(TAG, "setWatermarkAtBottom: finish creating watermark");

        return result;
    }


    /**
     * Creates a smaller overlay box at bottom of image and single row of text for detailing.
     * @param src
     * @param watermark
     * @param createOverlay
     * @return
     */

    public Bitmap setWatermarkAtBottom(Bitmap src, String watermark, Boolean createOverlay){
        Log.d(TAG, "setWatermarkAtBottom: Starting method");

        Paint rectBlackStroke,rectBlackFill;
        Rect rectWatermarkBounds = new Rect();
        RectF rectOverlay;

        int mTextSize = 40, mTextAlpha = 245;
        int bottomPadding = 20;
        float mLineWidth = 3;

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap result = Bitmap.createBitmap(w,h,src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src,0,0,null);


//        Text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(mTextAlpha);
        paint.setTextSize(mTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint.getTextBounds(watermark,0, watermark.length(),rectWatermarkBounds);
        int headerHeight = rectWatermarkBounds.height();
        int headerWidth = rectWatermarkBounds.width();

        int intStartTextX = 1;
        int intStartTextY = h - headerHeight - bottomPadding;

//        Overlay
        if (createOverlay) {
            rectBlackFill = new Paint();
            rectBlackFill.setStyle(Paint.Style.FILL);
            rectBlackFill.setColor(Color.BLACK);
            rectBlackFill.setAlpha(180);

            rectBlackStroke = new Paint();
            rectBlackStroke.setStyle(Paint.Style.STROKE);
            rectBlackStroke.setColor(Color.BLACK);
            rectBlackStroke.setStrokeWidth(mLineWidth);
            rectBlackStroke.setAntiAlias(true);

            int intStartBoxX = 0;
            int StartBoxY = intStartTextY - headerHeight - bottomPadding;

            rectOverlay = new RectF(intStartBoxX, StartBoxY, w, h);
            canvas.drawRect(rectOverlay, rectBlackFill);

        }

        canvas.drawText(watermark, intStartTextX, intStartTextY, paint);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        Log.d(TAG, "setWatermarkAtBottom: finish creating watermark");

        return result;
    }


    public Bitmap setWatermarkCompass(Bitmap src, int azimuth, boolean showMarker){
        //------------------------------------------------------------------------------------------
        int mBackgroundColor = Color.BLACK;
        int mMarkerColor = Color.RED;
        int mLineColor = Color.WHITE;
        int mTextColor = Color.WHITE;
        float mTextSize = 15 * mContext.getResources().getDisplayMetrics().scaledDensity;
        int mDegrees = azimuth;
        int mCompDegree =azimuth;
        float mRangeDegrees = 180f;
        //------------------------------------------------------------------------------------------
        Paint mCompassBackgroudFill = new Paint();
        mCompassBackgroudFill.setStyle(Paint.Style.FILL);
        mCompassBackgroudFill.setColor(mBackgroundColor);
        mCompassBackgroudFill.setAlpha(180);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint mMainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainLinePaint.setStrokeWidth(8f);

        Paint mSecondaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondaryLinePaint.setStrokeWidth(6f);

        Paint mTerciaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTerciaryLinePaint.setStrokeWidth(4f);

        Paint mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setStyle(Paint.Style.FILL);
        Path pathMarker = new Path();

        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mMainLinePaint.setColor(mLineColor);
        mSecondaryLinePaint.setColor(mLineColor);
        mTerciaryLinePaint.setColor(mLineColor);
        mMarkerPaint.setColor(mMarkerColor);
        //------------------------------------------------------------------------------------------
        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap result = Bitmap.createBitmap(w,h,src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src,0,0,null);

        RectF rectOverlay;

        int compassHeightBox = 50;
        int bottomPadding = 5;

        int intStartCompassBoxX = 0;
        int intStartCompassBoxY = h - compassHeightBox - bottomPadding;

        rectOverlay = new RectF(intStartCompassBoxX,intStartCompassBoxY,w,h);
        canvas.drawRect(rectOverlay,mCompassBackgroudFill);

        int paddingTop = 5;
        int paddingBottom = 5;
        int paddingLeft = 5;
        int paddingRight = 5;

        int unitHeight = (intStartCompassBoxY - paddingTop - paddingBottom) /100;
        float pixDeg = (w - paddingLeft - paddingRight)/ mRangeDegrees;

        int minDegrees = Math.round(mDegrees - mRangeDegrees / 2), maxDegrees = Math.round(mDegrees
                + mRangeDegrees / 2);

        int tickStart = h - bottomPadding;

        for (int i = -180; i < 540; i += 5) {
            if ((i >= minDegrees) && (i <= maxDegrees)) {
                canvas.drawLine(paddingLeft + pixDeg * (i - minDegrees), intStartCompassBoxY - paddingBottom,
                        paddingLeft + pixDeg * (i - minDegrees), h-(20 * unitHeight + paddingTop),
                        mTerciaryLinePaint);

                if (i % 15 == 0) {
                    canvas.drawLine(paddingLeft + pixDeg * (i - minDegrees),
                            intStartCompassBoxY - paddingBottom, paddingLeft + pixDeg * (i - minDegrees),
                            h-(15 * unitHeight + paddingTop), mTerciaryLinePaint);
                }


                if (i % 45 == 0) {
                    canvas.drawLine(paddingLeft + pixDeg * (i - minDegrees),
                            intStartCompassBoxY - paddingBottom, paddingLeft + pixDeg * (i - minDegrees),
                            h-(10 * unitHeight + paddingTop), mSecondaryLinePaint);
                }

                if (i % 90 == 0) {
                    canvas.drawLine(paddingLeft + pixDeg * (i - minDegrees),
                            intStartCompassBoxY - paddingBottom, paddingLeft + pixDeg * (i - minDegrees),
                            h-(5 * unitHeight + paddingTop), mMainLinePaint);

                    String coord = "";
                    switch (i) {
                        case -90:
                        case 270:
                            coord = mContext.getResources().getString(R.string.compassview_compass_west);
                            break;

                        case 0:
                        case 360:
                            coord = mContext.getResources().getString(R.string.compassview_compass_north);
                            break;

                        case 90:
                        case 450:
                            coord = mContext.getResources().getString(R.string.compassview_compass_east);
                            break;

                        case -180:
                        case 180:
                            coord = mContext.getResources().getString(R.string.compassview_compass_south);
                            break;

                    }

                    canvas.drawText(coord, paddingLeft + pixDeg * (i - minDegrees), 5 * unitHeight
                            + paddingTop, mTextPaint);
                }

            }
        }
//
        if (showMarker) {
            pathMarker.moveTo(w / 2, 3 * unitHeight + paddingTop);
            pathMarker.lineTo((w / 2) + 20, paddingTop);
            pathMarker.lineTo((w / 2) - 20, paddingTop);
            pathMarker.close();
            canvas.drawPath(pathMarker, mMarkerPaint);
        }


        //------------------------------------------------------------------------------------------

        canvas.save(Canvas.ALL_SAVE_FLAG);
        Log.d(TAG, "setWatermarkAtBottom: finish creating watermark");

        return result;


    }

    /**
     * Provides an overlay with larger text detailing that there are +# of items in set.
     * @param src
     * @param watermark
     * @param createOverlay
     * @return
     */
    public Bitmap setHeaderFullScreen(Bitmap src, String watermark, Boolean createOverlay){
        Log.d(TAG, "setWatermarkAtBottom: Starting method");

        Paint rectBlackStroke,rectBlackFill;
        Rect rectWatermarkBounds = new Rect();
        RectF rectOverlay;

        int mTextSize = 150, mTextAlpha = 245;
        int bottomPadding = 20;
        float mLineWidth = 3;

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap result = Bitmap.createBitmap(w,h,src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src,0,0,null);

//        Text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(mTextAlpha);
        paint.setTextSize(mTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint.getTextBounds(watermark,0, watermark.length(),rectWatermarkBounds);
        int headerHeight = rectWatermarkBounds.height();
        int headerWidth = rectWatermarkBounds.width();

        int intStartTextX = 1;
        int intStartTextY = h - headerHeight - bottomPadding;

//        Overlay
        if (createOverlay) {
            rectBlackFill = new Paint();
            rectBlackFill.setStyle(Paint.Style.FILL);
            rectBlackFill.setColor(Color.BLACK);
            rectBlackFill.setAlpha(180);

            rectBlackStroke = new Paint();
            rectBlackStroke.setStyle(Paint.Style.STROKE);
            rectBlackStroke.setColor(Color.BLACK);
            rectBlackStroke.setStrokeWidth(mLineWidth);
            rectBlackStroke.setAntiAlias(true);

            int intStartBoxX = 0;
            int StartBoxY = 0;

            rectOverlay = new RectF(intStartBoxX, StartBoxY, w, h);
            canvas.drawRect(rectOverlay, rectBlackFill);

        }

        canvas.drawText(watermark, w/2 - headerWidth/2, h/2 + headerHeight/2, paint);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        Log.d(TAG, "setWatermarkAtBottom: finish creating watermark");

        return result;
    }


    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}

package com.survlogic.survlogic.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

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
        int topPadding = 30, leftPadding = 20;
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
        int startTitleTextY = topPadding;

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

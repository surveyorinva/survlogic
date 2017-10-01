package com.survlogic.survlogic.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.MapScale;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 9/27/2017.
 */

public class PlanarMapScaleView extends View implements MapScale {
    private static final String TAG = "PlanMapScaleView";
    private static Context mContext;

    private Canvas scaleCanvas;
    private Bitmap scaleBitmap;
    private Paint scaleBarPaint, scaleTextPaint;
    private Rect rectScaleBarTextBounds = new Rect();

    private static int screenWidth, screenHeight;
    private  int scaleDistance = 0;
    private int mTextSize = 40;

    private boolean isInvalidateHandler = false;
    private Handler invalidateHandler;
    private Activity myActivity;


    public PlanarMapScaleView(Context context) {
        super(context);

    }

    public PlanarMapScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
        mContext = context;

        initScale(context);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: Started...");
        initView(w,h);

    }

    private void initScale(Context context){
        Log.d(TAG, "initScale: Start...");

        scaleBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        scaleBarPaint.setColor(ContextCompat.getColor(context,R.color.white));
        scaleBarPaint.setStyle(Paint.Style.STROKE);
        scaleBarPaint.setStrokeJoin(Paint.Join.ROUND);
        scaleBarPaint.setStrokeCap(Paint.Cap.ROUND);
        scaleBarPaint.setAntiAlias(true);

        scaleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleTextPaint.setColor(ContextCompat.getColor(context,R.color.white));
        scaleTextPaint.setStyle(Paint.Style.STROKE);
        scaleTextPaint.setTextSize(mTextSize);
        scaleTextPaint.setAntiAlias(true);

    }

    private void initView(int w, int h){
        screenWidth = w;
        screenHeight = h;

        int scaleWidth = screenWidth;
        int scaleHeight = screenHeight;

        Log.d(TAG, "initScale: Width/Height: " + scaleWidth + "x" + scaleHeight);

        scaleBitmap = Bitmap.createBitmap(scaleWidth,scaleHeight,Bitmap.Config.ARGB_8888);
        scaleCanvas = new Canvas(scaleBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Started");
        canvas.drawBitmap(scaleBitmap, 0, 0, scaleBarPaint);
        scaleCanvas.drawColor(Color.BLACK);

        drawScaleBar(canvas,0,0,screenWidth,screenHeight,Color.WHITE, 5);
        drawScaleDistance(canvas,0,0,screenWidth,screenHeight,Color.WHITE, 5);

    }

    private void drawScaleBar(Canvas c, float x, float y, int screen_Width, int screen_Height, int lineColor, float lineWidth){
        Log.d(TAG, "createScaleBar: Started");
        float startLeft = 10 ;
        float startRight = 10;
        float startTop = 15 ;
        float startBottom = 10;

        float x0, y0;
        x0 = x;
        y0 = y;

        float x1=0, y1=0;
        x1 = x0 + startLeft;
        y1 = y0 + startTop;

        float x2=0, y2=0;
        x2 = x1;
        y2 = screen_Height - startBottom;

        float x3=0, y3=0;
        x3 = screen_Width - startLeft - startRight;
        y3 = y2;

        float x4=0, y4=0;
        x4 = x3;
        y4 = y1;

        Path path = new Path();

        path.moveTo(x1,y1);
        path.lineTo(x1,y1);

        path.lineTo(x2,y2);

        path.lineTo(x3,y3);

        path.lineTo(x4,y4);

        //additional depth
        float x5=0,y5=0;
        x5=x4;
        y5 = y3/2;

        float x6 = 0, y6 = 0;
        x6=x1;
        y6=y5;

        Path pathTop = new Path();
        pathTop.moveTo(x5,y5);
        pathTop.lineTo(x6,y6);

        Log.d(TAG, "X: " + x1 + "," + x2 + "," + x3 + "," + x4 + "," + x5 + "," + x6);
        Log.d(TAG, "Y: " + y1 + "," + y2 + "," + y3 + "," + y4 + "," + y5 + "," + y6);

        c.drawPath(path,scaleBarPaint);
        c.drawPath(pathTop,scaleBarPaint);
    }

    private void  drawScaleDistance(Canvas c, float x, float y, int screen_Width, int screen_Height, int textColor, float textHeight) {
        Log.d(TAG, "createScaleDistance: Started");
        float startLeft = 10;
        float startRight = 10;
        float startTop = 15;
        float startBottom = 10;
        float textBuffer = 10;

        float x0, y0;
        x0 = x;
        y0 = y;

        float x1 = 0, y1 = 0;
        x1 = x0 + startLeft;
        y1 = y0 + startTop;

        float x2 = 0, y2 = 0;
        x2 = x1;
        y2 = screen_Height - startBottom;

        float x3 = 0, y3 = 0;
        x3 = screen_Width - startLeft - startRight;
        y3 = y2;

        float x4 = 0, y4 = 0;
        x4 = x3;
        y4 = y1;

        String txtScale = String.valueOf(scaleDistance);
        Log.d(TAG, "createScaleBar: Scale Distance: " + txtScale);

        scaleTextPaint.getTextBounds(txtScale, 0, txtScale.length(), rectScaleBarTextBounds);
        float headerHeight = rectScaleBarTextBounds.height();
        float headerWidth = rectScaleBarTextBounds.width();

        float startXHeader = startLeft + (x4 - x1) / 2 - headerWidth / 2;
        float startYHeader = y2 - textBuffer;

        if(scaleDistance !=0) {
            c.drawText(txtScale, startXHeader, startYHeader, scaleTextPaint);
            Log.d(TAG, "createScaleDistance: Scale Text drawn");
        }
    }

    @Override
    public int getScaleDistance() {
        return scaleDistance;
    }

    @Override
    public void setScale(int distance) {
        Log.d(TAG, "setScale: Distance: " + distance);
        this.scaleDistance = distance;
        invalidate();


        Log.d(TAG, "setScale: static distance: " + scaleDistance);

    }



}

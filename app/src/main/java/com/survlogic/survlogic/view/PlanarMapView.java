package com.survlogic.survlogic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogProjectDescriptionAdd;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chrisfillmore on 9/13/2017.
 */

public class PlanarMapView extends View {

    private static final String TAG = "PlanarMapView";

    private Canvas drawCanvas, backgroundCanvas, drawPlanarCanvas, scaleCanvas;
    private Bitmap canvasBitmap, planarCanvas, scaleBitmap;
    private Paint canvasPaint, drawPaint, drawBackground, bitmapAlphaCanvas, canvasCheckerboard, scaleBarPaint, scaleTextPaint;
    private Path drawPath;

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;

    private boolean showPointNo = true;
    private boolean showScale = true;

    private float currentBrushSize, lastBrushSize;

    private static int screenWidth, screenHeight, planarWidth, planarHeight;
    private int screenBufferY=100, screenBufferX=100;
    public double planarWidthScale, planarHeightScale;
    public static double planarScale = 0;
    public static double currentScreenScale = 0;
    private static int fakeOriginX = 0, fakeOriginY = 0;
    private static float currentScreenOriginX = 0, currentScreenOriginY = 0;

    private String databaseName;
    private ArrayList<PointSurvey> lstPoints = new ArrayList<>();
    private ArrayList<Integer> lstPointNo = new ArrayList<>();
    private ArrayList<Double> lstNorthings = new ArrayList<>();
    private ArrayList<Double> lstEasting = new ArrayList<>();

    private double mapScaleZoomIn = 1, mapScaleZoomOut = 1;
    private Rect clipBounds_canvas, originalBounds_canvas;

    public PlanarMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initPaint();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initView(w,h);

    }


    private void initPaint(){
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        currentBrushSize = getResources().getInteger(R.integer.small_size);

        drawBackground = new Paint();
        drawBackground.setStyle(Paint.Style.STROKE);
        drawBackground.setColor(Color.GRAY);

        bitmapAlphaCanvas = new Paint();

        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setColor(DEFAULT_COLOR);

        drawPaint.setStrokeWidth(currentBrushSize);


    }


    private void initScale(){
        Log.d(TAG, "initScale: Start...");
        int scaleWidth = screenWidth/4;
        int scaleHeight = (int) (screenHeight * 0.1);

        Log.d(TAG, "initScale: Width/Height: " + scaleWidth + "x" + scaleHeight);

        scaleBitmap = Bitmap.createBitmap(scaleWidth,scaleHeight,Bitmap.Config.ARGB_8888);
        scaleCanvas = new Canvas(scaleBitmap);


        scaleBarPaint = new Paint();
        scaleTextPaint = new Paint();

        scaleBarPaint.setColor(Color.WHITE);
        scaleBarPaint.setStyle(Paint.Style.STROKE);
        scaleBarPaint.setStrokeJoin(Paint.Join.ROUND);
        scaleBarPaint.setStrokeCap(Paint.Cap.ROUND);
        scaleBarPaint.setAntiAlias(true);



    }
    private void initView(int w, int h){
        screenWidth = w;
        screenHeight = h;

        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);



    }

    private void initPoints(){
        Log.d(TAG, "||initPoints: Started...");
        Log.d(TAG, "initPoints: Planar Scale: " + planarScale);
        for(int i=0; i<lstPoints.size(); i++) {
            PointSurvey pointSurvey = lstPoints.get(i);
            lstPointNo.add(pointSurvey.getPoint_no());
            lstNorthings.add(pointSurvey.getNorthing());
            lstEasting.add(pointSurvey.getEasting());
        }

        double minNorth = Collections.min(lstNorthings);
        double minEast = Collections.min(lstEasting);

        double maxNorth = Collections.max(lstNorthings);
        double maxEast = Collections.max(lstEasting);

        minNorth = minNorth - screenBufferY;
        minEast = minEast - screenBufferX;

        maxNorth = maxNorth + screenBufferY;
        maxEast = maxEast + screenBufferX;

        fakeOriginX = (int) minEast;
        fakeOriginY = (int) maxNorth;

        planarWidth = (int) maxEast - (int) minEast;
        planarHeight = (int) maxNorth - (int) minNorth;

        planarWidthScale = (double) screenWidth / (double) planarWidth;
        planarHeightScale = (double) screenHeight / (double) planarHeight;

        if(planarWidthScale<planarHeightScale){
            planarScale = planarWidthScale;
        }else{
            planarScale = planarHeightScale;
        }

        Log.d(TAG, "initView: Planar Scale Set: " + planarScale);
        Log.d(TAG, "initPoints: Creating Canvas");

        planarCanvas = Bitmap.createBitmap(screenWidth,screenHeight,Bitmap.Config.ARGB_8888);
        drawPlanarCanvas = new Canvas(planarCanvas);
        originalBounds_canvas = drawPlanarCanvas.getClipBounds();

        invalidate();



    }

    public boolean getShowPointNo(){
        return showPointNo;
    }

    public void setShowPointNo(boolean showPointNo){
        this.showPointNo = showPointNo;

    }

    public int getMapScale(Rect viewBounds_canvas){

        float x1 = viewBounds_canvas.left;
        float x2 = viewBounds_canvas.right;
//
        double n1 = convertCanvasCoordinates(x1);
        double n2 = convertCanvasCoordinates(x2);

        currentScreenScale = screenWidth/(n2-n1);

        Log.d(TAG, "setMapScale: Zoom Width: " + (n2-n1));
        Log.d(TAG, "Screen Scale: " + currentScreenScale + " Planar Scale:" + planarScale);

        currentScreenOriginX = x1;
        currentScreenOriginY = viewBounds_canvas.top;

        Log.d(TAG, "setMapScale: Origin:" + currentScreenOriginX + ", " + currentScreenOriginY);

        int results = ((int) n2- (int) n1)/2;

        return results;

    }

    public void setDatabaseName(String databaseName){
        this.databaseName = databaseName;
    }


    public void setPointList(ArrayList<PointSurvey> lstPnts){
        this.lstPoints = lstPnts;
    }

    public void setMap(){
        initPoints();
        initScale();
    }

    private double convertCanvasCoordinates(float x1){
        Log.d(TAG, "convertCanvasCoordinates: Started");

        double results = (x1/planarScale) + fakeOriginX;

        return results;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Started");
        canvas.save();



        if(planarCanvas != null){
            Log.d(TAG, "|onDraw: Planar Canvas = Added");
            canvas.drawBitmap(planarCanvas, 0, 0, canvasPaint);
            drawPlanarCanvas.drawColor(Color.BLACK);

            createPointsAll(canvas);
        }


        canvas.restore();

    }

    private void createPointsAll(Canvas c){
        Log.d(TAG, "createPointsAll: Started");

        for(int i=0; i<lstPoints.size(); i++) {
            PointSurvey pointSurvey = lstPoints.get(i);
            double pointNorth = pointSurvey.getNorthing();
            double deltaNorth = (fakeOriginY - pointNorth) * planarScale;
            int deltaNorthScaled = (int) deltaNorth;


            double pointEast = pointSurvey.getEasting();
            double deltaEast = (pointEast - fakeOriginX) * planarScale;
            int deltaEastScaled = (int) deltaEast;

            Log.d(TAG, "||createPointsAllPoint No. : " + pointSurvey.getPoint_no());

            int symbolColor = Color.WHITE;
            int symbolSize = 10;

            drawCross(c,deltaEastScaled, deltaNorthScaled,symbolSize, symbolColor,1);
            //drawCircle(c, deltaEastScaled, deltaNorthScaled, 10, 1, drawPaint, drawPaint);

            if(showPointNo){
                Log.d(TAG, "createPointsAll: planarScale: " + planarScale);
                double textScale = 100 * planarScale;

               drawPointNo(c, deltaEastScaled, deltaNorthScaled, String.valueOf(pointSurvey.getPoint_no()),10,Color.WHITE );
            }

        }
    }


    private void drawPointNo(Canvas c, float x, float y, String pointNumber, float textSize, int textColor){

        Paint paintPointNo;
        Rect rectTextBounds = new Rect();

        paintPointNo = new Paint();
        paintPointNo.setColor(textColor);
        paintPointNo.setStyle(Paint.Style.STROKE);
        paintPointNo.setTextSize(textSize);
        paintPointNo.setAntiAlias(true);

        paintPointNo.getTextBounds(pointNumber,0, pointNumber.length(),rectTextBounds);
        int textHeight = rectTextBounds.height();
        int textWidth = rectTextBounds.width();

        float startX = x + 10;
        float startY = y - 10;

        c.drawText(pointNumber,startX,startY,paintPointNo);

    }

    private void drawCircle(Canvas c, float x, float y, int CIRCLE_RADIUS, double scale, Paint fillPaint, Paint strokePaint) {
        Log.d(TAG, "drawCircle: Started...");
        c.drawCircle(x, y, CIRCLE_RADIUS, fillPaint);
        c.drawCircle(x, y, CIRCLE_RADIUS, strokePaint);


    }

    private void drawTriangle(Canvas c, float x, float y, int TRIANGLE_RADIUS, Paint fillPaint, Paint strokePaint) {
        Log.d(TAG, "drawTriangle: Started...");
        float x1, y1;  // Top
        x1 = x;
        y1 = y - TRIANGLE_RADIUS;

        float x2, y2; // Lower left
        x2 = x - TRIANGLE_RADIUS;
        y2 = y + TRIANGLE_RADIUS;

        float x3, y3; // Lower right
        x3 = x + TRIANGLE_RADIUS;
        y3 = y + TRIANGLE_RADIUS;

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

    private void drawCross(Canvas c, float x, float y, int LINE_RADIUS, int lineColor, float lineWidth){
        Log.d(TAG, "drawCross: Started...");
        float x0, y0;
        x0 = x;
        y0 = y;

        float x1=0, y1=0;
        x1 = x0;
        y1 = y0 - LINE_RADIUS;

        float x2=0, y2=0;
        x2 = x0;
        y2 = y0 + LINE_RADIUS;

        float x3=0, y3=0;
        x3 = x0 - LINE_RADIUS;
        y3 = y0;

        float x4=0, y4=0;
        x4 = x0 + LINE_RADIUS;
        y4 = y0;


        Paint paintLine;
        paintLine = new Paint();
        paintLine.setAntiAlias(false);
        paintLine.setDither(false);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeJoin(Paint.Join.ROUND);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        paintLine.setColor(lineColor);

        paintLine.setStrokeWidth(lineWidth);


        Path path = new Path();

        path.moveTo(x0,y0);
        path.lineTo(x1,y1);

        path.moveTo(x0,y0);
        path.lineTo(x2,y2);

        path.moveTo(x0,y0);
        path.lineTo(x3,y3);

        path.moveTo(x0,y0);
        path.lineTo(x4,y4);

        c.drawPath(path,paintLine);

    }

    private void drawSquare(Canvas c, float x, float y, int LINE_RADIUS, int lineColor, float lineWidth){
        float x0, y0;
        x0 = x;
        y0 = y;

        float x1=0, y1=0;
        x1 = x0 + LINE_RADIUS;
        y1 = y0 + LINE_RADIUS;

        float x2=0, y2=0;
        x1 = x0 + LINE_RADIUS;
        y1 = y0 - LINE_RADIUS;

        float x3=0, y3=0;
        x1 = x0 - LINE_RADIUS;
        y1 = y0 - LINE_RADIUS;

        float x4=0, y4=0;
        x1 = x0 - LINE_RADIUS;
        y1 = y0 + LINE_RADIUS;


        Paint paintLine;
        paintLine = new Paint();
        paintLine.setAntiAlias(false);
        paintLine.setDither(false);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeJoin(Paint.Join.ROUND);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        paintLine.setColor(lineColor);

        paintLine.setStrokeWidth(lineWidth);


        Path path = new Path();

        path.moveTo(x0,y0);
        path.lineTo(x1,y1);
        path.lineTo(x2,y2);
        path.lineTo(x3,y3);
        path.lineTo(x4,y4);
        path.close();

        c.drawPath(path,paintLine);
    }


}

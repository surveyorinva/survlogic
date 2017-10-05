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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    private Context mContext;

    private Canvas drawCanvas, drawPlanarCanvas, scaleCanvas;
    private Bitmap canvasBitmap, planarCanvas, scaleBitmap;
    private Paint canvasPaint, drawPaint, drawBackground, bitmapAlphaCanvas, scaleBarPaint;
    private Paint drawSelectedFill, drawSelectedStroke;
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

    private ArrayList<PointSurvey> lstSelectedPoints = new ArrayList<>();

    private double mapScaleZoomIn = 1, mapScaleZoomOut = 1;
    private Rect clipBounds_canvas, originalBounds_canvas;

    private float screenDistance, symbolSize, textSize;
    private static float scaleRatioSymbol= 0.08f, scaleRatioText = 0.1f;
    private float drawingTouchRadius = 30;

    private boolean mLongClick = false;

    public PlanarMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

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

        drawSelectedFill = new Paint();
        drawSelectedFill.setStyle(Paint.Style.FILL);
        drawSelectedFill.setColor((ContextCompat.getColor(mContext, R.color.red_primary)));

        drawSelectedStroke = new Paint();
        drawSelectedStroke.setStyle(Paint.Style.STROKE);
        drawSelectedStroke.setColor(Color.RED);
        drawSelectedStroke.setStrokeWidth(2);
        drawSelectedStroke.setAntiAlias(true);

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

        planarCanvas = Bitmap.createBitmap(screenWidth,screenHeight,Bitmap.Config.ARGB_8888);
        drawPlanarCanvas = new Canvas(planarCanvas);
        originalBounds_canvas = drawPlanarCanvas.getClipBounds();

        screenDistance = getInitialMapScaleDistance(0,screenWidth);
        Log.d(TAG, "initPoints: Screen Distance: " + screenDistance);

        setObjectSize(screenDistance);

        invalidate();

    }

    public boolean getShowPointNo(){
        return showPointNo;
    }

    public void setShowPointNo(boolean showPointNo){
        this.showPointNo = showPointNo;

    }

    public float getInitialMapScaleDistance(float x1, float x2){
        float value1 = (float) convertCanvasCoordinatesX(x1);
        float value2 = (float) convertCanvasCoordinatesX(x2);

        currentScreenScale = screenWidth/(value2-value1);

        Log.d(TAG, "getInitialMapScaleDistance: Zoom Width: " + (value2-value1));
        Log.d(TAG, "Screen Scale: " + currentScreenScale + " Planar Scale:" + planarScale);

        currentScreenOriginX = x1;

        Log.d(TAG, "getInitialMapScaleDistance: Origin:" + currentScreenOriginX + ", " + currentScreenOriginY);

        float results = (value2- value1);

        return results;

    }


    public float getMapScaleDistance(Rect viewBounds_canvas){
        Log.d(TAG, "getMapScaleDistance: Started...");
        float x1 = viewBounds_canvas.left;
        float x2 = viewBounds_canvas.right;
//
        float value1 = (float) convertCanvasCoordinatesX(x1);
        float value2 = (float) convertCanvasCoordinatesX(x2);

        currentScreenScale = screenWidth/(value2-value1);

        Log.d(TAG, "setMapScale: Zoom Width: " + (value2-value1));
        Log.d(TAG, "Screen Scale: " + currentScreenScale + " Planar Scale:" + planarScale);

        currentScreenOriginX = x1;
        currentScreenOriginY = viewBounds_canvas.top;

        Log.d(TAG, "setMapScale: Origin:" + currentScreenOriginX + ", " + currentScreenOriginY);

        float results = ( value2- value1);

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

    }


    public void setObjectSize(float scaleDistance){
        Log.d(TAG, "setObjectSize: Started...");
        this.symbolSize = scaleDistance * scaleRatioSymbol;

        this.textSize = scaleDistance * scaleRatioText;

        this.screenDistance = scaleDistance;


    }

    public void eraseAll(){
        clear_canvas_background();
    }


    public void setDrawingTouchRadius(float touchRadius){
        Log.d(TAG, "setdrawingTouchRadius: Started");

        this.drawingTouchRadius = touchRadius;
    }

    public float getDrawingTouchRadius(){
        Log.d(TAG, "getDrawingTouchRadius: Started");

        return drawingTouchRadius;
    }


    private double convertCanvasCoordinatesX(float x1){
        Log.d(TAG, "convertCanvasCoordinatesX: Started");

        double results = (x1/planarScale) + fakeOriginX;

        return results;

    }

    private double convertCanvasCoordinatesY(float y1){
        Log.d(TAG, "convertCanvasCoordinatesY: Started");

        double results = (y1/planarScale) + fakeOriginY;

        Log.i(TAG, "convertCanvasCoordinatesY: Results: " + results);

        return results;

    }

    private double convertPlanarCoordinatesX(float e1){
        Log.d(TAG, "convertPlanarCoordinatesX: Started...");

        return (e1 - fakeOriginX) * planarScale;


    }

    private double convertPlanarCoordinatesY(float n1){
        Log.d(TAG, "convertPlanarCoordinatesY: Started...");

        return(fakeOriginY - n1) * planarScale;

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


        if(lstSelectedPoints!=null && lstSelectedPoints.size()>0){
            Log.d(TAG, "onDraw: Selected Points = Added");
            createPointsSelected(canvas);
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

            //drawing points as a cross for now
            //drawing points as color white
            int symbolColor = Color.WHITE;

            Log.i(TAG, "POINT: " + String.valueOf(pointSurvey.getPoint_no()) + ":" + deltaNorthScaled + ", " + deltaEastScaled);
            drawCross(c,deltaEastScaled, deltaNorthScaled,symbolSize, symbolColor);


            if(showPointNo){
               drawPointNo(c, deltaEastScaled, deltaNorthScaled, String.valueOf(pointSurvey.getPoint_no()),textSize,Color.WHITE );
            }

        }
    }

    private void createPointsSelected(Canvas c){
        Log.d(TAG, "createPointsAll: Started");

        for(int i=0; i<lstSelectedPoints.size(); i++) {
            PointSurvey pointSurvey = lstSelectedPoints.get(i);
            double pointNorth = pointSurvey.getNorthing();
            double deltaNorth = (fakeOriginY - pointNorth) * planarScale;
            int deltaNorthScaled = (int) deltaNorth;

            double pointEast = pointSurvey.getEasting();
            double deltaEast = (pointEast - fakeOriginX) * planarScale;
            int deltaEastScaled = (int) deltaEast;

            int symbolColor = Color.WHITE;

            Log.i(TAG, "POINT: " + String.valueOf(pointSurvey.getPoint_no()) + ":" + deltaNorthScaled + ", " + deltaEastScaled);
            drawCircle(c,deltaEastScaled,deltaNorthScaled,symbolSize,drawSelectedFill,drawSelectedStroke);

        }
    }


    private void drawPointNo(Canvas c, float x, float y, String pointNumber, float textSize, int textColor){

        float textBufferX = symbolSize;
        float textBufferY = symbolSize;

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

        float startX = x + textBufferX;
        float startY = y - textBufferY;

        c.drawText(pointNumber,startX,startY,paintPointNo);

    }



    public ArrayList<PointSurvey> checkPointForTouch(float touchX, float touchY){
        Log.d(TAG, "checkPointForTouch: Started");



        for(int i=0; i<lstPoints.size(); i++) {
            Log.i(TAG, "checkPointForTouch: Cycling through Points...............................................");
            PointSurvey pointSurvey = lstPoints.get(i);

            float surveyX = (float) pointSurvey.getEasting();
            float surveyY = (float) pointSurvey.getNorthing();

            float canvasX = (float) convertPlanarCoordinatesX(surveyX);
            float canvasY = (float) convertPlanarCoordinatesY(surveyY);

            double dx = Math.pow(touchX - canvasX,2);
            double dy = Math.pow(touchY - canvasY,2);

            double sumCoordinates = dx + dy;

            double powerRadius = Math.pow(drawingTouchRadius,2);

            if (dx + dy < Math.pow(drawingTouchRadius,2)){
                Log.i(TAG, "checkPointForTouch: Point " + String.valueOf(pointSurvey.getPoint_no()) + " TOUCHED!");
                lstSelectedPoints.add(lstPoints.get(i));
                invalidate();
            }

        }

        return lstSelectedPoints;
    }

    public void clearPointSelection(){
        lstSelectedPoints.clear();
        invalidate();
    }





    private void drawCircle(Canvas c, float x, float y, float CIRCLE_RADIUS, Paint fillPaint, Paint strokePaint) {
        Log.d(TAG, "drawCircle: Started...");
        c.drawCircle(x, y, CIRCLE_RADIUS, fillPaint);
        c.drawCircle(x, y, CIRCLE_RADIUS, strokePaint);


    }

    private void drawTriangle(Canvas c, float x, float y, float TRIANGLE_RADIUS, Paint fillPaint, Paint strokePaint) {
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

    private void drawCross(Canvas c, float x, float y, float LINE_RADIUS, int lineColor){
        Log.d(TAG, "drawCross: Started...");
        Log.d(TAG, "drawCross: Line Radius Minimum: " + LINE_RADIUS);

        if(LINE_RADIUS < 2.5){
            LINE_RADIUS = 2.5f;
        }

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

        float lineWidth = 0.01f;
        float scaledLineWidth = lineWidth * screenDistance;

        paintLine.setStrokeWidth(scaledLineWidth);


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

    private void drawSquare(Canvas c, float x, float y, float LINE_RADIUS, int lineColor, float lineWidth){
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


    private void clear_canvas_background(){
        drawPlanarCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();

    }


}

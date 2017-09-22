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

    private Canvas drawCanvas, backgroundCanvas, drawPlanarCanvas;
    private Bitmap canvasBitmap, planarCanvas;
    private Paint canvasPaint, drawPaint, drawBackground, bitmapAlphaCanvas, canvasCheckerboard;
    private Path drawPath;

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;

    private float currentBrushSize, lastBrushSize;

    private int screenWidth, screenHeight, planarWidth, planarHeight;
    private int screenBufferY=100, screenBufferX=100;
    private double planarWidthScale, planarHeightScale, planarScale;
    private int fakeOriginX = 0, fakeOriginY = 0;

    private String databaseName;
    private ArrayList<PointSurvey> lstPoints = new ArrayList<>();
    private ArrayList<Integer> lstPointNo = new ArrayList<>();
    private ArrayList<Double> lstNorthings = new ArrayList<>();
    private ArrayList<Double> lstEasting = new ArrayList<>();

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

    private void initView(int w, int h){
        Log.d(TAG, "||initView: Started-------------------->");
        Log.d(TAG, "||Width Display: " + w);
        Log.d(TAG, "||Height Display: " + h);
        Log.d(TAG, "||initView: Ended-------------------->");


        screenWidth = w;
        screenHeight = h;

        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);


    }

    private void initPoints(){
        Log.d(TAG, "||initPoints: Started...");
        //Setup by calling in database name

        //create an array-list of points within job

        //split points into separate array-lists for Point No, Northing, Easting
        Log.d(TAG, "|initPoints: Splitting Northing and Eastings");
        for(int i=0; i<lstPoints.size(); i++) {
            PointSurvey pointSurvey = lstPoints.get(i);
            lstPointNo.add(pointSurvey.getPoint_no());
            lstNorthings.add(pointSurvey.getNorthing());
            lstEasting.add(pointSurvey.getEasting());
            Log.d(TAG, "|initPoints: Added " + pointSurvey.getPoint_no());
        }

        //determine lowest N/E values

        double minNorth = Collections.min(lstNorthings);
        double minEast = Collections.min(lstEasting);

        Log.d(TAG, "||initPoints: Started-------------------->");
        Log.d(TAG, "||initPoints: Min N: " + minNorth);
        Log.d(TAG, "||initPoints: Min E: " + minEast);

        //determine highest N/E values
        double maxNorth = Collections.max(lstNorthings);
        double maxEast = Collections.max(lstEasting);

        Log.d(TAG, "||initPoints: Max N: " + maxNorth);
        Log.d(TAG, "||initPoints: Max E: " + maxEast);

        //add buffer values to low and high

        minNorth = minNorth - screenBufferY;
        minEast = minEast - screenBufferX;

        maxNorth = maxNorth + screenBufferY;
        maxEast = maxEast + screenBufferX;

        fakeOriginX = (int) minEast;
        fakeOriginY = (int) maxNorth;

        planarWidth = (int) maxEast - (int) minEast;
        planarHeight = (int) maxNorth - (int) minNorth;

        Log.d(TAG, "||Max E - Min E: " + maxEast + "-" + minEast);
        Log.d(TAG, "||initPoints: PlanarWidth: " + planarWidth);

        Log.d(TAG, "||Max N - Min N: " + maxNorth + "-" + minNorth);
        Log.d(TAG, "||initPoints: PlanarHeight: " + planarHeight);

        //Determine false origin coordinates
        planarWidthScale = (double) screenWidth / (double) planarWidth;
        planarHeightScale = (double) screenHeight / (double) planarHeight;

        Log.d(TAG, "||initPoints: Width Scale: " + planarWidthScale);
        Log.d(TAG, "||initPoints: Height Scale: " + planarHeightScale);
        Log.d(TAG, "||initPoints: Ended-------------------->");


        if(planarWidthScale<planarHeightScale){
            planarScale = planarWidthScale;
        }else{
            planarScale = planarHeightScale;
        }


        //Create Canvas

        planarCanvas = Bitmap.createBitmap(screenWidth,screenHeight,Bitmap.Config.ARGB_8888);
        drawPlanarCanvas = new Canvas(planarCanvas);
        invalidate();



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

        for(int i=0; i<lstPoints.size(); i++) {
            PointSurvey pointSurvey = lstPoints.get(i);
            double pointNorth = pointSurvey.getNorthing();
            double deltaNorth = (fakeOriginY - pointNorth) * planarScale;
            int deltaNorthScaled = (int) deltaNorth;


            double pointEast = pointSurvey.getEasting();
            double deltaEast = (pointEast - fakeOriginX) * planarScale;
            int deltaEastScaled = (int) deltaEast;

            Log.d(TAG, "||createPointsAllPoint No. : " + pointSurvey.getPoint_no());
            Log.d(TAG, "||North: " + pointSurvey.getNorthing());
            Log.d(TAG, "||East: " + pointSurvey.getEasting());

            Log.d(TAG, "||Fake North: " + fakeOriginY);
            Log.d(TAG, "||Fake East: " + fakeOriginX);

            Log.d(TAG, "||Calc. PointNorth:  " + pointNorth);
            Log.d(TAG, "||Calc. PointEast:  " + pointEast);

            Log.d(TAG, "||initPoints: deltaNorth: " + deltaNorthScaled);
            Log.d(TAG, "||initPoints: deltaEast: " + deltaEastScaled);

            drawCircle(c, deltaEastScaled, deltaNorthScaled, 10, 1, true, drawPaint, drawPaint);
            Log.d(TAG, "|||Circle At: " + deltaEastScaled + "," + deltaNorthScaled);

        }





    }



    private void drawCircle(Canvas c, float x, float y, int CIRCLE_RADIUS, double scale, boolean pointNo, Paint fillPaint, Paint strokePaint) {

        c.drawCircle(x, y, CIRCLE_RADIUS, fillPaint);
        c.drawCircle(x, y, CIRCLE_RADIUS, strokePaint);


    }


}

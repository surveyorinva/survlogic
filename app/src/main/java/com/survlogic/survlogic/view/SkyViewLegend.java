package com.survlogic.survlogic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.GpsHelper;

/**
 * Created by chrisfillmore on 5/23/2017.
 */

public class SkyViewLegend extends View {
    private static int mWidth, mHeight;
    private float mLineWidth = 3;
    private int mTextSize = 40;
    private static int SAT_RADIUS;


    private WindowManager mWindowManager;
    private Context mContext;

    private Paint rectRedStroke,rectRedFill, rectOrangeStroke,rectOrangeFill,
            rectYellowStroke, rectYellowFill, rectLimeStroke, rectLimeFill,
            rectGreenStroke, rectGreenFill,
            rectBlackStroke,rectWhiteFill;

    private Paint txtHeader, txtValues, txtLegendHeader, txtLegendValues;

    private Rect rectHeaderBounds = new Rect();
    private Rect rectLegendBounds = new Rect();

    private RectF rectRed, rectOrange, rectYellow, rectLime, rectGreen;


    public SkyViewLegend(Context context) {
        super(context);
        initCanvas(context);
    }



    public SkyViewLegend(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initCanvas(context);
    }

    public SkyViewLegend(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCanvas(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mHeight = h;
        mWidth = w;

    }

    private void initCanvas(Context context){

        mContext = context;

        SAT_RADIUS = GpsHelper.dpToPixels(mContext, 5);

        rectRedFill = new Paint();
        rectRedFill.setStyle(Paint.Style.FILL);
        rectRedFill.setColor((ContextCompat.getColor(context, R.color.red_primary)));

        rectRedStroke = new Paint();
        rectRedStroke.setStyle(Paint.Style.STROKE);
        rectRedStroke.setColor(Color.RED);
        rectRedStroke.setStrokeWidth(mLineWidth);
        rectRedStroke.setAntiAlias(true);

        rectOrangeFill = new Paint();
        rectOrangeFill.setStyle(Paint.Style.FILL);
        rectOrangeFill.setColor((ContextCompat.getColor(context, R.color.orange_primary)));

        rectOrangeStroke = new Paint();
        rectOrangeStroke.setStyle(Paint.Style.STROKE);
        rectOrangeStroke.setColor(Color.BLUE);
        rectOrangeStroke.setStrokeWidth(mLineWidth);
        rectOrangeStroke.setAntiAlias(true);

        rectYellowFill = new Paint();
        rectYellowFill.setStyle(Paint.Style.FILL);
        rectYellowFill.setColor((ContextCompat.getColor(context, R.color.yellow_primary)));

        rectYellowStroke = new Paint();
        rectYellowStroke.setStyle(Paint.Style.STROKE);
        rectYellowStroke.setColor(Color.YELLOW);
        rectYellowStroke.setStrokeWidth(mLineWidth);
        rectYellowStroke.setAntiAlias(true);

        rectLimeFill = new Paint();
        rectLimeFill.setStyle(Paint.Style.FILL);
        rectLimeFill.setColor((ContextCompat.getColor(context, R.color.lime_primary)));

        rectLimeStroke = new Paint();
        rectLimeStroke.setStyle(Paint.Style.STROKE);
        rectLimeStroke.setColor(Color.BLACK);
        rectLimeStroke.setStrokeWidth(mLineWidth);
        rectLimeStroke.setAntiAlias(true);

        rectGreenFill = new Paint();
        rectGreenFill.setStyle(Paint.Style.FILL);
        rectGreenFill.setColor(Color.GREEN);

        rectGreenStroke = new Paint();
        rectGreenStroke.setStyle(Paint.Style.STROKE);
        rectGreenStroke.setColor(Color.GREEN);
        rectGreenStroke.setStrokeWidth(mLineWidth);
        rectGreenStroke.setAntiAlias(true);

        rectBlackStroke = new Paint();
        rectBlackStroke.setStyle(Paint.Style.STROKE);
        rectBlackStroke.setColor(Color.BLACK);
        rectBlackStroke.setStrokeWidth(mLineWidth);
        rectBlackStroke.setAntiAlias(true);

        rectWhiteFill = new Paint();
        rectWhiteFill.setStyle(Paint.Style.FILL);
        rectWhiteFill.setColor(Color.WHITE);

        txtHeader = new Paint();
        txtHeader.setColor(Color.BLACK);
        txtHeader.setStyle(Paint.Style.STROKE);
        txtHeader.setTextSize(mTextSize);
        txtHeader.setAntiAlias(true);

        txtValues = new Paint();
        txtValues.setColor(Color.BLACK);
        txtValues.setStyle(Paint.Style.STROKE);
        txtValues.setTextSize(mTextSize);
        txtValues.setAntiAlias(true);

        txtLegendHeader = new Paint();
        txtLegendHeader.setColor(Color.BLACK);
        txtLegendHeader.setStyle(Paint.Style.STROKE);
        txtLegendHeader.setTextSize(mTextSize);
        txtLegendHeader.setAntiAlias(true);

        txtLegendValues = new Paint();
        txtLegendValues.setColor(Color.BLACK);
        txtLegendValues.setStyle(Paint.Style.STROKE);
        txtLegendValues.setTextSize(mTextSize);
        txtLegendValues.setAntiAlias(true);

    }


    @Override
    protected void onDraw(Canvas canvas) {


        canvas.drawColor(ContextCompat.getColor(mContext, R.color.app_background));

        drawBar(canvas, mWidth, mHeight);
        drawLegend(canvas, mWidth, mHeight);
    }

    private void drawBar(Canvas c, int w, int h) {

        int startLeft = 80 ;
        int mbarWidth = 50;
        int mbarPaddingEnd = 50;
        int mbarPaddingTop = 15;

        int redLength = Math.round(w * 0.1f)-startLeft/5;
        int orangeLength = Math.round(w * 0.1f)-startLeft/5;
        int yellowLength = Math.round(w * 0.1f)-startLeft/5;
        int limeLength = Math.round(w * 0.2f)-startLeft/5;
        int greenLength = Math.round(w * 0.5f)-startLeft/5;

        int startTop = h/2 - mbarWidth/2 +  mbarPaddingTop;

        int endXRed = startLeft + redLength;
        int endYRed = startTop + mbarWidth;

        int startXOrange = startLeft + redLength;
        int startYOrange = startTop;
        int endXOrange = startXOrange + orangeLength;
        int endYOrange = startTop + mbarWidth;

        int startXYellow = startLeft + redLength + orangeLength;
        int startYYellow = startTop;
        int endXYellow = startXYellow + yellowLength;
        int endYYellow = startTop + mbarWidth;

        int startXLime = startLeft + redLength + orangeLength + yellowLength;
        int startYLime = startTop;
        int endXLime = startXLime + limeLength;
        int endYLime = startTop + mbarWidth;

        int startXGreen = startLeft + redLength + orangeLength + yellowLength + limeLength;
        int startYGreen = startTop;
        int endXGreen = startXGreen + greenLength-mbarPaddingEnd;
        int endYGreen = startTop + mbarWidth;

        String txtSNR = "SNR";
        String txtSNR0 = "00";
        String txtSNR10 = "10";
        String txtSNR20 = "20";
        String txtSNR30 = "30";
        String txtSNR50 = "50";
        String txtSNR100 = "99";

        txtHeader.getTextBounds(txtSNR,0, txtSNR.length(),rectHeaderBounds);
        int headerHeight = rectHeaderBounds.height();
        int headerWidth = rectHeaderBounds.width();

        int startXHeader = startLeft - headerWidth - 5;
        int startYHeader = startTop + mbarWidth/2 + headerHeight/2;

        int startYValues = startTop + mbarWidth + headerHeight + 5;

        rectRed = new RectF(startLeft, startTop, endXRed, endYRed);
        rectOrange = new RectF(startXOrange,startYOrange, endXOrange, endYOrange);
        rectYellow = new RectF(startXYellow, startYYellow, endXYellow, endYYellow);
        rectLime = new RectF(startXLime, startYLime, endXLime, endYLime);
        rectGreen = new RectF(startXGreen, startYGreen, endXGreen, endYGreen);
        

        c.drawRect(rectRed, rectRedFill);
        c.drawRect(rectOrange,rectOrangeFill);
        c.drawRect(rectYellow, rectYellowFill);
        c.drawRect(rectLime, rectLimeFill);
        c.drawRect(rectGreen, rectGreenFill);

        c.drawText(txtSNR,startXHeader,startYHeader,txtHeader);
        c.drawText(txtSNR0,startLeft,startYValues,txtValues);
        c.drawText(txtSNR10,startXOrange,startYValues,txtValues);
        c.drawText(txtSNR20,startXYellow,startYValues,txtValues);
        c.drawText(txtSNR30,startXLime,startYValues,txtValues);
        c.drawText(txtSNR50,startXGreen,startYValues,txtValues);
        c.drawText(txtSNR100,endXGreen,startYValues,txtValues);
    }

    private void drawLegend(Canvas c, int w, int h){
        int startLeft = 80 ;
        int startTop = 15 ;
        int textBuffer = 30;
        int columnBuffer = 60;

        String txtHeader = "Legend:";
        String txtSatNavstar = "NAVSTAR";
        String txtSatGlonass = "GLONASS";
        String txtSatQZSS= "QZSS";
        String txtSatBeidou = "BEIDOU";
        String txtSatGallelio = "GALILEO";


        txtLegendHeader.getTextBounds(txtHeader,0, txtHeader.length(),rectLegendBounds);
        int headerHeight = rectLegendBounds.height();
        int headerWidth = rectLegendBounds.width();
        
        int startXHeader = startLeft;
        int startYHeader = startTop + headerHeight/2+5;

        c.drawText(txtHeader,startXHeader,startYHeader,txtLegendHeader);

//        GNSS
        int circleBuffer = SAT_RADIUS/2;
        int startXValueCircle = startLeft + headerWidth + circleBuffer + textBuffer;
        int startYValueCircle = startYHeader-circleBuffer-5;

        drawCircle(c,startXValueCircle,startYValueCircle,rectWhiteFill,rectBlackStroke);

        int startXValueCircleText = startXValueCircle + textBuffer;

        txtLegendValues.getTextBounds(txtSatNavstar,0,txtSatNavstar.length(), rectLegendBounds);
        c.drawText(txtSatNavstar,startXValueCircleText,startYHeader,txtLegendValues);

//        GLONASS
        int rectangleBuffer = SAT_RADIUS/2;
        int startXValueRectangle = startXValueCircleText + rectLegendBounds.width() + columnBuffer;
        int startYValueRectangle = startYHeader - rectangleBuffer - 5;

        drawRectangle(c,startXValueRectangle,startYValueRectangle,rectWhiteFill,rectBlackStroke);

        int startXValueRectangleText = startXValueRectangle + textBuffer;

        txtLegendValues.getTextBounds(txtSatGlonass,0,txtSatNavstar.length(), rectLegendBounds);
        c.drawText(txtSatGlonass,startXValueRectangleText,startYHeader,txtLegendValues);

//          BEIDOU
        int pentagonBuffer = SAT_RADIUS/2;
        int startXValuePentagon = startXValueRectangleText + rectLegendBounds.width() + columnBuffer;
        int startYValuePentagon = startYHeader - rectangleBuffer - 7;

        drawPentagon(c,startXValuePentagon,startYValuePentagon, rectWhiteFill,rectBlackStroke);

        int startXValuePentagonText = startXValuePentagon + textBuffer;

        c.drawText(txtSatBeidou,startXValuePentagonText,startYHeader,txtLegendValues);

//        GALILEO

        int triangleBuffer = SAT_RADIUS/2;
        int startXValueTriangle = startXValuePentagonText + rectLegendBounds.width() + 10;
        int startYValueTriangle = startYHeader - triangleBuffer - 7;

        drawTriangle(c,startXValueTriangle,startYValueTriangle,rectWhiteFill,rectBlackStroke);

        int startXValueTriangleText = startXValueTriangle + textBuffer;

        c.drawText(txtSatGallelio,startXValueTriangleText,startYHeader,txtLegendValues);

    }

    private void drawCircle(Canvas c, float x, float y, Paint fillPaint, Paint strokePaint){
        c.drawCircle(x, y, SAT_RADIUS, fillPaint);
        c.drawCircle(x, y, SAT_RADIUS, strokePaint);


    }

    private void drawRectangle(Canvas c, float x, float y, Paint fillPaint, Paint strokePaint){
        c.drawRect(x - SAT_RADIUS, y - SAT_RADIUS, x + SAT_RADIUS, y + SAT_RADIUS,
                fillPaint);
        c.drawRect(x - SAT_RADIUS, y - SAT_RADIUS, x + SAT_RADIUS, y + SAT_RADIUS,
                strokePaint);
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


}

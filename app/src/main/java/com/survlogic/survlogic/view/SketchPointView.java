package com.survlogic.survlogic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.SketchFingerPath;
import com.survlogic.survlogic.PhotoEditor.utils.ImageHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 8/29/2017.
 */

public class SketchPointView extends View {

    private static final String TAG = "SketchPointView";
    private Context mContext;
    private ImageHelper imageHelper;

    private ArrayList<SketchFingerPath> paths = new ArrayList<>();
    private ArrayList<SketchFingerPath> undoPaths = new ArrayList<>();

    private Canvas drawCanvas, backgroundCanvas;
    private Bitmap canvasBitmap, backgroundImage;
    private Paint canvasPaint, drawPaint, drawBackground, bitmapAlphaCanvas, canvasCheckerboard;
    private Path drawPath;

    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final int NONE = 0;
    private static final int GRID_SMALL = 1;
    private static final int SYMBOL_TRAVERSE = 2;
    private static final int SYMBOL_MANHOLE = 3;

    private int cellWidth = 50, cellHeight = 50;
    private int cellRows = 0, cellColumns = 0;

    private int backgroundColor = DEFAULT_BG_COLOR;
    private int paintColor, backgroundAlpha = 255;
    private float currentBrushSize, lastBrushSize;
    private boolean emboss, blur;
    private MaskFilter mEmboss, mBlur;
    private boolean isTouchable = true;

    private int currentMode = 0;

    private int screenWidth, screenHeight;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;



    public SketchPointView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        imageHelper = new ImageHelper(context);
        initPaint();

    }


    private void initPaint(){
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        canvasCheckerboard = createCheckerBoard(10);


        currentBrushSize = getResources().getInteger(R.integer.small_size);
        lastBrushSize = currentBrushSize;

        paintColor = DEFAULT_COLOR;

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

        mEmboss = new EmbossMaskFilter(new float[] {1,1,1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);

        drawPaint.setColor(DEFAULT_COLOR);
        drawPaint.setStrokeWidth(currentBrushSize);

        drawPath = new Path();

    }



    private void pen_normal(){
        emboss = false;
        blur = false;
    }

    private void pen_emboss(){
        emboss = true;
        blur = false;
    }

    private void pen_blur(){
        emboss = false;
        blur = true;
    }

    private void clear_canvas(){
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        pen_normal();
        invalidate();

    }

    private void clear_canvas_background(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        backgroundCanvas = null;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initView(w,h);

    }

    private void initView(int w, int h){
        Log.d(TAG, "Width Display: " + w);
        Log.d(TAG, "Height Display: " + h);

        screenWidth = w;
        screenHeight = h;

        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

        cellRows = (screenWidth/cellWidth) + 100;
        cellColumns = (screenHeight/cellHeight) + 100;

        Log.d(TAG, "initView: Cell " + cellRows + "/" + cellColumns);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Started....>");
        
        canvas.save();
        if (backgroundCanvas != null){
            Log.d(TAG, "BackgroundCanvas is not null ");
            Log.d(TAG, "Current Alpha: " + backgroundAlpha);

            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

            bitmapAlphaCanvas.setAlpha(backgroundAlpha);
            drawCanvas.drawBitmap(backgroundImage,0,0,bitmapAlphaCanvas);

            Log.d(TAG, "Background canvas drawn");
        }else{

            switch(currentMode){
                case NONE:
                    Log.d(TAG, "onDraw: Drawing Color Only");
                    drawCanvas.drawColor(backgroundColor);
                    break;

                case GRID_SMALL:
                    Log.d(TAG, "onDraw: Drawing Grid");
                    drawCanvas.drawColor(backgroundColor);

                    for (int i = 0; i < cellRows; i++)
                    {
                        Log.d(TAG, "onDraw: Drawing " + cellRows + " rows");
                        drawCanvas.drawLine(0, i * cellHeight, screenWidth, i * cellHeight, drawBackground);
                    }

                    for (int i = 0; i < cellColumns; i++)
                    {
                        Log.d(TAG, "onDraw: Drawing " + cellColumns + " columns");
                        drawCanvas.drawLine(i * cellWidth, 0, i * cellWidth, screenHeight, drawBackground);
                    }


                    break;

                case SYMBOL_TRAVERSE:
                    drawTriangle(drawCanvas,screenWidth/2, screenHeight/2, 50, drawPaint, drawPaint);
                    break;

                case SYMBOL_MANHOLE:
                    drawCircle(drawCanvas,screenWidth/2, screenHeight/2, 100, drawPaint, drawPaint);
                    break;
            }

        }


        for (SketchFingerPath fp : paths){
            drawPaint.setColor(fp.color);
            drawPaint.setStrokeWidth(fp.strokeWidth);
            drawPaint.setMaskFilter(null);

            if (fp.emboss){
                drawPaint.setMaskFilter(mEmboss);
            }else if(fp.blur){
                drawPaint.setMaskFilter(mBlur);

            }

            drawCanvas.drawPath(fp.path,drawPaint);
        }

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

        canvas.restore();

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (isTouchable) {
            int action = event.getActionMasked();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    free_touch_start(touchX, touchY);
                    invalidate();
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.d(TAG, "onTouchEvent: Pointer Down");

                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    Log.d(TAG, "onTouchEvent: Pointer Up");

                    break;

                case MotionEvent.ACTION_MOVE:
                    free_touch_move(touchX, touchY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    free_touch_up();
                    invalidate();
                    break;
                default:
                    return false;
            }

            return true;
        }else{
            return false;
        }

    }

    private void free_touch_start(float x, float y){
        undoPaths.clear();

        drawPath = new Path();
        SketchFingerPath fp = new SketchFingerPath(paintColor, emboss, blur, currentBrushSize, drawPath);
        paths.add(fp);

        drawPath.reset();
        drawPath.moveTo(x,y);
        mX = x;
        mY = y;
    }

    private void free_touch_move(float x, float y){
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if (dx>=TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            drawPath.quadTo(mX,mY,(x+mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void free_touch_up(){
        drawPath.lineTo(mX,mY);
    }


    public void eraseAll(){
        clear_canvas_background();
        clear_canvas();
    }

    public void resetBackground(){
        clear_canvas_background();

    }

    public void onClickUndo(){
        if (paths.size()>0){
            undoPaths.add(paths.remove(paths.size()-1));
            invalidate();
        }
    }

    public void onClickRedo(){
        if (undoPaths.size()>0){
            paths.add(undoPaths.remove(undoPaths.size()-1));
            invalidate();
        }
    }

    public void setBrushSize(float newSize){
        Log.d(TAG, "setBrushSize: Received size of " + newSize);
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());

        Log.d(TAG, "setBrushSize: Pixel Size is: " + pixelAmount);

        invalidate();

        currentBrushSize = newSize;
        drawPaint.setStrokeWidth(newSize);

        Log.d(TAG, "setBrushSize: Canvas Paint updated");
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;

    }

    public int getCurrentBrushSize(){
        return (int) currentBrushSize;
    }

    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public int getCurrentAlphaValue(){
        return backgroundAlpha;
    }

    public void setAlphaValue(int alphaValue){
        backgroundAlpha = alphaValue;
        invalidate();
    }

    public int getCurrentBrushColor(){
        return paintColor;
    }

    public void setCurrentBrushColor(int paintColor){
        this.paintColor = paintColor;
    }


    public boolean isTouchable(){
        return isTouchable;
    }

    public void setTouchable(boolean isTouchable){
        this.isTouchable = isTouchable;

    }

    public void setBackgroundColor(){
        Log.d(TAG, "setBackgroundColor: Started");

        currentMode = NONE;
        backgroundImage = null;

        invalidate();

    }

    public void setBackgroundGrid(){
        Log.d(TAG, "setBackgroundGrid: Started");

        currentMode = GRID_SMALL;
        backgroundImage = null;

        invalidate();

    }

    public void setBackgroundTriangle(){
        Log.d(TAG, "setBackgroundGrid: Started");

        currentMode = SYMBOL_TRAVERSE;
        backgroundImage = null;

        invalidate();

    }

    public void setBackgroundCircle(){
        Log.d(TAG, "setBackgroundGrid: Started");

        currentMode = SYMBOL_MANHOLE;
        backgroundImage = null;

        invalidate();

    }

    public void setBackgroundImage(String imagePath){
        Log.d(TAG, "setBackgroundImage: Started");

        Bitmap originalImage = imageHelper.convertFileURLToBitmap(imagePath);

        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        Log.d(TAG, "Width Background:" + w);
        Log.d(TAG, "Height Background: " + h);

        backgroundImage = Bitmap.createScaledBitmap(originalImage,screenWidth,screenHeight,false);

        backgroundCanvas= new Canvas(backgroundImage.copy(Bitmap.Config.ARGB_8888, true));
        invalidate();

    }

    public void setBackgroundImage(Bitmap originalImage){
        Log.d(TAG, "setBackgroundImage: Started");

        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        Log.d(TAG, "Width Background:" + w);
        Log.d(TAG, "Height Background: " + h);

        backgroundImage = Bitmap.createScaledBitmap(originalImage,screenWidth,screenHeight,false);

        backgroundCanvas= new Canvas(backgroundImage.copy(Bitmap.Config.ARGB_8888, true));
        invalidate();
    }

    private void drawTriangle(Canvas c, float x, float y, int TRI_RADIUS, Paint fillPaint, Paint strokePaint) {
        float x1, y1;  // Top
        x1 = x;
        y1 = y - TRI_RADIUS;

        float x2, y2; // Lower left
        x2 = x - TRI_RADIUS;
        y2 = y + TRI_RADIUS;

        float x3, y3; // Lower right
        x3 = x + TRI_RADIUS;
        y3 = y + TRI_RADIUS;

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

    private void drawCircle(Canvas c, float x, float y, int CIRCLE_RADIUS, Paint fillPaint, Paint strokePaint) {

        c.drawCircle(x, y, CIRCLE_RADIUS, fillPaint);
        c.drawCircle(x, y, CIRCLE_RADIUS, strokePaint);


    }

    private Paint createCheckerBoard(int pixelSize)
    {
        Bitmap bitmap = Bitmap.createBitmap(pixelSize * 2, pixelSize * 2, Bitmap.Config.ARGB_8888);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(0x22000000);

        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, pixelSize, pixelSize);
        canvas.drawRect(rect, fill);
        rect.offset(pixelSize, pixelSize);
        canvas.drawRect(rect, fill);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT));
        return paint;
    }

}

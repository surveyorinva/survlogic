package com.survlogic.survlogic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.SketchInterfaceListener;
import com.survlogic.survlogic.model.SketchFingerPath;
import com.survlogic.survlogic.utils.ImageHelper;

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
    private Paint canvasPaint, drawPaint;
    private Path drawPath;

    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int paintColor;
    private float currentBrushSize, lastBrushSize;
    private boolean emboss, blur;
    private MaskFilter mEmboss, mBlur;
    private boolean isTouchable = true;

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

        currentBrushSize = getResources().getInteger(R.integer.small_size);
        lastBrushSize = currentBrushSize;

        paintColor = DEFAULT_COLOR;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Started....>");
        
        canvas.save();
        if (backgroundCanvas != null){
            Log.d(TAG, "BackgroundCanvas is not null ");
            drawCanvas.drawBitmap(backgroundImage,0,0,null);

            Log.d(TAG, "Background canvas drawn");
        }else{
            drawCanvas.drawColor(backgroundColor);
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
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
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

}

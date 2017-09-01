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
import android.view.View;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.SketchFingerPath;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 8/29/2017.
 */

public class SketchPointView extends View {

    private static final String TAG = "SketchPointView";
    private Context mContext;

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undoPaths = new ArrayList<>();

    private SketchFingerPath fp;

    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Paint canvasPaint, drawPaint;
    private Path drawPath;

    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int paintColor;
    private float currentBrushSize, lastBrushSize;
    private boolean emboss, blur;
    private MaskFilter mEmboss, mBlur;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public SketchPointView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initPaint();

    }


    private void initPaint(){
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        currentBrushSize = getResources().getInteger(R.integer.small_size);
        lastBrushSize = currentBrushSize;

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

    private void initView(DisplayMetrics metrics){
        int h = metrics.heightPixels;
        int w = metrics.widthPixels;

        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
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
    protected void onDraw(Canvas canvas) {
        canvas.save();
        drawCanvas.drawColor(backgroundColor);

        for (Path p:paths){
            canvas.drawPath(p,drawPaint);
        }
        canvas.drawPath(drawPath,drawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                free_touch_start(touchX,touchY);
                invalidate();
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
    }

    private void free_touch_start(float x, float y){
        undoPaths.clear();
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
        drawCanvas.drawPath(drawPath,drawPaint);
        paths.add(drawPath);
        drawPath = new Path();
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

        currentBrushSize = pixelAmount;
        drawPaint.setStrokeWidth(newSize);

        Log.d(TAG, "setBrushSize: Canvas Paint updated");
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;

    }

    public float getLastBurshSize(){
        return lastBrushSize;
    }

}

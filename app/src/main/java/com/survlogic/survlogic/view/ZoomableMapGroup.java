package com.survlogic.survlogic.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.MapZoomListener;
import com.survlogic.survlogic.model.SketchFingerPath;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by chrisfillmore on 9/4/2017.
 */

public class ZoomableMapGroup extends ViewGroup {

    private static final String TAG = "ZoomableViewGroup";
    private Context mContext;

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix matrixInverse = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int SELECT = 3;
    private int mode = NONE;
    private int historyMode = NONE;
    private ArrayList<Integer> historyArray;

    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float[] lastEvent = null;

    private boolean initZoomApplied=false;
    private boolean isZooming = false;
    
    private float[] mDispatchTouchEventWorkingArray = new float[2];
    private float[] mOnTouchEventWorkingArray = new float[2];

    private float overallScale = 1;
    private AttributeSet attrs;
    private PlanarMapView planarMapView;
    private PlanarMapScaleView planarMapScaleView;

    private Rect clipBounds_canvas;
    private MapZoomListener mapZoomListener;

    private float lastTouchX, lastTouchY;

    //version 2
    private boolean isZoomMode = true;

    private ArrayList<SketchFingerPath> paths = new ArrayList<>();
    private ArrayList<SketchFingerPath> undoPaths = new ArrayList<>();
    private Path drawFencePath;
    private float mX, mY, mOriginX, mOriginY;
    private static final float TOUCH_TOLERANCE = 4;

    private Paint drawFencePaint;
    private int paintFenceColor, backgroundAlpha = 255;
    private float currentBrushSize;
    private MaskFilter mFenceEmboss, mFenceBlur;
    private boolean embossFence, blurFence;
    private Canvas selectionCanvas;

    RectF canvasRectF;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDispatchTouchEventWorkingArray[0] = ev.getX();
        mDispatchTouchEventWorkingArray[1] = ev.getY();
        mDispatchTouchEventWorkingArray = screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);
        ev.setLocation(mDispatchTouchEventWorkingArray[0],
                mDispatchTouchEventWorkingArray[1]);
        return super.dispatchTouchEvent(ev);
    }

    private float[] scaledPointsToScreenPoints(float[] a) {
        matrix.mapPoints(a);
        return a;
    }

    private float[] screenPointsToScaledPoints(float[] a){
        matrixInverse.mapPoints(a);
        return a;
    }

    public ZoomableMapGroup(Context context) {
        super(context);
        this.mContext = context;
        init(context);

    }

    public ZoomableMapGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        this.mContext = context;
        init(context);
    }

    public ZoomableMapGroup(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
        init(context);
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    private void init(Context context){
        planarMapView = new PlanarMapView(context, attrs);
        planarMapScaleView = new PlanarMapScaleView(context,attrs);

        historyArray = new ArrayList<>();
        historyArray.add(NONE);

        initFencePaint();
    }

    public void setOnMapZoomListener(MapZoomListener listener){
        mapZoomListener = listener;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.layout(l, t, l+child.getMeasuredWidth(), t + child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float[] values = new float[9];
        matrix.getValues(values);
        float container_width = values[Matrix.MSCALE_X]*widthSize;
        float container_height = values[Matrix.MSCALE_Y]*heightSize;

        //Log.d("zoomToFit", "m width: "+container_width+" m height: "+container_height);
        //Log.d("zoomToFit", "m x: "+pan_x+" m y: "+pan_y);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);

                if(i==0 && !initZoomApplied && child.getWidth()>0){
                    int c_w = child.getWidth();
                    int c_h = child.getHeight();

                    //zoomToFit(c_w, c_h, container_width, container_height);
                }
            }
        }

    }

    private void zoomToFit(int c_w, int c_h, float container_width, float container_height){
        float proportion_firstChild = (float)c_w/(float)c_h;
        float proportion_container = container_width/container_height;

        //Log.d("zoomToFit", "firstChildW: "+c_w+" firstChildH: "+c_h);
        //Log.d("zoomToFit", "proportion-container: "+proportion_container);
        //Log.d("zoomToFit", "proportion_firstChild: "+proportion_firstChild);

        if(proportion_container<proportion_firstChild){
            float initZoom = container_height/c_h;
            //Log.d("zoomToFit", "adjust height with initZoom: "+initZoom);
            matrix.postScale(initZoom, initZoom);
            matrix.postTranslate(-1*(c_w*initZoom-container_width)/2, 0);
            matrix.invert(matrixInverse);
        }else {
            float initZoom = container_width/c_w;
            //Log.d("zoomToFit", "adjust width with initZoom: "+initZoom);
            matrix.postScale(initZoom, initZoom);
            matrix.postTranslate(0, -1*(c_h*initZoom-container_height)/2);
            matrix.invert(matrixInverse);
        }
        initZoomApplied=true;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.setMatrix(matrix);
        super.dispatchDraw(canvas);
        clipBounds_canvas = canvas.getClipBounds();

        selectionCanvas = canvas;

        for (SketchFingerPath fp : paths){
            drawFencePaint.setColor(fp.color);
            drawFencePaint.setStrokeWidth(fp.strokeWidth);
            drawFencePaint.setMaskFilter(null);

            if (fp.emboss){
                drawFencePaint.setMaskFilter(mFenceEmboss);
            }else if(fp.blur){
                drawFencePaint.setMaskFilter(mFenceBlur);

            }

            selectionCanvas.drawPath(fp.path,drawFencePaint);


        }

        //QC Check of bounds rectangle
//        if(canvasRectF !=null){
//            selectionCanvas.drawRect(canvasRectF,drawFencePaint);
//        }

        canvas.restore();
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isZoomMode) {
            // handle touch events here
            mOnTouchEventWorkingArray[0] = event.getX();
            mOnTouchEventWorkingArray[1] = event.getY();

            mOnTouchEventWorkingArray = scaledPointsToScreenPoints(mOnTouchEventWorkingArray);

            event.setLocation(mOnTouchEventWorkingArray[0], mOnTouchEventWorkingArray[1]);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    lastEvent = null;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    lastEvent = new float[4];
                    lastEvent[0] = event.getX(0);
                    lastEvent[1] = event.getX(1);
                    lastEvent[2] = event.getY(0);
                    lastEvent[3] = event.getY(1);
                    //d = rotation(event);
                    break;
                case MotionEvent.ACTION_UP:

                    convertCoordinatesToCanvas(event.getX(),event.getY());

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    lastEvent = null;
                    mapZoomListener.onTouchOnPoint(lastTouchX, lastTouchY);

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {

                        matrix.set(savedMatrix);
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;

                        double touchRadius = Math.sqrt((dx * dx) + (dy * dy));


                        matrix.postTranslate(dx, dy);
                        matrix.invert(matrixInverse);

                        Log.i(TAG, "LOOK AT ME PAN: " + dx + "," + dy );
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = (newDist / oldDist);

                            //planMapScaleDistance = planarMapView.getMapScale(clipBounds_canvas);

                            matrix.postScale(scale, scale, mid.x, mid.y);
                            matrix.invert(matrixInverse);

                            Log.i(TAG, "LOOK AT ME ZOOM: " + scale + ":" + mid.x + ", " + mid.y );
                        }
                    }
                    break;
            }

            invalidate();

            mapZoomListener.onReturnValues(clipBounds_canvas);
        }else{
            float touchX = event.getX();
            float touchY = event.getY();

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

        }

        return true;
    }

    public void setTouchable(boolean isTouchable){
        this.isZoomMode = isTouchable;
    }

    public boolean isTouchable(){
        return this.isZoomMode;
    }

    private void free_touch_start(float x, float y){
        undoPaths.clear();
        drawFencePath = new Path();
        SketchFingerPath fp = new SketchFingerPath(paintFenceColor, embossFence, blurFence, currentBrushSize, drawFencePath);
        paths.add(fp);

        drawFencePath.reset();
        drawFencePath.moveTo(x,y);
        mX = x;
        mY = y;

        mOriginX = mX;
        mOriginY = mY;
    }

    private void free_touch_move(float x, float y){
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if (dx>=TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            drawFencePath.quadTo(mX,mY,(x+mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void free_touch_up(){
        drawFencePath.lineTo(mX,mY);
        drawFencePath.lineTo(mOriginX,mOriginY);

        RectF bounds = new RectF();
        drawFencePath.computeBounds(bounds, false); // fills rect with bounds
        Log.i(TAG, "convertRectToCanvas: Bounds on Screen: " + bounds.top + "," + bounds.bottom + "," + bounds.left + ", " + bounds.right);

        canvasRectF = convertPathToCanvas(drawFencePath);

        mapZoomListener.onFenceAround(drawFencePath, canvasRectF);

    }

    private void initFencePaint(){
        currentBrushSize = getResources().getInteger(R.integer.selection_fence_size);
        paintFenceColor = Color.WHITE;

        drawFencePaint = new Paint();

        drawFencePaint.setAntiAlias(true);
        drawFencePaint.setDither(true);
        drawFencePaint.setStyle(Paint.Style.STROKE);
        drawFencePaint.setStrokeJoin(Paint.Join.ROUND);
        drawFencePaint.setStrokeCap(Paint.Cap.ROUND);
        drawFencePaint.setPathEffect(new DashPathEffect(new float[]{5,20},0));

        mFenceEmboss = new EmbossMaskFilter(new float[] {1,1,1}, 0.4f, 6, 3.5f);
        mFenceBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);

        drawFencePaint.setColor(paintFenceColor);
        drawFencePaint.setStrokeWidth(currentBrushSize);

        drawFencePath = new Path();

        select_normal();

    }

    private void select_normal(){
        embossFence = false;
        blurFence = false;
    }

    public void clearFenceSelection(){
        if (paths.size()>0){
            undoPaths.add(paths.remove(paths.size()-1));
            invalidate();
        }

    }

    private void convertCoordinatesToCanvas(float zoomX, float zoomY){
        float[] m = new float[9];
        matrix.getValues(m);

        Log.i(TAG, "convertCoordinatesToCanvas: Bounds on Screen: " + zoomX + ", " + zoomY);

        float transX = m[Matrix.MTRANS_X] * -1;
        float transY = m[Matrix.MTRANS_Y] * -1;
        float scaleX = m[Matrix.MSCALE_X];
        float scaleY = m[Matrix.MSCALE_Y];

        lastTouchX = ((zoomX + transX) / scaleX);
        lastTouchY = ((zoomY + transY) / scaleY);

        lastTouchX = Math.abs(lastTouchX);
        lastTouchY = Math.abs(lastTouchY);

        Log.i(TAG, "convertCoordinatesToCanvas: Bounds on Canvas: " + lastTouchX + ", " + lastTouchY);
    }

    private RectF convertPathToCanvas(Path path){
        float[] m = new float[9];
        matrix.getValues(m);

        float scaleX = m[Matrix.MSCALE_X];
        float scaleY = m[Matrix.MSCALE_Y];

        Matrix scaleMatrix = new Matrix();

        RectF bounds = new RectF();
        path.computeBounds(bounds, false); // fills rect with bounds

        scaleMatrix.setScale(scaleX, scaleY, bounds.centerX(), bounds.centerY());
        path.transform(scaleMatrix);

        float boundsLeft = bounds.left;
        float boundsTop = bounds.top;
        float boundsRight = bounds.right;
        float boundsBottom = bounds.bottom;
        Log.i(TAG, "convertRectToCanvas: Bounds on Canvas: " + boundsTop + "," + boundsBottom + "," + boundsLeft + ", " + boundsRight);

        return new RectF(boundsLeft,boundsTop,boundsRight,boundsBottom);

    }

    public void zoomToPoint(){
        Log.d(TAG, "zoomToPoint: Started");
        matrix.postScale(3.7887287f, 3.7887287f, 678.33984f, 1188.6875f);
    }





}
package com.survlogic.survlogic.interf;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by chrisfillmore on 9/30/2017.
 */

public interface MapZoomListener {


    void onReturnValues(Rect zoomRect);

    void onTouchOnPoint(float X, float Y);

    void onFenceAround(Path fencePath, RectF fenceRect);

}

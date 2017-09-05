package com.survlogic.survlogic.model;

import android.graphics.Path;

/**
 * Created by chrisfillmore on 9/1/2017.
 */

public class SketchFingerPath {

    public int color;
    public boolean emboss;
    public boolean blur;
    public float strokeWidth;
    public Path path;


    public SketchFingerPath(int color, boolean emboss, boolean blur, float strokeWidth, Path path) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}

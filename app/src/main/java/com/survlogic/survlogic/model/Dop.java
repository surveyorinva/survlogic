package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 5/18/2017.
 */

public class Dop {
    double mPositionDop;

    double mHorizontalDop;

    double mVerticalDop;

    public Dop(double positionDop, double horizontalDop, double verticalDop) {
        this.mPositionDop = positionDop;
        this.mHorizontalDop = horizontalDop;
        this.mVerticalDop = verticalDop;
    }

    public double getPositionDop() {
        return mPositionDop;
    }

    public void setPositionDop(double positionDop) {
        this.mPositionDop = positionDop;
    }

    public double getHorizontalDop() {
        return mHorizontalDop;
    }

    public void setHorizontalDop(double horizontalDop) {
        this.mHorizontalDop = horizontalDop;
    }

    public double getVerticalDop() {
        return mVerticalDop;
    }

    public void setVerticalDop(double verticalDop) {
        this.mVerticalDop = verticalDop;
    }
}



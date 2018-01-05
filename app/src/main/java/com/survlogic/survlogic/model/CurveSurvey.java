package com.survlogic.survlogic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by chrisfillmore on 12/30/2017.
 */

public class CurveSurvey implements Parcelable {
    private static final String TAG = "CurveSurvey";

    private double radius,deltaDEC,length,tangent;
    private double chord, directionDEC;
    private boolean isCurveRight;

    public CurveSurvey() {
    }

    private CurveSurvey(Parcel in){
        this.isCurveRight = in.readByte() !=0;
        this.deltaDEC = in.readDouble();
        this.radius = in.readDouble();
        this.length = in.readDouble();
        this.tangent = in.readDouble();
        this.chord = in.readDouble();
        this.directionDEC = in.readDouble();
    }


    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getDeltaDEC() {
        return deltaDEC;
    }

    public void setDeltaDEC(double deltaDEC) {
        this.deltaDEC = deltaDEC;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getTangent() {
        return tangent;
    }

    public void setTangent(double tangent) {
        this.tangent = tangent;
    }

    public double getChord() {
        return chord;
    }

    public void setChord(double chord) {
        this.chord = chord;
    }

    public double getDirectionDEC() {
        return directionDEC;
    }

    public void setDirectionDEC(double directionDEC) {
        this.directionDEC = directionDEC;
    }


    public boolean getIsCurveRight() {
        return isCurveRight;
    }

    public void setIsCurveRight(boolean isCurveRight) {
        this.isCurveRight = isCurveRight;
    }

    //----------------------------------------------------------------------------------------------//
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel: Started");

        dest.writeByte((byte) (isCurveRight ? 1 : 0));
        dest.writeDouble(deltaDEC);
        dest.writeDouble(radius);
        dest.writeDouble(length);
        dest.writeDouble(tangent);
        dest.writeDouble(chord);
        dest.writeDouble(directionDEC);

    }

    public static final Parcelable.Creator<CurveSurvey> CREATOR = new Parcelable.Creator<CurveSurvey>(){

        @Override
        public CurveSurvey createFromParcel(Parcel parcel) {
            return new CurveSurvey(parcel);
        }

        @Override
        public CurveSurvey[] newArray(int size) {
            return new CurveSurvey[0];
        }
    };
}

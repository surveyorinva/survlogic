package com.survlogic.survlogic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by chrisfillmore on 12/20/2017.
 */

public class PointMapCheck implements Parcelable{
    private static final String TAG = "PointMapCheck";

    int observationType, toPointNo, fromPointNo;
    String pointDescription;
    double lineAngle, lineDistance;
    double curveDelta, curveRadius, curveLength, curveAngle, curveChord;

    double toPointNorth, toPointEast;

    public PointMapCheck() {
    }

    public PointMapCheck(PointMapCheck pointMapCheck){
        this.observationType = pointMapCheck.getObservationType();
        this.toPointNo = pointMapCheck.getToPointNo();
        this.pointDescription = pointMapCheck.getPointDescription();
        this.lineAngle = pointMapCheck.getLineAngle();
        this.lineDistance = pointMapCheck.getLineDistance();

        this.curveDelta = pointMapCheck.getCurveDelta();
        this.curveRadius = pointMapCheck.getCurveRadius();
        this.curveLength = pointMapCheck.getCurveLength();
        this.curveAngle = pointMapCheck.getCurveAngle();
        this.curveChord = pointMapCheck.getCurveChord();
    }

    private PointMapCheck(Parcel in){
        this.observationType = in.readInt();
        this.toPointNo = in.readInt();
        this.pointDescription = in.readString();

        this.lineAngle = in.readDouble();
        this.lineDistance = in.readDouble();

        this.curveDelta = in.readDouble();
        this.curveRadius = in.readDouble();
        this.curveLength = in.readDouble();
        this.curveAngle = in.readDouble();
        this.curveChord = in.readDouble();
    }

    public PointMapCheck(int observationType, int toPointNo, String pointDescription, double lineAngle, double lineDistance) {
        this.observationType = observationType;
        this.toPointNo = toPointNo;
        this.pointDescription = pointDescription;
        this.lineAngle = lineAngle;
        this.lineDistance = lineDistance;
    }

    public PointMapCheck(int observationType, int toPointNo, String pointDescription, double curveDelta, double curveRadius, double curveLength, double curveAngle, double curveChord) {
        this.observationType = observationType;
        this.toPointNo = toPointNo;
        this.pointDescription = pointDescription;
        this.curveDelta = curveDelta;
        this.curveRadius = curveRadius;
        this.curveLength = curveLength;
        this.curveAngle = curveAngle;
        this.curveChord = curveChord;
    }

    public int getObservationType() {
        return observationType;
    }

    public void setObservationType(int observationType) {
        this.observationType = observationType;
    }

    public int getToPointNo() {
        return toPointNo;
    }

    public void setToPointNo(int toPointNo) {
        this.toPointNo = toPointNo;
    }

    public int getFromPointNo() {
        return fromPointNo;
    }

    public void setFromPointNo(int fromPointNo) {
        this.fromPointNo = fromPointNo;
    }

    public String getPointDescription() {
        return pointDescription;
    }

    public void setPointDescription(String pointDescription) {
        this.pointDescription = pointDescription;
    }

    public double getLineAngle() {
        return lineAngle;
    }

    public void setLineAngle(double lineAngle) {
        this.lineAngle = lineAngle;
    }

    public double getLineDistance() {
        return lineDistance;
    }

    public void setLineDistance(double lineDistance) {
        this.lineDistance = lineDistance;
    }

    public double getCurveDelta() {
        return curveDelta;
    }

    public void setCurveDelta(double curveDelta) {
        this.curveDelta = curveDelta;
    }

    public double getCurveRadius() {
        return curveRadius;
    }

    public void setCurveRadius(double curveRadius) {
        this.curveRadius = curveRadius;
    }

    public double getCurveLength() {
        return curveLength;
    }

    public void setCurveLength(double curveLength) {
        this.curveLength = curveLength;
    }

    public double getCurveAngle() {
        return curveAngle;
    }

    public void setCurveAngle(double curveAngle) {
        this.curveAngle = curveAngle;
    }

    public double getCurveChord() {
        return curveChord;
    }

    public void setCurveChord(double curveChord) {
        this.curveChord = curveChord;
    }

    public double getToPointNorth() {
        return toPointNorth;
    }

    public void setToPointNorth(double toPointNorth) {
        this.toPointNorth = toPointNorth;
    }

    public double getToPointEast() {
        return toPointEast;
    }

    public void setToPointEast(double toPointEast) {
        this.toPointEast = toPointEast;
    }


    //-----------------------------------------------------------------------------------------------//Parceable Functionality


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel: Started");
        dest.writeInt(observationType);
        dest.writeInt(toPointNo);
        dest.writeString(pointDescription);

        dest.writeDouble(lineAngle);
        dest.writeDouble(lineDistance);

        dest.writeDouble(curveDelta);
        dest.writeDouble(curveRadius);
        dest.writeDouble(curveLength);
        dest.writeDouble(curveAngle);
        dest.writeDouble(curveChord);
    }

    public static final Parcelable.Creator<PointMapCheck> CREATOR = new Parcelable.Creator<PointMapCheck>() {
        @Override
        public PointMapCheck createFromParcel(Parcel source) {
            return new PointMapCheck(source);

        }

        @Override
        public PointMapCheck[] newArray(int size) {
            return new PointMapCheck[size];
        }
    };

}

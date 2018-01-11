package com.survlogic.survlogic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by chrisfillmore on 12/20/2017.
 */

public class PointMapCheck implements Parcelable{
    private static final String TAG = "PointMapCheck";

    private int observationType, toPointNo, fromPointNo;
    private String pointDescription;
    private double lineAngle, lineDistance;
    private double curveDelta, curveRadius, curveLength, curveAngle, curveChord;
    private boolean isCurveToRight, isClosingPoint;
    private double toPointNorth, toPointEast;

    private boolean isEdit;

    public PointMapCheck() {
    }

    public PointMapCheck(PointMapCheck pointMapCheck){
        this.observationType = pointMapCheck.getObservationType();
        this.toPointNo = pointMapCheck.getToPointNo();
        this.pointDescription = pointMapCheck.getPointDescription();
        this.lineAngle = pointMapCheck.getLineAngle();
        this.lineDistance = pointMapCheck.getLineDistance();

        this.isCurveToRight = pointMapCheck.isCurveToRight();
        this.curveDelta = pointMapCheck.getCurveDelta();
        this.curveRadius = pointMapCheck.getCurveRadius();
        this.curveLength = pointMapCheck.getCurveLength();
        this.curveAngle = pointMapCheck.getCurveAngle();
        this.curveChord = pointMapCheck.getCurveChord();

        this.isClosingPoint = pointMapCheck.isClosingPoint();

        this.isEdit = pointMapCheck.isEdit();

    }

    private PointMapCheck(Parcel in){
        this.observationType = in.readInt();
        this.toPointNo = in.readInt();
        this.pointDescription = in.readString();

        this.lineAngle = in.readDouble();
        this.lineDistance = in.readDouble();

        this.isCurveToRight = in.readByte() !=0;
        this.curveDelta = in.readDouble();
        this.curveRadius = in.readDouble();
        this.curveLength = in.readDouble();
        this.curveAngle = in.readDouble();
        this.curveChord = in.readDouble();

        this.isClosingPoint = in.readByte() !=0;

        this.isEdit = in.readByte() !=0;
    }

    public PointMapCheck(int observationType, int toPointNo, String pointDescription, double lineAngle, double lineDistance, boolean isClosingPoint) {
        this.observationType = observationType;
        this.toPointNo = toPointNo;
        this.pointDescription = pointDescription;
        this.lineAngle = lineAngle;
        this.lineDistance = lineDistance;
        this.isClosingPoint = isClosingPoint;
    }

    public PointMapCheck(int observationType, int toPointNo, String pointDescription, boolean isCurveToRight, double curveDelta, double curveRadius, double curveLength, double curveAngle, double curveChord, boolean isClosingPoint) {
        this.observationType = observationType;
        this.toPointNo = toPointNo;
        this.pointDescription = pointDescription;
        this.isCurveToRight = isCurveToRight;
        this.curveDelta = curveDelta;
        this.curveRadius = curveRadius;
        this.curveLength = curveLength;
        this.curveAngle = curveAngle;
        this.curveChord = curveChord;
        this.isClosingPoint = isClosingPoint;
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

    public boolean isCurveToRight() {
        return isCurveToRight;
    }

    public void setCurveToRight(boolean curveToRight) {
        isCurveToRight = curveToRight;
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

    public boolean isClosingPoint() {
        return isClosingPoint;
    }

    public void setClosingPoint(boolean closingPoint) {
        isClosingPoint = closingPoint;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
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

        dest.writeByte((byte) (isCurveToRight ? 1 : 0));
        dest.writeDouble(curveDelta);
        dest.writeDouble(curveRadius);
        dest.writeDouble(curveLength);
        dest.writeDouble(curveAngle);
        dest.writeDouble(curveChord);

        dest.writeByte((byte) (isClosingPoint ? 1: 0 ));

        dest.writeByte((byte) (isEdit ? 1:0));
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

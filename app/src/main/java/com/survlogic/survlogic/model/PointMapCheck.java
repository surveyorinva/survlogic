package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 12/20/2017.
 */

public class PointMapCheck {

    int observationType, newPointNo;
    String pointDescription;
    double lineAngle, lineDistance;
    double curveDelta, curveRadius, curveLength, curveAngle, curveChord;

    public PointMapCheck() {
    }

    public PointMapCheck(int observationType, int newPointNo, String pointDescription, double lineAngle, double lineDistance) {
        this.observationType = observationType;
        this.newPointNo = newPointNo;
        this.pointDescription = pointDescription;
        this.lineAngle = lineAngle;
        this.lineDistance = lineDistance;
    }

    public PointMapCheck(int observationType, int newPointNo, String pointDescription, double curveDelta, double curveRadius, double curveLength, double curveAngle, double curveChord) {
        this.observationType = observationType;
        this.newPointNo = newPointNo;
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

    public int getNewPointNo() {
        return newPointNo;
    }

    public void setNewPointNo(int newPointNo) {
        this.newPointNo = newPointNo;
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
}

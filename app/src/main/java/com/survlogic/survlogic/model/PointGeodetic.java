package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 6/17/2017.
 */

public class PointGeodetic extends PointSurvey{
    private double latitude, longitude, ellipsoid, ortho, accuracy;
    private int pointGeodeticType;

    public PointGeodetic(){

    }


    public PointGeodetic(double latitude, double longitude, double ellipsoid, int pointGeodeticType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.ellipsoid = ellipsoid;
        this.pointGeodeticType = pointGeodeticType;
    }


    public PointGeodetic(double latitude, double longitude, double ellipsoid, double accuracy, int pointGeodeticType){
        this.latitude = latitude;
        this.longitude = longitude;
        this.ellipsoid = ellipsoid;
        this.accuracy = accuracy;
        this.pointGeodeticType = pointGeodeticType;

    }

    public PointGeodetic(double latitude, double longitude, double ellipsoid, double ortho, double accuracy, int pointGeodeticType){
        this.latitude = latitude;
        this.longitude = longitude;
        this.ellipsoid = ellipsoid;
        this.ortho = ortho;
        this.accuracy = accuracy;
        this.pointGeodeticType = pointGeodeticType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getEllipsoid() {
        return ellipsoid;
    }

    public void setEllipsoid(double ellipsoid) {
        this.ellipsoid = ellipsoid;
    }


    public double getOrtho() {
        return ortho;
    }

    public void setOrtho(double ortho) {
        this.ortho = ortho;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public int getPointGeodeticType() {
        return pointGeodeticType;
    }

    public void setPointGeodeticType(int pointGeodeticType) {
        this.pointGeodeticType = pointGeodeticType;
    }
}

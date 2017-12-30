package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 12/7/2017.
 */

public class Point {
    private double northing, easting, elevation;

    public Point(){

    }

    public Point(Point point){
        this.northing = point.getNorthing();
        this.easting = point.getEasting();
        this.elevation = point.getElevation();
    }

    public Point(double northing, double easting) {
        this.northing = northing;
        this.easting = easting;
    }

    public Point(double northing, double easting, double elevation) {
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
    }

    public double getNorthing() {
        return northing;
    }

    public void setNorthing(double northing) {
        this.northing = northing;
    }

    public double getEasting() {
        return easting;
    }

    public void setEasting(double easting) {
        this.easting = easting;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }
}

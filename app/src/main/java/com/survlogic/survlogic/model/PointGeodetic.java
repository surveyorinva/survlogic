package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 6/17/2017.
 */

public class PointGeodetic {
    private double latitude, longitude, ellipsoid;

    public PointGeodetic(double latitude, double longitude, double ellipsoid) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.ellipsoid = ellipsoid;
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

}

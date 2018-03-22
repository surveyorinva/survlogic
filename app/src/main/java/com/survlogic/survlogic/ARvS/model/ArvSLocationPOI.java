package com.survlogic.survlogic.ARvS.model;

/**
 * Created by chrisfillmore on 3/16/2018.
 */

public class ArvSLocationPOI {

    private int mId;
    private String mName;
    private String mDescription;
    private double mLatitude;
    private double mLongitude;


    public ArvSLocationPOI() {
    }

    public ArvSLocationPOI(String mName, String mDescription, double mLatitude, double mLongitude) {
        this.mName = mName;
        this.mDescription = mDescription;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}

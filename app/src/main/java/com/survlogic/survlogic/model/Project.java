package com.survlogic.survlogic.model;

import org.parceler.Parcel;

/**
 * Created by chrisfillmore on 6/25/2017.
 */

@Parcel
public class Project {

    String mProjectName;
    int mId, mStorage, mUnits;
    int mProjection, mZone, mSystemImage;
    int mDateCreated, mDateAccessed, mDateModified;
    double mLocationLat, mLocationLong;
    byte[] mImage;


    public Project() {

    }

    public Project(String mProjectName, int mStorage, int mUnits, int mProjection, int mZone, double mLocationLat, double mLocationLong, int mSystemImage, byte[] mImage) {
        this.mProjectName = mProjectName;
        this.mStorage = mStorage;
        this.mUnits = mUnits;
        this.mProjection = mProjection;
        this.mZone = mZone;
        this.mLocationLat = mLocationLat;
        this.mLocationLong = mLocationLong;
        this.mImage = mImage;
    }

    public Project(String mProjectName, int mStorage, int mUnits, int mProjection, int mZone, double mLocationLat, double mLocationLong, int mSystemImage, byte[] mImage, int mDateCreated) {
        this.mProjectName = mProjectName;
        this.mStorage = mStorage;
        this.mUnits = mUnits;
        this.mProjection = mProjection;
        this.mZone = mZone;
        this.mLocationLat = mLocationLat;
        this.mLocationLong = mLocationLong;
        this.mImage = mImage;
        this.mDateCreated = mDateCreated;
    }

    public Project(int mId, String mProjectName, int mStorage, int mUnits, int mProjection, int mZone, double mLocationLat, double mLocationLong, int mSystemImage, byte[] mImage) {
        this.mProjectName = mProjectName;
        this.mId = mId;
        this.mStorage = mStorage;
        this.mUnits = mUnits;
        this.mProjection = mProjection;
        this.mZone = mZone;
        this.mLocationLat = mLocationLat;
        this.mLocationLong = mLocationLong;
        this.mImage = mImage;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }


    public String getmProjectName() {
        return mProjectName;
    }

    public void setmProjectName(String mProjectName) {
        this.mProjectName = mProjectName;
    }


    public int getmStorage() {
        return mStorage;
    }

    public void setmStorage(int mStorage) {
        this.mStorage = mStorage;
    }

    public int getmUnits() {
        return mUnits;
    }

    public void setmUnits(int mUnits) {
        this.mUnits = mUnits;
    }

    public int getmProjection() {
        return mProjection;
    }

    public void setmProjection(int mProjection) {
        this.mProjection = mProjection;
    }

    public int getmZone() {
        return mZone;
    }

    public void setmZone(int mZone) {
        this.mZone = mZone;
    }

    public double getmLocationLat() {
        return mLocationLat;
    }

    public void setmLocationLat(double mLocationLat) {
        this.mLocationLat = mLocationLat;
    }

    public double getmLocationLong() {
        return mLocationLong;
    }

    public void setmLocationLong(double mLocationLong) {
        this.mLocationLong = mLocationLong;
    }

    public int getmSystemImage() {
        return mSystemImage;
    }

    public void setmSystemImage(int mSystemImage) {
        this.mSystemImage = mSystemImage;
    }

    public byte[] getmImage() {
        return mImage;
    }

    public void setmImage(byte[] mImage) {
        this.mImage = mImage;
    }

    public int getmDateCreated() {
        return mDateCreated;
    }

    public void setmDateCreated(int mDateCreated) {
        this.mDateCreated = mDateCreated;
    }
}

package com.survlogic.survlogic.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by chrisfillmore on 6/25/2017.
 */

public class Project implements Parcelable {
    private static final String TAG = "Project";

    private String mProjectName, mProjectDescription, mImagePath;
    private int mId, mStorage, mUnits;
    private int mProjection, mZone, mSystemImage;
    private String projectionString, zoneString;
    private int surveyStrategy;
    private double projectionScale, projectionOriginNorth, projectionOriginEast;
    private int mDateCreated, mDateAccessed, mDateModified;
    private double mLocationLat, mLocationLong;
    private byte[] mImage;


    public Project() {

    }

    public Project(int mId, String mProjectDescription){
        this.mId = mId;
        this.mProjectDescription = mProjectDescription;
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

    public Project(String mProjectName, int mStorage, int mUnits, int mProjection, int mZone, double mLocationLat, double mLocationLong, int mSystemImage, String mImagePath) {
        this.mProjectName = mProjectName;
        this.mStorage = mStorage;
        this.mUnits = mUnits;
        this.mProjection = mProjection;
        this.mZone = mZone;
        this.mLocationLat = mLocationLat;
        this.mLocationLong = mLocationLong;
        this.mImagePath = mImagePath;
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

    public Project(int mId, String mProjectName, int mStorage, int mUnits, int mProjection, int mZone, double mLocationLat, double mLocationLong, int mSystemImage, byte[] mImage, String mProjectDescription) {
        this.mId = mId;
        this.mProjectName = mProjectName;
        this.mStorage = mStorage;
        this.mUnits = mUnits;
        this.mProjection = mProjection;
        this.mZone = mZone;
        this.mLocationLat = mLocationLat;
        this.mLocationLong = mLocationLong;
        this.mImage = mImage;
        this.mProjectDescription = mProjectDescription;
    }


    private Project(Parcel in){
        this.mId = in.readInt();
        this.mProjectName = in.readString();
        this.mStorage = in.readInt();
        this.mUnits = in.readInt();
        this.mProjection = in.readInt();
        this.projectionString = in.readString();
        this.mZone = in.readInt();
        this.zoneString = in.readString();
        this.mLocationLat = in.readDouble();
        this.mLocationLong = in.readDouble();
        this.mProjectDescription = in.readString();
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

    public String getmProjectDescription() {
        return mProjectDescription;
    }

    public void setmProjectDescription(String mProjectDescription) {
        this.mProjectDescription = mProjectDescription;
    }

    public String getmImagePath() {
        return mImagePath;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public String getProjectionString() {
        return projectionString;
    }

    public void setProjectionString(String projectionString) {
        this.projectionString = projectionString;
    }

    public String getZoneString() {
        return zoneString;
    }

    public void setZoneString(String zoneString) {
        this.zoneString = zoneString;
    }

    public int getSurveyStrategy() {
        return surveyStrategy;
    }

    public void setSurveyStrategy(int surveyStrategy) {
        this.surveyStrategy = surveyStrategy;
    }

    public double getProjectionScale() {
        return projectionScale;
    }

    public void setProjectionScale(double projectionScale) {
        this.projectionScale = projectionScale;
    }

    public double getProjectionOriginNorth() {
        return projectionOriginNorth;
    }

    public void setProjectionOriginNorth(double projectionOriginNorth) {
        this.projectionOriginNorth = projectionOriginNorth;
    }

    public double getProjectionOriginEast() {
        return projectionOriginEast;
    }

    public void setProjectionOriginEast(double projectionOriginEast) {
        this.projectionOriginEast = projectionOriginEast;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel: Started");

        dest.writeInt(mId);
        dest.writeString(mProjectName);
        dest.writeInt(mStorage);
        dest.writeInt(mUnits);
        dest.writeInt(mProjection);
        dest.writeString(projectionString);
        dest.writeInt(mZone);
        dest.writeString(zoneString);
        dest.writeDouble(mLocationLat);
        dest.writeDouble(mLocationLong);
        dest.writeString(mProjectDescription);


    }

    public static final Parcelable.Creator<Project> CREATOR = new Parcelable.Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel source) {
            return new Project(source);

        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };



}

package com.survlogic.survlogic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by chrisfillmore on 8/4/2017.
 */

public class PointSurvey implements Parcelable {
    private static final String TAG = "PointSurvey";

    private long id, projectId, job_Id;
    private int point_no, dateCreated, dateModified, pointType;
    private double northing, easting, elevation;
    private String description;

    public PointSurvey() {

    }

    public PointSurvey(int point_no, double northing, double easting, double elevation, String description, int pointType) {
        Log.d(TAG, "PointSurvey: Started..");
        this.point_no = point_no;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.pointType = pointType;
    }

    public PointSurvey(long projectId, long job_Id, int point_no, double northing, double easting, double elevation, String description, int pointType) {
        this.projectId = projectId;
        this.job_Id = job_Id;
        this.point_no = point_no;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.pointType = pointType;
    }

    public PointSurvey(long id, long projectId, long job_Id, int point_no, double northing, double easting, double elevation, String description, int pointType) {
        this.id = id;
        this.projectId = projectId;
        this.job_Id = job_Id;
        this.point_no = point_no;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.pointType = pointType;
    }

    private PointSurvey(Parcel in){
        this.point_no = in.readInt();
        this.northing = in.readDouble();
        this.easting = in.readDouble();
        this.elevation = in.readDouble();
        this.description = in.readString();
        this.pointType = in.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPoint_no() {
        return point_no;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getJob_Id() {
        return job_Id;
    }

    public void setJob_Id(long job_Id) {
        this.job_Id = job_Id;
    }

    public void setPoint_no(int point_no) {
        this.point_no = point_no;
    }

    public int getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(int dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getDateModified() {
        return dateModified;
    }

    public void setDateModified(int dateModified) {
        this.dateModified = dateModified;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointType() {
        return pointType;
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel: Started");

        dest.writeInt(point_no);
        dest.writeDouble(northing);
        dest.writeDouble(easting);
        dest.writeDouble(elevation);
        dest.writeString(description);
        dest.writeInt(pointType);

    }

    public static final Parcelable.Creator<PointSurvey> CREATOR = new Parcelable.Creator<PointSurvey>() {
        @Override
        public PointSurvey createFromParcel(Parcel source) {
            return new PointSurvey(source);

        }

        @Override
        public PointSurvey[] newArray(int size) {
            return new PointSurvey[size];
        }
    };

}



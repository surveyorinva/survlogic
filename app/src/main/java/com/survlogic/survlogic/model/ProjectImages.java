package com.survlogic.survlogic.model;


/**
 * Created by chrisfillmore on 7/9/2017.
 */
public class ProjectImages {

    int id, projectId, jobId, pointId;
    byte[] image;
    String imagePath;
    float bearingAngle;
    double locationLat, locationLong;


    public ProjectImages(){

    }

    public ProjectImages(int projectId, int jobId, int pointId, byte[] image) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.pointId = pointId;
        this.image = image;
    }

    public ProjectImages(int projectId, int jobId, int pointId, String imagePath) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.pointId = pointId;
        this.imagePath = imagePath;
    }

    public ProjectImages(int id, int projectId, int jobId, int pointId, byte[] image) {
        this.id = id;
        this.projectId = projectId;
        this.jobId = jobId;
        this.pointId = pointId;
        this.image = image;
    }

    public ProjectImages(int id, int projectId, int jobId, int pointId, String imagePath) {
        this.id = id;
        this.projectId = projectId;
        this.jobId = jobId;
        this.pointId = pointId;
        this.imagePath = imagePath;
    }

    public ProjectImages(int projectId, int jobId, int pointId, byte[] image, float bearingAngle, double locationLat, double locationLong) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.pointId = pointId;
        this.image = image;
        this.bearingAngle = bearingAngle;
        this.locationLat = locationLat;
        this.locationLong = locationLong;
    }

    public ProjectImages(int projectId, int jobId, int pointId, String imagePath, float bearingAngle, double locationLat, double locationLong) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.pointId = pointId;
        this.imagePath = imagePath;
        this.bearingAngle = bearingAngle;
        this.locationLat = locationLat;
        this.locationLong = locationLong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public float getBearingAngle() {
        return bearingAngle;
    }

    public void setBearingAngle(float bearingAngle) {
        this.bearingAngle = bearingAngle;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(double locationLong) {
        this.locationLong = locationLong;
    }
}

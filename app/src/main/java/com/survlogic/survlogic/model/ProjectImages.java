package com.survlogic.survlogic.model;


/**
 * Created by chrisfillmore on 7/9/2017.
 */
public class ProjectImages {

    int id, projectId, pointId;
    byte[] image;
    String imagePath;
    float bearingAngle;
    double LocationLat, LocationLong;


    public ProjectImages(){

    }

    public ProjectImages(int projectId, int pointId, byte[] image) {
        this.projectId = projectId;
        this.pointId = pointId;
        this.image = image;
    }

    public ProjectImages(int projectId, int pointId, String imagePath) {
        this.projectId = projectId;
        this.pointId = pointId;
        this.imagePath = imagePath;
    }

    public ProjectImages(int id, int projectId, int pointId, byte[] image) {
        this.id = id;
        this.projectId = projectId;
        this.pointId = pointId;
        this.image = image;
    }

    public ProjectImages(int id, int projectId, int pointId, String imagePath) {
        this.id = id;
        this.projectId = projectId;
        this.pointId = pointId;
        this.imagePath = imagePath;
    }

    public ProjectImages(int projectId, int pointId, byte[] image, float bearingAngle, double locationLat, double locationLong) {
        this.projectId = projectId;
        this.pointId = pointId;
        this.image = image;
        this.bearingAngle = bearingAngle;
        LocationLat = locationLat;
        LocationLong = locationLong;
    }

    public ProjectImages(int projectId, int pointId, String imagePath, float bearingAngle, double locationLat, double locationLong) {
        this.projectId = projectId;
        this.pointId = pointId;
        this.imagePath = imagePath;
        this.bearingAngle = bearingAngle;
        LocationLat = locationLat;
        LocationLong = locationLong;
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
        return LocationLat;
    }

    public void setLocationLat(double locationLat) {
        LocationLat = locationLat;
    }

    public double getLocationLong() {
        return LocationLong;
    }

    public void setLocationLong(double locationLong) {
        LocationLong = locationLong;
    }
}

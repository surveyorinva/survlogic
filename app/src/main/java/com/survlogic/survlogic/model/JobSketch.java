package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 9/10/2017.
 */

public class JobSketch {

    private long id, point_id;
    private String imagePath, backgroundImagePath;
    private String pathsCreated;
    private int canvasX, canvasY;
    private int dateCreated, dateModified;


    public JobSketch(){

    }

    public JobSketch(long point_id, String imagePath) {
        this.point_id = point_id;
        this.imagePath = imagePath;

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getPoint_id() {
        return point_id;
    }

    public void setPoint_id(long point_id) {
        this.point_id = point_id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getPathsCreated() {
        return pathsCreated;
    }

    public void setPathsCreated(String pathsCreated) {
        this.pathsCreated = pathsCreated;
    }

    public int getCanvasX() {
        return canvasX;
    }

    public void setCanvasX(int canvasX) {
        this.canvasX = canvasX;
    }

    public int getCanvasY() {
        return canvasY;
    }

    public void setCanvasY(int canvasY) {
        this.canvasY = canvasY;
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
}

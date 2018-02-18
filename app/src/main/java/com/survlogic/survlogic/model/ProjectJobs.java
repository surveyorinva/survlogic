package com.survlogic.survlogic.model;

import com.survlogic.survlogic.utils.StringUtilityHelper;

/**
 * Created by chrisfillmore on 7/29/2017.
 */

public class ProjectJobs {

    private int id, projectId;
    private String mJobName, mJobDbName, mJobDescription;
    private int mDateCreated, mDateAccessed, mDateModified;

    private int isProjection, isProjectionZone;
    private String projectionString, zoneString;


    public ProjectJobs() {

    }


    public ProjectJobs(int projectId, String mJobName, String mJobDbName) {
        this.projectId = projectId;
        this.mJobName = mJobName;
        this.mJobDbName = mJobDbName;
    }

    public ProjectJobs(int projectId, String mJobName, String mJobDbName, String mJobDescription) {
        this.projectId = projectId;
        this.mJobName = mJobName;
        this.mJobDbName = mJobDbName;
        this.mJobDescription = mJobDescription;
    }

    public ProjectJobs(int projectId, String mJobName, String mJobDbName, String mJobDescription, int mDateCreated, int mDateAccessed, int mDateModified) {
        this.projectId = projectId;
        this.mJobName = mJobName;
        this.mJobDbName = mJobDbName;
        this.mJobDescription = mJobDescription;
        this.mDateCreated = mDateCreated;
        this.mDateAccessed = mDateAccessed;
        this.mDateModified = mDateModified;
    }

    public ProjectJobs(int id, int projectId, String mJobName, String mJobDbName, String mJobDescription, int mDateCreated, int mDateAccessed, int mDateModified) {
        this.id = id;
        this.projectId = projectId;
        this.mJobName = mJobName;
        this.mJobDbName = mJobDbName;
        this.mJobDescription = mJobDescription;
        this.mDateCreated = mDateCreated;
        this.mDateAccessed = mDateAccessed;
        this.mDateModified = mDateModified;
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

    public String getmJobName() {
        return mJobName;
    }

    public void setmJobName(String mJobName) {
        this.mJobName = mJobName;
    }

    public String getmJobDbName() {
        return mJobDbName;
    }

    public void setmJobDbName(String mJobDbName) {
        this.mJobDbName = mJobDbName;
    }

    public String getmJobDescription() {
        return mJobDescription;
    }

    public void setmJobDescription(String mJobDescription) {
        this.mJobDescription = mJobDescription;
    }

    public int getmDateCreated() {
        return mDateCreated;
    }

    public void setmDateCreated(int mDateCreated) {
        this.mDateCreated = mDateCreated;
    }

    public int getmDateAccessed() {
        return mDateAccessed;
    }

    public void setmDateAccessed(int mDateAccessed) {
        this.mDateAccessed = mDateAccessed;
    }

    public int getmDateModified() {
        return mDateModified;
    }

    public void setmDateModified(int mDateModified) {
        this.mDateModified = mDateModified;
    }

    public int isProjection() {
        return isProjection;
    }

    public void setProjection(int projection) {
        isProjection = projection;
    }

    public int isProjectionZone() {
        return isProjectionZone;
    }

    public void setProjectionZone(int projectionZone) {
        isProjectionZone = projectionZone;
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
}

package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 7/29/2017.
 */

public class ProjectJobs {

    int id, projectId;
    String mJobName, mJobDbName, mJobDescription;
    int mDateCreated, mDateAccessed, mDateModified;

    public ProjectJobs() {

    }

    public ProjectJobs(int projectId, String mJobName, String mJobDbName, String mJobDescription, int mDateCreated) {
        this.projectId = projectId;
        this.mJobName = mJobName;
        this.mJobDbName = mJobDbName;
        this.mJobDescription = mJobDescription;
        this.mDateCreated = mDateCreated;
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
}

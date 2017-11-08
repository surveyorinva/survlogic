package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 11/5/2017.
 */

public class JobInformation {
    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    public JobInformation(){

    }

    public JobInformation(int project_id, int job_id, int job_settings_id, String jobDatabaseName) {
        this.project_id = project_id;
        this.job_id = job_id;
        this.job_settings_id = job_settings_id;
        this.jobDatabaseName = jobDatabaseName;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int getJob_id() {
        return job_id;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;
    }

    public int getJob_settings_id() {
        return job_settings_id;
    }

    public void setJob_settings_id(int job_settings_id) {
        this.job_settings_id = job_settings_id;
    }

    public String getJobDatabaseName() {
        return jobDatabaseName;
    }

    public void setJobDatabaseName(String jobDatabaseName) {
        this.jobDatabaseName = jobDatabaseName;
    }
}

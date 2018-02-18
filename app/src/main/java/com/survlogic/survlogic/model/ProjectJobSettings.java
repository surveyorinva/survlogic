package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 8/4/2017.
 */

public class ProjectJobSettings {

    //Remote Keys
    private long projectId, job_id;

    // UI Elements for how system will react
    private int uiFirstStart, uiDrawerState, defaultJobType;

    // Override variable for job-centric organization
    private int overrideProjection, overrideZone, overrideUnits;

    private String overrideProjectionString, overrideZoneString;

    //System defaults for Data being displayed on screen
    private int systemDistanceDisplay, systemDistancePrecisionDisplay, systemCoordinatesPrecisionDisplay, systemAngleDisplay,
            formatAngleHorizontalDisplay, formatAngleVerticalObsDisplay, formatDistanceHorizontalObsDisplay;

    //Format for Data entry by user in UI
    private int formatCoordinatesEntry, formatAngleHorizontalObsEntry;

    // Raw Datafile elements
    private int optionsRawFile, optionsRawTimeStamp, optionsGpsAttribute, optionsCodeTable;

    //Attributes for Job
    private String jobName;
    private String attClient, attMission, attWeatherGeneral, attWeatherTemp, attWeatherPress;
    private String attStaffLeader, attStaff_1, attStaff_2, attStaffOther;

    public ProjectJobSettings() {

    }

    public ProjectJobSettings(long projectId, long job_id, String jobName, int uiFirstStart, int uiDrawerState, int defaultJobType,
                              int overrideProjection, int overrideZone, int overrideUnits, int systemDistanceDisplay,
                              int systemDistancePrecisionDisplay, int systemCoordinatesPrecisionDisplay, int systemAngleDisplay, int formatAngleHorizontalDisplay,
                              int formatAngleVerticalObsDisplay, int formatDistanceHorizontalObsDisplay,
                              int formatCoordinatesEntry, int formatAngleHorizontalObsEntry, int optionsRawFile,
                              int optionsRawTimeStamp, int optionsGpsAttribute, int optionsCodeTable) {
        this.projectId = projectId;
        this.job_id = job_id;
        this.jobName = jobName;
        this.uiFirstStart = uiFirstStart;
        this.uiDrawerState = uiDrawerState;
        this.defaultJobType = defaultJobType;
        this.overrideProjection = overrideProjection;
        this.overrideZone = overrideZone;
        this.overrideUnits = overrideUnits;
        this.systemDistanceDisplay = systemDistanceDisplay;
        this.systemDistancePrecisionDisplay = systemDistancePrecisionDisplay;
        this.systemCoordinatesPrecisionDisplay = systemCoordinatesPrecisionDisplay;
        this.systemAngleDisplay = systemAngleDisplay;
        this.formatAngleHorizontalDisplay = formatAngleHorizontalDisplay;
        this.formatAngleVerticalObsDisplay = formatAngleVerticalObsDisplay;
        this.formatDistanceHorizontalObsDisplay = formatDistanceHorizontalObsDisplay;
        this.formatCoordinatesEntry = formatCoordinatesEntry;
        this.formatAngleHorizontalObsEntry = formatAngleHorizontalObsEntry;
        this.optionsRawFile = optionsRawFile;
        this.optionsRawTimeStamp = optionsRawTimeStamp;
        this.optionsGpsAttribute = optionsGpsAttribute;
        this.optionsCodeTable = optionsCodeTable;
    }


    public ProjectJobSettings(long projectId, long job_id, String jobName, int uiFirstStart, int uiDrawerState, int defaultJobType,
                              int overrideProjection, int overrideZone, int overrideUnits, int systemDistanceDisplay,
                              int systemDistancePrecisionDisplay, int systemCoordinatesPrecisionDisplay, int systemAngleDisplay, int formatAngleHorizontalDisplay,
                              int formatAngleVerticalObsDisplay, int formatDistanceHorizontalObsDisplay,
                              int formatCoordinatesEntry, int formatAngleHorizontalObsEntry, int optionsRawFile,
                              int optionsRawTimeStamp, int optionsGpsAttribute, int optionsCodeTable, String attClient,
                              String attMission, String attWeatherGeneral, String attWeatherTemp, String attWeatherPress,
                              String attStaffLeader, String attStaff_1, String attStaff_2, String attStaffOther) {
        this.projectId = projectId;
        this.job_id = job_id;
        this.jobName = jobName;
        this.uiFirstStart = uiFirstStart;
        this.uiDrawerState = uiDrawerState;
        this.defaultJobType = defaultJobType;
        this.overrideProjection = overrideProjection;
        this.overrideZone = overrideZone;
        this.overrideUnits = overrideUnits;
        this.systemDistanceDisplay = systemDistanceDisplay;
        this.systemDistancePrecisionDisplay = systemDistancePrecisionDisplay;
        this.systemCoordinatesPrecisionDisplay = systemCoordinatesPrecisionDisplay;
        this.systemAngleDisplay = systemAngleDisplay;
        this.formatAngleHorizontalDisplay = formatAngleHorizontalDisplay;
        this.formatAngleVerticalObsDisplay = formatAngleVerticalObsDisplay;
        this.formatDistanceHorizontalObsDisplay = formatDistanceHorizontalObsDisplay;
        this.formatCoordinatesEntry = formatCoordinatesEntry;
        this.formatAngleHorizontalObsEntry = formatAngleHorizontalObsEntry;
        this.optionsRawFile = optionsRawFile;
        this.optionsRawTimeStamp = optionsRawTimeStamp;
        this.optionsGpsAttribute = optionsGpsAttribute;
        this.optionsCodeTable = optionsCodeTable;
        this.attClient = attClient;
        this.attMission = attMission;
        this.attWeatherGeneral = attWeatherGeneral;
        this.attWeatherTemp = attWeatherTemp;
        this.attWeatherPress = attWeatherPress;
        this.attStaffLeader = attStaffLeader;
        this.attStaff_1 = attStaff_1;
        this.attStaff_2 = attStaff_2;
        this.attStaffOther = attStaffOther;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getJob_id() {
        return job_id;
    }

    public void setJob_id(long job_id) {
        this.job_id = job_id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getUiFirstStart() {
        return uiFirstStart;
    }

    public void setUiFirstStart(int uiFirstStart) {
        this.uiFirstStart = uiFirstStart;
    }

    public int getUiDrawerState() {
        return uiDrawerState;
    }

    public void setUiDrawerState(int uiDrawerState) {
        this.uiDrawerState = uiDrawerState;
    }

    public int getDefaultJobType() {
        return defaultJobType;
    }

    public void setDefaultJobType(int defaultJobType) {
        this.defaultJobType = defaultJobType;
    }

    public int getOverrideProjection() {
        return overrideProjection;
    }

    public void setOverrideProjection(int overrideProjection) {
        this.overrideProjection = overrideProjection;
    }

    public int getOverrideZone() {
        return overrideZone;
    }

    public void setOverrideZone(int overrideZone) {
        this.overrideZone = overrideZone;
    }


    public String getOverrideProjectionString() {
        return overrideProjectionString;
    }

    public void setOverrideProjectionString(String overrideProjectionString) {
        this.overrideProjectionString = overrideProjectionString;
    }

    public String getOverrideZoneString() {
        return overrideZoneString;
    }

    public void setOverrideZoneString(String overrideZoneString) {
        this.overrideZoneString = overrideZoneString;
    }

    public int getOverrideUnits() {
        return overrideUnits;
    }

    public void setOverrideUnits(int overrideUnits) {
        this.overrideUnits = overrideUnits;
    }

    public int getSystemDistanceDisplay() {
        return systemDistanceDisplay;
    }

    public void setSystemDistanceDisplay(int systemDistanceDisplay) {
        this.systemDistanceDisplay = systemDistanceDisplay;
    }

    public int getSystemDistancePrecisionDisplay() {
        return systemDistancePrecisionDisplay;
    }

    public void setSystemDistancePrecisionDisplay(int systemDistancePrecisionDisplay) {
        this.systemDistancePrecisionDisplay = systemDistancePrecisionDisplay;
    }

    public int getSystemCoordinatesPrecisionDisplay() {
        return systemCoordinatesPrecisionDisplay;
    }

    public void setSystemCoordinatesPrecisionDisplay(int systemCoordinatesPrecisionDisplay) {
        this.systemCoordinatesPrecisionDisplay = systemCoordinatesPrecisionDisplay;
    }

    public int getSystemAngleDisplay() {
        return systemAngleDisplay;
    }

    public void setSystemAngleDisplay(int systemAngleDisplay) {
        this.systemAngleDisplay = systemAngleDisplay;
    }

    public int getFormatAngleHorizontalDisplay() {
        return formatAngleHorizontalDisplay;
    }

    public void setFormatAngleHorizontalDisplay(int formatAngleHorizontalDisplay) {
        this.formatAngleHorizontalDisplay = formatAngleHorizontalDisplay;
    }

    public int getFormatAngleVerticalObsDisplay() {
        return formatAngleVerticalObsDisplay;
    }

    public void setFormatAngleVerticalObsDisplay(int formatAngleVerticalObsDisplay) {
        this.formatAngleVerticalObsDisplay = formatAngleVerticalObsDisplay;
    }

    public int getFormatDistanceHorizontalObsDisplay() {
        return formatDistanceHorizontalObsDisplay;
    }

    public void setFormatDistanceHorizontalObsDisplay(int formatDistanceHorizontalObsDisplay) {
        this.formatDistanceHorizontalObsDisplay = formatDistanceHorizontalObsDisplay;
    }

    public int getFormatCoordinatesEntry() {
        return formatCoordinatesEntry;
    }

    public void setFormatCoordinatesEntry(int formatCoordinatesEntry) {
        this.formatCoordinatesEntry = formatCoordinatesEntry;
    }

    public int getFormatAngleHorizontalObsEntry() {
        return formatAngleHorizontalObsEntry;
    }

    public void setFormatAngleHorizontalObsEntry(int formatAngleHorizontalObsEntry) {
        this.formatAngleHorizontalObsEntry = formatAngleHorizontalObsEntry;
    }

    public int getOptionsRawFile() {
        return optionsRawFile;
    }

    public void setOptionsRawFile(int optionsRawFile) {
        this.optionsRawFile = optionsRawFile;
    }

    public int getOptionsRawTimeStamp() {
        return optionsRawTimeStamp;
    }

    public void setOptionsRawTimeStamp(int optionsRawTimeStamp) {
        this.optionsRawTimeStamp = optionsRawTimeStamp;
    }

    public int getOptionsGpsAttribute() {
        return optionsGpsAttribute;
    }

    public void setOptionsGpsAttribute(int optionsGpsAttribute) {
        this.optionsGpsAttribute = optionsGpsAttribute;
    }

    public int getOptionsCodeTable() {
        return optionsCodeTable;
    }

    public void setOptionsCodeTable(int optionsCodeTable) {
        this.optionsCodeTable = optionsCodeTable;
    }

    public String getAttClient() {
        return attClient;
    }

    public void setAttClient(String attClient) {
        this.attClient = attClient;
    }

    public String getAttMission() {
        return attMission;
    }

    public void setAttMission(String attMission) {
        this.attMission = attMission;
    }

    public String getAttWeatherGeneral() {
        return attWeatherGeneral;
    }

    public void setAttWeatherGeneral(String attWeatherGeneral) {
        this.attWeatherGeneral = attWeatherGeneral;
    }

    public String getAttWeatherTemp() {
        return attWeatherTemp;
    }

    public void setAttWeatherTemp(String attWeatherTemp) {
        this.attWeatherTemp = attWeatherTemp;
    }

    public String getAttWeatherPress() {
        return attWeatherPress;
    }

    public void setAttWeatherPress(String attWeatherPress) {
        this.attWeatherPress = attWeatherPress;
    }

    public String getAttStaffLeader() {
        return attStaffLeader;
    }

    public void setAttStaffLeader(String attStaffLeader) {
        this.attStaffLeader = attStaffLeader;
    }

    public String getAttStaff_1() {
        return attStaff_1;
    }

    public void setAttStaff_1(String attStaff_1) {
        this.attStaff_1 = attStaff_1;
    }

    public String getAttStaff_2() {
        return attStaff_2;
    }

    public void setAttStaff_2(String attStaff_2) {
        this.attStaff_2 = attStaff_2;
    }

    public String getAttStaffOther() {
        return attStaffOther;
    }

    public void setAttStaffOther(String attStaffOther) {
        this.attStaffOther = attStaffOther;
    }
}

package com.survlogic.survlogic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.view.DialogProjectDescriptionAdd;

import java.util.Date;

/**
 * Created by chrisfillmore on 8/4/2017.
 */

public class JobDatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "JobDatabaseHandler";

    //  Database Constants
    private static final int DB_VERSION = 1;
    private String DB_NAME;

    // Table Creation Queries
    private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE "
            + JobContract.JobSettingsEntry.TABLE_NAME
            + "("
            + JobContract.JobSettingsEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + JobContract.JobSettingsEntry.KEY_PROJECT_ID + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_JOB_ID + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_JOB_NAME + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_UI_FIRST_START + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_DRAWER_STATE + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_DEFAULT_JOB_TYPE + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_OVER_PROJECTION + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_OVER_ZONE + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_UNITS + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_ATTR_CLIENT + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_MISSION + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_GENERAL + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_TEMP + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_PRESS + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_STAFF_LEADER+ " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF1 + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF2 + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_ATTR_STAFF_OTHER + " TEXT,"
            + JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_DISPLAY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_PREC_DISPLAY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_SYSTEM_ANGLE_DISPLAY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_FORMAT_COORD_ENTRY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_DISPLAY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_OBSERVATION_ENTRY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_VZ_OBSERVATION_DISPLAY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_FORMAT_DISTANCE_HZ_OBSERVATION_DISPLAY + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_FILE + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_TIME_STAMP + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_OPTIONS_GPS_ATTRIBUTE + " INTEGER,"
            + JobContract.JobSettingsEntry.KEY_OPTIONS_CODE_TABLE + " INTEGER);";


    private static final String CREATE_TABLE_POINTS = "CREATE TABLE "
            + JobContract.PointEntry.TABLE_NAME
            + "("
            + JobContract.PointEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + JobContract.PointEntry.KEY_POINT_NO + " INTEGER,"
            + JobContract.PointEntry.KEY_NORTHING + " DOUBLE,"
            + JobContract.PointEntry.KEY_EASTING + " DOUBLE,"
            + JobContract.PointEntry.KEY_ELEVATION + " DOUBLE,"
            + JobContract.PointEntry.KEY_GEOLAT + " DOUBLE,"
            + JobContract.PointEntry.KEY_GEOLON + " DOUBLE,"
            + JobContract.PointEntry.KEY_GEOELLIPS + " DOUBLE,"
            + JobContract.PointEntry.KEY_GEOACCURACY + " DOUBLE,"
            + JobContract.PointEntry.KEY_DESCRIPTION + " TEXT,"
            + JobContract.PointEntry.KEY_DATE_CREATED + " INTEGER,"
            + JobContract.PointEntry.KEY_DATE_MODIFIED + " INTEGER);";



    public JobDatabaseHandler(Context context, String DB_NAME) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG,"Database connected " + DB_NAME );

        this.DB_NAME = DB_NAME;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SETTINGS);
        db.execSQL(CREATE_TABLE_POINTS);
        Log.d(TAG,"Tables created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: Updating Tables to new Version...");

        db.execSQL("DROP IF TABLE EXISTS " + JobContract.JobSettingsEntry.TABLE_NAME);
        db.execSQL("DROP IF TABLE EXISTS " + JobContract.PointEntry.TABLE_NAME);

        onCreate(db);

    }

/**
 * All CRUD(Create, Read, Update, Delete) Operations
 */

    //  Creating---------------------------------------------------------------------------------------------------

    /**
     * Adding Job Settings to Database
     */

    public long addDefaultJobSettingToDB(SQLiteDatabase db, ProjectJobSettings settings, boolean options){
        Log.d(TAG, "addDefaultJobSettingToDB: Starting");

        ContentValues contentValues = new ContentValues();


        //Required
        contentValues.put(JobContract.JobSettingsEntry.KEY_PROJECT_ID, settings.getProjectId());
        contentValues.put(JobContract.JobSettingsEntry.KEY_JOB_ID, settings.getJob_id());
        contentValues.put(JobContract.JobSettingsEntry.KEY_JOB_NAME, settings.getJobName());

        contentValues.put(JobContract.JobSettingsEntry.KEY_UI_FIRST_START, settings.getUiFirstStart());
        contentValues.put(JobContract.JobSettingsEntry.KEY_DRAWER_STATE,settings.getUiDrawerState());
        contentValues.put(JobContract.JobSettingsEntry.KEY_DEFAULT_JOB_TYPE, settings.getDefaultJobType());

        contentValues.put(JobContract.JobSettingsEntry.KEY_OVER_PROJECTION, settings.getOverrideProjection());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OVER_ZONE,settings.getOverrideZone());
        contentValues.put(JobContract.JobSettingsEntry.KEY_UNITS,settings.getOverrideUnits());

        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_DISPLAY,settings.getSystemDistanceDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_PREC_DISPLAY,settings.getSystemDistancePrecisionDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_ANGLE_DISPLAY,settings.getSystemAngleDisplay());

        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_COORD_ENTRY,settings.getFormatCoordinatesEntry());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_DISPLAY,settings.getFormatAngleHorizontalDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_OBSERVATION_ENTRY,settings.getFormatAngleHorizontalObsEntry());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_VZ_OBSERVATION_DISPLAY,settings.getFormatAngleVerticalObsDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_DISTANCE_HZ_OBSERVATION_DISPLAY,settings.getFormatDistanceHorizontalObsDisplay());

        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_FILE, settings.getOptionsRawFile());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_TIME_STAMP,settings.getOptionsRawTimeStamp());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_GPS_ATTRIBUTE, settings.getOptionsGpsAttribute());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_CODE_TABLE, settings.getOptionsCodeTable());

        //Optional

        if(options){
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_CLIENT,settings.getAttClient());
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_MISSION,settings.getAttMission());
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_GENERAL,settings.getAttWeatherGeneral());
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_TEMP,settings.getAttWeatherTemp());

            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_LEADER,settings.getAttStaffLeader());
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF1,settings.getAttStaff_1());
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF2,settings.getAttStaff_2());
            contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_OTHER,settings.getAttStaffOther());
        }

        Log.d(TAG, "addData: Adding Default Settings to " + JobContract.JobSettingsEntry.TABLE_NAME);

        Long result = db.insert(JobContract.JobSettingsEntry.TABLE_NAME, null, contentValues);
        db.close();

        if (result==-1){
            Log.d(TAG,"Error, Something went wrong...");
            return -1;
        } else {
            Log.d(TAG,"Success, Row inserted into Table...");
            return result;
        }

    }

    /**
     * Adding PointSurvey to Database
     */
    public long addPointSurveyToDB(SQLiteDatabase db, PointSurvey pointSurvey){
        Log.d(TAG, "addPointSurveyToDB: Starting...");

        ContentValues contentValues = new ContentValues();

        //Required
        contentValues.put(JobContract.PointEntry.KEY_POINT_NO, pointSurvey.getPoint_no());
        contentValues.put(JobContract.PointEntry.KEY_NORTHING, pointSurvey.getNorthing());
        contentValues.put(JobContract.PointEntry.KEY_EASTING, pointSurvey.getEasting());
        contentValues.put(JobContract.PointEntry.KEY_ELEVATION, pointSurvey.getElevation());
        contentValues.put(JobContract.PointEntry.KEY_DESCRIPTION, pointSurvey.getDescription());

        contentValues.put(JobContract.PointEntry.KEY_DATE_CREATED,(int) (new Date().getTime()/1000));

        Log.d(TAG, "addData: Adding Survey Point No. " + pointSurvey.getPoint_no() + " to " + JobContract.PointEntry.TABLE_NAME);

        Long result = db.insert(JobContract.PointEntry.TABLE_NAME, null, contentValues);
        db.close();

        if (result==-1){
            Log.d(TAG,"Error, Something went wrong...");
            return -1;
        } else {
            Log.d(TAG,"Success, Row inserted into Table...");
            return result;
        }
    }

    public long addPointGeodeticToDB(SQLiteDatabase db, PointGeodetic pointGeodetic){
        Log.d(TAG, "addPointGeodeticToDB: Starting...");

        ContentValues contentValues = new ContentValues();

        //Required - From PointSurvey
        contentValues.put(JobContract.PointEntry.KEY_POINT_NO, pointGeodetic.getPoint_no());
        contentValues.put(JobContract.PointEntry.KEY_NORTHING, pointGeodetic.getNorthing());
        contentValues.put(JobContract.PointEntry.KEY_EASTING, pointGeodetic.getEasting());
        contentValues.put(JobContract.PointEntry.KEY_ELEVATION, pointGeodetic.getElevation());
        contentValues.put(JobContract.PointEntry.KEY_DESCRIPTION, pointGeodetic.getDescription());

        //Required - From PointGeodetic
        contentValues.put(JobContract.PointEntry.KEY_GEOLAT, pointGeodetic.getLatitude());
        contentValues.put(JobContract.PointEntry.KEY_GEOLON, pointGeodetic.getLongitude());
        contentValues.put(JobContract.PointEntry.KEY_GEOELLIPS, pointGeodetic.getEllipsoid());
        contentValues.put(JobContract.PointEntry.KEY_GEOACCURACY, pointGeodetic.getAccuracy());

        contentValues.put(JobContract.PointEntry.KEY_DATE_CREATED,(int) (new Date().getTime()/1000));

        Log.d(TAG, "addData: Adding Survey Point No. " + pointGeodetic.getPoint_no() + " to " + JobContract.PointEntry.TABLE_NAME);

        Long result = db.insert(JobContract.PointEntry.TABLE_NAME, null, contentValues);
        db.close();

        if (result==-1){
            Log.d(TAG,"Error, Something went wrong...");
            return -1;
        } else {
            Log.d(TAG,"Success, Row inserted into Table...");
            return result;
        }

    }

    //Reading---------------------------------------------------------------------------------------//


    public ProjectJobSettings getJobSettingsById(SQLiteDatabase db, long job_settings_id){
        Log.d(TAG, "getJobSettingsbyId: Starting");
        String selectQuery = "SELECT  * FROM " + JobContract.JobSettingsEntry.TABLE_NAME + " WHERE "
                + JobContract.JobSettingsEntry.KEY_ID + " = " + job_settings_id;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        ProjectJobSettings settings = new ProjectJobSettings();

        //Required
        settings.setProjectId(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_PROJECT_ID))));
        settings.setJob_id(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_JOB_ID))));
        settings.setJobName((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_JOB_NAME))));

        settings.setUiFirstStart(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_UI_FIRST_START))));
        settings.setUiDrawerState(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_DRAWER_STATE))));

        settings.setDefaultJobType(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_DEFAULT_JOB_TYPE))));
        settings.setOverrideProjection(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_OVER_PROJECTION))));
        settings.setOverrideZone(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_OVER_ZONE))));
        settings.setOverrideUnits(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_UNITS))));

        settings.setAttClient((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_CLIENT))));
        settings.setAttMission((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_MISSION))));

        settings.setAttWeatherGeneral((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_GENERAL))));
        settings.setAttWeatherTemp((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_TEMP))));
        settings.setAttWeatherPress((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_PRESS))));

        settings.setAttStaffLeader((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_LEADER))));
        settings.setAttStaff_1((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF1))));
        settings.setAttStaff_2((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF2))));
        settings.setAttStaffOther((c.getString(c.getColumnIndex(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_OTHER))));

        settings.setSystemDistanceDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_DISPLAY))));
        settings.setSystemDistancePrecisionDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_PREC_DISPLAY))));
        settings.setSystemAngleDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_SYSTEM_ANGLE_DISPLAY))));

        settings.setFormatCoordinatesEntry(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_FORMAT_COORD_ENTRY))));
        settings.setFormatAngleHorizontalDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_DISPLAY))));
        settings.setFormatAngleHorizontalObsEntry(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_OBSERVATION_ENTRY))));
        settings.setFormatAngleVerticalObsDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_VZ_OBSERVATION_DISPLAY))));
        settings.setFormatDistanceHorizontalObsDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_FORMAT_DISTANCE_HZ_OBSERVATION_DISPLAY))));

        settings.setOptionsRawFile(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_FILE))));
        settings.setOptionsRawTimeStamp(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_TIME_STAMP))));
        settings.setOptionsGpsAttribute(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_OPTIONS_GPS_ATTRIBUTE))));
        settings.setOptionsCodeTable(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_OPTIONS_CODE_TABLE))));

        return settings;
    }

    public int updateJobSettings(SQLiteDatabase db, ProjectJobSettings settings){
        Log.d(TAG, "updateJobSettings: Starting...");
        int settingsKey = 1;

        ContentValues contentValues = new ContentValues();
        
        contentValues.put(JobContract.JobSettingsEntry.KEY_PROJECT_ID, settings.getProjectId());
        contentValues.put(JobContract.JobSettingsEntry.KEY_JOB_ID, settings.getJob_id());
        contentValues.put(JobContract.JobSettingsEntry.KEY_JOB_NAME, settings.getJobName());

        contentValues.put(JobContract.JobSettingsEntry.KEY_UI_FIRST_START, settings.getUiFirstStart());
        contentValues.put(JobContract.JobSettingsEntry.KEY_DRAWER_STATE,settings.getUiDrawerState());
        contentValues.put(JobContract.JobSettingsEntry.KEY_DEFAULT_JOB_TYPE, settings.getDefaultJobType());

        contentValues.put(JobContract.JobSettingsEntry.KEY_OVER_PROJECTION, settings.getOverrideProjection());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OVER_ZONE,settings.getOverrideZone());
        contentValues.put(JobContract.JobSettingsEntry.KEY_UNITS,settings.getOverrideUnits());

        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_DISPLAY,settings.getSystemDistanceDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_DIST_PREC_DISPLAY,settings.getSystemDistancePrecisionDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_ANGLE_DISPLAY,settings.getSystemAngleDisplay());

        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_COORD_ENTRY,settings.getFormatCoordinatesEntry());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_DISPLAY,settings.getFormatAngleHorizontalDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_HZ_OBSERVATION_ENTRY,settings.getFormatAngleHorizontalObsEntry());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_ANGLE_VZ_OBSERVATION_DISPLAY,settings.getFormatAngleVerticalObsDisplay());
        contentValues.put(JobContract.JobSettingsEntry.KEY_FORMAT_DISTANCE_HZ_OBSERVATION_DISPLAY,settings.getFormatDistanceHorizontalObsDisplay());

        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_FILE, settings.getOptionsRawFile());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_RAW_TIME_STAMP,settings.getOptionsRawTimeStamp());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_GPS_ATTRIBUTE, settings.getOptionsGpsAttribute());
        contentValues.put(JobContract.JobSettingsEntry.KEY_OPTIONS_CODE_TABLE, settings.getOptionsCodeTable());
        
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_CLIENT,settings.getAttClient());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_MISSION,settings.getAttMission());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_GENERAL,settings.getAttWeatherGeneral());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_TEMP,settings.getAttWeatherTemp());

        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_LEADER,settings.getAttStaffLeader());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF1,settings.getAttStaff_1());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF2,settings.getAttStaff_2());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_OTHER,settings.getAttStaffOther());

        return db.update(JobContract.JobSettingsEntry.TABLE_NAME, contentValues, JobContract.JobSettingsEntry.KEY_ID + " = ?",
                new String[] {String.valueOf(settingsKey)});
        
    }


}
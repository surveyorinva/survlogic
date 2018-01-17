package com.survlogic.survlogic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.ProjectJobSettings;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chrisfillmore on 8/4/2017.
 */

public class JobDatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "JobDatabaseHandler";

    //  Database Constants
    private static final int DB_VERSION = 2;
    private static final String APP_FOLDER = "SurvLogic";
    private static final String JOB_FOLDER = "jobs";
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
            + JobContract.JobSettingsEntry.KEY_SYSTEM_COORD_PREC_DISPLAY + " INTEGER,"
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
            + JobContract.PointEntry.KEY_GEOORTHO + " DOUBLE,"
            + JobContract.PointEntry.KEY_GEOACCURACY + " DOUBLE,"
            + JobContract.PointEntry.KEY_DESCRIPTION + " TEXT,"
            + JobContract.PointEntry.KEY_POINT_TYPE + " INTEGER,"
            + JobContract.PointEntry.KEY_POINT_GEODETIC_TYPE + " INTEGER,"
            + JobContract.PointEntry.KEY_DATE_CREATED + " INTEGER,"
            + JobContract.PointEntry.KEY_DATE_MODIFIED + " INTEGER);";

    private static final String CREATE_TABLE_SKETCH = "CREATE TABLE "
            + JobContract.SketchEntry.TABLE_NAME
            + "("
            + JobContract.SketchEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + JobContract.SketchEntry.KEY_POINT_ID + " INTEGER,"
            + JobContract.SketchEntry.KEY_IMAGE_PATH + " TEXT,"
            + JobContract.SketchEntry.KEY_IMAGE_PATH_BACKGROUND + " TEXT,"
            + JobContract.SketchEntry.KEY_PATHS + " TEXT,"
            + JobContract.SketchEntry.KEY_CANVAS_X + " INTEGER,"
            + JobContract.SketchEntry.KEY_CANVAS_Y + " INTEGER,"
            + JobContract.SketchEntry.KEY_DATE_CREATED + " INTEGER,"
            + JobContract.SketchEntry.KEY_DATE_MODIFIED + " INTEGER);";



    public JobDatabaseHandler(Context context, String DB_NAME) {
        super(context, Environment.getExternalStorageDirectory() +
                File.separator + APP_FOLDER + File.separator + JOB_FOLDER + File.separator + DB_NAME,
                null, DB_VERSION);

        Log.d(TAG,"Database connected " + DB_NAME );

        this.DB_NAME = DB_NAME;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SETTINGS);
        db.execSQL(CREATE_TABLE_POINTS);
        db.execSQL(CREATE_TABLE_SKETCH);
        Log.d(TAG,"Tables created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: Updating Tables to new Version...");

        db.execSQL("DROP TABLE IF EXISTS " + JobContract.JobSettingsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + JobContract.PointEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + JobContract.SketchEntry.TABLE_NAME);
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
        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_COORD_PREC_DISPLAY,settings.getSystemCoordinatesPrecisionDisplay());
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
        contentValues.put(JobContract.PointEntry.KEY_POINT_TYPE, pointSurvey.getPointType());

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


    public long addMultiplePointSurveyToDB(SQLiteDatabase db, ArrayList<PointSurvey> lstPointSurvey){
        Log.d(TAG, "addMultiplePointSurveyToDB: Started");

        Long result = 0L;

        for(int i=0; i<lstPointSurvey.size(); i++) {
            Log.d(TAG, "addMultiplePointSurveyToDB: i: " + i);
            PointSurvey pointSurvey = lstPointSurvey.get(i);

            ContentValues contentValues = new ContentValues();

            //Required
            contentValues.put(JobContract.PointEntry.KEY_POINT_NO, pointSurvey.getPoint_no());
            contentValues.put(JobContract.PointEntry.KEY_NORTHING, pointSurvey.getNorthing());
            contentValues.put(JobContract.PointEntry.KEY_EASTING, pointSurvey.getEasting());
            contentValues.put(JobContract.PointEntry.KEY_ELEVATION, pointSurvey.getElevation());
            contentValues.put(JobContract.PointEntry.KEY_DESCRIPTION, pointSurvey.getDescription());
            contentValues.put(JobContract.PointEntry.KEY_POINT_TYPE, pointSurvey.getPointType());

            contentValues.put(JobContract.PointEntry.KEY_DATE_CREATED,(int) (new Date().getTime()/1000));

            Log.d(TAG, "addData: Adding Survey Point No. " + pointSurvey.getPoint_no() + " to " + JobContract.PointEntry.TABLE_NAME);

            result = result + db.insert(JobContract.PointEntry.TABLE_NAME, null, contentValues);

        }

        if (result==-1){
            Log.d(TAG,"Error, Something went wrong...");
            return -1;
        } else {
            Log.d(TAG,"Success," + result + " Rows inserted into Table...");
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
        contentValues.put(JobContract.PointEntry.KEY_POINT_TYPE, pointGeodetic.getPointType());

        //Required - From PointGeodetic
        contentValues.put(JobContract.PointEntry.KEY_GEOLAT, pointGeodetic.getLatitude());
        contentValues.put(JobContract.PointEntry.KEY_GEOLON, pointGeodetic.getLongitude());
        contentValues.put(JobContract.PointEntry.KEY_GEOELLIPS, pointGeodetic.getEllipsoid());
        contentValues.put(JobContract.PointEntry.KEY_GEOORTHO, pointGeodetic.getOrtho());
        contentValues.put(JobContract.PointEntry.KEY_GEOACCURACY, pointGeodetic.getAccuracy());
        contentValues.put(JobContract.PointEntry.KEY_POINT_GEODETIC_TYPE, pointGeodetic.getPointGeodeticType());

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

    /**
     * Add Sketch to Database
     */

    public long addSketchToDB(SQLiteDatabase db, JobSketch jobSketch){
        Log.d(TAG, "addSketchToDB: Started...");

        ContentValues contentValues = new ContentValues();

        //Required
        contentValues.put(JobContract.SketchEntry.KEY_POINT_ID, jobSketch.getPoint_id());
        contentValues.put(JobContract.SketchEntry.KEY_IMAGE_PATH, jobSketch.getImagePath());
        contentValues.put(JobContract.SketchEntry.KEY_IMAGE_PATH_BACKGROUND, jobSketch.getBackgroundImagePath());
        contentValues.put(JobContract.SketchEntry.KEY_PATHS, jobSketch.getPathsCreated());
        contentValues.put(JobContract.SketchEntry.KEY_CANVAS_X, jobSketch.getCanvasX());
        contentValues.put(JobContract.SketchEntry.KEY_CANVAS_Y, jobSketch.getCanvasY());

        contentValues.put(JobContract.SketchEntry.KEY_DATE_CREATED,(int) (new Date().getTime()/1000));

        Log.d(TAG, "addData: Adding Sketch to " + JobContract.PointEntry.TABLE_NAME);

        Long result = db.insert(JobContract.SketchEntry.TABLE_NAME, null, contentValues);
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
        settings.setSystemCoordinatesPrecisionDisplay(c.getInt((c.getColumnIndex(JobContract.JobSettingsEntry.KEY_SYSTEM_COORD_PREC_DISPLAY))));
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
        contentValues.put(JobContract.JobSettingsEntry.KEY_SYSTEM_COORD_PREC_DISPLAY,settings.getSystemDistancePrecisionDisplay());
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
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_WEATHER_PRESS,settings.getAttWeatherPress());

        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_LEADER,settings.getAttStaffLeader());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF1,settings.getAttStaff_1());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_STAFF2,settings.getAttStaff_2());
        contentValues.put(JobContract.JobSettingsEntry.KEY_ATTR_STAFF_OTHER,settings.getAttStaffOther());

        return db.update(JobContract.JobSettingsEntry.TABLE_NAME, contentValues, JobContract.JobSettingsEntry.KEY_ID + " = ?",
                new String[] {String.valueOf(settingsKey)});
        
    }


    public List<PointSurvey> getPointSurveysAll(SQLiteDatabase db){
        Log.d(TAG, "getPointSurveysAll: Starting");

        List<PointSurvey> lstPoints = new ArrayList<PointSurvey>();
        String selectQuery = "SELECT * FROM " + JobContract.PointEntry.TABLE_NAME
                + " ORDER BY " + JobContract.PointEntry.KEY_POINT_NO + " ASC";

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                PointSurvey pointSurvey = new PointSurvey();
                pointSurvey.setId(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_ID))));
                pointSurvey.setPoint_no(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_NO))));
                pointSurvey.setNorthing(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_NORTHING)));
                pointSurvey.setEasting(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_EASTING)));
                pointSurvey.setElevation(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_ELEVATION)));
                pointSurvey.setDescription((c.getString(c.getColumnIndex(JobContract.PointEntry.KEY_DESCRIPTION))));
                pointSurvey.setPointType(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_TYPE))));

                // Project MetaData
                pointSurvey.setDateCreated(c.getInt(c.getColumnIndex(JobContract.PointEntry.KEY_DATE_CREATED)));


                lstPoints.add(pointSurvey);
            } while (c.moveToNext());
        }

        return lstPoints;
    }

    public List<PointGeodetic> getPointGeodeticAll(SQLiteDatabase db){
        Log.d(TAG, "getPointGeodeticAll: Starting");

        List<PointGeodetic> lstPoints = new ArrayList<PointGeodetic>();
        String selectQuery = "SELECT * FROM " + JobContract.PointEntry.TABLE_NAME
                + " WHERE " + JobContract.PointEntry.KEY_POINT_GEODETIC_TYPE + " IS NOT NULL "
                + " ORDER BY " + JobContract.PointEntry.KEY_POINT_NO + " ASC";

        Log.i(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                PointGeodetic pointGeodetic = new PointGeodetic();
                pointGeodetic.setId(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_ID))));
                pointGeodetic.setPoint_no(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_NO))));
                pointGeodetic.setNorthing(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_NORTHING)));
                pointGeodetic.setEasting(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_EASTING)));
                pointGeodetic.setElevation(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_ELEVATION)));
                pointGeodetic.setDescription((c.getString(c.getColumnIndex(JobContract.PointEntry.KEY_DESCRIPTION))));
                pointGeodetic.setPointType(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_TYPE))));
                pointGeodetic.setPointGeodeticType(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_GEODETIC_TYPE))));

                pointGeodetic.setLatitude(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOLAT)));
                pointGeodetic.setLongitude(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOLON)));
                pointGeodetic.setEllipsoid(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOELLIPS)));
                pointGeodetic.setOrtho(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOORTHO)));

                // Project MetaData
                pointGeodetic.setDateCreated(c.getInt(c.getColumnIndex(JobContract.PointEntry.KEY_DATE_CREATED)));


                lstPoints.add(pointGeodetic);
            } while (c.moveToNext());
        }

        return lstPoints;
    }




    public PointGeodetic getPointByPointNo(SQLiteDatabase db, int point_no){
        Log.d(TAG, "getPointByPointNo: Started...");

        String selectQuery = "SELECT  * FROM " + JobContract.PointEntry.TABLE_NAME + " WHERE "
                + JobContract.PointEntry.KEY_POINT_NO + " = " + point_no;

        Log.e(TAG, selectQuery);


        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        PointGeodetic pointGeodetic = new PointGeodetic();

        pointGeodetic.setId(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_ID))));
        pointGeodetic.setPoint_no(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_NO))));
        pointGeodetic.setNorthing(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_NORTHING)));
        pointGeodetic.setEasting(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_EASTING)));
        pointGeodetic.setElevation(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_ELEVATION)));
        pointGeodetic.setDescription((c.getString(c.getColumnIndex(JobContract.PointEntry.KEY_DESCRIPTION))));
        pointGeodetic.setPointType(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_TYPE))));

        pointGeodetic.setLatitude(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOLAT)));
        pointGeodetic.setLongitude(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOLON)));
        pointGeodetic.setEllipsoid(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOELLIPS)));
        pointGeodetic.setOrtho(c.getDouble(c.getColumnIndex(JobContract.PointEntry.KEY_GEOORTHO)));

        // Project MetaData
        pointGeodetic.setDateCreated(c.getInt(c.getColumnIndex(JobContract.PointEntry.KEY_DATE_CREATED)));

        return pointGeodetic;

    }

    public List<JobSketch> getJobSketchesByPointID(SQLiteDatabase db, int point_id){
        Log.d(TAG, "getJobSketchesByPointID: Started...");
        List<JobSketch> lstprojectSketches = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + JobContract.SketchEntry.TABLE_NAME+  " WHERE "
                + JobContract.SketchEntry.KEY_POINT_ID+ " = " + point_id;

        Log.d(TAG, "SketchesByPointID: " + selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                JobSketch jobSketches = new JobSketch();

                //Required
                jobSketches.setId(c.getInt((c.getColumnIndex(JobContract.SketchEntry.KEY_ID))));
                jobSketches.setPoint_id(c.getInt((c.getColumnIndex(JobContract.SketchEntry.KEY_POINT_ID))));
                jobSketches.setImagePath(c.getString((c.getColumnIndex(JobContract.SketchEntry.KEY_IMAGE_PATH))));

                Log.d(TAG, "getJobSketchesByPointID: " + c.getString((c.getColumnIndex(JobContract.SketchEntry.KEY_IMAGE_PATH))));

                jobSketches.setDateCreated(c.getInt(c.getColumnIndex(JobContract.SketchEntry.KEY_DATE_CREATED)));

                lstprojectSketches.add(jobSketches);
            } while (c.moveToNext());
        }

        Log.d(TAG, "getJobSketchesByPointID: Success");

        c.close();


        return lstprojectSketches;
    }

    public boolean checkPointNumberExists(SQLiteDatabase db, int point_no){
        Log.d(TAG, "checkPointNumberExists: Starting");
        String selectQuery = "SELECT  * FROM " + JobContract.PointEntry.TABLE_NAME + " WHERE "
                + JobContract.PointEntry.KEY_POINT_NO + " = " + point_no;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        boolean results = false;
        if (c.moveToFirst()){
            Log.d(TAG, "checkPointNumberExists: Point Found...");
            results = true;
        }

        c.close();
        db.close();

        return results;

    }

    public int findNextPointNumber(SQLiteDatabase db, int point_no){
        Log.d(TAG, "findNextPointNumber: Started");
        int nextPointNumber = 0;
        List<Integer> lstPoints = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + JobContract.PointEntry.TABLE_NAME
                + " WHERE " + JobContract.PointEntry.KEY_POINT_NO + " > " + point_no
                + " ORDER BY " + JobContract.PointEntry.KEY_POINT_NO + " ASC";

        Log.i(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                lstPoints.add(c.getInt((c.getColumnIndex(JobContract.PointEntry.KEY_POINT_NO))));
            } while (c.moveToNext());
        }

        int sequenceSize = lstPoints.size();

        Log.d(TAG, "findNextPointNumber: List Points Size: " + sequenceSize);

        if(sequenceSize ==0){
            //no other points above point number, add 1 to point_no
            nextPointNumber = point_no + 1;
        }else{
            //run through and find sequence
            int first = lstPoints.get(0);
            for (int i = 0; i < sequenceSize; i++){

                if((first + i) !=lstPoints.get(i)){
                    nextPointNumber = (first + i);
                }else{
                    nextPointNumber = lstPoints.get(lstPoints.size() - 1) + 1;
                }

            }
        }

        Log.d(TAG, "findNextPointNumber: Next Point No is: " + nextPointNumber);

        return nextPointNumber;


    }


    public static long getCountJobSketchByPointID(SQLiteDatabase db, int point_id){
        Log.d(TAG, "getCountJobSketchByPointID: Starting...");

        String countQuery = "SELECT * FROM " + JobContract.SketchEntry.TABLE_NAME +  " WHERE "
                + JobContract.SketchEntry.KEY_POINT_ID + " = " + point_id;

        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        return cnt;

    }

}

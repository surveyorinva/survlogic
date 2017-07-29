package com.survlogic.survlogic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.model.ProjectJobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chrisfillmore on 6/25/2017.
 */

public class ProjectDatabaseHandler extends SQLiteOpenHelper {


    //  Debugging Static Constants
    private static final String TAG = "ProjectDatabaseHandler";

    //  Database Constants
    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "projects.db";

    // Table Creation Queries
    private static final String CREATE_TABLE_PROJECT = "CREATE TABLE "
            + ProjectContract.ProjectEntry.TABLE_NAME
            + "("
            + ProjectContract.ProjectEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ProjectContract.ProjectEntry.KEY_PROJECTNAME + " TEXT,"
            + ProjectContract.ProjectEntry.KEY_PROJECTDESC + " TEXT,"
            + ProjectContract.ProjectEntry.KEY_STORAGESPACE + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_UNITSMEASURE + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_PROJECTION + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_ZONE + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_GEOLAT + " DOUBLE,"
            + ProjectContract.ProjectEntry.KEY_GEOLON + " DOUBLE,"
            + ProjectContract.ProjectEntry.KEY_IMAGE_SYSTEM + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_IMAGE_PATH + " TEXT,"
            + ProjectContract.ProjectEntry.KEY_DATE_CREATED + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_DATE_MODIFIED + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_DATE_ACCESSED + " INTEGER);";

    private static final String CREATE_TABLE_PROJECT_IMAGES = "CREATE TABLE "
            + ProjectContract.ProjectImageEntry.TABLE_NAME
            + "("
            + ProjectContract.ProjectImageEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ProjectContract.ProjectImageEntry.KEY_PROJECT_ID + " INTEGER,"
            + ProjectContract.ProjectImageEntry.KEY_POINT_ID + " INTEGER,"
            + ProjectContract.ProjectImageEntry.KEY_IMAGE_PATH + " TEXT,"
            + ProjectContract.ProjectImageEntry.KEY_IMAGE + " BLOB,"
            + ProjectContract.ProjectImageEntry.KEY_BEARING + " FLOAT,"
            + ProjectContract.ProjectImageEntry.KEY_GEOLAT + " DOUBLE,"
            + ProjectContract.ProjectImageEntry.KEY_GEOLON + " DOUBLE);";

    public static final String CREATE_TABLE_PROJECT_JOBS = "CREATE TABLE "
            + ProjectContract.ProjectJobEntry.TABLE_NAME
            + "("
            + ProjectContract.ProjectJobEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ProjectContract.ProjectJobEntry.KEY_PROJECT_ID + " INTEGER,"
            + ProjectContract.ProjectJobEntry.KEY_JOBNAME + " TEXT,"
            + ProjectContract.ProjectJobEntry.KEY_JOBDBNAME + " TEXT,"
            + ProjectContract.ProjectJobEntry.KEY_JOBDESC + " TEXT,"
            + ProjectContract.ProjectJobEntry.KEY_DATE_CREATED + " INTEGER,"
            + ProjectContract.ProjectJobEntry.KEY_DATE_MODIFIED + " INTEGER,"
            + ProjectContract.ProjectJobEntry.KEY_DATE_ACCESSED + " INTEGER);";

    public ProjectDatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG,"Database connected " + DB_NAME );

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROJECT);
        db.execSQL(CREATE_TABLE_PROJECT_IMAGES);
        db.execSQL(CREATE_TABLE_PROJECT_JOBS);
        Log.d(TAG,"Tables created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        Log.d(TAG, "onUpgrade: Updating Tables to new Version...");
        db.execSQL("DROP IF TABLE EXISTS " + ProjectContract.ProjectEntry.TABLE_NAME);
        db.execSQL("DROP IF TABLE EXISTS " + ProjectContract.ProjectImageEntry.TABLE_NAME);
        db.execSQL("DROP IF TABLE EXISTS " + ProjectContract.ProjectJobEntry.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    //  Creating---------------------------------------------------------------------------------------------------

    /**
     * Adding Project to Database
     */
    public long addProjectToDB(SQLiteDatabase db, Project project){

        Log.d(TAG, "addProjectToDB: Start");
        //  ContentValues is a name value pair, used to get the values from database tables.  Content values object
        //  returned from SQLiteDatabase objects query() function.
        ContentValues contentValues = new ContentValues();

        //  Required Fields
        contentValues.put(ProjectContract.ProjectEntry.KEY_PROJECTNAME,project.getmProjectName());
        contentValues.put(ProjectContract.ProjectEntry.KEY_STORAGESPACE,project.getmStorage());
        contentValues.put(ProjectContract.ProjectEntry.KEY_UNITSMEASURE,project.getmUnits());
        contentValues.put(ProjectContract.ProjectEntry.KEY_PROJECTION,project.getmProjection());
        contentValues.put(ProjectContract.ProjectEntry.KEY_ZONE,project.getmZone());

        //  Optional Fields
        contentValues.put(ProjectContract.ProjectEntry.KEY_GEOLAT,project.getmLocationLat());
        contentValues.put(ProjectContract.ProjectEntry.KEY_GEOLON,project.getmLocationLong());

        contentValues.put(ProjectContract.ProjectEntry.KEY_IMAGE_SYSTEM,project.getmSystemImage());
        contentValues.put(ProjectContract.ProjectEntry.KEY_IMAGE_PATH,project.getmImagePath());

        contentValues.put(ProjectContract.ProjectEntry.KEY_PROJECTDESC,project.getmProjectDescription());

        //  Metadata Fields
        contentValues.put(ProjectContract.ProjectEntry.KEY_DATE_CREATED,(int) (new Date().getTime()/1000));

        Log.d(TAG, "addData: Adding " + project.getmProjectName() + " to " + ProjectContract.ProjectEntry.TABLE_NAME);

        //        Inserts new row
        Long result =  db.insert(ProjectContract.ProjectEntry.TABLE_NAME,null,contentValues);
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
     * Adding Project Images to Database
     */
    public long addProjectImageToDB(SQLiteDatabase db, ProjectImages projectImages){
        Log.d(TAG, "addProjectImageToDB: Start");
        ContentValues contentValues = new ContentValues();

        //  Required Fields
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_PROJECT_ID, projectImages.getProjectId());
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_POINT_ID, projectImages.getPointId());
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_IMAGE_PATH, projectImages.getImagePath());
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_IMAGE, projectImages.getImage());

        //  Optional Fields
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_BEARING, projectImages.getBearingAngle());
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_GEOLAT, projectImages.getLocationLat());
        contentValues.put(ProjectContract.ProjectImageEntry.KEY_GEOLON, projectImages.getLocationLong());

        //  Metadata Fields


        Log.d(TAG, "addData: Adding Image to " + ProjectContract.ProjectImageEntry.TABLE_NAME);

        //        Inserts new row
        Long result =  db.insert(ProjectContract.ProjectImageEntry.TABLE_NAME,null,contentValues);
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
     *Adding Project Jobs to Database
     */

    public long addProjectJobToDB(SQLiteDatabase db, ProjectJobs projectJob){
        Log.d(TAG, "addProjectJobToDB: Start");

        ContentValues contentValues = new ContentValues();

        //  Required Fields
        contentValues.put(ProjectContract.ProjectJobEntry.KEY_PROJECT_ID, projectJob.getProjectId());
        contentValues.put(ProjectContract.ProjectJobEntry.KEY_JOBNAME, projectJob.getmJobName());
        contentValues.put(ProjectContract.ProjectJobEntry.KEY_JOBDBNAME, projectJob.getmJobDbName());


        //  Optional Fields
        contentValues.put(ProjectContract.ProjectJobEntry.KEY_JOBDESC, projectJob.getmJobDescription());

        //  Metadata Fields
        contentValues.put(ProjectContract.ProjectJobEntry.KEY_DATE_CREATED, projectJob.getmDateCreated());

        Log.d(TAG, "addData: Adding Image to " + ProjectContract.ProjectJobEntry.TABLE_NAME);

        //      Inserts new row
        Long result = db.insert(ProjectContract.ProjectJobEntry.TABLE_NAME,null,contentValues);
        db.close();

        if (result==-1){
            Log.d(TAG,"Error, Something went wrong...");
            return -1;
        } else {
            Log.d(TAG,"Success, Row inserted into Table...");
            return result;
        }

    }

    //  Reading---------------------------------------------------------------------------------------------------
        //  ALL Projects

    public static int getCountProjectsAll(SQLiteDatabase db){
        int results = 0;

        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_NAME
                + " ORDER BY " + ProjectContract.ProjectEntry.KEY_PROJECTNAME + " ASC";

        Cursor c = db.rawQuery(selectQuery, null);

        c.moveToFirst();

        results = c.getCount();
        c.close();

        return results;
    }


    public List<Project> getProjectsAll(SQLiteDatabase db){
        Log.d(TAG, "getProjectsAll: Starting");
        
        List<Project> lstprojects = new ArrayList<Project>();
        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_NAME
                + " ORDER BY " + ProjectContract.ProjectEntry.KEY_PROJECTNAME + " ASC";

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Project project = new Project();
                project.setmId(c.getInt((c.getColumnIndex(ProjectContract.ProjectEntry.KEY_ID))));
                project.setmProjectName((c.getString(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_PROJECTNAME))));
                project.setmStorage((c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_STORAGESPACE))));
                project.setmUnits(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_UNITSMEASURE)));
                project.setmProjection(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_PROJECTION)));
                project.setmZone(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_ZONE)));
                project.setmLocationLat(c.getDouble(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_GEOLAT)));
                project.setmLocationLong(c.getDouble(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_GEOLON)));
                project.setmSystemImage(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_IMAGE_SYSTEM)));
                project.setmImagePath((c.getString(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_IMAGE_PATH))));

                project.setmProjectDescription((c.getString(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_PROJECTDESC))));

                // Project MetaData
                project.setmDateCreated(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_DATE_CREATED)));


                lstprojects.add(project);
            } while (c.moveToNext());
        }

        return lstprojects;
    }


    //  Specific Project

    public int getCountProjectsById(long project_id){
        int results = 0;

        String selectQuery = "SELECT  * FROM " + ProjectContract.ProjectEntry.TABLE_NAME + " WHERE "
                + ProjectContract.ProjectEntry.KEY_ID + " = " + project_id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        c.moveToFirst();

        results = c.getCount();
        c.close();

        return results;
    }


    public Project getProjectById(SQLiteDatabase db, long project_id){
//        Version 1 - Pull db in with method.  If possible, see if below will work w/o pull in.
//        SQLiteDatabase db = this.getReadableDatabase();

        Log.d(TAG, "getProjectById: Starting");
        String selectQuery = "SELECT  * FROM " + ProjectContract.ProjectEntry.TABLE_NAME + " WHERE "
                + ProjectContract.ProjectEntry.KEY_ID + " = " + project_id;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

            Project project = new Project();
            project.setmId(c.getInt((c.getColumnIndex(ProjectContract.ProjectEntry.KEY_ID))));
            project.setmProjectName((c.getString(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_PROJECTNAME))));
            project.setmStorage((c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_STORAGESPACE))));
            project.setmUnits(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_UNITSMEASURE)));
            project.setmProjection(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_PROJECTION)));
            project.setmZone(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_ZONE)));
            project.setmLocationLat(c.getDouble(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_GEOLAT)));
            project.setmLocationLong(c.getDouble(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_GEOLON)));
            project.setmSystemImage(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_IMAGE_SYSTEM)));
            project.setmImagePath((c.getString(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_IMAGE_PATH))));
            project.setmProjectDescription((c.getString(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_PROJECTDESC))));

        // Project MetaData
            project.setmDateCreated(c.getInt(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_DATE_CREATED)));

        return project;
    }

    //  Updating

    public int updateProject(SQLiteDatabase db, Project project){
        //  Version 1 - Pull db in with method.  If possible, see if below will work w/o pull in.
//        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProjectContract.ProjectEntry.KEY_PROJECTNAME, project.getmProjectName());
        values.put(ProjectContract.ProjectEntry.KEY_STORAGESPACE, project.getmStorage());
        values.put(ProjectContract.ProjectEntry.KEY_UNITSMEASURE, project.getmUnits());
        values.put(ProjectContract.ProjectEntry.KEY_PROJECTION, project.getmProjection());
        values.put(ProjectContract.ProjectEntry.KEY_ZONE, project.getmZone());
        values.put(ProjectContract.ProjectEntry.KEY_GEOLAT, project.getmLocationLat());
        values.put(ProjectContract.ProjectEntry.KEY_GEOLON, project.getmLocationLong());
        values.put(ProjectContract.ProjectEntry.KEY_IMAGE_SYSTEM, project.getmSystemImage());
        values.put(ProjectContract.ProjectEntry.KEY_IMAGE_PATH, project.getmImagePath());
        values.put(ProjectContract.ProjectEntry.KEY_PROJECTDESC, project.getmProjectDescription());

        Log.e(TAG,"Updating Project ID=" + project.getmId());

        return db.update(ProjectContract.ProjectEntry.TABLE_NAME,values, ProjectContract.ProjectEntry.KEY_ID + " = ?",
                new String[] {String.valueOf(project.getmId())});

    }

    //  Deleting
    public boolean deleteProjectById(SQLiteDatabase db, long project_id){
        boolean results = false;

        db.delete(ProjectContract.ProjectEntry.TABLE_NAME, ProjectContract.ProjectEntry.KEY_ID + "= ?",
                new String[] {String.valueOf(project_id)});

        db.close();
        results = true;
        return results;
    }

    //---------------------------------------------------------------------------------------------------------------------------//

    /**
     *Project Images CRUD
     */

    public List<ProjectImages> getProjectImagesAll(SQLiteDatabase db){

        List<ProjectImages> lstprojectImages = new ArrayList<ProjectImages>();
        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectImageEntry.TABLE_NAME;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                ProjectImages projectImages = new ProjectImages();

                //Required
                projectImages.setId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_ID))));
                projectImages.setProjectId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_PROJECT_ID))));
                projectImages.setPointId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_POINT_ID))));
                projectImages.setImagePath(c.getString((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_IMAGE_PATH))));
                projectImages.setImage(c.getBlob(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_IMAGE)));

                //Optional
                projectImages.setBearingAngle(c.getFloat(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_BEARING)));
                projectImages.setLocationLat(c.getDouble(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_GEOLAT)));
                projectImages.setLocationLong(c.getDouble(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_GEOLON)));

                // MetaData

                lstprojectImages.add(projectImages);
            } while (c.moveToNext());
        }

        return lstprojectImages;
    }

    public ProjectImages getProjectImageById(SQLiteDatabase db, long project_image_id){
        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectImageEntry.TABLE_NAME + " WHERE "
                + ProjectContract.ProjectImageEntry.KEY_ID + " = " + project_image_id;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        ProjectImages projectImages = new ProjectImages();

        //Required
        projectImages.setId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_ID))));
        projectImages.setProjectId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_PROJECT_ID))));
        projectImages.setPointId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_POINT_ID))));
        projectImages.setImagePath(c.getString((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_IMAGE_PATH))));
        projectImages.setImage(c.getBlob(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_IMAGE)));

        //Optional
        projectImages.setBearingAngle(c.getFloat(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_BEARING)));
        projectImages.setLocationLat(c.getDouble(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_GEOLAT)));
        projectImages.setLocationLong(c.getDouble(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_GEOLON)));

        // MetaData


        return projectImages;
    }

    public List<ProjectImages> getProjectImagesbyProjectID(SQLiteDatabase db, long project_id){
        Log.d(TAG, "getProjectImagesbyProjectID: Starting");
        List<ProjectImages> lstprojectImages = new ArrayList<ProjectImages>();
        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectImageEntry.TABLE_NAME+  " WHERE "
                + ProjectContract.ProjectImageEntry.KEY_PROJECT_ID + " = " + project_id;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                ProjectImages projectImages = new ProjectImages();

                //Required
                projectImages.setId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_ID))));
                projectImages.setProjectId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_PROJECT_ID))));
                projectImages.setPointId(c.getInt((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_POINT_ID))));
                projectImages.setImagePath(c.getString((c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_IMAGE_PATH))));
                projectImages.setImage(c.getBlob(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_IMAGE)));

                //Optional
                projectImages.setBearingAngle(c.getFloat(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_BEARING)));
                projectImages.setLocationLat(c.getDouble(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_GEOLAT)));
                projectImages.setLocationLong(c.getDouble(c.getColumnIndex(ProjectContract.ProjectImageEntry.KEY_GEOLON)));

                // MetaData

                lstprojectImages.add(projectImages);
            } while (c.moveToNext());
        }

        return lstprojectImages;
    }

    public static long getCountProjectImagesByProjectID(SQLiteDatabase db, long project_id){
        Log.d(TAG, "getCountProjectImagesByProjectID: Starting");
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectImageEntry.TABLE_NAME+  " WHERE "
                + ProjectContract.ProjectImageEntry.KEY_PROJECT_ID + " = " + project_id;

        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        return cnt;
    }

    public int updateProjectImages(SQLiteDatabase db, ProjectImages projectImages){
        //  Version 1 - Pull db in with method.  If possible, see if below will work w/o pull in.
//        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProjectContract.ProjectImageEntry.KEY_PROJECT_ID, projectImages.getProjectId());
        values.put(ProjectContract.ProjectImageEntry.KEY_POINT_ID, projectImages.getPointId());
        values.put(ProjectContract.ProjectImageEntry.KEY_IMAGE_PATH, projectImages.getImagePath());
        values.put(ProjectContract.ProjectImageEntry.KEY_IMAGE, projectImages.getImage());

        values.put(ProjectContract.ProjectImageEntry.KEY_BEARING, projectImages.getBearingAngle());
        values.put(ProjectContract.ProjectImageEntry.KEY_GEOLAT, projectImages.getLocationLat());
        values.put(ProjectContract.ProjectImageEntry.KEY_GEOLON, projectImages.getLocationLong());

        Log.e(TAG,"Updating Project ID=" + projectImages.getId());

        return db.update(ProjectContract.ProjectImageEntry.TABLE_NAME,values, ProjectContract.ProjectImageEntry.KEY_ID + " = ?",
                new String[] {String.valueOf(projectImages.getId())});

    }


    //  Deleting
    public boolean deleteProjectImageById(SQLiteDatabase db, long project_image_id){
        Log.d(TAG, "deleteProjectImageById: Starting");
        boolean results = false;

        db.delete(ProjectContract.ProjectImageEntry.TABLE_NAME, ProjectContract.ProjectImageEntry.KEY_ID + "= ?",
                new String[] {String.valueOf(project_image_id)});

        db.close();
        results = true;
        return results;
    }

    public boolean deleteProjectImageByProjectId(SQLiteDatabase db, long project_id){
        boolean results = false;

        db.delete(ProjectContract.ProjectImageEntry.TABLE_NAME, ProjectContract.ProjectImageEntry.KEY_PROJECT_ID + "= ?",
                new String[] {String.valueOf(project_id)});

        db.close();
        results = true;
        return results;
    }


    //--------------------------------------------------------------------------------------------------------------------------//

    /**
     * Project Jobs CRUD
     */


    public List<ProjectJobs> getProjectJobsByProjectID(SQLiteDatabase db, long project_id){
        Log.d(TAG, "getProjectJobsbyProjectID: Starting");

        List<ProjectJobs> lstprojectJobs = new ArrayList<ProjectJobs>();

        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectJobEntry.TABLE_NAME+  " WHERE "
                + ProjectContract.ProjectJobEntry.KEY_PROJECT_ID + " = " + project_id;

        Log.e(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                ProjectJobs projectJobs = new ProjectJobs();

                //Required
                projectJobs.setId(c.getInt((c.getColumnIndex(ProjectContract.ProjectJobEntry.KEY_ID))));
                projectJobs.setProjectId(c.getInt((c.getColumnIndex(ProjectContract.ProjectJobEntry.KEY_PROJECT_ID))));
                projectJobs.setmJobName(c.getString((c.getColumnIndex(ProjectContract.ProjectJobEntry.KEY_JOBNAME))));
                projectJobs.setmJobDbName(c.getString((c.getColumnIndex(ProjectContract.ProjectJobEntry.KEY_JOBDBNAME))));

                //Optional
                projectJobs.setmJobDescription(c.getString((c.getColumnIndex(ProjectContract.ProjectJobEntry.KEY_JOBDESC))));

                // MetaData
                projectJobs.setmDateCreated(c.getInt((c.getColumnIndex(ProjectContract.ProjectJobEntry.KEY_DATE_CREATED))));


                lstprojectJobs.add(projectJobs);
            } while (c.moveToNext());
        }

        return lstprojectJobs;
    }


    public static long getCountProjectJobsByProjectID(SQLiteDatabase db, long project_id){
        Log.d(TAG, "getCountProjectJobsByProjectID: Starting");
        String countQuery = "SELECT * FROM " + ProjectContract.ProjectJobEntry.TABLE_NAME+  " WHERE "
                + ProjectContract.ProjectJobEntry.KEY_PROJECT_ID + " = " + project_id;

        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        return cnt;
    }

    //Updating
    public int updateProjectJobs(SQLiteDatabase db, ProjectJobs projectJobs){

        ContentValues values = new ContentValues();
        values.put(ProjectContract.ProjectJobEntry.KEY_PROJECT_ID, projectJobs.getProjectId());
        values.put(ProjectContract.ProjectJobEntry.KEY_JOBNAME, projectJobs.getmJobName());
        values.put(ProjectContract.ProjectJobEntry.KEY_JOBDBNAME, projectJobs.getmJobDbName());
        values.put(ProjectContract.ProjectJobEntry.KEY_JOBDESC, projectJobs.getmJobDescription());

        values.put(ProjectContract.ProjectJobEntry.KEY_DATE_CREATED, projectJobs.getmDateCreated());

        Log.e(TAG,"Updating Project ID=" + projectJobs.getId());

        return db.update(ProjectContract.ProjectJobEntry.TABLE_NAME,values, ProjectContract.ProjectJobEntry.KEY_ID + " = ?",
                new String[] {String.valueOf(projectJobs.getId())});

    }


    //  Deleting
    public boolean deleteProjectJobById(SQLiteDatabase db, long project_job_id){
        Log.d(TAG, "deleteProjectJobById: Starting");
        boolean results = false;

        db.delete(ProjectContract.ProjectJobEntry.TABLE_NAME, ProjectContract.ProjectJobEntry.KEY_ID + "= ?",
                new String[] {String.valueOf(project_job_id)});

        db.close();
        results = true;
        return results;
    }

    public boolean deleteProjectJobByProjectId(SQLiteDatabase db, long project_id){
        boolean results = false;

        db.delete(ProjectContract.ProjectJobEntry.TABLE_NAME, ProjectContract.ProjectJobEntry.KEY_PROJECT_ID + "= ?",
                new String[] {String.valueOf(project_id)});

        db.close();
        results = true;
        return results;
    }


    //--------------------------------------------------------------------------------------------------------------------------//
    public void closeProjectDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()){
            db.close();
        }
    }

}

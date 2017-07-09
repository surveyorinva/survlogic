package com.survlogic.survlogic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.survlogic.survlogic.model.Project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chrisfillmore on 6/25/2017.
 */

public class ProjectDatabaseHandler extends SQLiteOpenHelper {


    //  Debugging Static Constants
    private String TAG = getClass().getSimpleName();

    //  Database Constants
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "projects_v11.db";

    // Table Creation Queries
    private static final String CREATE_TABLE_PROJECT = "CREATE TABLE "
            + ProjectContract.ProjectEntry.TABLE_NAME
            + "("
            + ProjectContract.ProjectEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ProjectContract.ProjectEntry.KEY_PROJECTNAME + " TEXT,"
            + ProjectContract.ProjectEntry.KEY_STORAGESPACE + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_UNITSMEASURE + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_PROJECTION + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_ZONE + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_GEOLAT + " DOUBLE,"
            + ProjectContract.ProjectEntry.KEY_GEOLON + " DOUBLE,"
            + ProjectContract.ProjectEntry.KEY_IMAGE_SYSTEM + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_IMAGE + " BLOB,"
            + ProjectContract.ProjectEntry.KEY_DATE_CREATED + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_DATE_MODIFIED + " INTEGER,"
            + ProjectContract.ProjectEntry.KEY_DATE_ACCESSED + " INTEGER);";

    public ProjectDatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG,"Database created...");


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROJECT);

        Log.d(TAG,"Table created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP IF TABLE EXISTS " + ProjectContract.ProjectEntry.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    //  Creating

    public long addProjectToDB(SQLiteDatabase db, Project project){

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
        contentValues.put(ProjectContract.ProjectEntry.KEY_IMAGE,project.getmImage());

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

    //  Reading
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

        List<Project> lstprojects = new ArrayList<Project>();
        String selectQuery = "SELECT * FROM " + ProjectContract.ProjectEntry.TABLE_NAME
                + " ORDER BY " + ProjectContract.ProjectEntry.KEY_PROJECTNAME + " ASC";

        Log.e(TAG, selectQuery);

//        Version 1 - Pull db in with method.  If possible, see if below will work w/o pull in.
//        SQLiteDatabase db = this.getReadableDatabase();
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
                project.setmImage(c.getBlob(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_IMAGE)));

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
            project.setmImage(c.getBlob(c.getColumnIndex(ProjectContract.ProjectEntry.KEY_IMAGE)));

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
        values.put(ProjectContract.ProjectEntry.KEY_IMAGE, project.getmImage());

        Log.e(TAG,"Updating Project ID=" + project.getmId());

        return db.update(ProjectContract.ProjectEntry.TABLE_NAME,values, ProjectContract.ProjectEntry.KEY_ID + " = ?",
                new String[] {String.valueOf(project.getmId())});

    }


    //  Deleting
    public boolean deleteProject(SQLiteDatabase db, long project_id){
        boolean results = false;

        db.delete(ProjectContract.ProjectEntry.TABLE_NAME, ProjectContract.ProjectEntry.KEY_ID + "= ?",
                new String[] {String.valueOf(project_id)});

        db.close();
        results = true;
        return results;
    }

    public void closeProjectDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()){
            db.close();
        }
    }

}

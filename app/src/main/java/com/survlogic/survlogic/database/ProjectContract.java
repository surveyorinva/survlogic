package com.survlogic.survlogic.database;

/**
 * Created by chrisfillmore on 6/25/2017.
 */

public class ProjectContract {

//    Contract Class:
    //A contract class defines constants that help applications work with the content URIs, column names, intent actions and other
    //features of a content provider.

    //Constructor for this Contract Class
    public ProjectContract() {

    }

    //this is an abstract class.  The purpose of this class is to create the necessary variables that will be
    //used to define the database.

    public static abstract class ProjectEntry{

        public static final String TABLE_NAME = "project_table";

        public static final String KEY_ID = "id";
        public static final String KEY_PROJECTNAME = "name";
        public static final String KEY_STORAGESPACE = "storage";
        public static final String KEY_UNITSMEASURE = "units";
        public static final String KEY_PROJECTION = "projection";
        public static final String KEY_ZONE = "zone";
        public static final String KEY_GEOLAT = "latitude";
        public static final String KEY_GEOLON = "longitude";
        public static final String KEY_IMAGE_SYSTEM = "systemImage";
        public static final String KEY_IMAGE = "image";




    }

}

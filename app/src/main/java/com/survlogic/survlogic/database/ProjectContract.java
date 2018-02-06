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
    public static abstract class ProjectEntry{

        public static final String TABLE_NAME = "project_table";

        public static final String KEY_ID = "id";
        public static final String KEY_PROJECTNAME = "name";
        public static final String KEY_PROJECTDESC = "description";
        public static final String KEY_STORAGESPACE = "storage";
        public static final String KEY_UNITSMEASURE = "units";
        public static final String KEY_PROJECTION = "projection";
        public static final String KEY_PROJECTION_STRING = "projectionString";
        public static final String KEY_PROJECTION_STRATEGY = "projectionStrategy";
        public static final String KEY_PROJECTION_SCALE = "projectionScale";
        public static final String KEY_PROJECTION_ORIGIN_NORTH = "projectionOriginNorth";
        public static final String KEY_PROJECTION_ORIGIN_EAST = "projectionOriginEast";
        public static final String KEY_ZONE = "zone";
        public static final String KEY_ZONE_STRING = "zoneString";
        public static final String KEY_GEOLAT = "latitude";
        public static final String KEY_GEOLON = "longitude";
        public static final String KEY_IMAGE_SYSTEM = "systemImage";
        public static final String KEY_IMAGE_PATH = "imagepath";
        public static final String KEY_DATE_CREATED = "created";
        public static final String KEY_DATE_ACCESSED = "accessed";
        public static final String KEY_DATE_MODIFIED = "modified";


    }

    public static abstract class ProjectImageEntry{
        public static final String TABLE_NAME = "project_image_table";

        public static final String KEY_ID = "id";
        public static final String KEY_PROJECT_ID = "projectid";
        public static final String KEY_JOB_ID = "jobid";
        public static final String KEY_POINT_ID = "pointid";
        public static final String KEY_IMAGE_PATH = "imagepath";
        public static final String KEY_IMAGE = "image";
        public static final String KEY_BEARING = "bearing";
        public static final String KEY_GEOLAT = "latitude";
        public static final String KEY_GEOLON = "longitude";

    }

    public static abstract class ProjectJobEntry{
        public static final String TABLE_NAME = "project_job_table";

        public static final String KEY_ID = "id";
        public static final String KEY_PROJECT_ID = "projectid";
        public static final String KEY_JOBNAME = "jobname";
        public static final String KEY_JOBDBNAME = "jobdbname";
        public static final String KEY_JOBDESC = "description";
        public static final String KEY_DATE_CREATED = "created";
        public static final String KEY_DATE_ACCESSED = "accessed";
        public static final String KEY_DATE_MODIFIED = "modified";

    }



}

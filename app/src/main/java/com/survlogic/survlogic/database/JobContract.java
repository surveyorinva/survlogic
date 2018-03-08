package com.survlogic.survlogic.database;

/**
 * Created by chrisfillmore on 8/4/2017.
 */

public class JobContract {

    public JobContract() {
    }

    public static abstract class PointEntry{

        public static final String TABLE_NAME = "point_table";

        public static final String KEY_ID = "id";
        public static final String KEY_POINT_NO = "point";
        public static final String KEY_NORTHING = "northing";
        public static final String KEY_EASTING = "easting";
        public static final String KEY_ELEVATION = "elevation";
        public static final String KEY_GEOLAT = "latitude";
        public static final String KEY_GEOLON = "longitude";
        public static final String KEY_GEOELLIPS = "ellipsoid";
        public static final String KEY_GEOORTHO = "ortho";
        public static final String KEY_GEOACCURACY = "accuracy";
        public static final String KEY_DESCRIPTION = "description";
        public static final String KEY_DATE_CREATED = "created";
        public static final String KEY_DATE_MODIFIED = "modified";
        public static final String KEY_POINT_PLANAR_TYPE = "pointType";
        public static final String KEY_POINT_GEODETIC_TYPE = "geodeticType";


    }

    public static abstract class SketchEntry {

        public static final String TABLE_NAME = "sketch_table";

        public static final String KEY_ID = "id";
        public static final String KEY_POINT_ID = "pointID";
        public static final String KEY_IMAGE_PATH = "image_path";
        public static final String KEY_IMAGE_PATH_BACKGROUND = "background_image_path";
        public static final String KEY_PATHS = "paths";
        public static final String KEY_CANVAS_X = "canvas_x";
        public static final String KEY_CANVAS_Y = "canvas_y";
        public static final String KEY_DATE_CREATED = "created";
        public static final String KEY_DATE_MODIFIED = "modified";

    }



    public static abstract class JobSettingsEntry{

        public static final String TABLE_NAME = "settings_table";

        public static final String KEY_ID = "id";
        public static final String KEY_JOB_NAME = "job_name";
        public static final String KEY_PROJECT_ID = "project_id";
        public static final String KEY_JOB_ID = "job_id";
        public static final String KEY_UI_FIRST_START = "ui_first_start";
        public static final String KEY_DRAWER_STATE = "drawer_state";
        public static final String KEY_DEFAULT_JOB_TYPE = "default_job_type";
        public static final String KEY_OVER_PROJECTION = "over_projection";
        public static final String KEY_OVER_ZONE = "over_zone";
        public static final String KEY_OVER_PROJECTION_STRING = "over_projection_string";
        public static final String KEY_OVER_PROJECTION_ZONE_STRING = "over_projection_zone_string";
        public static final String KEY_UNITS = "units";
        public static final String KEY_ATTR_CLIENT = "attr_client";
        public static final String KEY_ATTR_MISSION = "attr_mission";
        public static final String KEY_ATTR_WEATHER_GENERAL = "attr_weather_general";
        public static final String KEY_ATTR_WEATHER_TEMP = "attr_weather_temp";
        public static final String KEY_ATTR_WEATHER_PRESS = "attr_weather_press";
        public static final String KEY_ATTR_STAFF_LEADER = "attr_staff_leader";
        public static final String KEY_ATTR_STAFF_STAFF1 = "attr_staff_staff_1";
        public static final String KEY_ATTR_STAFF_STAFF2 = "attr_staff_staff_2";
        public static final String KEY_ATTR_STAFF_OTHER = "attr_staff_other";
        public static final String KEY_SYSTEM_DIST_DISPLAY = "system_dist_display";
        public static final String KEY_SYSTEM_DIST_PREC_DISPLAY = "system_dist_prec_display";
        public static final String KEY_SYSTEM_COORD_PREC_DISPLAY = "system_coord_prec_display";
        public static final String KEY_SYSTEM_ANGLE_DISPLAY = "system_angle_display";
        public static final String KEY_FORMAT_COORD_ENTRY = "format_coord_entry";
        public static final String KEY_FORMAT_ANGLE_HZ_DISPLAY = "format_angle_hz_display";
        public static final String KEY_FORMAT_ANGLE_HZ_OBSERVATION_ENTRY = "format_angle_hz_obs_entry";
        public static final String KEY_FORMAT_ANGLE_VZ_OBSERVATION_DISPLAY = "format_angle_vz_obs_entry";
        public static final String KEY_FORMAT_DISTANCE_HZ_OBSERVATION_DISPLAY = "format_hz_obs_display";
        public static final String KEY_OPTIONS_RAW_FILE = "options_raw_file";
        public static final String KEY_OPTIONS_RAW_TIME_STAMP = "options_raw_time_stamp";
        public static final String KEY_OPTIONS_GPS_ATTRIBUTE= "options_gps_attribute";
        public static final String KEY_OPTIONS_CODE_TABLE = "options_code_table";


    }


}

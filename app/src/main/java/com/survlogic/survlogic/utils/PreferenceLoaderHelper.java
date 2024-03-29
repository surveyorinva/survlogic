package com.survlogic.survlogic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 8/12/2017.
 */

public class PreferenceLoaderHelper {
    private static final String TAG = "PreferenceLoaderHelper";
    private Context mContext;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String general_name, attr_client, attr_mission, attr_weather_general,attr_weather_temp,attr_weather_pres,
            attr_staff_chief, attr_staff_iman, attr_staff_rman, attr_staff_other;

    private String general_over_projection_string, general_over_zone_string;

    private int general_type, general_over_projection, general_over_zone, general_over_units,
            system_distance_display, system_distance_precision_display, system_coordinates_precision_display, system_angle_display,
            format_coord_entry, format_angle_hz_display, format_angle_hz_obsrv_entry, format_angle_vz_obsrv_display, format_distance_hz_obsrv_display,
            raw_file, raw_file_timestamp, raw_gps_attribute, raw_desc_code_list,
            options_drawer_state, options_first_start;

    private int cogoOccupyPoint, cogoBacksightPoint;
    private float cogoOccupyHeight, cogoBacksightHeight;
    private float defaultProjectionScaleGridToGround, defaultProjectionOriginNorth, defaultProjectionOriginEast;

    private int cogoSurveySetHAngle, cogoSurveySetDistance;

    private String gpsSurveyRate;
    private float gpsSurveyHeight;

    private boolean gps_location_use_prediction, gps_location_sensor_use_fusion, gps_location_sensor_use_gmf;

    private boolean camera_location_use;

    public PreferenceLoaderHelper(Context mContext) {
        Log.d(TAG, "PreferenceLoaderHelper: Starting...");
        this.mContext = mContext;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        initPreferencesFromXML();
    }


    private void initPreferencesFromXML(){
        Log.d(TAG, "initPreferencesFromXML: Starting...");

        general_name = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_name),mContext.getString(R.string.general_default));
        general_type = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_type),"0"));
        general_over_projection = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_over_projection),"0"));
        general_over_projection_string = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_over_projection_string),mContext.getString(R.string.general_blank));
        general_over_zone = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_over_zone),"0"));
        general_over_zone_string = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_over_zone_string),mContext.getString(R.string.general_blank));
        general_over_units = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_over_units),"0"));

        Log.d(TAG, "initPreferencesFromXML: Saving Notes Data...");
        //Notes
        attr_client = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_client),mContext.getString(R.string.general_blank));
        attr_mission = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_mission),mContext.getString(R.string.general_blank));
        attr_weather_general = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_weather_general),mContext.getString(R.string.general_blank));
        attr_weather_temp = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_weather_temp),mContext.getString(R.string.general_blank));
        attr_weather_pres = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_weather_pres),mContext.getString(R.string.general_blank));
        attr_staff_chief = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_staff_chief),mContext.getString(R.string.general_blank));
        attr_staff_iman = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_staff_iman),mContext.getString(R.string.general_blank));
        attr_staff_rman = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_staff_rman),mContext.getString(R.string.general_blank));
        attr_staff_other = sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_attr_staff_other),mContext.getString(R.string.general_blank));

        Log.d(TAG, "initPreferencesFromXML: Save Display Data...");
        //Display
        system_distance_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_system_distance_display),"0"));
        system_distance_precision_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_system_distance_precision_display),"0"));
        system_coordinates_precision_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_system_coordinates_precision_display),"0"));

        system_angle_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_system_angle_display),"0"));

        format_coord_entry = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_format_coord_entry),"0"));
        format_angle_hz_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_format_angle_hz_display),"0"));
        format_angle_hz_obsrv_entry = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_format_angle_hz_obsrv_entry),"0"));
        format_angle_vz_obsrv_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_format_angle_vz_obsrv_display),"0"));
        format_distance_hz_obsrv_display = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_format_distance_hz_obsrv_display),"0"));

        Log.d(TAG, "initPreferencesFromXML: Save Raw Data...");
        //Raw Data

        raw_file = 0;
        if(sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_current_job_options_raw_file),true)){
            raw_file = 1;
        }

        raw_file_timestamp = 0;
        if(sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_current_job_options_raw_time_stamp),true)){
            raw_file_timestamp = 1;
        }

        raw_gps_attribute = 0;
        if(sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_current_job_options_gps_attribute),true)){
            raw_gps_attribute = 1;
        }

        raw_desc_code_list = 0;
        if(sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_current_job_options_code_table),true)){
            raw_desc_code_list = 1;
        }

        Log.d(TAG, "initPreferencesFromXML: Save Options...");
        //Options
        options_drawer_state = 0;
        if(sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_current_job_drawer_open),true)){
            options_drawer_state = 1;
        }

        options_first_start = 0;

        cogoOccupyPoint = sharedPreferences.getInt(mContext.getString(R.string.pref_key_current_job_cogo_occupy_point),0);
        cogoBacksightPoint = sharedPreferences.getInt(mContext.getString(R.string.pref_key_current_job_cogo_backsight_point),0);

        cogoOccupyHeight = sharedPreferences.getFloat(mContext.getString(R.string.pref_key_current_job_cogo_occupy_height),0);
        cogoBacksightHeight = sharedPreferences.getFloat(mContext.getString(R.string.pref_key_current_job_cogo_backsight_height),0);

        cogoSurveySetHAngle = sharedPreferences.getInt(mContext.getString(R.string.pref_key_current_job_cogo_sideshot_hAngle),1);
        cogoSurveySetDistance = sharedPreferences.getInt(mContext.getString(R.string.pref_key_current_job_cogo_sideshot_distance),1);

        defaultProjectionScaleGridToGround = sharedPreferences.getFloat(mContext.getString(R.string.pref_key_default_project_projection_scale),1.00F);
        defaultProjectionOriginNorth = sharedPreferences.getFloat(mContext.getString(R.string.pref_key_default_project_projection_origin_North),0.00F);
        defaultProjectionOriginEast = sharedPreferences.getFloat(mContext.getString(R.string.pref_key_default_project_projection_origin_East),0.00F);

        //GPS Options Menu
        gps_location_use_prediction = sharedPreferences.getBoolean(mContext.getString(R.string.pref_gps_location_use_prediction),true);
        gps_location_sensor_use_fusion  = sharedPreferences.getBoolean(mContext.getString(R.string.pref_gps_location_sensor_use_fusion),false);
        gps_location_sensor_use_gmf = sharedPreferences.getBoolean(mContext.getString(R.string.pref_gps_location_sensor_use_gmf),false);

        gpsSurveyRate = sharedPreferences.getString(mContext.getString(R.string.pref_gps_min_time),"1.0s");
        gpsSurveyHeight = sharedPreferences.getFloat(mContext.getString(R.string.pref_gps_height),3.0f);

        camera_location_use = sharedPreferences.getBoolean(mContext.getString(R.string.pref_camera_location_use),true);

    }

    public String getGpsSurveyRate() {
        return gpsSurveyRate;
    }

    public void setGpsSurveyRate(String gpsSurveyRate) {
        this.gpsSurveyRate = gpsSurveyRate;
    }

    public float getGpsSurveyHeight() {
        return gpsSurveyHeight;
    }

    public void setGpsSurveyHeight(float gpsSurveyHeight, boolean isForceSave){
        this.gpsSurveyHeight = gpsSurveyHeight;

        editor = sharedPreferences.edit();
        editor.putFloat(mContext.getString(R.string.pref_gps_height),gpsSurveyHeight);

        if(isForceSave){
            editor.commit();
        }else{
            editor.apply();
        }
    }

    public String getValueSystemDistancePrecisionDisplay() {
        String results;
        switch (system_distance_precision_display){

            case 0:
                results = "0";
                break;

            case 1:
                results = "0.0";
                break;

            case 2:
                results = "0.00";
                break;

            case 3:
                results = "0.000";
                break;

            case 4:
                results = "0.0000";
                break;

            default:
                results = "0.00";
                break;

        }

        return results;
    }


    public String getValueSystemCoordinatesPrecisionDisplay() {
        String results;
        switch (system_coordinates_precision_display){

            case 0:
                results = "0";
                break;

            case 1:
                results = "0.0";
                break;

            case 2:
                results = "0.00";
                break;

            case 3:
                results = "0.000";
                break;

            case 4:
                results = "0.0000";
                break;

            default:
                results = "0.00";
                break;

        }

        return results;
    }


    /**
     * Returns if Coordinate Format is in pattern Northing - Easting
     * @return True if pattern is Northing - Easting
     */

    public boolean getValueFormatCoordinateEntry(){
        boolean results = false;

        if(format_coord_entry == 0){
            results = true;
        }

        return results;
    }

    public int getCogoOccupyPoint(){
        return cogoOccupyPoint;
    }

    public int getCogoBacksightPoint(){
        return cogoBacksightPoint;
    }

    public double getCogoOccupyHeight(){
        return (double) cogoOccupyHeight;
    }

    public double getCogoBacksightHeight(){
        return (double) cogoBacksightHeight;
    }

    public void setCogoOccupyPoint(int pointNo){
        editor = sharedPreferences.edit();

        editor.putInt(mContext.getString(R.string.pref_key_current_job_cogo_occupy_point),pointNo);

        editor.apply();
    }

    public void setCogoBacksightPoint(int pointNo){
        editor = sharedPreferences.edit();

        editor.putInt(mContext.getString(R.string.pref_key_current_job_cogo_backsight_point),pointNo);

        editor.apply();
    }

    public void setCogoOccupyHeight(double height){
        editor = sharedPreferences.edit();


        editor.putFloat(mContext.getString(R.string.pref_key_current_job_cogo_occupy_height),(float) height);

        editor.apply();
    }

    public void setCogoBacksightHeight(double height){
        editor = sharedPreferences.edit();

        editor.putFloat(mContext.getString(R.string.pref_key_current_job_cogo_backsight_height),(float) height);

        editor.apply();
    }

    public void clearCogoSettings(){
        Log.d(TAG, "clearCogoSettings: Clearing");
        editor = sharedPreferences.edit();

        editor.putInt(mContext.getString(R.string.pref_key_current_job_cogo_occupy_point),0);
        editor.putInt(mContext.getString(R.string.pref_key_current_job_cogo_backsight_point),0);
        editor.putFloat(mContext.getString(R.string.pref_key_current_job_cogo_backsight_height),0f);
        editor.putFloat(mContext.getString(R.string.pref_key_current_job_cogo_backsight_height),0f);

        editor.apply();
    }


    public int getSurveyGeneralType(){
        return general_type;
    }

    public int getSurveyFormatAngleVZ(){
        return  format_angle_vz_obsrv_display;
    }

    public int getSurveyFormatAngleHZ(){
        return system_angle_display;
    }

    public int getCogoSurveySetHAngle(){
        return cogoSurveySetHAngle;
    }

    public int getCogoSurveySetDistance(){
        return cogoSurveySetDistance;
    }

    public void setCogoSurveyHAngle(int hAngleType){
        editor = sharedPreferences.edit();

        editor.putInt(mContext.getString(R.string.pref_key_current_job_cogo_sideshot_hAngle),hAngleType);

        editor.apply();
    }

    public void setCogoSurveyDistance(int distanceType){
        editor = sharedPreferences.edit();

        editor.putInt(mContext.getString(R.string.pref_key_current_job_cogo_sideshot_distance),distanceType);

        editor.apply();
    }

    public int getFormatAngleHzDisplay(){
        return format_angle_hz_display;
    }

    public void setDefaultProjectProjectionScale(float scaleFactor){
        this.defaultProjectionScaleGridToGround = scaleFactor;
    }

    public float getDefaultProjectProjectionScaleGridToGround(){
        return defaultProjectionScaleGridToGround;
    }

    public void setDefaultProjectProjectionOriginNorth(float northing){
        this.defaultProjectionOriginNorth = northing;
    }

    public float getDefaultProjectProjectionOriginNorth(){
        return defaultProjectionOriginNorth;
    }

    public void setDefaultProjectProjectionOriginEast(float easting){
        this.defaultProjectionOriginEast = easting;
    }

    public float getDefaultProjectProjectionOriginEast(){
        return defaultProjectionOriginEast;
    }


    public int getGeneral_over_projection() {
        return general_over_projection;
    }

    public void setGeneral_over_projection(int general_over_projection) {
        this.general_over_projection = general_over_projection;
    }

    public int getGeneral_over_zone() {
        return general_over_zone;
    }

    public void setGeneral_over_zone(int general_over_zone) {
        this.general_over_zone = general_over_zone;
    }

    public String getGeneral_over_projection_string() {
        return general_over_projection_string;
    }

    public void setGeneral_over_projection_string(String general_over_projection_string) {
        this.general_over_projection_string = general_over_projection_string;
    }

    public String getGeneral_over_zone_string() {
        return general_over_zone_string;
    }

    public void setGeneral_over_zone_string(String general_over_zone_string) {
        this.general_over_zone_string = general_over_zone_string;
    }


    public String getJob_name() {
        return general_name;
    }

    public void setJob_name(String general_name) {
        this.general_name = general_name;
    }

    public int getGeneral_over_units() {
        return general_over_units;
    }

    public void setGeneral_over_units(int general_over_units) {
        this.general_over_units = general_over_units;
    }

    public boolean getGPSLocationUsePrediction(){
        return gps_location_use_prediction;
    }

    public void setGPSLocationUsePrediction(boolean usePrediction){
        this.gps_location_use_prediction = usePrediction;

        editor = sharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.pref_gps_location_use_prediction),usePrediction);
        editor.apply();


    }


    public boolean getGPSLocationSensorUseFusion(){
        return gps_location_sensor_use_fusion;
    }

    public void setGPSLocationSensorUseFusion(boolean useFusion, boolean isForceSave){
        this.gps_location_sensor_use_fusion = useFusion;
        editor = sharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.pref_gps_location_sensor_use_fusion),useFusion);

        if(isForceSave){
            editor.commit();
        }else{
            editor.apply();
        }

    }

    public boolean getGPSLocationSensorUseGMF(){
        return gps_location_sensor_use_gmf;
    }

    public void setGPSLocationSensorUseGMF(boolean useGMF, boolean isForceSave){
        this.gps_location_sensor_use_gmf = useGMF;

        editor = sharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.pref_gps_location_sensor_use_gmf),useGMF);

        if(isForceSave){
            editor.commit();
        }else{
            editor.apply();
        }
    }

    public boolean getCameraLocationUse(){
        return camera_location_use;
    }

    public void setCameraLocationUse(boolean useGps,  boolean isForceSave){
        this.camera_location_use = useGps;

        editor = sharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.pref_camera_location_use),useGps);

        if(isForceSave){
            editor.commit();
        }else{
            editor.apply();
        }


    }

}

package com.survlogic.survlogic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
    private int general_type, general_over_projection, general_over_zone, general_over_units,
            system_distance_display, system_distance_precision_display, system_coordinates_precision_display, system_angle_display,
            format_coord_entry, format_angle_hz_display, format_angle_hz_obsrv_entry, format_angle_vz_obsrv_display, format_distance_hz_obsrv_display,
            raw_file, raw_file_timestamp, raw_gps_attribute, raw_desc_code_list,
            options_drawer_state, options_first_start;


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
        general_over_zone = Integer.parseInt(sharedPreferences.getString(mContext.getString(R.string.pref_key_current_job_over_zone),"0"));
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




}

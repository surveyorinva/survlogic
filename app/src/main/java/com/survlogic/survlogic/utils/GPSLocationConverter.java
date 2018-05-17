package com.survlogic.survlogic.utils;

/**
 * Created by chrisfillmore on 5/19/2017.
 */

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.survlogic.survlogic.R;

public class GPSLocationConverter {

    public static String getLatitudeAsDMS(Location location, int decimalPlace){
        String strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
        strLatitude = replaceDelimiters(strLatitude, decimalPlace);
        strLatitude = strLatitude + " N";
        return strLatitude;
    }

    public static String getLongitudeAsDMS(Location location, int decimalPlace){
        String strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);
        strLongitude = replaceDelimiters(strLongitude, decimalPlace);
        strLongitude = strLongitude + " W";
        return strLongitude;
    }

    @NonNull
    private static String replaceDelimiters(String str, int decimalPlace) {
        str = str.replaceFirst(":", "°");
        str = str.replaceFirst(":", "'");
        int pointIndex = str.indexOf(".");
        int endIndex = pointIndex + 1 + decimalPlace;
        if(endIndex < str.length()) {
            str = str.substring(0, endIndex);
        }
        str = str + "\"";
        return str;
    }

    public static double convertMetersToValue(double valueIN, int unitType){
        double valueOUT = 0;

        switch (unitType){
            case 1:  //US Survey Foot
                valueOUT = valueIN * 3.2808333333465;
                break;

            case 2:  //International Foot
                valueOUT = valueIN * 3.2808398950131;
                break;

            case 3:
                valueOUT = valueIN * 1;
                break;

            default:
                valueOUT = 0;
                break;
        }

        return valueOUT;
    }


    public static String getLocationAccuracyStatus(Context context, Location location){
        String valueOut;

        // Accuracy
        float mPositionAutonomousMeters = 20;
        float mPositionFloatMeters = 10;
        float mPositionFixedMeters = 5;
        float mAccuarcy = location.getAccuracy();

        if (mAccuarcy > mPositionAutonomousMeters){
            valueOut = context.getResources().getString(R.string.gnss_status_no_fix);

        }else if(mAccuarcy < mPositionAutonomousMeters && mAccuarcy > mPositionFloatMeters){
            valueOut = context.getResources().getString(R.string.gnss_status_autonomous);

        }else if(mAccuarcy < mPositionFloatMeters && mAccuarcy > mPositionFixedMeters){
            valueOut = context.getResources().getString(R.string.gnss_status_float);

        }else {
            valueOut = context.getResources().getString(R.string.gnss_status_fix);

        }
        return valueOut;
    }

}
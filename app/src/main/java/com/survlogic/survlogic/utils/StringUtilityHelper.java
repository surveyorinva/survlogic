package com.survlogic.survlogic.utils;

import android.util.Log;

/**
 * Created by chrisfillmore on 8/13/2017.
 */

public class StringUtilityHelper {

    private static final String TAG = "StringUtilityHelper";

    public static boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        return (string==null || string.trim().equals(""));
    }



}

package com.survlogic.survlogic.utils;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by chrisfillmore on 8/13/2017.
 */



public class StringUtilityHelper {

    private static final String TAG = "StringUtilityHelper";

    /**
     * isStringNull
     * @param string
     * @return True if String is Empty
     */

    public static boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        return (string==null || string.trim().equals(""));
    }


    public static DecimalFormat createUSNonBiasDecimalFormat(){
        DecimalFormatSymbols dcf = new DecimalFormatSymbols(Locale.US);
        dcf.setDecimalSeparator('.');

        DecimalFormat result = new DecimalFormat("0.##",dcf);
        return result;




    }



}

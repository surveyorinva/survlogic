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

        DecimalFormat result = new DecimalFormat("#,##0.00",dcf);
        return result;


    }

    public static DecimalFormat createUSNonBiasDecimalFormatSelect(int noDecimal){
        DecimalFormatSymbols dcf = new DecimalFormatSymbols(Locale.US);
        dcf.setDecimalSeparator('.');

        DecimalFormat result;
        switch(noDecimal){
            case 0:
                result = new DecimalFormat("#,##0",dcf);
                break;
            case 1:
                result = new DecimalFormat("#,##0.0",dcf);
                break;
            case 2:
                result = new DecimalFormat("#,##0.00",dcf);
                break;
            case 3:
                result = new DecimalFormat("#,##0.000",dcf);
                break;
            case 4:
                result = new DecimalFormat("#,##0.0000",dcf);
                break;
            case 5:
                result = new DecimalFormat("#,##0.00000",dcf);
                break;
            default:
                result = new DecimalFormat("#,##0.00",dcf);
                break;

        }

        return result;
    }

    public static DecimalFormat createUSGeodeticDecimalFormat(){
        DecimalFormatSymbols dcf = new DecimalFormatSymbols(Locale.US);
        dcf.setDecimalSeparator('.');

        DecimalFormat result = new DecimalFormat("0.0000",dcf);
        return result;


    }

}

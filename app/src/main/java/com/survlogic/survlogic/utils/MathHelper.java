package com.survlogic.survlogic.utils;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrisfillmore on 6/17/2017.
 */

public class MathHelper {


    public static double createAverageValueFromArray(List<Double> arrayList){
        double valueTotal = 0.00;
        for(int i=0; i < arrayList.size(); i++){
            valueTotal += arrayList.get(i);
        }
        double avg = (valueTotal/arrayList.size());

        return avg;

    }


    public static String convertDECtoDMS(double angle,int decimalPlace, boolean signCounts){
        boolean neg = angle < 0;
        String myAngleDMS;

        angle = Math.abs(angle);

        if (angle ==0){
            myAngleDMS = "0";
        }else{
            double deg = Math.floor(angle);                  // Round down
            double minutes = Math.floor((angle * 60) % 60);  // This is an integer in the range [0, 60)
            double seconds = (angle * 3600) % 60;            // This is a decimal in the range [0, 60)

            myAngleDMS = (int)deg + "°" + (int)minutes + "'"+ seconds;

            int pointIndex = myAngleDMS.indexOf(".");
            int endIndex = pointIndex + 1 + decimalPlace;
            if(endIndex < myAngleDMS.length()) {
                myAngleDMS = myAngleDMS.substring(0, endIndex);
            }

            if (neg){
                myAngleDMS = myAngleDMS + "\" W";
            }else{
                myAngleDMS = myAngleDMS + "\" E";
            }
        }

        return myAngleDMS;
    }

}

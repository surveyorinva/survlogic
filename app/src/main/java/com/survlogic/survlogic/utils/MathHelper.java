package com.survlogic.survlogic.utils;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by chrisfillmore on 6/17/2017.
 */

public class MathHelper {

    private Context mContext;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    public MathHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Creates an average value from an Array list of values
     * @param arrayList
     * @return
     */

    public static double createAverageValueFromArray(List<Double> arrayList){
        double valueTotal = 0.00;
        for(int i=0; i < arrayList.size(); i++){
            valueTotal += arrayList.get(i);
        }
        double avg = (valueTotal/arrayList.size());

        return avg;

    }


    /**
     * Covert decimal degrees to Degrees - Minutes - Seconds.
     * If the entry requires the cardinal direction to be included, signCounts = true
     * @param angle
     * @param decimalPlace
     * @param signCounts
     * @return
     */


    public static String convertDECtoDMS(double angle,int decimalPlace, boolean signCounts){
        boolean neg = angle < 0;
        String myAngleDMS;

        //TODO Deal with 0 for decimal place.  right now returns with "."  need to return " "

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


    /**
     * A simple algorithm to generate a random generate of code based upon size entered
     * @param sizeOfRandomString
     * @return
     */

    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

}

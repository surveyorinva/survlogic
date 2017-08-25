package com.survlogic.survlogic.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by chrisfillmore on 6/17/2017.
 */

public class MathHelper {

    private static final String TAG = "MathHelper";
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
        Log.d(TAG, "createAverageValueFromArray: Started...");
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
        Log.d(TAG, "convertDECtoDMS: Started...");
        
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

    public static double convertPartsToDEC(String degreeIn, String minuteIn, String secondIn){
        Log.d(TAG, "convertPartsToDEC: Started...");
        double results = 0;

        double degreeOut = 0, minuteOut = 0;
        double secondOut = 0;

        degreeOut = Double.parseDouble(degreeIn);

        boolean neg = degreeOut < 0;
        degreeOut = Math.abs(degreeOut);

        minuteOut = Double.parseDouble(minuteIn) * (1/60d);
        secondOut = Double.parseDouble(secondIn) * (1/60d) * (1/60d);

        Log.d(TAG, "_V:Degree IN: " + degreeIn);
        Log.d(TAG, "_V:Degree OUT: " + degreeOut);

        Log.d(TAG, "_V:Minute IN: " + minuteIn);
        Log.d(TAG, "_V:Minute OUT: " + minuteOut);

        Log.d(TAG, "_V:Second IN: " + secondIn);
        Log.d(TAG, "_V:Second OUT: " + secondOut);

        double angle = degreeOut + minuteOut + secondOut;

        if (neg){
            results = angle * -1;
        }else{
            results = angle;
        }

        return results;
    }

    public static int convertDECToDegree(double angle){
        Log.d(TAG, "convertDECToDegree: Started for: " + angle);
        int results;
        boolean neg = angle < 0;

        angle = Math.abs(angle);

        if (angle ==0){
            results = 0;
        }else {
            if(neg){
                results = (int) Math.floor(angle) * -1;
            }else{
                results = (int) Math.floor(angle);                  // Round down
            }

        }
        return results;

    }

    public static int convertDECToMinute(double angle){
        int results;

        boolean neg = angle < 0;

        angle = Math.abs(angle);

        if (angle ==0){
            results = 0;
        }else {
            results = (int) Math.abs(Math.floor((angle * 60) % 60));

        }
        return results;

    }

    public static double convertDECToSeconds(double angle){
        double results;

        if (angle ==0){
            results = 0;
        }else {
            results = Math.abs((angle * 3600) % 60);

        }
        return results;

    }


    /**
     * Convert a DEC to the individual parts
     * @param angle is a double DEC
     * @param partNo which part to return.  1 returns Degree, 2 returns Minute, 3 returns Seconds
     * @return String
     */


    public static String convertDECToParts(double angle, int partNo){
        String results;
        double degrees, minutes, seconds;

        if (angle ==0){
            results = "0";
        }else {
            degrees = Math.floor(angle);                  // Round down
            minutes = Math.abs(Math.floor((angle * 60) % 60));  // This is an integer in the range [0, 60)
            seconds = Math.abs((angle * 3600) % 60);            // This is a decimal in the range [0, 60)

            switch (partNo) {
                case 1: //degree
                    results = String.valueOf((int)degrees);
                    Log.d(TAG, "convertDECToParts: Degrees: " + results);
                    break;

                case 2: //minutes
                    results = String.valueOf((int)minutes);
                    Log.d(TAG, "convertDECToParts: Minutes: " + results);

                    break;


                case 3://seconds
                    results = String.valueOf(seconds);
                    Log.d(TAG, "convertDECToParts: Seconds: " + results);
                    break;

                default:
                    results = "0";
            }
        }
        return results;
    }



    /**
     * A simple algorithm to generate a random generate of code based upon size entered
     * @param sizeOfRandomString
     * @return
     */

    public static String getRandomString(final int sizeOfRandomString){
        Log.d(TAG, "getRandomString: Started...");
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

}

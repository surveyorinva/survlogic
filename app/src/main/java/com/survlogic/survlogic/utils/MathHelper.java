package com.survlogic.survlogic.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointSurvey;

import java.math.BigDecimal;
import java.math.RoundingMode;
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


    public static String convertDECtoDMSGeodetic(double angle, int decimalPlace, boolean signCounts){
        Log.d(TAG, "convertDECtoDMSGeodetic: Started...");
        
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

    public static double convertDECToSeconds(double angle, int decimalPlace){
        double results;

        if (angle ==0){
            results = 0;
        }else {
            results = Math.abs((angle * 3600) % 60);

        }

        BigDecimal bd = new BigDecimal(results);
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);

        return bd.doubleValue();

    }

    public static String convertDECtoDMSAzimuth(double angle, int decimalPlace){
        Log.d(TAG, "convertDECtoDMSGeodetic: Started...");

        boolean neg = angle < 0;
        String myAngleDMS;

        //TODO Deal with 0 for decimal place.  right now returns with "."  need to return " "

        angle = Math.abs(angle);

        if (angle ==0){
            myAngleDMS = "0";
        }else {
            double deg = Math.floor(angle);                  // Round down
            double minutes = Math.floor((angle * 60) % 60);  // This is an integer in the range [0, 60)
            double seconds = (angle * 3600) % 60;            // This is a decimal in the range [0, 60)

            myAngleDMS = (int) deg + "°" + (int) minutes + "'" + seconds;

        }


        int pointIndex = myAngleDMS.indexOf(".");
        int endIndex;

        if (decimalPlace==0){
            endIndex = pointIndex;
        }else {
            endIndex = pointIndex + 1 + decimalPlace;
        }

        if (endIndex < myAngleDMS.length()) {
            myAngleDMS = myAngleDMS.substring(0, endIndex);
        }

        myAngleDMS = myAngleDMS + "\"";


        return myAngleDMS;
    }

    public static String convertDECtoDMSBearing(double angle, int decimalPlace){
        Log.d(TAG, "convertDECtoDMSBearing: Started...");
        Log.d(TAG, "convertDECtoDMSBearing: Angle: " + angle);
        Log.d(TAG, "convertDECtoDMSBearing: Decimal Place: " + decimalPlace);

        String frontDirection, rearDirection;
        String myAngleDMS;

        //TODO Deal with 0 for decimal place.  right now returns with "."  need to return " "

        angle = Math.abs(angle);
        Log.d(TAG, "convertDECtoDMSBearing: Angle: " + angle);

        int quadrant = Integer.parseInt(Integer.toString((int) angle).substring(0, 1));
        Log.d(TAG, "convertDECtoDMSBearing: Quadrant: " + quadrant);
        switch(quadrant){
            case 1:
                angle = angle - 100;
                frontDirection = "N";
                rearDirection = "E";
                break;
            case 2:
                angle = angle - 200;
                frontDirection = "S";
                rearDirection = "E";
                break;
            case 3:
                angle = angle - 300;
                frontDirection = "S";
                rearDirection = "W";
                break;
            case 4:
                angle = angle - 400;
                frontDirection = "N";
                rearDirection = "W";
                break;
            default:
                frontDirection = "N";
                rearDirection = "E";
        }

        Log.d(TAG, "convertDECtoDMSBearing: Angle: " + angle);
        if (angle ==0){
            Log.d(TAG, "convertDECtoDMSBearing: Angle is 0");
            myAngleDMS = "0.0";
        }else {
            Log.d(TAG, "convertDECtoDMSBearing: Angle is Not 0");
            double deg = Math.floor(angle);                  // Round down
            double minutes = Math.floor((angle * 60) % 60);  // This is an integer in the range [0, 60)
            double seconds = (angle * 3600) % 60;            // This is a decimal in the range [0, 60)

            myAngleDMS = (int) deg + "°" + (int) minutes + "'" + seconds;

        }

        Log.d(TAG, "convertDECtoDMSBearing: myAngleDMS: " + myAngleDMS);
        int pointIndex = myAngleDMS.indexOf(".");
        int endIndex;

        Log.d(TAG, "convertDECtoDMSBearing: pointIndex: " + pointIndex);

        if (decimalPlace==0){
            endIndex = pointIndex;
        }else {
            endIndex = pointIndex + 1 + decimalPlace;
        }

        if (endIndex < myAngleDMS.length()) {
            myAngleDMS = myAngleDMS.substring(0, endIndex);
        }

        myAngleDMS = frontDirection + " " + myAngleDMS + "\" " + rearDirection;


        return myAngleDMS;
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


    public static double inverseAzimuthFromPointSurvey(PointSurvey pointSurvey1, PointSurvey pointSurvey2){
        double results;

        double northing1 = pointSurvey1.getNorthing();
        double northing2 = pointSurvey2.getNorthing();

        double easting1 = pointSurvey1.getEasting();
        double easting2 = pointSurvey2.getEasting();

        double deltaNorth, deltaEast;

        deltaNorth = northing2 - northing1;
        deltaEast = easting2 - easting1;

        results = (deltaEast)/(deltaNorth);
        results = Math.atan(results);
        results = (180/Math.PI) * results;

        boolean isNorthPos = false;
        boolean isEastPos = false;

        if (deltaNorth > 0){
            isNorthPos = true;
        }else{
            isNorthPos = false;
        }

        if (deltaEast > 0){
            isEastPos = true;
        }else{
            isEastPos = false;
        }


        if(isNorthPos && isEastPos) { //Q1
            results = Math.abs(results);
        }else if(!isNorthPos && isEastPos){ //Q2
            results = 180 - Math.abs(results);

        }else if(!isNorthPos && !isEastPos) { //Q3
            results = 180 + Math.abs(results);

        }else if(isNorthPos && !isEastPos) { //Q4
            results = 360 - Math.abs(results);

        }

        return results;
    }

    public static double inverseBearingFromPointSurvey(PointSurvey pointSurvey1, PointSurvey pointSurvey2){
        double results;

        double northing1 = pointSurvey1.getNorthing();
        double northing2 = pointSurvey2.getNorthing();

        double easting1 = pointSurvey1.getEasting();
        double easting2 = pointSurvey2.getEasting();

        double deltaNorth, deltaEast;

        deltaNorth = northing2 - northing1;
        deltaEast = easting2 - easting1;

        results = (deltaEast)/(deltaNorth);
        results = Math.atan(results);
        results = (180/Math.PI) * results;

        boolean isNorthPos = false;
        boolean isEastPos = false;

        if (deltaNorth > 0){
            isNorthPos = true;
        }else{
            isNorthPos = false;
        }

        if (deltaEast > 0){
            isEastPos = true;
        }else{
            isEastPos = false;
        }


        if(isNorthPos && isEastPos) { //Q1
            results = 100 + Math.abs(results);
        }else if(!isNorthPos && isEastPos){ //Q2
            results = 200 + Math.abs(results);

        }else if(!isNorthPos && !isEastPos) { //Q3
            results = 300 + Math.abs(results);

        }else if(isNorthPos && !isEastPos) { //Q4
            results = 400 + Math.abs(results);

        }

        return results;
    }

    public static double convertSlopeDistanceToHorizontalDistanceByZenith(double slopeDistance, double zenithAngle){
        double results=0;
        double zenithAngleInRadians = Math.toRadians(zenithAngle);

        results = slopeDistance * Math.sin(zenithAngleInRadians);


        return results;
    }

    public static double convertSlopeDistanceToDeltaElevationByZenith(double slopeDistance, double zenithAngle){
        double results=0;
        double zenithAngleInRadians = Math.toRadians(zenithAngle);

        results = slopeDistance * Math.cos(zenithAngleInRadians);


        return results;
    }

    public static Point solveForCoordinatesFromTurnedAngleAndDistance(PointSurvey occupyPointSurvey, PointSurvey backsightPointSurvey, double hzAngle, double sDistance, double znAngle){
        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Started");
        Point results = new Point();
        double observedAzimuth, hzDistance, dNorth, dEast;
        double northing, easting;

        //determine Azimuth
        double baseAzimuth = MathHelper.inverseAzimuthFromPointSurvey(occupyPointSurvey,backsightPointSurvey);

        observedAzimuth = baseAzimuth + hzAngle;
        observedAzimuth = (Math.PI/180) * observedAzimuth;

        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Observed Azimith: " + observedAzimuth);

        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);

        dNorth = hzDistance * Math.cos(observedAzimuth);
        dEast = hzDistance * Math.sin(observedAzimuth);

        Log.d(TAG, "MathHelper: dN: " + dNorth + " dEast: " + dEast);

        northing = occupyPointSurvey.getNorthing() + dNorth;
        easting = occupyPointSurvey.getEasting() + dEast;

        Log.d(TAG, "Northing: " + northing + ", Easting: " + easting);

        results.setNorthing(northing);
        results.setEasting(easting);
        results.setElevation(0d);

        return results;

    }

    public static Point solveForCoordinatesFromTurnedAngleAndDistance(PointSurvey occupyPointSurvey, PointSurvey backsightPointSurvey, double hzAngle, double sDistance, double znAngle, double heightInstrument, double targetHeight){
        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Started");
        Point results = new Point();
        double observedAzimuth, hzDistance, dNorth, dEast, dElev;
        double northing, easting, elevation;

        //determine Azimuth
        double baseAzimuth = MathHelper.inverseAzimuthFromPointSurvey(occupyPointSurvey,backsightPointSurvey);

        observedAzimuth = baseAzimuth + hzAngle;
        observedAzimuth = (Math.PI/180) * observedAzimuth;

        Log.i(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Observed Azimith: " + observedAzimuth);

        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);

        dNorth = hzDistance * Math.cos(observedAzimuth);
        dEast = hzDistance * Math.sin(observedAzimuth);

        Log.i(TAG, "MathHelper: dN: " + dNorth + " dEast: " + dEast);

        northing = occupyPointSurvey.getNorthing() + dNorth;
        easting = occupyPointSurvey.getEasting() + dEast;

        Log.i(TAG, "Northing: " + northing + ", Easting: " + easting);

        dElev = convertSlopeDistanceToDeltaElevationByZenith(sDistance,znAngle);

        elevation = occupyPointSurvey.getElevation() + heightInstrument + dElev - targetHeight;

        Log.i(TAG, "Elevation: " + elevation);
        results.setNorthing(northing);
        results.setEasting(easting);
        results.setElevation(elevation);

        return results;

    }


}

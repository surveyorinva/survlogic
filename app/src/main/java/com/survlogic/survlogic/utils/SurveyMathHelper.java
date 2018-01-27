package com.survlogic.survlogic.utils;

import android.content.Context;
import android.util.Log;

import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointSurvey;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

/**
 * Created by chrisfillmore on 6/17/2017.
 */

public class SurveyMathHelper {

    private static final String TAG = "SurveyMathHelper";
    private Context mContext;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    public SurveyMathHelper(Context mContext) {
        this.mContext = mContext;
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

    public static String convertDECtoDMS(double angle, int decimalPlace){
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

            //myAngleDMS = (int) deg + "°" + (int) minutes + "'" + seconds;

            String sDeg, sMin, sSec;

            if(deg <10){
                sDeg = "0" + (int)deg + "°";
            }else{
                sDeg = (int) deg + "°";
            }

            if(minutes <10){
                sMin = "0" + (int)minutes + "'";
            }else{
                sMin = (int) minutes + "'";
            }

            if(seconds <10){
                sSec = "0" + (int) seconds + ".0";
            }else{
                sSec = (int) seconds + ".0";
            }

            myAngleDMS = sDeg + "" + sMin + "" + sSec;
            Log.d(TAG, "convertDECtoDMS: Angle: " + myAngleDMS);

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

    public static double convertDECBearingtToDECAzimuth(double bearing){
        Log.d(TAG, "convertDECBearingtToDECAzimuth: Started...");
        Log.d(TAG, "convertDECBearingtToDECAzimuth: Bearing: " + bearing);

        int quadrant = Integer.parseInt(Integer.toString((int) bearing).substring(0, 1));
        Log.d(TAG, "convertDECBearingtToDECAzimuth: Quadrant: " + quadrant);
        double angle = 0;

        switch(quadrant){
            case 1:
                angle = bearing - 100;
                Log.d(TAG, "convertDECBearingtToDECAzimuth: Angle: " + angle);
                break;
            case 2:
                angle = bearing - 200;
                angle = 180 - angle;
                Log.d(TAG, "convertDECBearingtToDECAzimuth: Angle: " + angle);

                break;
            case 3:
                angle = bearing - 300;
                angle = 180 + angle;
                Log.d(TAG, "convertDECBearingtToDECAzimuth: Angle: " + angle);
                break;
            case 4:
                angle = bearing - 400;
                angle = 360 - angle;
                Log.d(TAG, "convertDECBearingtToDECAzimuth: Angle: " + angle);
                break;
            default:

        }

        return angle;


    }

    public static double convertDECAzimuthtToDECBearing(double azimuth){
        Log.d(TAG, "convertDECAzimuthtToDECBearing: Started");

        double angle = 0;

        if(azimuth >0 && angle < 90){
            angle = azimuth + 100;
        }else if(azimuth > 90 && azimuth < 180){
            angle = (180 - azimuth) + 200;
        }else if(azimuth > 180 && azimuth < 270){
            angle = (azimuth - 180) + 300;
        }else if(azimuth > 270 && azimuth < 360){
            angle = (azimuth - 360) + 400;
        }

        return angle;


    }

    public static String convertDECtoDMSBearing(double angle, int decimalPlace){
        Log.d(TAG, "convertDECtoDMSBearing: Started...");
        Log.d(TAG, "convertDECtoDMSBearing: Angle: " + angle);
        Log.d(TAG, "convertDECtoDMSBearing: Decimal Place: " + decimalPlace);

        String frontDirection, rearDirection;
        String myAngleDMS;

        //TODO Deal with 0 for decimal place.  right now returns with "."  need to return " "

        angle = Math.abs(angle);


        int quadrant = Integer.parseInt(Integer.toString((int) angle).substring(0, 1));

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


        if (angle ==0){
            myAngleDMS = "00°00'00.0";

        }else {
            double deg = Math.floor(angle);                  // Round down
            double minutes = Math.floor((angle * 60) % 60);  // This is an integer in the range [0, 60)
            double seconds = (angle * 3600) % 60;            // This is a decimal in the range [0, 60)

            //myAngleDMS = (int) deg + "°" + (int) minutes + "'" + seconds;

            String sDeg, sMin, sSec;

            if(deg <10){
                sDeg = "0" + (int)deg + "°";
            }else{
                sDeg = (int) deg + "°";
            }

            if(minutes <10){
                sMin = "0" + (int)minutes + "'";
            }else{
                sMin = (int) minutes + "'";
            }

            if(seconds <10){
                sSec = "0" + (int) seconds + ".0";
            }else{
                sSec = (int) seconds + ".0";
            }

            myAngleDMS = sDeg + "" + sMin + "" + sSec;
            Log.d(TAG, "convertDECtoDMSBearing: Angle: " + myAngleDMS);

        }

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

    public static String convertDECBearingToParts(double angle, int partNo){
        Log.d(TAG, "convertDECtoDMSBearing: Started...");
        Log.d(TAG, "convertDECtoDMSBearing: Angle: " + angle);
        String sDeg, sMin, sSec;

        int decimalPlace = 0;

        //TODO Deal with 0 for decimal place.  right now returns with "."  need to return " "

        angle = Math.abs(angle);


        int quadrant = Integer.parseInt(Integer.toString((int) angle).substring(0, 1));
        String sQuadrant = String.valueOf(quadrant);

        switch(quadrant){
            case 1:
                angle = angle - 100;

                break;
            case 2:
                angle = angle - 200;

                break;
            case 3:
                angle = angle - 300;

                break;
            case 4:
                angle = angle - 400;

                break;
            default:

        }


        if (angle ==0){
            sDeg = "00";
            sMin = "00";
            sSec = "00";

        }else {
            double deg = Math.floor(angle);                  // Round down
            double minutes = Math.floor((angle * 60) % 60);  // This is an integer in the range [0, 60)
            double seconds = (angle * 3600) % 60;            // This is a decimal in the range [0, 60)

            if(deg <10){
                sDeg = "0" + (int)deg;
            }else{
                sDeg = String.valueOf((int) deg);
            }
            if(minutes <10){
                sMin = "0" + (int)minutes;
            }else{
                sMin = String.valueOf((int) minutes);
            }

            if(seconds <10){
                sSec = "0" + (int) seconds;
            }else{
                sSec = String.valueOf((int) seconds);
            }

        }

        String myPart;

        switch (partNo){
            case 0:
                myPart = sQuadrant;
                break;
            case 1:
                myPart = sDeg;
                break;
            case 2:
                myPart = sMin;
                break;
            case 3:
                myPart = sSec;
                break;

            default:
                myPart = "";
        }


        return myPart;
    }

    /**
     * Convert a DEC to the individual parts
     * @param angle is a double DEC
     * @param partNo which part to return.  1 returns Degree, 2 returns Minute, 3 returns Seconds
     * @return String
     */


    public static String convertDECToParts(double angle, int partNo){
        String sDeg, sMin, sSec;
        int decimalPlace = 0;

        angle = Math.abs(angle);

        if (angle ==0){
            sDeg = "00";
            sMin = "00";
            sSec = "00";

        }else {
            double deg = Math.floor(angle);                  // Round down
            double minutes = Math.floor((angle * 60) % 60);  // This is an integer in the range [0, 60)
            double seconds = (angle * 3600) % 60;            // This is a decimal in the range [0, 60)

            if(deg <10){
                sDeg = "0" + (int)deg;
            }else{
                sDeg = String.valueOf((int) deg);
            }
            if(minutes <10){
                sMin = "0" + (int)minutes;
            }else{
                sMin = String.valueOf((int) minutes);
            }

            if(seconds <10){
                sSec = "0" + (int) seconds;
            }else{
                sSec = String.valueOf((int) seconds);
            }

        }

        String myPart;

        switch (partNo){
            case 1:
                myPart = sDeg;
                break;
            case 2:
                myPart = sMin;
                break;
            case 3:
                myPart = sSec;
                break;

            default:
                myPart = "";
        }


        return myPart;
    }

    public static double inverseAzimuthFromPoint(Point pointSurvey1, Point pointSurvey2){
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

    public static double inverseHzDistanceFromPointSurvey(PointSurvey pointSurvey1, PointSurvey pointSurvey2){
        Log.d(TAG, "inverseHzDistanceFromPointSurvey: Started");

        double n1,e1,n2,e2;
        double results;

        n1 = pointSurvey1.getNorthing();
        e1 = pointSurvey1.getEasting();

        n2 = pointSurvey2.getNorthing();
        e2 = pointSurvey2.getEasting();

        results = Math.pow((n1-n2),2) + Math.pow((e1-e2),2);
        results = Math.sqrt(results);

        Log.d(TAG, "inverseHzDistanceFromPointSurvey: Dist: " + results);
        return results;

    }

    public static double inverseSlDistanceFromPointSurvey(PointSurvey pointSurvey1, PointSurvey pointSurvey2){
        Log.d(TAG, "inverseSlDistanceFromPointSurvey: Started");

        double n1,e1,n2,e2, el1, el2;
        double hzDistance, vtDelta;
        double verticalAngle;

        n1 = pointSurvey1.getNorthing();
        e1 = pointSurvey1.getEasting();
        el1 = pointSurvey1.getElevation();

        n2 = pointSurvey2.getNorthing();
        e2 = pointSurvey2.getEasting();
        el2 = pointSurvey2.getElevation();

        hzDistance = Math.pow((n1-n2),2) + Math.pow((e1-e2),2);
        hzDistance = Math.sqrt(hzDistance);

        vtDelta = el2 - el1;

        verticalAngle = Math.atan(vtDelta/hzDistance);

        return vtDelta/Math.sin(verticalAngle);

    }

    public static double inverseCoordinateDeltas(PointSurvey pointSurvey1, PointSurvey pointSurvey2, int partNo){
        Log.d(TAG, "inverseCoordinateDeltas: Started");
        //part No: 1 = Northing, 2 = Easting, 3 = Elev.

        double n1,e1,n2,e2, el1, el2;

        n1 = pointSurvey1.getNorthing();
        e1 = pointSurvey1.getEasting();
        el1 = pointSurvey1.getElevation();

        n2 = pointSurvey2.getNorthing();
        e2 = pointSurvey2.getEasting();
        el2 = pointSurvey2.getElevation();

        switch(partNo) {
            case 1: //northing
                return n2 - n1;
            case 2: //easting
                return e2 - e1;
            case 3: //elev
                return el2 - el1;
            default:
                return 0;
        }
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


    public static Point solveForCoordinatesFromBearing(Point occupyPoint, double hzAngle, double sDistance, double znAngle){
        Log.d(TAG, "solveForCoordinatesFromBearing: Started");
        double hzAzimuth, hzDistance, dNorth, dEast;
        Point results = new Point();

        hzAzimuth = convertDECBearingtToDECAzimuth(hzAngle);
        hzAzimuth = (Math.PI/180) * hzAzimuth;

        Log.d(TAG, "solveForCoordinatesFromBearing: Converted Az: " + hzAzimuth);
        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);
        Log.d(TAG, "solveForCoordinatesFromBearing: Converted Hd:" + hzDistance);

        dNorth = hzDistance * Math.cos(hzAzimuth);
        dEast = hzDistance * Math.sin(hzAzimuth);

        results.setNorthing(occupyPoint.getNorthing() + dNorth);
        results.setEasting(occupyPoint.getEasting() + dEast);
        results.setElevation(0d);

        return results;

    }

    public static Point solveForCoordinatesFromAzimuth(Point occupyPoint, double hzAngle, double sDistance, double znAngle){
        Log.d(TAG, "solveForCoordinatesFromAzimuth: Started");

        double hzAzimuth, hzDistance, dNorth, dEast;
        Point results = new Point();

        hzAzimuth = hzAngle * (Math.PI/180);

        Log.d(TAG, "solveForCoordinatesFromAzimuth: Converted Az: " + hzAzimuth);
        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);
        Log.d(TAG, "solveForCoordinatesFromAzimuth: Converted Hd:" + hzDistance);

        dNorth = hzDistance * Math.cos(hzAzimuth);
        dEast = hzDistance * Math.sin(hzAzimuth);

        results.setNorthing(occupyPoint.getNorthing() + dNorth);
        results.setEasting(occupyPoint.getEasting() + dEast);
        results.setElevation(0d);

        return results;


    }

    public static Point solveForCoordinatesFromTurnedAngleAndDistance(Point occupyPoint, Point backsightPoint,double hzAngle, double sDistance, double znAngle){
        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Started");
        Point results = new Point();
        double observedAzimuth, hzDistance, dNorth, dEast;
        double northing, easting;

        //determine Azimuth
        double baseAzimuth = SurveyMathHelper.inverseAzimuthFromPoint(backsightPoint,occupyPoint);

        observedAzimuth = baseAzimuth + hzAngle;
        Log.d(TAG, "toDelete: Observed Azimuth: (Pre Radians): " + observedAzimuth);

        observedAzimuth = (Math.PI/180) * observedAzimuth;
        Log.d(TAG, "toDelete: Observed Azimuth: (Post Radians): " + observedAzimuth);
        Log.d(TAG, "toDelete: Chord Bearing: ");
        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Observed Azimith: " + observedAzimuth);

        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);

        dNorth = hzDistance * Math.cos(observedAzimuth);
        dEast = hzDistance * Math.sin(observedAzimuth);

        Log.d(TAG, "SurveyMathHelper: dN: " + dNorth + " dEast: " + dEast);

        northing = occupyPoint.getNorthing() + dNorth;
        easting = occupyPoint.getEasting() + dEast;

        Log.d(TAG, "Northing: " + northing + ", Easting: " + easting);

        results.setNorthing(northing);
        results.setEasting(easting);
        results.setElevation(0d);

        return results;
    }

    public static Point solveForCoordinatesFromTurnedAngleAndDistance(PointSurvey occupyPointSurvey, PointSurvey backsightPointSurvey, double hzAngle, double sDistance, double znAngle){
        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Started");
        Point results = new Point();
        double observedAzimuth, hzDistance, dNorth, dEast;
        double northing, easting;

        //determine Azimuth
        double baseAzimuth = SurveyMathHelper.inverseAzimuthFromPointSurvey(occupyPointSurvey,backsightPointSurvey);

        observedAzimuth = baseAzimuth + hzAngle;
        observedAzimuth = (Math.PI/180) * observedAzimuth;

        Log.d(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Observed Azimith: " + observedAzimuth);

        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);

        dNorth = hzDistance * Math.cos(observedAzimuth);
        dEast = hzDistance * Math.sin(observedAzimuth);

        Log.d(TAG, "SurveyMathHelper: dN: " + dNorth + " dEast: " + dEast);

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
        double baseAzimuth = SurveyMathHelper.inverseAzimuthFromPointSurvey(occupyPointSurvey,backsightPointSurvey);

        observedAzimuth = baseAzimuth + hzAngle;
        observedAzimuth = (Math.PI/180) * observedAzimuth;

        Log.i(TAG, "solveForCoordinatesFromTurnedAngleAndDistance: Observed Azimith: " + observedAzimuth);

        hzDistance = convertSlopeDistanceToHorizontalDistanceByZenith(sDistance,znAngle);

        dNorth = hzDistance * Math.cos(observedAzimuth);
        dEast = hzDistance * Math.sin(observedAzimuth);

        Log.i(TAG, "SurveyMathHelper: dN: " + dNorth + " dEast: " + dEast);

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

    public static double solveForCurveDeltaAngle(double radius, double length){
        Log.d(TAG, "solveForCurveDeltaAngle: Started");

        return (180*length)/(Math.PI * radius);


    }
    
    public static double solveForCurveRadius(double deltaDEC, double length){
        Log.d(TAG, "solveForCurveRadius: Started");
        
        return ((180 * length)/(Math.PI * deltaDEC));
        
        
    }

    public static double solveForCurveLength(double deltaDEC, double radius){
        Log.d(TAG, "solveForCurveLength: Started");

        return (deltaDEC/360) * (2*(Math.PI * radius));

    }

    public static double solveForCurveTangent(double deltaDEC, double radius){
        Log.d(TAG, "solveForCurveTangent: Started");
        double results = 0;

        results = (Math.PI/180)*deltaDEC;
        results = Math.tan(results/2);

        return radius * results;

    }

    public static double solveForCurveChordDistance(double deltaDEC, double radius){
        Log.d(TAG, "solveForCurveTangent: Started");
        double results = 0;

        results = (Math.PI/180)*deltaDEC;
        results = Math.sin(results/2);

        return (2*radius) * results;

    }

    public static double solveForCurveChordDirectionAzimuthDEC(Point backTangent, Point pcPoint, double deltaDEC, boolean isCurveToRight){
        Log.d(TAG, "solveForCurveChordBearingDEC: Started");
        double hzAzimuth, chordAzimuth;

        Log.d(TAG, "solve: backTangent (n,e): " + backTangent.getNorthing() + ", " + backTangent.getEasting());
        Log.d(TAG, "solve: pcPoint (n,e): " + pcPoint.getNorthing() + ", " + pcPoint.getEasting());

        //Find azimuth of back tangent

        hzAzimuth = inverseAzimuthFromPoint(backTangent, pcPoint);

        Log.d(TAG, "solve: backTangent - radius:(DEC) " + hzAzimuth);

        if(isCurveToRight){
            chordAzimuth = hzAzimuth + (deltaDEC/2);
        }else{
            chordAzimuth = hzAzimuth - (deltaDEC/2);
        }

        Log.d(TAG, "solve: chord azimuth: " + chordAzimuth);
        return  chordAzimuth;

    }

    public static Point solveForCurvePIForBackTangent(Point pcPoint, Point ptPoint, double deltaDEC, double radius, boolean isCurveToRight){
        Log.d(TAG, "solveForCurvePIForBackTangent: Started");
        double chordAzimuth, tangent, backTangentAzimuth;
        Point piPoint;

        Log.d(TAG, "solveForCurvePIForBackTangent: PC: " + pcPoint.getNorthing() + "," + pcPoint.getEasting());
        Log.d(TAG, "solveForCurvePIForBackTangent: PT: " + ptPoint.getNorthing() + "," + ptPoint.getEasting());
        Log.d(TAG, "solveForCurvePIForBackTangent: Delta/Radius: " + deltaDEC + "/" + radius);

        chordAzimuth = inverseAzimuthFromPoint(pcPoint, ptPoint);
        Log.d(TAG, "solveForCurvePIForBackTangent: Chord Azimuth: " + chordAzimuth);

        if(isCurveToRight){
            backTangentAzimuth = chordAzimuth - (deltaDEC/2);
        }else{
            backTangentAzimuth = chordAzimuth + (deltaDEC/2);
        }

        Log.d(TAG, "solveForCurvePIForBackTangent: Back Tangent Azimuth: " + backTangentAzimuth);

        tangent = solveForCurveTangent(deltaDEC,radius);
        Log.d(TAG, "solveForCurvePIForBackTangent: Chord Distance: " + tangent);

        piPoint = solveForCoordinatesFromAzimuth(pcPoint,backTangentAzimuth,tangent,90d);

        return piPoint;


    }

    public static double solveForCurveSegmentArea(double deltaAngleDEC, double radius){
        Log.d(TAG, "solveForCurveSegmentArea: Started...");
        double results, radianAngle;

        results = (Math.pow(radius,2))/2;
        radianAngle = ((Math.PI * deltaAngleDEC)/180) - (Math.sin((Math.PI * deltaAngleDEC)/180));

        results = results * radianAngle;
        Log.d(TAG, "solveForCurveSegmentArea: Area: " + results);
        return results;


    }


}

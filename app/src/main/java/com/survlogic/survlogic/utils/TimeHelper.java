package com.survlogic.survlogic.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by chrisfillmore on 7/2/2017.
 */

public class TimeHelper {

    public static String getDateinFormat(int value, SimpleDateFormat dateFormat){

        Date date = TimeHelper.getDateFromInteger(value);

        String stringDate = dateFormat.format(date);

        return stringDate;

    }


    public static Date getDateFromInteger(int value){

        Date date = new Date(((long)value)*1000L);

        return date;

    }



}

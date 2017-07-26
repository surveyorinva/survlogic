package com.survlogic.survlogic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrisfillmore on 7/25/2017.
 */

public class FileHelper {

    private static final String TAG = "FileHelper";
    private Context mContext;

    public FileHelper(Context mContext) {
        this.mContext = mContext;

    }


    public Uri saveImageToExternal(Bitmap image){
        Log.d(TAG, "saveImagetoFilePath: Creating file path");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        //String path = Environment.getExternalStorageDirectory().toString();

        boolean mediaMount;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mediaMount =  true;
        }else{
            mediaMount = false;
        }

        Log.d(TAG, "Media Mounted: " + mediaMount);

        String path = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();

        Log.d(TAG, "Save Path:" + path);

        File file = new File(path,imageFileName + ".jpg");

        try{
            FileOutputStream stream = null;
            stream = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            stream.flush();
            stream.close();

            Log.d(TAG, "saveImagetoFilePath: Image Saved");
        }catch (FileNotFoundException e) {
            e.printStackTrace();

        }catch (IOException e){
            e.printStackTrace();

        }

        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        return savedImageURI;

    }



}

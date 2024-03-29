package com.survlogic.survlogic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;

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

    public final int FOLDER_PHOTO = 0, FOLDER_SKETCH = 1, FOLDER_JOB = 2, FOLDER_JOB_PHOTOS = 3;

    public FileHelper(Context mContext) {
        this.mContext = mContext;

    }


    public boolean createApplicationFolders(){
        Log.d(TAG, "createApplicationFolders: Started");

        String storageState = Environment.getExternalStorageState();

        String path = Environment.getExternalStorageDirectory().toString();
        path = path  +"/"+ mContext.getString(R.string.app_name);

        String pathJobs = path + "/jobs";
        String pathJobPhotos = path + "/jobs/photos";
        String pathPhotos = path + "/photos";
        String pathSketch = path + "/sketches";

        File folderPhotos = new File(pathPhotos);
        File noMediaPhotos = new File(pathPhotos,".nomedia");

        File folderSketch = new File(pathSketch);
        File noMediaSketch = new File(pathSketch,".nomedia");

        File folderJobPhotos = new File(pathJobPhotos);
        File noMediaJobPhotos = new File(pathJobPhotos,".nomedia");


        File folderJob = new File(pathJobs);

        if (Environment.MEDIA_MOUNTED.equals(storageState)){

            try{
                if (!folderPhotos.isDirectory()|| !folderPhotos.exists()) {
                    folderPhotos.mkdirs();

                    if(!noMediaPhotos.exists()) {
                        FileOutputStream noMediaPhotosFile = new FileOutputStream(noMediaPhotos);
                        noMediaPhotosFile.write(0);
                        noMediaPhotosFile.close();
                    }
                }

                if (!folderJobPhotos.isDirectory()|| !folderJobPhotos.exists()) {
                    folderJobPhotos.mkdirs();

                    if(!noMediaJobPhotos.exists()) {
                        FileOutputStream noMediaJobPhotosFile = new FileOutputStream(noMediaJobPhotos);
                        noMediaJobPhotosFile.write(0);
                        noMediaJobPhotosFile.close();
                    }
                }

                if (!folderSketch.isDirectory()|| !folderSketch.exists()) {
                    folderSketch.mkdirs();

                    if(!noMediaSketch.exists()) {
                        FileOutputStream noMediaSketchFile = new FileOutputStream(noMediaSketch);
                        noMediaSketchFile.write(0);
                        noMediaSketchFile.close();
                    }

                }

                if(!folderJob.isDirectory()|| !folderJob.exists()) {
                    folderJob.mkdirs();
                }

                return true;

            } catch (Exception e) {
                Toast unsavedToast = Toast.makeText(mContext.getApplicationContext(),
                        "Oops! Image could not be saved. Do you have enough space in your device2?", Toast.LENGTH_SHORT);
                unsavedToast.show();
                e.printStackTrace();
                return false;
            }



        }else{
            Log.d(TAG, "createApplicationFolders: Error Writing Storage");
            return false;
        }

    }



    public String getPathToFolder(int folderValue){

        String path;

        switch (folderValue){

            case FOLDER_PHOTO:  //Photos
                path = Environment.getExternalStorageDirectory().toString();
                path = path  +"/"+ mContext.getString(R.string.app_name) + "/photos";

                break;

            case FOLDER_SKETCH:  //Sketches
                path = Environment.getExternalStorageDirectory().toString();
                path = path  +"/"+ mContext.getString(R.string.app_name) + "/sketches";

                break;

            case FOLDER_JOB: //Jobs
                path = Environment.getExternalStorageDirectory().toString();
                path = path + "/" + mContext.getString(R.string.app_name) + "/jobs";

                break;

            case FOLDER_JOB_PHOTOS: //Jobs/Photos
                path = Environment.getExternalStorageDirectory().toString();
                path = path + "/" + mContext.getString(R.string.app_name) + "/jobs/photos";

                break;

            default:
                path = "crashes";
                break;

        }

        return path;



    }


    public Uri saveImageToExternal(Bitmap image, int folderType){
        Log.d(TAG, "saveImagetoFilePath: Creating file path");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        //String path = Environment.getExternalStorageDirectory().toString();

        boolean mediaMount;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mediaMount =  true;
        }else{
            mediaMount = false;
        }

        Log.d(TAG, "Media Mounted: " + mediaMount);


        String path = getPathToFolder(folderType);


        Log.d(TAG, "Save Path:" + path);

        File folder = new File(path);

        File file = new File(path,imageFileName + ".jpg");

        try{

            if (!folder.isDirectory()|| !folder.exists()) {
                folder.mkdirs();
            }

            FileOutputStream stream = null;
            stream = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            stream.flush();
            stream.close();


            Toast.makeText(mContext, "Saved", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "saveImagetoFilePath: Image Saved");
        }catch (FileNotFoundException e) {
            e.printStackTrace();

        }catch (IOException e){
            e.printStackTrace();

        }

        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        return savedImageURI;

    }


    public boolean deleteImage(Bitmap bitmap){
            //String path = Environment.getExternalStorageDirectory().toString();

            boolean mediaMount;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                mediaMount =  true;
            }else{
                mediaMount = false;
            }

            String path = getPathToFolder(0);
            File folder = new File(path);

            String imageFileName = "";

            File file = new File(path,imageFileName + ".jpg");

            try{
               file.delete();
                return true;

            }catch (Exception e) {
                e.printStackTrace();
                return false;

            }

    }

    public File getPhotoFile(){
        Log.d(TAG, "saveImagetoFilePath: Creating file path");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        //String path = Environment.getExternalStorageDirectory().toString();

        boolean mediaMount;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mediaMount =  true;
        }else{
            mediaMount = false;
        }

        Log.d(TAG, "Media Mounted: " + mediaMount);


        String path = getPathToFolder(0);

        Log.d(TAG, "Save Path:" + path);

        File folder = new File(path);

        File file = new File(path,imageFileName + ".jpg");

        try{

            if (!folder.isDirectory()|| !folder.exists()) {
                folder.mkdirs();
            }

        }catch (Exception e){
            e.printStackTrace();

        }

        return file;

    }


    public String uriToString(Uri uri){
        return uri.toString();
    }

    public Uri stringToUri(String stringUri){
        return Uri.parse(stringUri);
    }


}

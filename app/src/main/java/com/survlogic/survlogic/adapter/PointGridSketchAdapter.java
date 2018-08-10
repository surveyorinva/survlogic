package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.PhotoEditor.utils.ImageHelper;
import com.survlogic.survlogic.view.SquareImageView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 7/13/2017.
 */

public class PointGridSketchAdapter extends ArrayAdapter {

    private static final String TAG = "PointGridSketchAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private ImageHelper imageHelper;

    private ArrayList<JobSketch> mImages = new ArrayList<>();
    private Bitmap mImage,mImageWatermark;
    private String imgURL, mAppend;

    private int currentCount = 0, imageCap = 0, arrayListCount = 0;
    private static final int limitCount = 4, limitArrayCount = 3;
    private boolean overLimit = false;

    public PointGridSketchAdapter(Context context, int layoutResource, String append, ArrayList<JobSketch> images) {
        super(context, layoutResource, images);
        Log.d(TAG, "PointGridSketchAdapter: Started...");
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;

        this.layoutResource = layoutResource;
        this.mAppend = append;
        this.mImages = images;
    }

    private static class ViewHolder {
        SquareImageView image;
        ProgressBar mProgressBar;
    }


    @Override
    public int getCount() {

        arrayListCount=mImages.size(); //counts the total number of elements from the arrayList.

        if (arrayListCount <= 4){
            imageCap = arrayListCount;

        }else{
            imageCap = limitCount;
            overLimit = true;
        }
        return imageCap;//returns the total count to adapter

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        View row = convertView;
        imageHelper = new ImageHelper(mContext);

        currentCount = position;

        if (row == null){
            row = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.image = (SquareImageView) row.findViewById(R.id.gridImageView);
            holder.mProgressBar = (ProgressBar) row.findViewById(R.id.gridImageProgressBar);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        JobSketch item = mImages.get(position);
        ImageLoader imageLoader = ImageLoader.getInstance();

        Log.d(TAG, "Staring Switch: Limit: " + limitArrayCount + " at position " + position);

        switch (position){
            case limitArrayCount:
                Log.d(TAG, "In Switch: Array Count: " + arrayListCount + " Limit Count: " + limitCount);
                String description = "+ " + String.valueOf(arrayListCount - limitCount + 1);

                imgURL = item.getImagePath();

                mImage = imageHelper.convertFileURLToBitmap(imgURL);
                mImageWatermark = imageHelper.setHeaderFullScreen(mImage, description, true);
                holder.image.setImageBitmap(mImageWatermark);
                holder.mProgressBar.setVisibility(View.GONE);
                break;

            default:
                imgURL = item.getImagePath();

                imageLoader.displayImage(mAppend + imgURL, holder.image, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        if(holder.mProgressBar != null){
                            holder.mProgressBar.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if(holder.mProgressBar != null) {
                            holder.mProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if(holder.mProgressBar != null) {
                            holder.mProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        if(holder.mProgressBar != null) {
                            holder.mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
                break;
        }


        return row;
    }


}

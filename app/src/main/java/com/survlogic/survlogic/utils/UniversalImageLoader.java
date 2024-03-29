package com.survlogic.survlogic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 7/27/2017.
 */

public class UniversalImageLoader {

    private static final String TAG = "UniversalImageLoader";
    private static final int defaultImage = R.drawable.ic_default_image;
    private Context mContext;

    public UniversalImageLoader(Context mContext) {
        Log.d(TAG, "UniversalImageLoader: Started");
        this.mContext = mContext;
    }

    public ImageLoaderConfiguration getConfig(){
        Log.d(TAG, "getConfig: Started");
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();

        return configuration;

    }


    /**
     * This method can be used to set images that are static.  It cannot be used if the images are being changed in the Fragment or Activity.
     * OR if the are being set a list or gridview.
     * @param imgURL
     * @param image
     * @param mProgressBar
     * @param append
     */

    public static void setImage(String imgURL, ImageView image, final ProgressBar mProgressBar, String append){
        Log.d(TAG, "setImage: Started");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(mProgressBar !=null){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(mProgressBar !=null){
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(mProgressBar !=null){
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(mProgressBar !=null){
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

    }


}

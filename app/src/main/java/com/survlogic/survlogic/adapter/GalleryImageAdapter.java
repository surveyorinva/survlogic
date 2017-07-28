package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.ImageHelper;
import com.survlogic.survlogic.utils.SquareImageView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 7/13/2017.
 */

public class GalleryImageAdapter extends ArrayAdapter {


    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private ImageHelper imageHelper;

    private ArrayList<ProjectImages> mImages = new ArrayList<ProjectImages>();
    private String imgURL, mAppend;

    public GalleryImageAdapter(Context context, int layoutResource, String append, ArrayList<ProjectImages> images) {
        super(context, layoutResource, images);
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
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        View row = convertView;
        imageHelper = new ImageHelper(mContext);

        if (row == null){
            row = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.image = (SquareImageView) row.findViewById(R.id.gridImageView);
            holder.mProgressBar = (ProgressBar) row.findViewById(R.id.gridImageProgressBar);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        ProjectImages item = mImages.get(position);
        ImageLoader imageLoader = ImageLoader.getInstance();

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

        return row;
    }


}

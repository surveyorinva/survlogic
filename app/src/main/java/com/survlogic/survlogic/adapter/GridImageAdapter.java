package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.ImageHelper;
import com.survlogic.survlogic.utils.SquareImageView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 7/13/2017.
 */

public class GridImageAdapter extends ArrayAdapter {


    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private ImageHelper imageHelper;

    private ArrayList<ProjectImages> mImages = new ArrayList<ProjectImages>();
    private Bitmap mImageWatermark;

    private int currentCount = 0, imageCap = 0, arrayListCount = 0;
    private static final int limitCount = 4, limitArrayCount = 3;
    private boolean overLimit = false;

    public GridImageAdapter(Context context, int layoutResource, ArrayList<ProjectImages> images) {
        super(context, layoutResource, images);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        this.layoutResource = layoutResource;
        this.mImages = images;
    }

    private static class ViewHolder {
        SquareImageView image;
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

        ViewHolder holder;
        View row = convertView;
        imageHelper = new ImageHelper(mContext);

        currentCount = position;

        if (row == null){
            row = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.image = (SquareImageView) row.findViewById(R.id.gridImageView);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        ProjectImages item = mImages.get(position);

        if(position == limitArrayCount){
            if(overLimit){
                String description = "+ " + String.valueOf(arrayListCount - limitCount);

                mImageWatermark = imageHelper.setHeaderFullScreen(imageHelper.convertToBitmap(item.getImage()), description, true);
                holder.image.setImageBitmap(mImageWatermark);
                holder.image.setTag(true);
            }else{
                holder.image.setImageBitmap(imageHelper.convertToBitmap(item.getImage()));
                holder.image.setTag(false);
            }

        }else{
            holder.image.setImageBitmap(imageHelper.convertToBitmap(item.getImage()));
            holder.image.setTag(false);
        }

        return row;
    }


}

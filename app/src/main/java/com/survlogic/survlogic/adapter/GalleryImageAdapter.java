package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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


    public GalleryImageAdapter(Context context, int layoutResource, ArrayList<ProjectImages> images) {
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
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        View row = convertView;
        imageHelper = new ImageHelper(mContext);

        if (row == null){
            row = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.image = (SquareImageView) row.findViewById(R.id.gridImageView);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        ProjectImages item = mImages.get(position);

            holder.image.setImageBitmap(imageHelper.convertToBitmap(item.getImage()));
            holder.image.setTag(false);

        return row;
    }


}

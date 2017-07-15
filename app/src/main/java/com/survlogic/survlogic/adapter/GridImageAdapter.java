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

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.SquareImageView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 7/13/2017.
 */

public class GridImageAdapter extends ArrayAdapter {


    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private ArrayList<ProjectImages> mImages = new ArrayList<ProjectImages>();

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


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        View row = convertView;

        if (row == null){
            row = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.image = (SquareImageView) row.findViewById(R.id.gridImageView);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }


        ProjectImages item = mImages.get(position);

        holder.image.setImageBitmap(convertToBitmap(item.getImage()));

        return row;
    }

    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

}

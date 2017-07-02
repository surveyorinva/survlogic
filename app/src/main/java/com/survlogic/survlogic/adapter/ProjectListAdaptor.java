package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Project_Small;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class ProjectListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Project> projects = new ArrayList<Project>();
    private final int SMALL = 0, MEDIUM = 1, LARGE = 2;

    private int lastPosition = 0;
    private final static int FADE_DURATION = 1000; // in milliseconds

    private Context mContext;

//    CONSTRUCTOR!!!!!
    public ProjectListAdaptor(Context context,ArrayList<Project> projects){
        this.projects = projects;
        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case SMALL:
                View v1 = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Small(v1);
                break;

            case MEDIUM:
                View v2 = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Small(v2);
                break;

            case LARGE:
                View v3 = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Small(v3);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Small(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        String cardUseType;
        Project project = projects.get(position);

        switch(project.getmStorage()){
            case 0:
                cardUseType = "LOCAL";
                break;

            default:
                cardUseType = "LOCAL";
                break;

        }

        if(cardUseType.equals("LOCAL")){
            return SMALL;
        }else if (cardUseType.equals("NETWORK")){
            return MEDIUM;
        }else if (cardUseType.equals("BOTH")){
            return LARGE;
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
       switch (holder.getItemViewType()){

            case SMALL:
                Card_View_Holder_Project_Small vh1 = (Card_View_Holder_Project_Small) holder;
                configureViewHolderSmall(vh1,position);


            case MEDIUM:

            case LARGE:


        }



    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    private void configureViewHolderSmall(Card_View_Holder_Project_Small vh1, int position) {
        Project project = projects.get(position);

        vh1.txtProjectName.setText(project.getmProjectName());

        if(project.getmLocationLat() != 0){
            vh1.txtLocation.setText(mContext.getString(R.string.project_card_location_geo,
                    MathHelper.convertDECtoDMS(project.getmLocationLat(),1,true),
                    MathHelper.convertDECtoDMS(project.getmLocationLong(),1,true)));
        }

        vh1.imgProjectImage.setImageBitmap(convertToBitmap(project.getmImage()));



    }
    public void insert(int position, Project data){
        projects.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (Project data){
        int position = projects.indexOf(data);
        projects.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<Project> newData){
        this.projects = newData;

        notifyDataSetChanged();
    }

    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }


    private void setAnimationbyHelper(RecyclerView.ViewHolder holder, int position){

        if(position > lastPosition){ // We are scrolling DOWN
            AnimateHelper.animateRecyclerView(holder, true);

        }else{ // We are scrolling UP
            AnimateHelper.animateRecyclerView(holder, false);

        }

        lastPosition = position;
    }

    private void setXMLAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_recycler_item_show);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

}

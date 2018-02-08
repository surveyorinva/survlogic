package com.survlogic.survlogic.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.ProjectDetailsActivity;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.TimeHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Project_Small;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class ProjectListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ProjectListAdaptor";

    private ArrayList<Project> projects = new ArrayList<Project>();
    private final int SMALL = 0, MEDIUM = 1, LARGE = 2;

    private int lastPosition = 0;
    private final static int FADE_DURATION = 1000; // in milliseconds

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private String imgURL;
    private String mURLSyntex = "file://";

    private Context mContext;

//    CONSTRUCTOR!!!!!
    public ProjectListAdaptor(Context context,ArrayList<Project> projects){
        this.projects = projects;

        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Started");
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
        Log.d(TAG, "onBindViewHolder: Started");
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
        Log.d(TAG, "getItemCount: Project Size: " + projects.size());
        return projects.size();
    }

    private void configureViewHolderSmall(final Card_View_Holder_Project_Small vh1, int position) {
        Log.d(TAG, "configureViewHolderSmall: Started");
        final Project project = projects.get(position);
        final int projectID = project.getmId();
        Log.d(TAG, "configureViewHolderSmall: Project ID:" + projectID);


//        Project ID
        vh1.txtProjectId.setTag(project);


//        Project Name
        Log.d(TAG, "configureViewHolderSmall: Loading Project Name: " + project.getmProjectName());

        vh1.txtProjectName.setText(project.getmProjectName());

//        Location
        Log.d(TAG, "configureViewHolderSmall: Loading Location");
        if(project.getmLocationLat() != 0){
            vh1.txtLocation.setText( mContext.getResources().getString(R.string.project_card_location_geo,
                    SurveyMathHelper.convertDECtoDMSGeodetic(project.getmLocationLat(),1,true),
                    SurveyMathHelper.convertDECtoDMSGeodetic(project.getmLocationLong(),1,true)));
        }

//        Image
        Log.d(TAG, "configureViewHolderSmall: Loading Image");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imgURL = project.getmImagePath();

        imageLoader.displayImage(mURLSyntex + imgURL, vh1.imgProjectImage, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        if(vh1.progressBar != null){
                            vh1.progressBar.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if(vh1.progressBar != null){
                            vh1.progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if(vh1.progressBar != null){
                            vh1.progressBar.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        if(vh1.progressBar != null){
                            vh1.progressBar.setVisibility(View.GONE);
                        }

                    }
                });


        Log.d(TAG, "configureViewHolderSmall: Loading Transition Animation");
        ViewCompat.setTransitionName(vh1.imgProjectImage, project.getmProjectName());
        Pair<View,String> pair1 = Pair.create((View)vh1.imgProjectImage,vh1.imgProjectImage.getTransitionName());

//        Date
        Log.d(TAG, "configureViewHolderSmall: Loading Date: " + project.getmDateCreated());
        int d = project.getmDateCreated();
        String stringDate = TimeHelper.getDateinFormat(d,dateFormat);
        Log.d(TAG, "configureViewHolderSmall: Converted Date: " + stringDate);

        vh1.txtLastModify.setText(mContext.getResources().getString(R.string.project_card_last_modified_date_create,stringDate));


        Log.d(TAG, "configureViewHolderSmall: Setting OnClick Buttons");
        vh1.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Set Onclick Listener: Start");

                Intent intent = new Intent(mContext, ProjectDetailsActivity.class);
                intent.putExtra("PROJECT_ID",projectID);
                //intent.putExtra("project", Parcels.wrap(project));

                Log.e(TAG, "Set Onclick Listener: Bundled");

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation
                        ((Activity) mContext, vh1.imgProjectImage,"profile");

                mContext.startActivity(intent, options.toBundle());

                Log.e(TAG, "Set Onclick Listener: Complete");

            }
        });

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


    private void setAnimationbyHelper(RecyclerView.ViewHolder holder, int position){

        if(position > lastPosition){ // We are scrolling DOWN
            AnimateHelper.animateRecyclerView(holder, true);

        }else{ // We are scrolling UP
            AnimateHelper.animateRecyclerView(holder, false);

        }

        lastPosition = position;
    }

    private void setAnimation(View viewToAnimate, int position)
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

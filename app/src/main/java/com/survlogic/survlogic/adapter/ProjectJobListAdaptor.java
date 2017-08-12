package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.JobHomeActivity;
import com.survlogic.survlogic.model.ProjectJobs;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.TimeHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Project_Job_Small;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class ProjectJobListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ProjectJobListAdaptor";

    private ArrayList<ProjectJobs> projectJobs = new ArrayList<ProjectJobs>();
    private final int SMALL = 0;

    private int lastPosition = 0;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    private Context mContext;

//    CONSTRUCTOR!!!!!
    public ProjectJobListAdaptor(Context context, ArrayList<ProjectJobs> projectJobs){
        this.projectJobs = projectJobs;

        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case SMALL:
                View v1 = mInflater.inflate(R.layout.card_project_job_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Job_Small(v1);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_project_job_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Job_Small(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return SMALL;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
       switch (holder.getItemViewType()){

            case SMALL:
                Card_View_Holder_Project_Job_Small vh1 = (Card_View_Holder_Project_Job_Small) holder;
                configureViewHolderSmall(vh1,position);

        }

    }

    @Override
    public int getItemCount() {
        return projectJobs.size();
    }

    private void configureViewHolderSmall(final Card_View_Holder_Project_Job_Small vh1, int position) {

        final ProjectJobs projectJob = projectJobs.get(position);

        final int projectID = projectJob.getProjectId();
        final int projectJobID = projectJob.getId();

        final String projectJobDatabaseName = projectJob.getmJobDbName();

//        Job Name
        vh1.txtJobName.setText(projectJob.getmJobName());


//        Date
        int d = projectJob.getmDateCreated();
        String stringDate = TimeHelper.getDateinFormat(d,dateFormat);

        vh1.txtLastModify.setText(mContext.getString(R.string.project_card_last_modified_date_create,
                stringDate));


//        On Click Listener
        vh1.btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, JobHomeActivity.class);
                intent.putExtra("PROJECT_ID",projectID);
                intent.putExtra("JOB_ID", projectJobID);
                intent.putExtra("JOB_DB_NAME", projectJobDatabaseName);
                mContext.startActivity(intent);
            }
        });

    }



    public void insert(int position, ProjectJobs data){
        projectJobs.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (ProjectJobs data){
        int position = projectJobs.indexOf(data);
        projectJobs.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<ProjectJobs> newData){
        this.projectJobs = newData;

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





}

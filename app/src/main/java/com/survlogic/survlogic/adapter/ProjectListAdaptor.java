package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.view.Project_Card_View_Holder_Small;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class ProjectListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Project> projects = new ArrayList<Project>();
    private final int SMALL = 0, MEDIUM = 1, LARGE = 2;
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
                viewHolder = new Project_Card_View_Holder_Small(v1);
                break;

            case MEDIUM:
                View v2 = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Project_Card_View_Holder_Small(v2);
                break;

            case LARGE:
                View v3 = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Project_Card_View_Holder_Small(v3);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_project_view_small,parent,false);
                viewHolder = new Project_Card_View_Holder_Small(v);
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
                Project_Card_View_Holder_Small vh1 = (Project_Card_View_Holder_Small) holder;
                configureViewHolderSmall(vh1,position);
                break;

            case MEDIUM:

                break;

            case LARGE:
                break;


        }
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    private void configureViewHolderSmall(Project_Card_View_Holder_Small vh1, int position) {
        Project project = projects.get(position);

        vh1.txtProjectName.setText(project.getmProjectName());


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

}

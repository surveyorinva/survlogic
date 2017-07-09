package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.MainActivity;
import com.survlogic.survlogic.background.BackgroundProjectList;
import com.survlogic.survlogic.background.BackgroundProjectListRefresh;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class MainHomeFragment extends Fragment {

    private View v;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;

    private Context mContext = this.getContext();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main_home,container,false);

        initView();

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showProjectsLocal();

    }

    @Override
    public void onResume() {
        super.onResume();

        showProjectsLocalRefresh();

    }

    private void initView(){

        fab = ((MainActivity) getActivity()).getFloatingActionButton();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_in_content_project_view);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_view_in_content_project_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    showProjectsLocalRefresh();

                    }
                }, 1000);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    private void showProjectsLocal(){
        BackgroundProjectList backgroundProjectList = new BackgroundProjectList(getActivity());
        backgroundProjectList.execute();


    }

    private void showProjectsLocalRefresh(){
        BackgroundProjectListRefresh backgroundProjectListRefresh = new BackgroundProjectListRefresh(getActivity(),swipeRefreshLayout);
        backgroundProjectListRefresh.execute();


    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }

    }


}

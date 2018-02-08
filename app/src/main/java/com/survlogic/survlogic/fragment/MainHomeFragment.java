package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.MainActivity;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.background.BackgroundProjectList;
import com.survlogic.survlogic.background.BackgroundProjectListRefresh;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.interf.ProjectListListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.Project;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class MainHomeFragment extends Fragment implements OnMapReadyCallback{

    private static final String TAG = "MainHomeFragment";
    private View v;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProjectListAdaptor adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FloatingActionButton fab;

    private Context mContext;
    private SupportMapFragment supportMapFragment;

    private ArrayList<Project> lstProjectsLocal = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Started--------------------->");
        v = inflater.inflate(R.layout.fragment_main_home,container,false);
        mContext = getActivity();

        initView();
        initProjectListRecyclerView();
        setOnClickListeners();
        
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initMapView();
    }



    private void initView(){
        Log.d(TAG, "initView: Started");
        fab = ((MainActivity) getActivity()).getFloatingActionButton();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_in_content_project_view);
        layoutManager = new LinearLayoutManager(mContext);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_view_in_content_project_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);


    }


    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showProjectsLocalRefresh();
                        swipeRefreshLayout.setRefreshing(false);
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

    private void showProjectsLocalRefresh(){
        ProjectListListener listener = (ProjectListListener) getActivity();

        listener.refreshProjectList();

    }


    private void initMapView(){
        Log.d(TAG, "initMapView: Map Initializing");
        FragmentManager fm = getActivity().getSupportFragmentManager();
        supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);

        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, supportMapFragment).commit();
        }

        supportMapFragment.getMapAsync(this);
        Log.d(TAG, "initMapView: Map Sync'ed");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        
    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }

    }

    //----------------------------------------------------------------------------------------------//
    private void initProjectListRecyclerView(){
        Log.d(TAG,"Start: initProjectListRecyclerView");

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);

        adapter = new ProjectListAdaptor(mContext,lstProjectsLocal);
        mRecyclerView.setAdapter(adapter);

        Log.d(TAG,"Complete: initProjectListRecyclerView");

    }

    public void setArrayListProjects(ArrayList<Project> lstArray){
        Log.d(TAG, "setArrayListPointSurvey: Started...");
        lstProjectsLocal = lstArray;

        Log.d(TAG, "setArrayListPointSurvey: Listen: " + lstProjectsLocal.size());

        if(adapter != null){
            Log.d(TAG, "setArrayListPointSurvey: Adapter Not Null");
            adapter.swapDataSet(lstArray);
        }else{
            Log.d(TAG, "setArrayListProjects: Adapter Null");
        }
    }



}

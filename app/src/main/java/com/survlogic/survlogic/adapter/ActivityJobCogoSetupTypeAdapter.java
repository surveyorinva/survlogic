package com.survlogic.survlogic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.survlogic.survlogic.fragment.JobCogoSetupKnownFragment;
import com.survlogic.survlogic.fragment.JobCogoSetupOrientationFragment;
import com.survlogic.survlogic.fragment.MapOptionsTypePlanarFragment;
import com.survlogic.survlogic.fragment.MapOptionsTypeWorldFragment;

/**
 * Created by chrisfillmore on 10/12/2017.
 */

public class ActivityJobCogoSetupTypeAdapter extends FragmentPagerAdapter {
    private JobCogoSetupKnownFragment jobCogoSetupKnownFragment;
    private JobCogoSetupOrientationFragment jobCogoSetupOrientationFragment;

    public ActivityJobCogoSetupTypeAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new JobCogoSetupKnownFragment();
            case 1:
                return new JobCogoSetupOrientationFragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);


        switch (position){
            case 0:
                jobCogoSetupKnownFragment = (JobCogoSetupKnownFragment) createdFragment;
                break;
            case 1:
                jobCogoSetupOrientationFragment = (JobCogoSetupOrientationFragment) createdFragment;
                break;

        }
        return createdFragment;
    }

    public JobCogoSetupKnownFragment getJobCogoSetupKnownFragment() {
        return jobCogoSetupKnownFragment;
    }


    public JobCogoSetupOrientationFragment getJobCogoSetupOrientationFragment() {
        return jobCogoSetupOrientationFragment;
    }



}

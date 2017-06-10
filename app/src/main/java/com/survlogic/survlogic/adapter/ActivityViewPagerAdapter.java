package com.survlogic.survlogic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class ActivityViewPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> fragments= new ArrayList<>();
    ArrayList<String> tabTitles = new ArrayList<>();

    public void addFragments(Fragment fragments, String title){
        this.fragments.add(fragments);
        this.tabTitles.add(title);

    }


    public ActivityViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);



    }





}

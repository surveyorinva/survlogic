package com.survlogic.survlogic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.survlogic.survlogic.fragment.MapOptionsTypePlanarFragment;
import com.survlogic.survlogic.fragment.MapOptionsTypeWorldFragment;

/**
 * Created by chrisfillmore on 10/12/2017.
 */

public class DialogJobMapOptionsMapTypeAdapter extends FragmentPagerAdapter {

    public DialogJobMapOptionsMapTypeAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MapOptionsTypePlanarFragment();
            case 1:
                return new MapOptionsTypeWorldFragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}

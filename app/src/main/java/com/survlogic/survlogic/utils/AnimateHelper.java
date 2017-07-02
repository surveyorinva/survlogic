package com.survlogic.survlogic.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by chrisfillmore on 7/1/2017.
 */

public class AnimateHelper {

    public static void animateRecyclerView(RecyclerView.ViewHolder holder , boolean goesDown){


        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(holder.itemView, "translationY", goesDown==true ? 200 : -200, 0);
        animatorTranslateY.setDuration(500);

        animatorSet.playTogether(animatorTranslateY);

        animatorSet.start();

    }



}

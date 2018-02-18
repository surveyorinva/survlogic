package com.survlogic.survlogic.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by chrisfillmore on 7/1/2017.
 */

public class AnimateHelper {
    private static final String TAG = "AnimateHelper";


    public static void animateRecyclerView(RecyclerView.ViewHolder holder , boolean goesDown){
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(holder.itemView, "translationY", goesDown==true ? 200 : -200, 0);
        animatorTranslateY.setDuration(500);

        animatorSet.playTogether(animatorTranslateY);

        animatorSet.start();

    }

    public static void animateCardSlideNormalVisible(final CardView container, final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(null);


        }else{
            view.animate()
                    .setDuration(duration)
                    .translationY(-view.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });


        }

    }


    public static void animateViewSlideNormalVisible(final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .setDuration(duration)
                    .translationY(-view.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static void animateViewFadeNormalVisible(final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .setDuration(duration)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static void animateViewSlideOvershootVisible(final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    .setInterpolator(new BounceInterpolator())
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .setDuration(duration)
                    .translationY(-view.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static class ResizeCardAnimation extends Animation {
        final int targetHeight;
        View view;
        int startHeight;

        public ResizeCardAnimation(View view, int targetHeight, int startHeight) {
            this.view = view;
            this.targetHeight = targetHeight;
            this.startHeight = startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            //int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
            //to support decent animation, change new heigt as Nico S. recommended in comments
            int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }



}

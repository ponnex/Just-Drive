package com.ponnex.justdrive.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.ponnex.justdrive.MainActivity;
import com.ponnex.justdrive.R;
import com.ponnex.justdrive.Utils;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by ramos on 4/18/2015.
 */
public class ScreenSlidePageFragment_3 extends Fragment {

    View mParent;
    FrameLayout mBluePair;

    FloatingActionButton mBlue;

    float startBlueX;
    float startBlueY;

    int endBlueX;
    int endBlueY;

    final static AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    final static AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
    final static DecelerateInterpolator DECELERATE = new DecelerateInterpolator();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_slide_page_3, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mParent = view;
        mBluePair = (FrameLayout) view.findViewById(R.id.transition_blue_pair);
        mBlue = (FloatingActionButton) view.findViewById(R.id.fab_blue);
        mBlue.setOnClickListener(mClicker2);
    }

    View.OnClickListener mClicker2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startBlueX = Utils.centerX(mBlue);
            startBlueY = Utils.centerY(mBlue);

            endBlueX = (mBlue.getLeft() + mBlue.getRight()) / 2;
            endBlueY = (mBlue.getTop() + mBlue.getBottom()) / 2;

            mBlue.setVisibility(View.INVISIBLE);
            appearOrangePair();
        }
    };

    void appearOrangePair(){
        mBluePair.setVisibility(View.VISIBLE);

        float finalRadius = Math.max(mBluePair.getWidth(), mBluePair.getHeight()) * 1.5f;

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(mBluePair, endBlueX, endBlueY, mBlue.getWidth() / 2.5f,
                finalRadius);
        animator.setDuration(500);
        animator.setInterpolator(ACCELERATE);
        animator.addListener(new SimpleListener() {
            @Override
            public void onAnimationEnd() {
                //disappearOrangePair();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        animator.start();
    }

    void disappearOrangePair(){

        float finalRadius = Math.max(mBluePair.getWidth(), mBluePair.getHeight()) * 1.5f;

        endBlueX = (mBlue.getLeft() + mBlue.getRight()) / 2;
        endBlueY = (mBlue.getTop() + mBlue.getBottom()) / 2;

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(mBluePair, endBlueX, endBlueY,
                finalRadius, mBlue.getWidth() / 2.5f);
        animator.setDuration(500);
        animator.addListener(new SimpleListener() {
            @Override
            public void onAnimationEnd() {
                mBlue.setVisibility(View.VISIBLE);
                mBluePair.setVisibility(View.INVISIBLE);
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        animator.setInterpolator(DECELERATE);
        animator.start();
    }


    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private static class SimpleListener implements SupportAnimator.AnimatorListener, ObjectAnimator.AnimatorListener{

        @Override
        public void onAnimationStart() {

        }

        @Override
        public void onAnimationEnd() {

        }

        @Override
        public void onAnimationCancel() {

        }

        @Override
        public void onAnimationRepeat() {

        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}

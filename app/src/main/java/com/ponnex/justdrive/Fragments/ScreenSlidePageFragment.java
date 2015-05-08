package com.ponnex.justdrive.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

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
public class ScreenSlidePageFragment extends Fragment {

    View mParent;
    FloatingActionButton mBlue;
    FloatingActionButton mYellow;
    FrameLayout mBluePair;

    float startBlueX;
    float startBlueY;

    int endBlueX;
    int endBlueY;

    int startBluePairBottom;

    final static AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    final static AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
    final static DecelerateInterpolator DECELERATE = new DecelerateInterpolator();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screen_slide_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mParent = view;
        mBlue = (FloatingActionButton) view.findViewById(R.id.fab_blue);
        mBluePair = (FrameLayout) view.findViewById(R.id.transition_blue_pair);
        mBlue.setOnClickListener(mClicker);
    }

    View.OnClickListener mClicker = new View.OnClickListener() {
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
                //disappearBluePair();
                getFragmentManager().beginTransaction().replace(R.id.container, new ScreenSlidePageFragment_2()).commit();
            }
        });
        animator.start();
    }

    void disappearBluePair(){

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
                getFragmentManager().beginTransaction().replace(R.id.container, new ScreenSlidePageFragment_2()).commit();
            }
        });
        animator.setInterpolator(DECELERATE);
        animator.start();
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

    /*
    EditText usernameEditText;
    TextView usernameTextView;
    View usernameLine;
    Button welcomenext;
    FrameLayout mRipple;

    int ButtonX;
    int ButtonY;

    final static AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    final static AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences isAgree = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = isAgree.edit();
        editor.putBoolean("isAgree", false);
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);

        usernameEditText = (EditText) rootView.findViewById(R.id.username);
        usernameTextView = (TextView) rootView.findViewById(R.id.hello_text);
        usernameLine = (View) rootView.findViewById(R.id.line);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isHasName = (mSharedPreference.getBoolean("isHasName", false));

        SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String isUserName = (mSharedPreference1.getString("UserName", "Human"));

        if (isHasName){
            usernameEditText.setVisibility(View.GONE);
            usernameLine.setVisibility(View.GONE);
            usernameTextView.setText("Hello, " + isUserName);
        }

        rootView.findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRipple = (FrameLayout) view.findViewById(R.id.frame_screen1);
        welcomenext = (Button) view.findViewById(R.id.button_next);
        welcomenext.setOnClickListener(mNext);
    }

    View.OnClickListener mNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Boolean isDone = (mSharedPreference.getBoolean("isDone", false));

            if (isDone) {
                appearBluePair();
            }

            if (!isDone) {
                String sUsername = usernameEditText.getText().toString();
                if (sUsername.matches("")) {
                    usernameEditText.setError("Come on! Don't be shy I know you have a name");
                    return;
                }
                if (sUsername.length() < 3) {
                    usernameEditText.setError("Must be at least 3 characters");
                } else {
                    final String user_name = usernameEditText.getText().toString();

                    Toast toast;
                    toast = Toast.makeText(getActivity(), "Hello, " + user_name, Toast.LENGTH_SHORT);
                    toast.show();

                    SharedPreferences isUserName = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isUserName.edit();
                    editor.putString("UserName", user_name);
                    editor.apply();

                    SharedPreferences isHasName = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor1 = isHasName.edit();
                    editor1.putBoolean("isHasName", true);
                    editor1.apply();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            SharedPreferences isDone = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = isDone.edit();
                            editor.putBoolean("isDone", true);
                            editor.apply();

                            appearBluePair();
                            //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            //FragmentTransaction transaction = fragmentManager.beginTransaction();
                            //transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                            //transaction.replace(R.id.container, new ScreenSlidePageFragment_2());
                            //transaction.commit();
                        }
                    }, 500);
                }
            }

            ButtonX = (welcomenext.getLeft() + welcomenext.getRight()) / 2;
            ButtonY = (welcomenext.getTop() + welcomenext.getBottom()) / 2;
        }
    };

    void appearBluePair(){
        mRipple.setVisibility(View.VISIBLE);

        float finalRadius = Math.max(mRipple.getWidth(), mRipple.getHeight()) * 1.5f;

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(mRipple, ButtonX, ButtonY, welcomenext.getWidth() / 2.5f,
                finalRadius);
        animator.setDuration(500);
        animator.setInterpolator(ACCELERATE);
        animator.addListener(new SimpleListener() {
            @Override
            public void onAnimationEnd() {
                //raise();
                //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                //FragmentTransaction transaction = fragmentManager.beginTransaction();
                //transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                //transaction.replace(R.id.container, new ScreenSlidePageFragment_2());
                //transaction.commit();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new ScreenSlidePageFragment_2()).commit();
            }
        });
        animator.start();
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
    */
}
package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by EmmanuelFrancis on 6/1/2015.
 */

public class BaseActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    private View coordinatorLayout;

    private View mLoadingView;

    private int mShortAnimationDuration;

    protected static final int NAVDRAWER_ITEM_HOME = R.id.navigation_home;
    protected static final int NAVDRAWER_ITEM_ABOUT = R.id.navigation_about;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        coordinatorLayout = findViewById(R.id.layout_main);

        mLoadingView = findViewById(R.id.loading_spinner);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            coordinatorLayout.animate()
                    .alpha(0f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(null);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer NavItem = (mSharedPreference2.getInt("NavItem", NAVDRAWER_ITEM_HOME));

        if(NavItem == NAVDRAWER_ITEM_HOME) {
            navigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);
        }
        if(NavItem == NAVDRAWER_ITEM_ABOUT) {
            navigationView.getMenu().findItem(R.id.navigation_about).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.about:
                Intent intent = new Intent(BaseActivity.this, AboutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.debug_on:
                SharedPreferences debug = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = debug.edit();
                editor.putBoolean("debug", true);
                editor.apply();

                if(!isServiceRunning(AppLockService.class)) {
                    startService(new Intent(BaseActivity.this, AppLockService.class));
                    //get audio service
                    final AudioManager current = (AudioManager) this
                            .getSystemService(Context.AUDIO_SERVICE);

                    //get and store the users current sound mode
                    int audioMode = current.getRingerMode();
                    SharedPreferences audio = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor1 = audio.edit();
                    editor1.putInt("audioMode", audioMode);
                    editor1.apply();
                }
                if(!isServiceRunning(CallerService.class)){
                    startService(new Intent(BaseActivity.this, CallerService.class));
                }
                return true;
            case R.id.debug_off:
                SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean debug2 = (mSharedPreference.getBoolean("debug", false));

                if(debug2) {
                    SharedPreferences debug1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor1 = debug1.edit();
                    editor1.putBoolean("debug", false);
                    editor1.apply();

                    if (isServiceRunning(AppLockService.class)) {
                        stopService(new Intent(BaseActivity.this, AppLockService.class));
                    }
                    if (isServiceRunning(CallerService.class)) {
                        stopService(new Intent(BaseActivity.this, CallerService.class));
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        if (menuItem.getItemId() == getSelfNavDrawerItem()) {
                            mDrawerLayout.closeDrawers();
                            return true;
                        } else {
                            switch (menuItem.getItemId()) {
                                case R.id.navigation_home:
                                    coordinatorLayout.animate()
                                            .alpha(0f)
                                            .setDuration(mShortAnimationDuration)
                                            .setListener(null);

                                    mLoadingView.setVisibility(View.VISIBLE);

                                    mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                                        @Override
                                        public void onDrawerSlide(View drawerView, float slideOffset) {

                                        }

                                        @Override
                                        public void onDrawerOpened(View drawerView) {

                                        }

                                        @Override
                                        public void onDrawerClosed(View drawerView) {
                                            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onDrawerStateChanged(int newState) {

                                        }
                                    });

                                    SharedPreferences NavItem = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor = NavItem.edit();
                                    editor.putInt("NavItem", NAVDRAWER_ITEM_HOME);
                                    editor.apply();

                                    menuItem.setChecked(true);
                                    mDrawerLayout.closeDrawers();
                                    return true;
                                case R.id.navigation_about:
                                    coordinatorLayout.animate()
                                            .alpha(0f)
                                            .setDuration(mShortAnimationDuration)
                                            .setListener(null);

                                    mLoadingView.setVisibility(View.VISIBLE);

                                    mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                                        @Override
                                        public void onDrawerSlide(View drawerView, float slideOffset) {

                                        }

                                        @Override
                                        public void onDrawerOpened(View drawerView) {

                                        }

                                        @Override
                                        public void onDrawerClosed(View drawerView) {
                                            Intent intent = new Intent(BaseActivity.this, AboutActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onDrawerStateChanged(int newState) {

                                        }
                                    });

                                    SharedPreferences NavItem1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor1 = NavItem1.edit();
                                    editor1.putInt("NavItem", NAVDRAWER_ITEM_ABOUT);
                                    editor1.apply();

                                    menuItem.setChecked(true);
                                    mDrawerLayout.closeDrawers();
                                    return true;
                            }
                        }
                        return true;
                    }
                });
    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();

        coordinatorLayout.setAlpha(0f);
        coordinatorLayout.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        mLoadingView = findViewById(R.id.loading_spinner);
        mLoadingView.setVisibility(View.GONE);

        super.onResume();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

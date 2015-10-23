package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
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

    private Toolbar mActionBarToolbar;

    private Handler mHandler;

    protected static final int NAVDRAWER_ITEM_HOME = R.id.navigation_home;
    protected static final int NAVDRAWER_ITEM_ABOUT = R.id.navigation_about;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        getActionBarToolbar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        coordinatorLayout = findViewById(R.id.layout_main);
        if(coordinatorLayout != null) {
            coordinatorLayout.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            if(coordinatorLayout != null) {
                coordinatorLayout.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
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
                if(!isServiceRunning(AppLockService.class)) {
                    startService(new Intent(this, AppLockService.class));
                }
                if(!isServiceRunning(CallerService.class)) {
                    startService(new Intent(this, CallerService.class));
                    //get audio service
                    final AudioManager current = (AudioManager) this
                            .getSystemService(Context.AUDIO_SERVICE);

                    //get and store the users current sound mode
                    int audioMode = current.getRingerMode();
                    SharedPreferences audio = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = audio.edit();
                    editor.putInt("audioMode", audioMode);
                    editor.apply();
                }

                SharedPreferences debug = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = debug.edit();
                editor.putBoolean("debug", true);
                editor.apply();

                return true;
            case R.id.debug_off:
                if(isServiceRunning(AppLockService.class)) {
                    stopService(new Intent(this, AppLockService.class));
                }
                if(isServiceRunning(CallerService.class)) {
                    stopService(new Intent(this, CallerService.class));
                }

                SharedPreferences debug1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor1 = debug1.edit();
                editor1.putBoolean("debug", false);
                editor1.apply();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {

            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
            }
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mActionBarToolbar != null) {
                // Depending on which version of Android you are on the Toolbar or the ActionBar may be
                // active so the a11y description is set here.
                mActionBarToolbar.setNavigationContentDescription(getResources().getString(R.string
                        .navdrawer_description));
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
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
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            if (coordinatorLayout != null) {
                                coordinatorLayout.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
                            }
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    goToNavDrawerItem(menuItem.getItemId());
                                }
                            }, NAVDRAWER_LAUNCH_DELAY);
                        }
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
    }

    private void goToNavDrawerItem(int item) {
        switch (item) {
            case R.id.navigation_home:
                createBackStack(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.navigation_about:
                createBackStack(new Intent(this, AboutActivity.class));
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        invalidateOptionsMenu();
        if(coordinatorLayout != null) {
            coordinatorLayout.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
            overridePendingTransition(0, 0);
        } else {
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
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

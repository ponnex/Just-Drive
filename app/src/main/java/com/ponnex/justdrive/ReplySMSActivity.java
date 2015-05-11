package com.ponnex.justdrive;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MenuItem;

/**
 * Created by EmmanuelFrancis on 5/10/2015.
 */
public class ReplySMSActivity extends AppCompatActivity {
    public final static int ORANGELIGHT = 0;
    public final static int ORANGEDARK = 1;
    public final static int BLUEGREYLIGHT = 2;
    public final static int BLUEGREYDARK = 3;
    public final static int INDIGOLIGHT = 4;
    public final static int INDIGODARK = 5;
    private Integer theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().hasExtra("bundle") && savedInstanceState == null){
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
        }

        SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        theme = (mSharedPreference1.getInt("theme", 1));

        switch(theme)
        {
            case ORANGELIGHT:
                setTheme(R.style.JustDriveOrangeLightTheme);
                break;
            case ORANGEDARK:
                setTheme(R.style.JustDriveOrangeDarkTheme);
                break;
            case BLUEGREYLIGHT:
                setTheme(R.style.JustDriveBlueGreyLightTheme);
                break;
            case BLUEGREYDARK:
                setTheme(R.style.JustDriveBlueGreyDarkTheme);
                break;
            case INDIGOLIGHT:
                setTheme(R.style.JustDriveIndigoLightTheme);
                break;
            case INDIGODARK:
                setTheme(R.style.JustDriveIndigoDarkTheme);
                break;

            default:
        }

        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_replysms);
        if (Build.VERSION.SDK_INT >= 21) {
            getFragmentManager().beginTransaction().replace(R.id.container_sms, new ReplySettingsFragmentLollipop()).commit();
        }else{
            getFragmentManager().beginTransaction().replace(R.id.container_sms, new ReplySettingsFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}


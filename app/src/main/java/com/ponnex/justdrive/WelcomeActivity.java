package com.ponnex.justdrive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.ponnex.justdrive.Fragments.ScreenSlidePageFragment;

/**
 * Created by ramos on 4/18/2015.
 */
public class WelcomeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new ScreenSlidePageFragment()).commit();

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isFirstRun=(mSharedPreference.getBoolean("isFirstRun", true));

        SharedPreferences isHasName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor1 = isHasName.edit();
        editor1.putBoolean("isHasName", false);
        editor1.apply();

        SharedPreferences isDone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = isDone.edit();
        editor.putBoolean("isDone", false);
        editor.apply();

        if(!isFirstRun){
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            WelcomeActivity.this.finish();
        }
    }

}

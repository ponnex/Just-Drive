package com.ponnex.justdrive;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by EmmanuelFrancis on 6/1/2015.
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        SharedPreferences NavItem = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = NavItem.edit();
        editor.putInt("NavItem", NAVDRAWER_ITEM_ABOUT);
        editor.apply();
        super.onResume();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ABOUT;
    }
}

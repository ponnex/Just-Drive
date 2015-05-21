package com.ponnex.justdrive;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by EmmanuelFrancis on 5/10/2015.
 */

public class ReplySMSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


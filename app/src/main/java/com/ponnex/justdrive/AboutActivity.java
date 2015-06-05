package com.ponnex.justdrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * Created by EmmanuelFrancis on 6/1/2015.
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView_version = (TextView)findViewById(R.id.version_name);

        textView_version.setText("Version " + getApplicationVersionName(getApplicationContext()));

        TextView textView_source = (TextView)findViewById(R.id.source);
        textView_source.setText(Html.fromHtml(
                "Just Drive, created by" +
                        "\n<b>Emmanuel Francis Ramos Jr.</b>" +
                        "\n<a href=\'https://github.com/ponnex/Just-Drive\'>Github</a>&nbsp;&nbsp;" +
                        "<a href=\'https://plus.google.com/+EmmanuelFrancisRamos\'>Google+</a>&nbsp;&nbsp;" +
                        "<a href=\'https://www.linkedin.com/in/ponnex\'>LinkedIn</a>"));
        textView_source.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static String getApplicationVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            return "";
        }
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

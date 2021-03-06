package com.ponnex.justdrive;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;

/**
 * Created by ramos on 4/15/2015.
 */

public class SettingsFragmentLollipop extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;
    private SwitchPreference switchbloxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        addPreferencesFromResource(R.xml.prefs);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isphone = (mSharedPreference2.getBoolean("phone", true));

        SharedPreferences mSharedPreference3= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isstartonboot = (mSharedPreference3.getBoolean("startonboot", true));

        if (isSwitch){
            getPreferenceScreen().findPreference("switch").setSummary("Enabled");
        }
        else {
            getPreferenceScreen().findPreference("switch").setSummary("Disabled");
        }

        if (isstartonboot){
            getPreferenceScreen().findPreference("startonboot").setSummary("Enable Just Drive on boot");
        }
        else {
            getPreferenceScreen().findPreference("startonboot").setSummary("Disable Just Drive on boot");
        }

        if (isphone){
            getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                    "Read caller ID of incoming phone calls");
        }
        else {
            getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                    "Disable reading caller ID of incoming phone calls");
        }

        SharedPreferences mSharedPreference4= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReplyCalls = (mSharedPreference4.getBoolean("autoReplyCalls", true));

        SharedPreferences mSharedPreference5= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReply = (mSharedPreference5.getBoolean("autoReply", true));

        SharedPreferences mSharedPreference6= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String isMsg=(mSharedPreference6.getString("msg", "I am driving right now, I will contact you later."));

        if (isautoReplyCalls){
            getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Reply incoming calls with SMS");
        }
        else {
            getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Disabled");
        }

        if (isautoReply){
            getPreferenceScreen().findPreference("autoReply").setSummary("Reply text messages with SMS");
        }
        else {
            getPreferenceScreen().findPreference("autoReply").setSummary("Disabled");
        }

        getPreferenceScreen().findPreference("msg").setSummary("''" + isMsg + " --This is an automated SMS--''");

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("autoReplyCalls");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Reply incoming calls with SMS");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Disabled");
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("autoReply");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("autoReply").setSummary("Reply text messages with SMS");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("autoReply").setSummary("Disabled");
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("switch", true);
                    editor.apply();

                    getPreferenceScreen().findPreference("switch").setSummary("Enabled");
                }
                if (newValue.toString().equals("false")) {
                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("switch", false);
                    editor.apply();

                    getPreferenceScreen().findPreference("switch").setSummary("Disabled");
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("phone");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                            "Read caller ID of incoming phone calls");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                            "Disable reading caller ID of incoming phone calls");
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("startonboot");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("startonboot").setSummary("Enable Just Drive on boot");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("startonboot").setSummary("Disable Just Drive on boot");
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.listview_layout, container, false);
        ListView lv = (ListView) view.findViewById(android.R.id.list);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
                    floatingActionButton.animate().translationY(floatingActionButton.getHeight() + 16).setInterpolator(new AccelerateInterpolator(2)).start();
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
                    floatingActionButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        try {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                    for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                        updatePreference(preferenceGroup.getPreference(j));
                    }
                } else {
                    updatePreference(preference);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private boolean switchstate(){
        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        return (mSharedPreference.getBoolean("switch", true));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
            switchbloxt.setChecked(switchstate());

            if(switchstate()){
                getPreferenceScreen().findPreference("switch").setSummary("Enabled");
            }
            else{
                getPreferenceScreen().findPreference("switch").setSummary("Disabled");
            }
        }
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof MaterialEditTextPreference) {
            MaterialEditTextPreference editbloxtmsg = (MaterialEditTextPreference) preference;

            preference.setSummary("''" + editbloxtmsg.getText() + " --This is an automated SMS--''");

            if (editbloxtmsg.getText() == null || editbloxtmsg.getText().equals("")) {
                preference.setSummary("''I am driving right now, I will contact you later. --This is an automated SMS--''");
            }
        }
    }
}

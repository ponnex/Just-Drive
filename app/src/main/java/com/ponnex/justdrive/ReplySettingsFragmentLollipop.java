package com.ponnex.justdrive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import android.preference.CheckBoxPreference;

/**
 * Created by EmmanuelFrancis on 5/10/2015.
 */

public class ReplySettingsFragmentLollipop extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;
    static String mPhoneNumber;
    static boolean active = false;
    private View positiveAction;
    private  MaterialEditTextPreference editbloxtmsg;

    private String TAG = "com.ponnex.justdrive.ReplySettingsFragmentLollipop";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CheckBoxPreference checkbloxtautoReplyCalls;
        CheckBoxPreference checkbloxtautoReply;

        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        addPreferencesFromResource(R.xml.smsprefs);

        TelephonyManager mManager =(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber =  mManager.getLine1Number();

        SharedPreferences isPhoneNumber = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = isPhoneNumber.edit();
        editor.putString("PhoneNumber", mPhoneNumber);
        editor.apply();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReplyCalls = (mSharedPreference.getBoolean("autoReplyCalls", true));

        SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReply = (mSharedPreference2.getBoolean("autoReply", true));

        SharedPreferences mSharedPreference5= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String isMsg=(mSharedPreference5.getString("msg", "I am driving right now, I will contact you later."));

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

        checkbloxtautoReplyCalls = (CheckBoxPreference) getPreferenceManager().findPreference("autoReplyCalls");
        checkbloxtautoReplyCalls.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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

        checkbloxtautoReply = (CheckBoxPreference) getPreferenceManager().findPreference("autoReply");
        checkbloxtautoReply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {

                    SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String isPhoneNumber = (mSharedPreference2.getString("PhoneNumber", null));

                    if (isPhoneNumber == null){
                        showBasicNoTitleNumberCheck();
                    }
                    else {
                        getPreferenceScreen().findPreference("autoReply").setSummary("Reply text messages with SMS");
                    }

                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("autoReply").setSummary("Disabled");
                }
                return true;
            }
        });
    }

    private void showBasicNoTitleNumberCheck() {
        EditText phonenumber;

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.phonenumber)
                .customView(R.layout.dialog_numberview, true)
                .positiveText(R.string.button_update)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String isPhoneNumber = (mSharedPreference2.getString("PhoneNumber", null));

                        getPreferenceScreen().findPreference("autoReply").setSummary("Enabled");

                        Log.d(TAG, isPhoneNumber);
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                }).build();

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        phonenumber = (EditText) dialog.getCustomView().findViewById(R.id.phonenumber);
        phonenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                positiveAction.setEnabled(s.toString().trim().length() > 6);
                SharedPreferences isPhoneNumber = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = isPhoneNumber.edit();
                editor.putString("PhoneNumber", s.toString());
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dialog.show();
        positiveAction.setEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;

    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onResume() {
        super.onResume();

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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof MaterialEditTextPreference) {
            editbloxtmsg = (MaterialEditTextPreference) preference;

            preference.setSummary("''" + editbloxtmsg.getText()+ " --This is an automated SMS--''");

            if (editbloxtmsg.getText() == null || editbloxtmsg.getText().equals("")) {
                preference.setSummary("''I am driving right now, I will contact you later. --This is an automated SMS--''");

            }
        }
    }
}

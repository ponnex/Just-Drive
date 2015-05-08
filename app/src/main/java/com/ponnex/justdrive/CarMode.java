package com.ponnex.justdrive;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Locale;

/**
 * Created by ramos on 4/15/2015.
 */

public class CarMode extends Service {
    //the sound mode of the user's phone
    private static int audioMode;
    //autoreply
    public static boolean autoreply = false;
    //auto-reply message
    static String msg = "I am driving right now, I will contact you later --Auto reply message--";
    //user auto-reply
    private boolean auto;
    //allow phone
    private boolean phone;
    //prefs
    private SharedPreferences getPrefs;
    //text to speech
    TextToSpeech tts;
    PhoneStateListener psl;
    TelephonyManager tm;
    private HeadSetIntentReceiver myReceiver;

    private int headphonestate;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Log.e("TYPE", "CM Created");
        myReceiver = new HeadSetIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //read headset state
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(bluetoothreceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        //get audio service
        final AudioManager current = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        //get and store the users current sound mode
        audioMode = current.getRingerMode();
        //get the preferences
        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        //assign the preferences
        userSettings();
        //enable or disable auto-reply
        autoreply = auto;
        //set the phone to silent
        silent();
        //tts or block phone calls
        if (!phone && headphonestate==1) {
            //get the phone service
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //call state listener
            psl = new PhoneStateListener() {
                public void onCallStateChanged(int state, String incomingNumber) {
                    //incoming number
                    final String incoming = incomingNumber;
                    //if the phone is ringing
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        Log.d("Phone State", "Ringing");
                        //read out the caller name
                        tts = new TextToSpeech(CarMode.this, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    tts.setLanguage(Locale.US);
                                    String readNumber = "New call from ";
                                    String name = quickCallerId(incoming);
                                    //if the number is not in the contacts, say the number
                                    if (name.isEmpty()) {
                                        for (int i = 0; i < incoming.length(); i++) {
                                            readNumber = readNumber + incoming.charAt(i) + " ";
                                        }
                                    } else {
                                        readNumber = readNumber + name;
                                    }
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                                        int amStreamMusicMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
                                        am.setStreamVolume(am.STREAM_MUSIC, amStreamMusicMaxVol, 15);
                                        tts.speak(readNumber, TextToSpeech.QUEUE_FLUSH, null, null);
                                    }
                                    else {
                                        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                                        int amStreamMusicMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
                                        am.setStreamVolume(am.STREAM_MUSIC, amStreamMusicMaxVol, 15);
                                        tts.speak(readNumber, TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }
                            }
                        });

                    }
                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        Log.d("Phone State", "Idle");
                        //end text to speech
                        if (tts != null) {
                            tts.stop();
                            tts.shutdown();
                        }
                    }
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        Log.d("Phone State", "Offhook");
                        if (tts != null) {
                            //Stop text to speech if phone is offhook
                            tts.stop();
                            tts.shutdown();
                        }
                    }
                }

            };
            //listen for calls
            tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
            silent();
        }
        //run until stopped
        return START_STICKY;
    }

    //Identify if headset is connected
    private class HeadSetIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                headphonestate = intent.getIntExtra("state", -1);
                switch (headphonestate) {
                    case 0:
                        Log.e("TYPE", "Headset is unplugged");
                        silent();
                        break;
                    case 1:
                        Log.e("TYPE", "Headset is plugged");
                        soundMode();
                        break;
                    default:
                        Log.e("TYPE", "I have no idea what the headset state is");
                }
            }
        }
    }

    //identify if bluetooth is on and connected to headset
    //haven't tested yet
    private BroadcastReceiver bluetoothreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.STATE_CONNECTED)) {
                if (action.equals(BluetoothA2dp.HEADSET)) {
                    soundMode();
                }
            }
        }
    };


    //identify the caller
    private String quickCallerId(String phoneNumber) {
        //path to contacts
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        //get the contact name
        ContentResolver resolver = getContentResolver();
        Cursor cur = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cur != null && cur.moveToFirst()) {
            String value = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            if (value != null) {
                //close the cursor and return the name
                cur.close();
                return value;
            }
        }
        //if there is no name, return nothing
        try {
            assert cur != null;
            cur.close();
        } catch (NullPointerException e) {
            Log.e("CarMode", "Cursor caused a null pointer exception");
        }
        return "";
    }

    @Override
    public void onDestroy() {
        Log.e("TYPE", "CM Destoyed");
        //turn off auto-reply
        autoreply = false;
        //turn back the user's last sound mode
        soundMode();
        //remove phone state listener
        try {
            tm.listen(psl, psl.LISTEN_NONE);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        //disable tts
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        //unregister headset receiver
        unregisterReceiver(myReceiver);

        stopService(new Intent(CarMode.this, NotificationListener.class));

        super.onDestroy();
    }

    void userSettings() {
        //phone calls
        phone = getPrefs.getBoolean("phone", false);
        // autoreply
        auto = getPrefs.getBoolean("autoReply", true);
        // autoreply message
        msg = getPrefs
                .getString("msg", "I am driving right now, I will contact you later");
        if (msg.contentEquals("")) {
            msg = "I am driving right now, I will contact you later";
        }
    }

    // SilentToNormal and NormalToSilent device
    void silent() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // Silent Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        if (Build.VERSION.SDK_INT >= 18) {
           //add notification listener here
            startService(new Intent(CarMode.this, NotificationListener.class));
        }
    }

    void vibrate() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // vibrate mode
        mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    void normal() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // Normal Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    void soundMode() {
        if (audioMode == 1) {
            vibrate();
        } else if (audioMode == 2) {
            normal();
        } else {
            silent();
        }
    }
}
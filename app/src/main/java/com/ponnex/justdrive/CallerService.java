package com.ponnex.justdrive;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Locale;

/**
 * Created by ramos on 4/15/2015.
 */

public class CallerService extends Service {
    //autoreply
    public static boolean autoreply = false;
    //auto-reply message
    static String msg = "I am driving right now, I will contact you later --Auto reply message--";
    //user auto-replySMS
    private boolean autoSMS;
    //user auto-replyCall
    private boolean autoCall;
    //allow phone
    private boolean phone;
    //prefs
    private SharedPreferences getPrefs;

    static boolean ring = false;
    static boolean callReceived = false;

    //text to speech
    TextToSpeech tts;
    PhoneStateListener psl;
    TelephonyManager tm;
    private HeadSetIntentReceiver myReceiver;

    private String TAG = "com.ponnex.justdrive.Telephony.CallerService";

    private int headphonestate;

    private boolean bluetoothstate = false;

    private String getCallNumber = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Log.d(TAG, "CS Created");
        myReceiver = new HeadSetIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);

        //read headset state
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(bluetoothreceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        //read ringermode change
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(RingerModereceiver, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get the preferences
        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        //assign the preferences
        userSettings();
        //enable or disable auto-reply
        autoreply = autoSMS;
        //set the phone to silent
        silent();
        //tts or block phone calls
        if (phone && (headphonestate == 1 || bluetoothstate)) {
            //get the phone service
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //call state listener
            psl = new PhoneStateListener() {
                public void onCallStateChanged(int state, String incomingNumber) {
                    //incoming number
                    final String incoming = incomingNumber;
                    //if the phone is ringing
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        Log.d(TAG + "Phone State", "Ringing");
                        //read out the caller name
                        tts = new TextToSpeech(CallerService.this, new TextToSpeech.OnInitListener() {
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
                                            getCallNumber = getCallNumber + incoming.charAt(i);
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
                        Log.d(TAG + "Phone State", "Idle");
                        //end text to speech
                        if (tts != null) {
                            tts.stop();
                            tts.shutdown();
                        }
                    }

                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        Log.d(TAG + "Phone State", "Offhook");
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

        //auto-reply calls
        if(autoCall && (headphonestate == 0 || !bluetoothstate)){
            //get the phone service
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //call state listener
            psl = new PhoneStateListener() {
                public void onCallStateChanged(int state, String incomingNumber) {
                    //if the phone is ringing
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        Log.d(TAG + "Phone State Auto Reply Calls", "Ringing");
                        //get the number of incoming call
                        for (int i = 0; i < incomingNumber.length(); i++) {
                            getCallNumber = getCallNumber + incomingNumber.charAt(i);
                        }
                        ring = true;
                    }

                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        Log.d(TAG + "Phone State Auto Reply Calls", "Idle");
                        if(ring && !callReceived){ //when the call is not received == missed call
                            SendMessage(getCallNumber);
                            ring = false;
                            Log.d(TAG, "ring = false, " + "SendMessage");
                        } else {
                            callReceived = false;
                            Log.d(TAG, "callReceived = false");
                        }
                    }

                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        Log.d(TAG + "Phone State Auto Reply Calls", "Offhook");
                        // Call received
                        callReceived = true;
                        Log.d(TAG, "callReceived = true");
                    }
                }

            };
        }

        //run until stopped
        return START_STICKY;
    }

    private void SendMessage(final String msg_from) {
        try {
            String SENT = "sent";
            String DELIVERED = "delivered";

            SharedPreferences mSharedPreference6= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String isMsg = (mSharedPreference6.getString("msg", "I am driving right now, I will contact you later."));
            String msg = isMsg + "\n--This is an automated SMS--";

            Intent sentIntent = new Intent(SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deliveryIntent = new Intent(DELIVERED);
            PendingIntent deliverPI = PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            /* Register for SMS send action */
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String result;

                    switch (getResultCode()) {

                        case Activity.RESULT_OK:
                            result = "Sending Auto Reply Message...";
                            Log.d(TAG + "TEXT", "Sending Failed: " + result);
                            NotifyMessage("Sending Failed: " + result, "", false);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "Message Sending failed";
                            Log.d(TAG + "TEXT", "Sending Failed: " + result);
                            NotifyMessage(result, "", false);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = "Radio is Off";
                            Log.d(TAG + "TEXT", "Sending Failed: " + result);
                            NotifyMessage("Sending Failed: " + result, "", false);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = "No PDU defined";
                            Log.d(TAG + "TEXT", "Sending Failed: " + result);
                            NotifyMessage("Sending Failed: " + result, "", false);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = "Service is currently unavailable";
                            Log.d(TAG + "TEXT", "Sending Failed: " + result);
                            NotifyMessage("Sending Failed: " + result, "", false);
                            break;
                    }
                }

            }, new IntentFilter(SENT));
            /* Register for Delivery event */
            registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    NotifyMessage("Auto Reply Message sent to ", msg_from, true);
                    Log.d(TAG + "TEXT", "MessageNotification, DELIVERED");
                }

            }, new IntentFilter(DELIVERED));

            /*Send SMS*/
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(msg_from, null, msg, sentPI, deliverPI);
            Log.d(TAG + "TEXT", "smsManager.sendTextMessage");

        } catch (Exception ex) {
            ex.printStackTrace();
            NotifyMessage(ex.getMessage(), "", false);
        }
    }

    private void NotifyMessage(String result, String msg_from, boolean status) {
        Intent intent = new Intent(this, MessageNotification.class);
        intent.putExtra("From", msg_from);
        intent.putExtra("Message", result);
        intent.putExtra("Status", status);
        startService(intent);
    }

    //Identify if headset is connected
    private class HeadSetIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                headphonestate = intent.getIntExtra("state", -1);
                switch (headphonestate) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        silent();
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        soundMode();
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
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
                    bluetoothstate = true;
                }
            }
        }
    };

    private BroadcastReceiver RingerModereceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            silent();
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
            Log.d(TAG, "Cursor caused a null pointer exception");
        }
        return "";
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "CS Destoyed");
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

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(bluetoothreceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(RingerModereceiver);

        super.onDestroy();
    }

    void userSettings() {
        //phone calls
        phone = getPrefs.getBoolean("phone", true);
        // autoreplySMS
        autoCall = getPrefs.getBoolean("autoReplyCalls", true);
        // autoreplySMS
        autoSMS = getPrefs.getBoolean("autoReply", true);
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
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer audio = (mSharedPreference.getInt("audioMode", 2));

        if (audio == 1) {
            vibrate();
        } else if (audio == 2) {
            normal();
        } else {
            silent();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),
                this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 500,
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}
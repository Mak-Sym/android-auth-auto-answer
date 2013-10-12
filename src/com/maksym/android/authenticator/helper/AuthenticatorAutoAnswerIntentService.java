package com.maksym.android.authenticator.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Auto answer intent service. Functionality is exposed as separate service as it may take several seconds
 * to process, so it is not very good to handle it in receiver.
 * Service is called by {@link AuthenticatorAutoAnswerReceiver} only
 * if application is allowed (SettingsActivity.IS_ACTIVE_PREFERENCE) and phone number is recognized.
 *
 * @author maksym
 */
public class AuthenticatorAutoAnswerIntentService extends IntentService {

    public AuthenticatorAutoAnswerIntentService() {
        this("AuthenticatorAutoAnswerIntentService");
    }

    public AuthenticatorAutoAnswerIntentService(String name) {
        super("AuthenticatorAutoAnswerIntentService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get application settings.
     * Get phone number from the settings
     * Compare with incoming number
     * If equals, pick up the phone and press "#" key after short delay
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("AuthenticatorAutoAnswerIntentService.onReceive", "Received!");
        try {
            //delay for some time to ring
            //if service picks up phone too early, call will run "in background", which looks ugly
            try { Thread.sleep(1500);
            } catch (InterruptedException ignored) {}

            Context context = getBaseContext();
            // Make sure the phone is still ringing
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
                Log.d("AuthenticatorAutoAnswerIntentService.answerToPhonePhactor", "not ringing any more :(");
                return;
            }
            Log.d("AuthenticatorAutoAnswerIntentService.answerToPhonePhactor", "Still Ringing!");

            //get telephony service
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(tm);

            //silence ringer and answering the call
            pickUp(context, telephonyService);
            answer(context);
            hangUp(context, telephonyService);
        } catch(Exception e){
            e.printStackTrace();
            Log.w("AuthenticatorAutoAnswerIntentService.onReceive","Error trying to answer using telephony service. Intent: " + intent);
        }
        return;
    }

    protected void pickUp(Context context, ITelephony telephonyService) throws Exception {
        try {
            /*
             * ITelephony hack does not work in Android 2.3+ b/c of permissions problem
             * {@link http://stackoverflow.com/questions/4715250/how-to-grant-modify-phone-state-permission-for-apps-ran-on-gingerbread}
             * that's why in "catch" section I use another hack that may work to pick-up the phone
             */
            Log.d("AuthenticatorAutoAnswerIntentService.pickUp", "Silence Ringer using ITelephony");
            telephonyService.silenceRinger();
            Log.d("AuthenticatorAutoAnswerIntentService.pickUp", "Answer the call using ITelephony");
            telephonyService.answerRingingCall();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("AuthenticatorAutoAnswerIntentService.pickUp", "Error pickimng up the phone using ITelephony", e);

            // Simulate a press of the headset button to pick up the call
            Log.d("AuthenticatorAutoAnswerIntentService.pickUp", "Intent.ACTION_MEDIA_BUTTON Down");
            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
            context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

            // froyo and beyond trigger on buttonUp instead of buttonDown
            Log.d("AuthenticatorAutoAnswerIntentService.pickUp", "Intent.ACTION_MEDIA_BUTTON Up");
            Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
            context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
        }
    }

    /**
     * This method should send "#" dtmf to caller.
     * That cannot be done on android using public api (https://code.google.com/p/android/issues/detail?id=1428)
     *
     * I've tried:
     *
     * 1. Send "#" key pressed event (does not work due to security restrictions - event may be sent only for the app that initiates event,
     *    it is impossible to send keypressed events to another app)
     *    http://stackoverflow.com/questions/5383401/android-inject-events-permission
     *    http://www.pocketmagic.net/2012/04/injecting-events-programatically-on-android/#.UlOWuWSG3wM
     * 2. Send "dial" intent and try to dial "#" - does not work
     * 3. I've figured out that if to play "#" tone loud enough, it can be treated as keypress (at least worked in my case)
     *    I was not able to play that sound loud enough (on android phone) to make it work. However, if I put my android device close enough
     *    to the powerful sound speaker that plays "#" tone, it works. So I left that variant as "almost work" one.
     *
     *
     * @param context
     */
    protected void answer(Context context) {
        //wait
        Log.d("AuthenticatorAutoAnswerIntentService.answer", "Waiting for 1000ms");
        try { Thread.sleep(7000); }
        catch(Exception ignored){}

        /*
        //does not work - see method's javadoc
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel://1,##"));
        startActivity(intent);*/

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

        Log.d("AuthenticatorAutoAnswerIntentService.answer", "generating tone");
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, ToneGenerator.MAX_VOLUME);
        for(int i = 0; i < 10; i++){
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_P, 300);
            try { Thread.sleep(1000); }
            catch(Exception ignored){}
        }
    }

    protected void hangUp(Context context, ITelephony telephonyService) throws Exception {
        Log.d("AuthenticatorAutoAnswerIntentService.hangUp", "Waiting for 1000ms");
        try { Thread.sleep(1000); }
        catch(Exception ignored){}
        //end call
        Log.d("AuthenticatorAutoAnswerIntentService.answerToPhonePhactor", "End Call");
        telephonyService.endCall();  //unexpectedly it works :) at least for now
    }

}

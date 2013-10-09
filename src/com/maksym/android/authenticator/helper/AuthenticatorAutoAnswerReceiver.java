package com.maksym.android.authenticator.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AuthenticatorAutoAnswerReceiver extends BroadcastReceiver {
	
	private final static String LOG_TAG = "PhonePhactorAutoAnswerService";
	
	/**
	 * Receives information about incomming call.
	 * If service is allowed, sends event to the service to handle event (b/c handling may take several seconds,
	 * and we cannot do expensive operations in receiver)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//Phone state
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		
		if(phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING) &&
				prefs.getBoolean(SettingsActivity.IS_ACTIVE_PREFERENCE, false)){
			
			//get number from settings
			String phoneNumber = prefs.getString(SettingsActivity.PHONE_NUMBER_PREFERENCE, null);
			Log.d(LOG_TAG, "phoneNumber: " + phoneNumber);
			//get incoming number
			String incomingNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			Log.d(LOG_TAG, "incomingNumber: " + incomingNumber);
			
			if(incomingNumber != null && incomingNumber.endsWith(phoneNumber)){
				// Call a service, since this could take a few seconds
				Log.d(LOG_TAG, "Calling PhonePhactorAutoAnswerIntentService");
				context.startService(new Intent(context, AuthenticatorAutoAnswerIntentService.class));
			}
		}
		
		return;
	}

}

package com.maksym.android.authenticator.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AuthenticatorAutoAnswerNotifier {

private final static int NOTIFICATION_ID = 1;
	
	private Context context;
	private NotificationManager notificationManager;
	private SharedPreferences sharedPreferences;
	
	public AuthenticatorAutoAnswerNotifier(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void update() {
		if (sharedPreferences.getBoolean(SettingsActivity.IS_ACTIVE_PREFERENCE, false)) {
			// Intent to call to turn off AutoAnswer
			Intent notificationIntent = new Intent(context, SettingsActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			
			// Create the notification
			//notification can be created like this, but this is new api - may not work on old androids
			/*Notification n = new Notification.Builder(context)
							.setContentTitle(context.getString(R.string.notification_title))
							.setContentText(context.getString(R.string.notification_text))
							.setSmallIcon(R.drawable.status_phone_ico)
							.setDeleteIntent(pendingIntent)
							.build();*/
			//that's why we're using old-style deprecated methods
			Notification n = new Notification(R.drawable.status_phone_ico, null, 0);
			n.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			n.setLatestEventInfo(context, context.getString(R.string.notification_title), context.getString(R.string.notification_text), pendingIntent);
			notificationManager.notify(NOTIFICATION_ID, n);
		}
		else {
			cancel();
		}
	}
	
	public void cancel(){
		notificationManager.cancel(NOTIFICATION_ID);
	}

}

package com.maksym.android.authenticator.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AuthenticatorAutoAnswerBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		new AuthenticatorAutoAnswerNotifier(context).update();
	}

}

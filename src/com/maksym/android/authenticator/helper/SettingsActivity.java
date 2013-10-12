package com.maksym.android.authenticator.helper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String IS_ACTIVE_PREFERENCE = "is_active";
    public static final String PHONE_NUMBER_PREFERENCE = "phone_number";

    private EditTextPreference phoneNumberPreference;
    private AuthenticatorAutoAnswerNotifier notifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        phoneNumberPreference = (EditTextPreference) getPreferenceScreen().findPreference(PHONE_NUMBER_PREFERENCE);
        notifier = new AuthenticatorAutoAnswerNotifier(getBaseContext());
        notifier.update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update phone preference summary with actual value
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        phoneNumberPreference.setSummary(preferences.getString(PHONE_NUMBER_PREFERENCE, getResources().getString(R.string.phone_number_desc)));
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        //update phone preference summary with actual value
        if(key.equals(PHONE_NUMBER_PREFERENCE)){
            phoneNumberPreference.setSummary(preferences.getString(key, getResources().getString(R.string.phone_number_desc)));
        }
        //update notification
        notifier.update();
    }

    @Override
    public void onDestroy(){
        //I think android will do it automatically, but still just in case
        notifier.cancel();
    }
}

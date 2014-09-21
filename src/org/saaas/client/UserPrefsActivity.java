package org.saaas.client;

import android.os.Bundle;
import android.preference.PreferenceActivity;
 
public class UserPrefsActivity extends PreferenceActivity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.settings);
 
    }
}
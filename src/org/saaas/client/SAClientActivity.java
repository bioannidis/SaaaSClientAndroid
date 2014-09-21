package org.saaas.client;
/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Main UI for the demo app.
 */
public class SAClientActivity extends Activity {
	 private static final int RESULT_SETTINGS = 1;
	 String userName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
        userName=sharedPrefs.getString("prefUsername", "NULL");
        Log.d("Debug","username  "+userName);
        setContentView(R.layout.activity_main);
        showUserSettings();
        Button changePrefsBut=(Button)findViewById(R.id.start_but);
		changePrefsBut.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent i = new Intent(SAClientActivity.this, UserPrefsActivity.class);
				startActivityForResult(i, RESULT_SETTINGS);
				
			}
		});
        
      
        if (!(userName.equals("NULL"))){
        
        Intent i = new Intent(getApplicationContext(), SAClientService.class);
        startService(i);
        }
        //commend out finish();  

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
			break;

		}
		  if (!(userName.equals("NULL"))){
		        
		        Intent i = new Intent(getApplicationContext(), SAClientService.class);
		        startService(i);
		 }
    }
		private void showUserSettings() {
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			userName=sharedPrefs.getString("prefUsername", "NULL");
			StringBuilder builder = new StringBuilder();

			builder.append("\n Username: "
					+ sharedPrefs.getString("prefUsername", "NULL"));

			builder.append("\n Send report:"
					+ sharedPrefs.getBoolean("prefSendReport", false));

			builder.append("\n Sync Frequency: "
					+ sharedPrefs.getString("prefSyncFrequency", "NULL"));

			TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);

			settingsTextView.setText(builder.toString());
		


	}
	@Override
	public void onDestroy() {
        super.onDestroy();
    }

    
}

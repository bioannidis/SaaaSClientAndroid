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

import static org.saaas.client.CommonUtilities.SENDER_ID;
import static org.saaas.client.CommonUtilities.displayMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

//import org.saaas.client.threads.SAClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	File f;
	String path;
	String pathfile;
//	SAClient clientmcs;

	    @SuppressWarnings("hiding")
	    private static final String TAG = "GCMIntentService";

	    public GCMIntentService() {
	        super(SENDER_ID);
	        //bill
	       Log.i("Debug", "GCM intent Service");
	        //bill
	    }	


    @SuppressWarnings("deprecation")
	@Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, getString(R.string.gcm_registered));
        generateNotification(context, "you are connected");
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
	    String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        ServerUtilities.register(context, registrationId, ipAddress);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = null;
		String title = null;
		
		if (intent != null) {      	
			//Check the bundle for the pay load body and title
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				displayMessage(context, "Message bundle2: " +  bundle);
				Log.i(TAG, "Message bundle2: " +  bundle);
				message = bundle.getString("message");   			 	   		
				title = bundle.getString("title"); 	   		
			} 
		}
		// If there was no body just use a standard message
		if (message == null) {
			message = getString(R.string.gcm_message);
		}
	    try{
			if(title!=null)
				{
				Log.i("Debug","eimai sto file read");
					File file = new File("/sdcard");
					File nameCheck = new File(file, title);
					Log.i("Debug","eimai sto file read "+ nameCheck.exists());
					if(!nameCheck.exists()) {
						Log.i("Debug","perasa to namecheck");
						SAClientService.savedTitle = title;
						message = "Installation of a new APK : ";
						generateAPKNotification(context, message, title);
						//change url00000000000000000
						URL u = new URL("http://147.102.22.82:8080/saaas-server/download?fname=" + title);
						Log.i(TAG, "URL (including filename): " +  u.toString());
						HttpURLConnection c = (HttpURLConnection) u.openConnection();
						
						
						c.setRequestMethod("POST");
						c.setDoOutput(true);
						c.connect();
						/*
						 * den borw na brw to swsto url mallon
						 * arxika eixe :80
						 * alla enw sindeontan pernaga to connect
						 * skaei meta mallon dn briksei to arxeio.
						 * na psa3w ston server mipws exei download sinartisi SOS
						 */
						FileOutputStream f = new FileOutputStream(nameCheck);
						InputStream ins = c.getInputStream();
						byte[] buffer = new byte[1024];
						int len1 = 0;
						Log.i("Debug","sinde8ika");
						while((len1 = ins.read(buffer))>0){
							f.write(buffer, 0, len1);
						}
						ins.close();
						f.close();
						c.disconnect();
						Log.i("Debug","8a kanw install");
						InstallAPK(nameCheck);
					}
				}
	    }catch(Exception e)
	    {
	    	Log.i("Debug","den kanei swsto install");
	    	Log.e("Error GCM", e.getMessage() + "\n" + e.getLocalizedMessage());
	    }
        displayMessage(context, message);
        
    }

	
	protected void InstallAPK(File apk_file)
	{
		Log.i("Debug","3ekinw to app");
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (intent!=null){
		intent.setDataAndType(Uri.fromFile(apk_file), "application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Log.i("Debug","3ekinw to app");
		this.startActivity(intent);
		
		}
	}
	
	
	
	private boolean isAppInstalled(String packageName) {
	    PackageManager pm = getPackageManager();
	    boolean installed = false;
	    try {
	       pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
	       installed = true;
	    } catch (PackageManager.NameNotFoundException e) {
	       installed = false;
	    }
	    return installed;
	}

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, SAClientService.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateAPKNotification(Context context, String message, String title) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        Log.i("Debug","3ekinw to notification");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message + title, when);
        String title2 = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, SAClientService.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title2, message+title, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
	

}

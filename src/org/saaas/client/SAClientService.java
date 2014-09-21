package org.saaas.client;


import static org.saaas.client.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static org.saaas.client.CommonUtilities.EXTRA_MESSAGE;
import static org.saaas.client.CommonUtilities.SENDER_ID;
import static org.saaas.client.CommonUtilities.SERVER_URL;
import static org.saaas.client.CommonUtilities.TAG;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.saaas.client.R;
//import org.saaas.client.threads.SAClient;
import org.saaas.client.ServerUtilities;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

public class SAClientService extends Service{

	public static String savedTitle;
	public static String appContent="";
	public static ArrayList<String> listContent;
	public static String appContent2;
	public static int position;
	AsyncTask<Void, Void, Void> mRegisterTask;
	public String regIdka;
	public heartbeat hb = new heartbeat();
	public another_heartbeat hb1 = new another_heartbeat();
	private final static String TAG = "Lab-SaaaS";
	BatManager my_manager;
	private LocationManager mLocationManager = null;
	private static final int LOCATION_INTERVAL = 1000;
	private static final float LOCATION_DISTANCE = 10f;
	double longitude;
	double latitude;
    File f;
	String path;
	String pathfile;

	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class heartbeat extends	Handler{
		@Override
		public void handleMessage(Message msg){
			SAClientService.this.hbUpd();
			
		}
		public void sleep(long del){
		this.removeMessages(0);
		sendMessageDelayed(obtainMessage(0), del);
		};
		}
	class another_heartbeat extends Handler{
		@Override
		public void handleMessage(Message msg){
			SAClientService.this.usageUpd();
		}
		public void sleep(long del){
		this.removeMessages(0);
		sendMessageDelayed(obtainMessage(0), del);
		};
	}

	
	class mHeartbeatTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String...strings) {
			// TODO Auto-generated method stub
			ServerUtilities.sendKeepAlive(regIdka);
			return null;
		}
		
	}
	class learnUsageTask extends AsyncTask <Void,Void,Float>{

		@Override
		protected Float doInBackground(Void... params) {
			float CpuUsage=0;
			try {
				// -m 10, how many entries you want, -d 1, delay by how much, -n 1,
				// number of iterations
				Process p = Runtime.getRuntime().exec("top -m 1 -d 100 -n 1");
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				int i = 0;
				reader.readLine();
				reader.readLine();
				reader.readLine();
				String line=reader.readLine();
				StringTokenizer st= new StringTokenizer(line);
				st.nextToken();			
				String user_usage =st.nextToken();
				Float userd = (float) (Float.parseFloat(user_usage.trim().replace("%", "").replace("," ,""))/100.0);
				st.nextToken();
				String system_usage=st.nextToken();
				Float systemd = (float) (Float.parseFloat(system_usage.trim().replace("%", "").replace("," ,""))/100.0);
				CpuUsage= systemd+userd;
				ServerUtilities.change_cpu_usage(CpuUsage);
				Log.i("Debug"," usage "+CpuUsage);
				p.waitFor();

				//Toast.makeText(act.getBaseContext(), "Got update"+ line1,Toast.LENGTH_LONG).show();

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Debug", "exception");
			}
			return CpuUsage;
		}

	

}

	public void usageUpd(){
		hb1.sleep(100000);
		//Log.i("Debug",  "kaleite to task");
		new learnUsageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
		return;
	}
	public void hbUpd(){
		hb.sleep(5000);
		
        final String regId = GCMRegistrar.getRegistrationId(this);
		regIdka = regId;
		new mHeartbeatTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, regIdka);
		Log.i(TAG, "ONPOST");
		return;
	}
	private class LocationListener implements android.location.LocationListener{
	    Location mLastLocation;
	    public LocationListener(String provider)
	    {
	        Log.i(TAG, "LocationListener " + provider);
	        mLastLocation = new Location(provider);
	    }
	    @Override
	    public void onLocationChanged(Location location)
	    {
	        Log.i(TAG, "onLocationChanged: " + location);
	        mLastLocation.set(location);
	        longitude=location.getLongitude();
	        latitude=location.getLatitude();
	        ServerUtilities.change_lat_long(latitude, longitude);
	    }
	    @Override
	    public void onProviderDisabled(String provider)
	    {
	        Log.i(TAG, "onProviderDisabled: " + provider);            
	    }
	    @Override
	    public void onProviderEnabled(String provider)
	    {
	        Log.i(TAG, "onProviderEnabled: " + provider);
	    }
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras)
	    {
	        Log.i(TAG, "onStatusChanged: " + provider);
	    }
		
	} 
	LocationListener[] mLocationListeners = new LocationListener[] {
	        new LocationListener(LocationManager.GPS_PROVIDER),
	        new LocationListener(LocationManager.NETWORK_PROVIDER)
	};
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
	    final String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
	    //bill
	    if (my_manager==null){
	    my_manager=new BatManager();
	    getApplicationContext().registerReceiver( new BatManager() , new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    my_manager.getservice(SAClientService.this);
	    initializeLocationManager();
	    try {
	        mLocationManager.requestLocationUpdates(
	                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
	                mLocationListeners[1]);
	    } catch (java.lang.SecurityException ex) {
	        Log.i(TAG, "fail to request location update, ignore", ex);
	    } catch (IllegalArgumentException ex) {
	        Log.d(TAG, "network provider does not exist, " + ex.getMessage());
	    }
	    try {
	        mLocationManager.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
	                mLocationListeners[0]);
	    } catch (java.lang.SecurityException ex) {
	        Log.i(TAG, "fail to request location update, ignore", ex);
	    } catch (IllegalArgumentException ex) {
	        Log.d(TAG, "gps provider does not exist " + ex.getMessage());
	    }
	    //bill
	    Toast.makeText(getApplicationContext(), "Contributor App Launched", Toast.LENGTH_SHORT).show();
	    
        checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
        
        Log.i(TAG, "Entered the onCreate() method");
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
        final String regId = GCMRegistrar.getRegistrationId(this);

        regIdka = regId;
        
        
        Log.i(TAG, "ONPOST2");
        
        if (regId.equals("")) {
            // Automatically registers application on startup.
            GCMRegistrar.register(this, SENDER_ID);
            Log.i(TAG,"keno regId" );
            
        } else {
            // Device is already registered on GCM, check server.
         //   if (GCMRegistrar.isRegisteredOnServer(this)) {
		// Skips registration and start sending heartbeats asynchronously.
            	//callAsyncHeartbeat(ipAddress);
        //    } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered =
                                ServerUtilities.register(context, regId, ipAddress);
                        Log.i(TAG,"kanw register in back round" );
                        // At this point all attempts to register with the app
                        // server failed, so we need to unregister the device
                        // from GCM - the app will try to register again when
                        // it is restarted. Note that GCM will send an
                        // unregistered callback upon completion, but
                        // GCMIntentService.onUnregistered() will ignore it.
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute(null, null, null);
                Log.i(TAG, "FREE");
                // Start sending heartbeats asynchronously.
                //callAsyncHeartbeat(ipAddress);
               
            }
      ////  }
	    hbUpd();
        usageUpd();
	}
	   
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
//		if (mHeartbeatTask != null) {
//			mHeartbeatTask.cancel(true);
//        }
		if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
		if (mLocationManager != null) {
	        for (int i = 0; i < mLocationListeners.length; i++) {
	            try {
	                mLocationManager.removeUpdates(mLocationListeners[i]);
	            } catch (Exception ex) {
	                Log.i(TAG, "fail to remove location listners, ignore", ex);
	            }
	        }
	    }
        unregisterReceiver(mHandleMessageReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
	}



	private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }

    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
        	Log.i(TAG,"minima : "+newMessage );
        }
    };

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
	
}

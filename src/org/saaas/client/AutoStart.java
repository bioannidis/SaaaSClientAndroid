package org.saaas.client;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver 
{
	
	 

    public void onReceive(Context arg0, Intent arg1) 
    {
    	Log.i("Autostart", "started");
    	
        Intent intent = new Intent(arg0, SAClientActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        arg0.startActivity(intent);
        
    }
}

package org.saaas.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

 public  class  BatManager  extends BroadcastReceiver{
	Context context;
	float batteryPct;
	boolean isCharging,usbCharge,acCharge;
	File apk_file;
	PackageManager pm;
	ActivityManager manager;
	SAClientService act;
	String appName;
	SensorManager sm;
	float CpuUsage;
	//gia apo8ikeusi pi8anwn apk tr douleuw me ena mono
	public void getservice (SAClientService ser){
		this.act=ser;
		Log.i("Debug","act not null");
		this.pm=act.getPackageManager();
		this.manager=(ActivityManager)act.getSystemService(Context.ACTIVITY_SERVICE);
		this.sm=(SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
		//extracts appName
		
	}
	
	HashSet<String> myset = new HashSet<String>();
	
	/*
	 * to sinexes update borei en telei na einai xeirotero
	 * psa3e gia battery.Low receiving event.
	 */
	/*kaleitai ka8e fora pou ginei allagei sto battery level
	sto charging status kai dinei diafores plirofories an fortizei apo 
	usb i ac to battery percentage kai ta dinei ston xristh 
	*/
	@Override
	public void onReceive(Context arg0, Intent intent) {
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		Log.i("Debug","is charging :"+isCharging);
		batteryPct = level*100/ (float)scale;
		ServerUtilities.change_batpct(batteryPct);
		int bLevel = intent.getIntExtra("level", 0); // gets the battery level
	    Log.i("Debug", "battery pct"+batteryPct);
		  // Here you do something useful with the battery level...
	    
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		Log.i("Debug","USB "+ usbCharge +"ac "+ acCharge);
		//edw elegxw an exei egatasta8ei kapoio apk
		//CpuUsage=readUsage();
		//Log.i("Debug","Usage : "+ CpuUsage);
		//if (act!=null){
		//Log.i("Debug","act not null");
		//CpuUsagePerApp();
		//monitor_sensors();
		//monitor_installations();
		//monitorbatteryPct(batteryPct);
		//informCritical(batteryPct);
		//}cc
	}
	/*
	 * needed for getting instaces
	 */
	protected void get_installed_file(File apk_file ){
		this.apk_file=apk_file;
		String apkPath = apk_file.getAbsolutePath();
		PackageInfo pi=pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES);
		if (pi==null){
			Log.e("Debug","null info");
		}
		else{
			ApplicationInfo ai=pi.applicationInfo;
        this.appName = pm.getApplicationLabel(ai).toString();
        //for future use scalability
        //for many apps
        if (!myset.contains(appName)){
        	myset.add(appName);
        }
		}
	}
	/*
	 *registerSensorListener
	 */
	public void registerSensorListener() {
		
	}
	/*
	 * find cpu usage per app android
	 */
	/*
	  String getInfo() {
	    StringBuffer sb = new StringBuffer();
	    sb.append("abi: ").append(Build.CPU_ABI).append("\n");
	    if (new File("/proc/cpuinfo").exists()) {
	        try {
	            BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
	            String aLine;
	            while ((aLine = br.readLine()) != null) {
	                sb.append(aLine + "\n");
	            }
	            if (br != null) {
	                br.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } 
	    }
	    return sb.toString();
	}
	*/
	protected void CpuUsagePerApp(){
		// for specific app : Sadb shell top -m 10 | FINDSTR packagename
		ArrayList<String> list = new ArrayList<String>();
		try {
			// -m 10, how many entries you want, -d 1, delay by how much, -n 1,
			// number of iterations
			Process p = Runtime.getRuntime().exec("top -m 1 -d 50 -n 1");
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
			selectTopCpuConsumingApps(list);
			p.waitFor();

			//Toast.makeText(act.getBaseContext(), "Got update"+ line1,Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Debug", "exception");
		}
	}
	
	
	private float readUsage() {
	    try {
	        RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        String load = reader.readLine();

	        String[] toks = load.split(" ");

	        long idle1 = Long.parseLong(toks[4]);
	        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        try {
	            Thread.sleep(3600);
	        } catch (Exception e) {}

	        reader.seek(0);
	        load = reader.readLine();
	        reader.close();

	        toks = load.split(" ");

	        long idle2 = Long.parseLong(toks[4]);
	        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
	            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }

	    return 0;
	}
	
	private void selectTopCpuConsumingApps(ArrayList<String> list) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * send broadcast and will inform msaas app
	 * app must implement broadcastreceiver
	 */
	
	protected void informCritical(Float pct){
		if (pct<15){
		Intent i = new Intent("org.saas.client.android.USER_ACTION");
		act.sendBroadcast(i);
		Log.d("Debug","stal8ike to bdcast");
		}
	}
	/*
	 *kills all installed apps dont need extra code from 
	 *msaas app except the onStartCommand Return START_NOT_STICKY;
	 */
	protected void monitorbatteryPct(Float pct){
		if (pct<15){
			Log.i("Debug", "Critical battery percentage must take action");
			//running proccess
			List<ActivityManager.RunningAppProcessInfo> listOfProcesses = manager.getRunningAppProcesses();
			//if one matches with one we have installed we kill it
			for (ActivityManager.RunningAppProcessInfo process : listOfProcesses)
	        {
				for(String nameOfProcess:myset){
					if (process.processName.contains(nameOfProcess)){
	                Log.i("Debug" , process.processName + " : " + process.pid);
	                manager.killBackgroundProcesses(process.processName);
	                android.os.Process.killProcess(process.pid);
	                android.os.Process.sendSignal(process.pid, android.os.Process.SIGNAL_KILL);
	                               
	                Log.i("Debug" , "egine to kill");
	                break;
	                }
	            }
			}
		}
	}
	
	
	//sensor list an the power consumption in mA
	protected void monitor_sensors(){
		List<Sensor> deviceSensors = sm.getSensorList(Sensor.TYPE_ALL);
		for(int i=0;i<deviceSensors.size();i++){
		Log.i("Debug","Sensor name :"+deviceSensors.get(i).getName()+
				" Sensor mA :"+deviceSensors.get(i).getPower());
		//edw einai kati allo pou epsaxna ignore it
		/*
		 * fifo indicator 8elei api 19
		 */
		//Log.i("Debug","Fifo for batching "+deviceSensors.get(i).getFifoReservedEventCount() );
		//https://source.android.com/devices/sensors/index.html
		}
	}
	
	//ignore it
	
	protected void monitor_installations(){
		String apkPath = apk_file.getAbsolutePath();
		PackageInfo pi=pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES);
		if (pi==null){
			Log.e("Debug","null info");
		}
		else{
			ApplicationInfo ai=pi.applicationInfo;
            String appVersion = pi.versionName;
            String pkgName = ai.packageName;
            for (android.content.pm.ActivityInfo a : pi.activities) {
                Log.i("Debug", "To be installed app" +a.name);
            }
            for (android.content.pm.ServiceInfo s : pi.services) {
                Log.i("Debug","To be installed service" +s.name);
            }
            /*
             * me ton akolou8w tropo briskw ta running processes by name
             */
            List<RunningAppProcessInfo> services = manager.getRunningAppProcesses(); 
            StringBuilder b = new StringBuilder();
            for (ActivityManager.RunningAppProcessInfo process: services) {
                b.append(process.processName);
                b.append(':');
                b.append(process.uid);
                b.append("\n");
            }
            Log.i("Debug",b.toString());
            Log.i("Debug", "Class name"+getClass().getName()+"pkgName = " + pkgName + ", appName = " + appName + ", appVersion = " + appVersion );
		}
		
	}
	
 }
	

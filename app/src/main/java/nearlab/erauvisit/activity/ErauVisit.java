package nearlab.erauvisit.activity;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import nearlab.erauvisit.Data.BeaconStructure;
import nearlab.erauvisit.HandleBeaconData.DownloadTask;
import nearlab.erauvisit.R;

public class ErauVisit extends Application implements BootstrapNotifier, RangeNotifier {
    private static final boolean USE_LOCAL_JSON = true;
    private static final boolean USE_LOCAL_SERVER = false;
    //192.168.2.
    private static final String LOCAL_SERVER_URL = "http://10.33.93.176/erauvisit.com/";
    private static final String ERAU_SERVER_URL_JSON = "http://earl.erau.edu/lab/ibeacon/";

    private final static String NO_LOCAL_SERVER_WELCOME_PAGE_URL ="http://cdn.stateuniversity.com/assets/logos/images/11938/large_db-map.jpg";

    public static String DEFAUT_PAGE_URL;

    private static final String TAG = "ErauVisit";
    private static boolean goOn=false;
    private static String beacons_json_file_url;
	private BeaconManager mBeaconManager;
    List<Region> listRegions= new ArrayList<Region>();

	private MonitoringActivity mMonitoringActivity;
	private RangingActivity mRangingActivity;
	private BackgroundPowerSaver mBackgroundPowerSaver;
    private static HashMap <String,BeaconStructure> mapKeyBeaconStruct = new HashMap<String, BeaconStructure>();
	@SuppressWarnings("unused")
	private static RegionBootstrap mRegionBootstrap;
    private Region mRegion;

    //"Local variables" made global
    List<String> mkeys= new ArrayList<String>();

	@Override 
	public void onCreate() {
        if(USE_LOCAL_SERVER){
            beacons_json_file_url=LOCAL_SERVER_URL+"beacons.json";
            DEFAUT_PAGE_URL =LOCAL_SERVER_URL+"welcome_page.html";
        }
        else {
            beacons_json_file_url = ERAU_SERVER_URL_JSON+"beacons.json";
            DEFAUT_PAGE_URL =NO_LOCAL_SERVER_WELCOME_PAGE_URL;
        }
        beacons_json_file_url=beacons_json_file_url+"beacons.json";

        String mJson;

        //Get the json file with the beacon structures
        if (!USE_LOCAL_JSON){
            mJson = downloadJSONFromServer();
        }
        else {
            mJson = loadJSONFromRaw();
        }
         mRegion = new Region("backgroundRegion",
                null, null, null);
        createBeaconManager();
        mBackgroundPowerSaver = new BackgroundPowerSaver(this);//Without this line, didRangeBeacon not working
        mRegionBootstrap = new RegionBootstrap(this, mRegion);


        //Create the map mapKeyBeaconStruct from the json file
        createMapKeyBeaconStruct(mJson);

        //Create listRegions with the map and affect it to
        createListRegions();

        mRegionBootstrap = new RegionBootstrap(this, listRegions);

        //Create mBeaconManager with increased scan frequency
        createBeaconManager();

	}

    private String downloadJSONFromServer() {
        String mJson=null;
        try {
            mJson = new DownloadTask(getApplicationContext()).execute(beacons_json_file_url).get();
            Log.i("just got json string", mJson);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return mJson;
    }

    @Override
	public void didRangeBeaconsInRegion(Collection<Beacon> arg0, Region arg1) {
       /*http://altbeacon.github.io/android-beacon-library/javadoc/index.html?org/altbeacon/beacon/RangeNotifier.html
         Called once per second to give an estimate of the mDistance to visible beacons
        Parameters:
        beacons - a collection of Beacon objects that have been seen in the past second
        region - the Region object that defines the criteria for the ranged beacons*/
		if (mMonitoringActivity != null) {
            mMonitoringActivity.didRangeBeaconsInRegion(arg0, arg1);
		}
        if (mRangingActivity != null) {
            mRangingActivity.didRangeBeaconsInRegion(arg0, arg1);
        }
		
	}

	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didEnterRegion(Region arg0) {
		if (mMonitoringActivity != null) {
			mMonitoringActivity.didEnterRegion(arg0);
		}
		try {
            String line =" entered region";
			Log.d(TAG, "entered region :" +arg0.getUniqueId()+"  starting ranging");
			mBeaconManager.startRangingBeaconsInRegion(mRegion);
			mBeaconManager.setRangeNotifier(this);

		} catch (RemoteException e) {
			Log.e(TAG, "Cannot start ranging");
		}
//        if(!MonitoringActivity.isActive) {
//            Intent intent = new Intent(this, MonitoringActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            // Important:  make sure to add android:launchMode="singleInstance" in the manifest to keep multiple copies of this activity from getting created if the user has  already manually launched the app.
//            this.startActivity(intent);
//        }
	}

	@Override
	public void didExitRegion(Region arg0) {
		if (mMonitoringActivity != null) {
			mMonitoringActivity.didExitRegion(arg0);
		}				
	}


    //Util methods

    private void createBeaconManager() {
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
//        mBeaconManager.setForegroundScanPeriod(BeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD/5);
//        mBeaconManager.setBackgroundScanPeriod(BeaconManager.DEFAULT_BACKGROUND_SCAN_PERIOD/3);
    }

    private void createListRegions() {
        String key =mkeys.get(1);
        BeaconStructure beaconStructure=mapKeyBeaconStruct.get(key);
        Region region = new Region("campus", null,
                null,
                null);
        listRegions.add(region);
//        String key;
//        BeaconStructure beaconStructure;
//        for(int i=0; i<mkeys.size();i++ ) {
//            key =mkeys.get(i);
//            BeaconStructure beaconStructure = mapKeyBeaconStruct.get(key);
//            Region region = new Region(key, Identifier.parse(beaconStructure.getUUID()),
//                    Identifier.parse(String.valueOf(beaconStructure.getMajor())),
//                    Identifier.parse(String.valueOf(beaconStructure.getMinor())));
//            listRegions.add(region);
//        }
    }

    private void createMapKeyBeaconStruct(String mJson) {
        try {
            JSONObject jObject = new JSONObject(mJson);
            Iterator<String> keys=  jObject.keys();
            String key;
            while(keys.hasNext()){
                key = (String) keys.next();
                Log.i("JSON key", key);
                if (jObject.get(key) instanceof JSONObject) {
                    JSONObject jObj= (JSONObject) jObject.get(key);
                    BeaconStructure beaconStructure = new BeaconStructure();
                    beaconStructure.setUUID((String) jObj.get("uuid"));
                    beaconStructure.setMajor((Integer) jObj.get("major"));
                    beaconStructure.setMinor((Integer) jObj.get("minor"));
                    beaconStructure.setRange((Double) jObj.get("required_distance"));
                    beaconStructure.setURL((String) jObj.get("URL"));
                    beaconStructure.setContentText1((String) jObj.get("contentText1"));

                    mapKeyBeaconStruct.put(key, beaconStructure);
                    mkeys.add(key);
                }
            }
        } catch (JSONException e) {
            if (e != null) {
                Log.e("e", "e is not null, toString is " + e + " and message is " + e.getMessage());
            }
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            assetManager.getLocales();
            InputStream is = assetManager.open("beacons.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String loadJSONFromRaw() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.beacons);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
	
	public void setMonitoringActivity(MonitoringActivity activity) {
		mMonitoringActivity = activity;
	}

	public void setRangingActivity(RangingActivity activity) {
		mRangingActivity = activity;
	}

    public ArrayList<String> makeLineIntoStrings(File file) {
        Scanner sc;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return lines;
    }

    public void displayStringList(List<String> listStrings ){
        for(String string :listStrings){
            Log.i("JSON File line  ", string+"/n");
        }
    }

    public static HashMap<String, BeaconStructure> getMapKeyBeaconStruct() {
        return mapKeyBeaconStruct;
    }

    public static void setMapKeyBeaconStruct(HashMap<String, BeaconStructure> mapKeyBeaconStruct) {
        ErauVisit.mapKeyBeaconStruct = mapKeyBeaconStruct;
    }

    @Deprecated
    public static BeaconStructure getBSFromRegion(Region region){
        String key = region.getUniqueId();
        return mapKeyBeaconStruct.get(key);
    }

    public static BeaconStructure getBSFromBeacon(Beacon beacon){
        String key = "beacon_"+ String.valueOf(beacon.getId3());
        return mapKeyBeaconStruct.get(key);
    }


    public static boolean isGoOn() {
        return goOn;
    }

    public static void setGoOn(boolean goOn) {
        ErauVisit.goOn = goOn;
    }

    public static RegionBootstrap getmRegionBootstrap() {
        return mRegionBootstrap;
    }
}

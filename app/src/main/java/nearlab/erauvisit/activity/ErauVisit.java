package nearlab.erauvisit.activity;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
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
	private static final String TAG = "ErauVisit";
    private static boolean goOn=false;
    private static final String beacons_json_file_url="http://earl.erau.edu/lab/ibeacon/beacons.json";
	private BeaconManager mBeaconManager;
    List<Region> listRegions= new ArrayList<Region>();

	private MonitoringActivity mMonitoringActivity;
	private RangingActivity mRangingActivity;
	private BackgroundPowerSaver mBackgroundPowerSaver;
    private static HashMap <String,BeaconStructure> mapKeyBeaconStruct = new HashMap<String, BeaconStructure>();
	@SuppressWarnings("unused")
	private RegionBootstrap mRegionBootstrap;

    //"Local variables" made global
    List<String> mkeys= new ArrayList<String>();
    private static final boolean do_download_json = false;

	@Override 
	public void onCreate() {
        String mJson;

        //Get the json file with the beacon structures
        if (do_download_json){
            mJson = downloadJSONFromServer();
        }
        else {
            mJson = loadJSONFromRaw();
        }

        //Create the map mapKeyBeaconStruct from the json file
        createMapKeyBeaconStruct(mJson);

        //Create listRegions with the map and affect it to
        createListRegions();

        mRegionBootstrap = new RegionBootstrap(this, listRegions);

        //Create mBeaconManager with increased scan frequency
        createBeaconManager();

        mBackgroundPowerSaver = new BackgroundPowerSaver(this);

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
			Log.d(TAG, "entered region.  starting ranging");
			mBeaconManager.startRangingBeaconsInRegion(arg0);
			mBeaconManager.setRangeNotifier(this);

		} catch (RemoteException e) {
			Log.e(TAG, "Cannot start ranging");
		}
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
        mBeaconManager.setForegroundScanPeriod(BeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD/5);
        mBeaconManager.setBackgroundScanPeriod(BeaconManager.DEFAULT_BACKGROUND_SCAN_PERIOD/5);
    }

    private void createListRegions() {
        for(int i=0; i<mkeys.size();i++ ) {
            String key =mkeys.get(i);
            BeaconStructure beaconStructure = mapKeyBeaconStruct.get(key);
            Region region = new Region(key, Identifier.parse(beaconStructure.getUUID()),
                    Identifier.parse(String.valueOf(beaconStructure.getMajor())),
                    Identifier.parse(String.valueOf(beaconStructure.getMinor())));
            listRegions.add(region);
        }
    }

    private void createMapKeyBeaconStruct(String mJson) {
        try {
            JSONObject jObject = new JSONObject(mJson);
            Iterator<String> keys=  jObject.keys();
            while(keys.hasNext()){
                String key = (String) keys.next();
                Log.i("JSON key", key);
                if (jObject.get(key) instanceof JSONObject) {
                    JSONObject jObj= (JSONObject) jObject.get(key);
                    BeaconStructure beaconStructure = new BeaconStructure();
                    beaconStructure.setUUID((String) jObj.get("uuid"));
                    beaconStructure.setMajor((Integer) jObj.get("major"));
                    beaconStructure.setMinor((Integer) jObj.get("minor"));
                    beaconStructure.setURL((String) jObj.get("URL"));
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

    public static boolean isGoOn() {
        return goOn;
    }

    public static void setGoOn(boolean goOn) {
        ErauVisit.goOn = goOn;
    }
}

package nearlab.erauvisit;

import android.app.Application;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ErauVisit extends Application implements BootstrapNotifier, RangeNotifier {
	private static final String TAG = "ErauVisit";
	private BeaconManager mBeaconManager;
	private Region mRegion1;
    private Region mRegion2;
    private Region mAllBeaconsRegion;
	private MonitoringActivity mMonitoringActivity;
	private RangingActivity mRangingActivity;
	private BackgroundPowerSaver mBackgroundPowerSaver;
	@SuppressWarnings("unused")
	private RegionBootstrap mRegionBootstrap;
	
	@Override 
	public void onCreate() {
//        Region region2 = new Region("com..backgroundRegion",
//                Identifier.parse("e20a39f4-73f5-4bc4-a12f-17d1ad07a961"),  Identifier.parse("1"),  Identifier.parse("2"));
//        mRegion =region2;
		//mAllBeaconsRegion = new Region("ErauVisit UUID", Identifier.parse("e20a39f4-73f5-4bc4-a12f-17d1ad07a961"), null, null);
        mRegion1= new Region("Mohamed piBeacon", Identifier.parse("e20a39f4-73f5-4bc4-a12f-17d1ad07a961"),  Identifier.parse("1"),  Identifier.parse("1"));
        mRegion2= new Region("Christophe piBeacon", Identifier.parse("e20a39f4-73f5-4bc4-a12f-17d1ad07a961"),  Identifier.parse("1"),  Identifier.parse("2"));
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        mBeaconManager.setForegroundScanPeriod(BeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD/5);
        mBeaconManager.setBackgroundScanPeriod(BeaconManager.DEFAULT_BACKGROUND_SCAN_PERIOD/5);
		mBackgroundPowerSaver = new BackgroundPowerSaver(this);
        List<Region> listBeacon= new ArrayList<Region>();
       // listBeacon.add(mAllBeaconsRegion);
        listBeacon.add(mRegion1);
        listBeacon.add(mRegion2);

        mRegionBootstrap = new RegionBootstrap(this, listBeacon);

	
        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb
        //
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //
        // In order to find out the proper BeaconLayout definition for other kinds of beacons, do
        // a Google search for "setBeaconLayout" (including the quotes in your search.)
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
			mBeaconManager.startRangingBeaconsInRegion(mRegion1);
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
	
	public void setMonitoringActivity(MonitoringActivity activity) {
		mMonitoringActivity = activity;
	}

	public void setRangingActivity(RangingActivity activity) {
		mRangingActivity = activity;
	}
	
}

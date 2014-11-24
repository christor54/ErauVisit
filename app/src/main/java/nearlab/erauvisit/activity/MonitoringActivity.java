package nearlab.erauvisit.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;

import nearlab.erauvisit.Data.BeaconStructure;
import nearlab.erauvisit.R;

public class MonitoringActivity extends Activity {
	protected static final String TAG = "MonitoringActivity";

    private final static String WELCOME_MESSAGE = "Hi, I will be your guide for the visit :) " +"\n"+"Move around, I will show you cool stuff !";
    private final static String NO_BEACON_MESSAGE = "keep visiting, I will guide you";

    private Beacon lastBeacon;
    static boolean isActive =false;

    private BeaconManager beaconManager;
    private WebView webView;
    private TextView monitoring_text_textview;
    private Button backButton;

    private String last_monitor_text;
    private String lastURL;
    private static Beacon closestBeacon=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
        verifyBluetooth();
		setContentView(R.layout.activity_monitoring);
        webView= (WebView) this.findViewById(R.id.webView);
        backButton = (Button)this.findViewById(R.id.go_back_map);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayDefaultView();
            }
        });
        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        else {
            logToDisplay(WELCOME_MESSAGE);
            openWebPageInWebView(ErauVisit.DEFAUT_PAGE_URL);
        }
        isActive =true;
	}

	public void onRangingClicked(View view) {
        Intent myIntent = new Intent(this, RangingActivity.class);
        this.startActivity(myIntent);
	}


    @Override 
    protected void onPause() {
    	super.onPause();
    	// Tell the Application not to pass off monitoring updates to this activity
    	((ErauVisit)this.getApplication()).setMonitoringActivity(null);
    }
    @Override 
    protected void onResume() {
    	super.onResume();
    	// Tell the Application to pass off monitoring updates to this activity
    	((ErauVisit)this.getApplication()).setMonitoringActivity(this);
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive =true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    public void didEnterRegion(Region region) {
        System.out.println("Enter region : "+region.getUniqueId());
    }

    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        BeaconStructure bs;
        Beacon beaconAnalyzed;
        String currentURL,monitor_text;

       //Find the closest beacon
        if (beacons.size() > 0) {
           // beaconAnalyzed=findBeaconAnalyzed(beacons,region);
            Iterator it=beacons.iterator();
            beaconAnalyzed=(Beacon)it.next();
            bs = ErauVisit.getBSFromBeacon(beaconAnalyzed);
            //if the beacon is the closest and is in the required range
            if(beaconAnalyzed!=null&&isTheClosestBeacon(beaconAnalyzed)&&closestBeacon.getDistance() <= bs.getRange()){
                monitor_text = bs.getContentText1();
                currentURL = bs.getURL();
                //If the web page to displayed was not already displayed
                if(!currentURL.equals(lastURL)) {
                    logToDisplay(monitor_text);
                    openWebPageInWebView(currentURL);
                }
            }
        }
    }

    public static boolean isTheClosestBeacon(Beacon beaconAnalyzed) {
        boolean response;
        if(closestBeacon!=null) {
            if (beaconAnalyzed.getDistance() <= closestBeacon.getDistance()) {
                closestBeacon = beaconAnalyzed;
            }
            if(areBeaconsEquals(beaconAnalyzed,closestBeacon)){
                response=true;
            } else {
                response = false;
            }
        }
        else {
            closestBeacon=beaconAnalyzed;
            response=true;
        }
        return response;
    }

    private static boolean areBeaconsEquals(Beacon beaconAnalyzed, Beacon closestBeacon) {
        boolean areEquals=true;
        for(int i=1;i<3;i++){
            if(!beaconAnalyzed.getIdentifier(i).equals(closestBeacon.getIdentifier(i))){
                areEquals=false;
            }
        }
        return areEquals;
    }


    private boolean isTheClosestBeaconWithinTheList(Collection<Beacon> beacons, Beacon beaconAnalyzed) {
        Beacon closestBeacon=beaconAnalyzed;
        Double minDistance=100.0;
        for(Beacon beacon:beacons){
            if(beacon.getDistance()<minDistance){
                minDistance=beacon.getDistance();
            }
        }
        if(beaconAnalyzed.getDistance()== minDistance)
            return true;
        else
            return false;
    }

    public static Beacon findBeaconAnalyzed(Collection<Beacon> beacons, Region region) {
        Beacon beaconMatchingRegion=null;
        for(Beacon beacon:beacons){
            if(region.matchesBeacon(beacon)){
                beaconMatchingRegion=beacon;
            }
        }
        return beaconMatchingRegion;
    }


    public void didExitRegion(Region region) {
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Read values from the "savedInstanceState"-object and put them in your textview
        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }


    public void didDetermineStateForRegion(int state, Region region) {
    	logToDisplay("I have just switched from seeing/not seeing beacons: "+state);
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                monitoring_text_textview = (TextView)MonitoringActivity.this
                        .findViewById(R.id.monitoringText);
                monitoring_text_textview.setText(line);
            }
        });
        last_monitor_text=line;
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void openWebPageInWebView(final String url1) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url1);
                if (url1 != ErauVisit.DEFAUT_PAGE_URL)
                    backButton.setVisibility(View.VISIBLE);
                else
                    backButton.setVisibility(View.GONE);
            }
        });
        lastURL=url1;

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });



    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void displayDefaultView() {
        String currentURL,monitor_text;
        currentURL = ErauVisit.DEFAUT_PAGE_URL;
        monitor_text = NO_BEACON_MESSAGE;
        logToDisplay( monitor_text);
        openWebPageInWebView(ErauVisit.DEFAUT_PAGE_URL);
        lastURL = currentURL;
        last_monitor_text= monitor_text;
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();
        }
    }
}

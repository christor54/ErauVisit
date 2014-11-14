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
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import nearlab.erauvisit.Data.BeaconStructure;
import nearlab.erauvisit.R;

public class MonitoringActivity extends Activity {
	protected static final String TAG = "MonitoringActivity";

    private final static String NO_BEACON_MESSAGE = "keep visiting, I will guide you";
    private final static String DEFAULT_URL ="http://cdn.stateuniversity.com/assets/logos/images/11938/large_db-map.jpg";

    private Beacon lastBeacon;
    static boolean isActive =false;
    private BeaconManager beaconManager;
    private WebView webView;
    private TextView monitoring_text_textview;
    private boolean isInRegion= false;
    private String monitor_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
        webView= (WebView) this.findViewById(R.id.webView);
        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        verifyBluetooth();
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
      //  BeaconStructure bs= ErauVisit.getBSFromRegion(region);
       // monitor_text=bs.getContentText1();
        //logToDisplay(monitor_text);
        //openWebPageInWebView(bs.getURL());
    }

    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
            for (Beacon beacon: beacons) {

                if(region.matchesBeacon(beacon)&&beacon.getDistance()<=ErauVisit.getBSFromRegion(region).getRange()) {
//                    boolean goOn=false;
                    BeaconStructure bs = ErauVisit.getBSFromRegion(region);
                    monitor_text = bs.getContentText1();
                    logToDisplay(monitor_text);
                    openWebPageInWebView(bs.getURL());

                }
                else{
                    monitor_text=NO_BEACON_MESSAGE;
                    logToDisplay(monitor_text);
                    openWebPageInWebView(DEFAULT_URL);
                }
            }
        }
    }


    public void didExitRegion(Region region) {
        monitor_text= "Keep visiting I will guide you";
        logToDisplay(monitor_text);
        isInRegion=false;
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
                monitoring_text_textview.setText(monitor_text);
            }
        });
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
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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

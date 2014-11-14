package nearlab.erauvisit.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import nearlab.erauvisit.R;

public class RangingActivity extends Activity {
    protected static final String TAG = "RangingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranging);
    }
    @Override 
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override 
    protected void onPause() {
    	super.onPause();
    	// Tell the Application not to pass off ranging updates to this activity
    	((ErauVisit)this.getApplication()).setRangingActivity(null);
    }
    @Override 
    protected void onResume() {
    	super.onResume();
    	// Tell the Application to pass off ranging updates to this activity
    	((ErauVisit)this.getApplication()).setRangingActivity(this);
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }    

    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
        	TextView editText = (TextView)RangingActivity.this
					.findViewById(R.id.rangingText);
            for (Beacon beacon: beacons) {
            	logToDisplay("Beacon "+beacon.toString()+" is about "+beacon.getDistance()+" meters away, with Rssi: "+beacon.getRssi() );
            }
        }
    }

    private void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	TextView editText = (TextView)RangingActivity.this
    					.findViewById(R.id.rangingText);
    	    	editText.setText(line+"\n");
    	    }
    	});
    }
}

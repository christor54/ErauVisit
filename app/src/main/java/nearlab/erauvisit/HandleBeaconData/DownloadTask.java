package nearlab.erauvisit.HandleBeaconData;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DownloadTask extends AsyncTask <String, Integer, String>{
    private NotificationHelper mNotificationHelper;
    private File beacons_json_file ;
    public DownloadTask(Context context){
        mNotificationHelper = new NotificationHelper(context);
    }

//    protected void onPreExecute(){
//        //Create the notification in the statusbar
//        mNotificationHelper.createNotification();
//    }

    @Override
    protected String doInBackground(String... Urls) {
        //This is where we would do the actual download stuff
        //for now I'm just going to loop for 10 seconds
        // publishing progress every second
        String json=null;
        beacons_json_file  = new File("assets/beacons.json");
        if (!beacons_json_file.exists()) {
            try {
                beacons_json_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String path = beacons_json_file.getAbsolutePath();
            Log.i("json file path ",path);
            // "/beacons.json
        }
        try {
            URL url = new URL(Urls[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100%
            // progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            json=loadJSONFromServer(input);
//            OutputStream output = new FileOutputStream(beacons_json_file);
//            byte data[] = new byte[1024];
//            long total = 0;
//            int count;
//            while ((count = input.read(data)) != -1) {
//                total += count ;
//                // publishing the progress....
//                publishProgress((int) (total * 100 / fileLength));
//                output.write(data, 0, count);
//            }
//            output.flush();
//            output.close();
//            input.close();
//            if(beacons_json_file!=null){
//                Log.i("JSON File path  ", beacons_json_file.getAbsolutePath() + "/n");
//                ArrayList<String> listStrings  = makeLineIntoStrings(beacons_json_file);
//                displayStringList(listStrings);
//            }
        }
        catch (Exception e){
            e.printStackTrace();
            if(e!=null) {
                Log.e("Download Failed", e.getMessage());
                Log.e("Download Failed", e.toString());
            }
        }
        return json;
    }
    protected void onProgressUpdate(Integer... progress) {
        //This method runs on the UI thread, it receives progress updates
        //from the background thread and publishes them to the status bar
        //mNotificationHelper.progressUpdate(progress[0]);
    }
    protected void onPostExecute(Void result)    {
        //The task is complete, tell the status bar about it
       // mNotificationHelper.completed();
      //  ErauVisit.setGoOn(true);
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



    public String loadJSONFromServer( InputStream is ) throws IOException {
        String json = null;
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        json = new String(buffer, "UTF-8");
        return json;
    }

    public void displayStringList(List<String> listStrings ){
        for(String string :listStrings){
            Log.i("JSON File line  ", string+"/n");
        }
    }
}
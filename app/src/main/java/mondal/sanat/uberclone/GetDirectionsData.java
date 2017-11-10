package mondal.sanat.uberclone;


import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

/**
 * Created by 20170546 on 11/9/2017.
 */

public class GetDirectionsData extends AsyncTask<Object, String, String> {

    String googleDirectionData;
    GoogleMap mMap;
    String url;
    String duration, distance;

    LatLng latlng;

    @Override
    protected String doInBackground(Object... params) {
        Log.d("GetDirectionsData", "doInBackground Start");
        mMap = (GoogleMap) params[0];
        url = (String) params[1];

        latlng = (LatLng) params[2];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googleDirectionData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionData;
    }

    @Override
    protected void onPostExecute(String result) {

        DataParser parser = new DataParser();
        String[] directionsList;
        directionsList = parser.parseDirections(result);
        displayDirection(directionsList);

//        Log.d("GetDirectionsData: ", " onPostExecute Start");
//        HashMap<String, String> directionsList = null;
//        DataParser parser = new DataParser();
//        directionsList = parser.parseDirections(result);
//        duration = directionsList.get("duration");
//        distance = directionsList.get("distance");
//
//        mMap.clear();
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latlng);
//        markerOptions.draggable(true);
//        markerOptions.title("Duration: " + duration);
//        markerOptions.snippet("Distance: " + distance);
//
//        mMap.addMarker(markerOptions);
    }


    public void displayDirection(String[] directionList)
    {
        int count = directionList.length;
        for (int i = 0; i < count; i++)
        {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.RED);
            options.width(10);
            options.addAll(PolyUtil.decode(directionList[i]));

            mMap.addPolyline(options);
        }
    }
}

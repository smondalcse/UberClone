package mondal.sanat.uberclone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener{

    private static final String TAG = "MapsActivity";
    private static int PROXIMITY_RADIUS = 1000;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    double latitude, longitude;

    double destlatitude = 23.8223;
    double destlongitude = 90.3654;

    EditText tf_source, tf_destination;

    public static final int REQUEST_LOCATION_CODE = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkRequestPermission();
        }

        tf_source = (EditText) findViewById(R.id.TF_Sourcelocation);
        tf_destination = (EditText) findViewById(R.id.TF_Destlocation);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    public boolean checkRequestPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client != null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this, "Permission Denied!!!", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: start");
        lastLocation = location;

        if(currentLocationMarker != null)
        {
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        String CurrentAddress =  ConvertLatLngToAddress(latLng);
        tf_source.setText(CurrentAddress);

        Log.d(TAG, "latitude : " + location.getLatitude());
        Log.d(TAG, "Longitude : " + location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");

        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(latLng);
        markerOptions1.title("Destination");
        // markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);
        currentLocationMarker = mMap.addMarker(markerOptions1);

        CameraUpdate CameraUpdatelocation = CameraUpdateFactory.newLatLngZoom(
                latLng, 15);
        mMap.animateCamera(CameraUpdatelocation);


        // mMap.animateCamera(zoom);

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }

    }

    public String ConvertLatLngToAddress(LatLng latLng)
    {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        return address;
    }

    public void onClick(View v)
    {
        hideSoftKeyboard();
        Object[] dataTransfer = new Object[2];
        String url;

        switch (v.getId())
        {
            case R.id.B_search:
            {
                EditText tf_location = (EditText) findViewById(R.id.TF_Destlocation);
                String location = tf_location.getText().toString();

                List<Address> addressList = null;
                MarkerOptions mo = new MarkerOptions();

                if(!location.equals(""))
                {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for(int i = 0; i < addressList.size(); i++)
                    {
                        Address myAddress = addressList.get(i);
                        LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        mo.position(latLng);
                        mo.title("Search Result");
                        mMap.addMarker(mo);
                        //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        CameraUpdate CameraUpdatelocation = CameraUpdateFactory.newLatLngZoom(
                                latLng, 15);
                        mMap.animateCamera(CameraUpdatelocation);
                    }
                }else {
                    Toast.makeText(this, "Please Enter Destination Address", Toast.LENGTH_SHORT).show() ;
                }
                break;
            }
            case R.id.B_restaurent:
            {
                Log.d("onClick", "Restaurant is Clicked");
                mMap.clear();
                String Restaurant = "restaurant";
                Log.d(TAG, "latitude: " + latitude);
                Log.d(TAG, "longitude: " + longitude);

                url = getUrl(latitude, longitude, Restaurant);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(dataTransfer);

                Toast.makeText(MapsActivity.this,"Nearby Restaurants", Toast.LENGTH_LONG).show();

                break;
            }
            case R.id.B_Food:
            {
                Log.d("onClick", "Food is Clicked");
                mMap.clear();
                String Food = "food";
                url = getUrl(latitude, longitude, Food);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(dataTransfer);

                Toast.makeText(MapsActivity.this,"Nearby Food", Toast.LENGTH_LONG).show();

                break;
            }
            case R.id.B_School:
            {
                Log.d("onClick", "School is Clicked");
                mMap.clear();
                String School = "school";
                url = getUrl(latitude, longitude, School);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(dataTransfer);

                Toast.makeText(MapsActivity.this,"Nearby School", Toast.LENGTH_LONG).show();

                break;
            }
            case R.id.B_Direction:
            {
                Log.d("onClick", "Direction");
                dataTransfer = new Object[3];
                url = getDirectionUrl();
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(destlatitude, destlongitude);

                Log.d(TAG, "GetDirection URL: " + url);

                getDirectionsData.execute(dataTransfer);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(latitude, longitude));
                builder.include(new LatLng(destlatitude, destlongitude));
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));

                break;
            }
        }
    }

    private String getDirectionUrl()
    {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin=" + latitude + "," + longitude);
        googleDirectionUrl.append("&destination=" + destlatitude + "," + destlongitude);
        googleDirectionUrl.append("&key=" + "AIzaSyAk0trsMNNyObASi0Zhs_52-oKAXXSaXoo");

        return googleDirectionUrl.toString();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&key=" + "AIzaSyAk0trsMNNyObASi0Zhs_52-oKAXXSaXoo");
        googlePlaceUrl.append("&sensor=" + true);

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: start");
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: start");
        destlatitude = marker.getPosition().latitude;
        destlongitude = marker.getPosition().longitude;
        String DestinationAddress =  ConvertLatLngToAddress(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        tf_destination.setText(DestinationAddress);
        Log.d(TAG, "onMarkerDragEnd: destlatitude:" + destlatitude);
        Log.d(TAG, "onMarkerDragEnd: destlongitude:" + destlongitude);
    }
}

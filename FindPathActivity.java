package com.nhtthuan.trackingbus;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhtthuan.trackingbus.MyBottomSheet.Action;
import com.nhtthuan.trackingbus.MyBottomSheet.CustomBottomSheetDialogFragment;
import com.nhtthuan.trackingbus.MyBottomSheet.ItemBottomSheet;
import com.nhtthuan.trackingbus.MyBottomSheet.ItemBottomSheetCollection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class FindPathActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 5000;
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/geocode/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBlMRlP96BilfqiOnxcAFB3VydvPenBRs4";
    private FusedLocationProviderClient fusedLocationClient;
    boolean flagStart = false;
    boolean flagStop = false;

    private GoogleMap mMap;
    private MapView mapView;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location mLocation;


    private TextView txtStart, txtStop, tvOption;
    private ImageButton btChange;
    private Button btTimkiem;
    private FloatingActionButton actionButton;

    private BottomSheetBehavior behavior;

    Polyline polyline;
    private LatLng latLngA, latLngB;
    private String origin, destination;
    private Marker mkOrigin, mkDestination;
    private int option = 1;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_path);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tìm kiếm lộ trình");

        txtStart = (TextView) findViewById(R.id.txtStart);
        txtStop = (TextView) findViewById(R.id.txtStop);
        btChange = (ImageButton) findViewById(R.id.btChange);
        tvOption = (TextView) findViewById(R.id.tvOption);
        btTimkiem = (Button) findViewById(R.id.btTimkiem);
        actionButton = (FloatingActionButton) findViewById(R.id.fbGPS);
        mapView = (MapView) findViewById(R.id.maps);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        requestLocationPermissions();
        if (isPlayServicesAvailable()) {
            setUpLocationClientIfNeeded();
            buildLocationRequest();
        } else {
            Toast.makeText(this, "Thiết bị không hỗ trợ Google Play Services", Toast.LENGTH_SHORT).show();
        }
        if (isGpsOn()) {
            callMyLocation();
        } else {
            Toast.makeText(FindPathActivity.this, "GPS is OFF", Toast.LENGTH_SHORT).show();
        }

        Bundle bundle = getIntent().getExtras();
        destination = bundle.getString("id");
        latLngB = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
        txtStop.setText(bundle.getString("name"));

        txtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAutoComplete();
                flagStart = true;
            }
        });
        txtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAutoComplete();
                flagStop = true;
            }
        });

        btChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation animRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                btChange.startAnimation(animRotate);
                String st = txtStart.getText().toString();
                txtStart.setText(txtStop.getText());
                txtStop.setText(st);

                String tmp = origin;
                origin = destination;
                destination = tmp;


                if (mkOrigin != null) {
                    LatLng latLngTmp = mkOrigin.getPosition();
                    mkOrigin.remove();
                    mkOrigin = mMap.addMarker(new MarkerOptions().position(mkDestination.getPosition())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_start_trip)));
                    if (mkDestination != null)
                        mkDestination.remove();
                    mkDestination = mMap.addMarker(new MarkerOptions().position(latLngTmp)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_end_trip)));
                } else {
                    mkOrigin = mMap.addMarker(new MarkerOptions().position(mkDestination.getPosition())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_start_trip)));
                    if (mkDestination != null)
                        mkDestination.remove();
                }
                zoomToAllMarker();
            }
        });

        tvOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(ItemBottomSheet.ITEMS_KEY, ItemBottomSheetCollection.getActions());
                CustomBottomSheetDialogFragment fragment = new CustomBottomSheetDialogFragment();
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), CustomBottomSheetDialogFragment.FRAGMENT_KEY);
            }
        });

        btTimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mkOrigin == null) {
                    Toast.makeText(FindPathActivity.this, "Nhập nơi bắt đầu", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mkDestination == null) {
                    Toast.makeText(FindPathActivity.this, "Nhập điểm đến", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mkOrigin != null && mkDestination != null) {

                    Intent intent = new Intent(FindPathActivity.this, DirectionActivity.class);
                    intent.putExtra("origin", origin);
                    intent.putExtra("destination", destination);
                    intent.putExtra("tvorigin", txtStart.getText());
                    intent.putExtra("tvdestination", txtStop.getText());
                    intent.putExtra("option", option);
                    startActivity(intent);
                }
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGpsOn()) {
                    callMyLocation();
                } else {
                    Toast.makeText(FindPathActivity.this, "GPS is OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (isGpsOn()) {
            callMyLocation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMarker(latLngB);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void setMarker(LatLng latLng) {
        if (mkDestination != null)
            mkDestination.remove();
        mkDestination = mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_end_trip)));
        zoomToAllMarker();
    }

    private void zoomToAllMarker() {
        if (mkOrigin != null && mkDestination != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mkOrigin.getPosition());
            builder.include(mkDestination.getPosition());

            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (height * 0.10);
            if (polyline != null)
                polyline.remove();
            polyline = mMap.addPolyline(new PolylineOptions()
                    .add(mkOrigin.getPosition(), mkDestination.getPosition())
                    .width(8)
                    .color(Color.RED));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cameraUpdate);
        } else if (mkDestination != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mkDestination.getPosition(), 16);
            mMap.moveCamera(cameraUpdate);
        } else if (mkOrigin != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mkOrigin.getPosition(), 16);
            mMap.moveCamera(cameraUpdate);
        }
    }

    public void startAutoComplete() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (flagStart) {
                    txtStart.setText(place.getName());
                    origin = place.getId();
                    if (mkOrigin != null)
                        mkOrigin.remove();
                    mkOrigin = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_start_trip)));
                    zoomToAllMarker();
                    flagStart = false;
                }
                if (flagStop) {
                    txtStop.setText(place.getName());
                    destination = place.getId();
                    if (mkDestination != null)
                        mkDestination.remove();
                    mkDestination = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_end_trip)));
                    zoomToAllMarker();
                    flagStop = false;
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {
                flagStart = false;
                flagStop = false;
            }
        }
    }

    //-----------------------
    public void onResultDialogFragment(int position) {
        ArrayList<Action> lsActions = ItemBottomSheetCollection.getActionsWhite();
        Action action = lsActions.get(position);
        tvOption.setText(action.getLabel());
        tvOption.setCompoundDrawablesWithIntrinsicBounds(action.getIconId(), 0, 0, 0);
        option = position;
    }

    public String creadUrl() {
        return DIRECTION_URL_API + "latlng=" + mLocation.getLatitude() +
                "," + mLocation.getLongitude()
                + "&key=" + GOOGLE_API_KEY;
    }

    public String getData(String strUrl) {
        String strResult = "";
        URL url;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(60000);
            InputStream in = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                strResult += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }

    public class LoadData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String strResult = "";
            String strUrl = params[0];
            strResult = getData(strUrl);
            return strResult;
        }

        @Override
        protected void onPostExecute(String s) {
            final String data = s;
            try {
                JSONObject jsData = new JSONObject(data);
                String status = jsData.getString("status");
                if (status.equals("OK")) {
                    JSONArray results = jsData.getJSONArray("results");
                    origin = results.getJSONObject(0).getString("place_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }

    //
    //get My Location

    private void callMyLocation() {
        if (mLocation != null) {
            txtStart.setText("Vị trí hiện tại");
            new LoadData().execute(creadUrl());
            Drawable iconDrawble = getResources().getDrawable(R.drawable.ic_marker_dot_blue);
            BitmapDescriptor icon = getMarkerIconFromDrawable(iconDrawble);
            mkOrigin = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                    .icon(icon));
            zoomToAllMarker();
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    requestLocationPermissions();
                }
                break;
        }
    }

    private boolean isPlayServicesAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS;
    }

    private boolean isGpsOn() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void setUpLocationClientIfNeeded() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
//        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                // Update UI with location data
                mLocation = location;
                // ... other actions on location change ...
            }
        }
    };

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some situations, this can be null.
                        if (location != null) {
                            mLocation = location;
                            // Use the location object to update your UI
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onResume() {
        //  this.initBottomSheet();
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (googleApiClient != null
                && googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
            googleApiClient = null;
        }
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            mLocation = lastLocation;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }
}

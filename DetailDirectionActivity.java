package com.nhtthuan.trackingbus;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhtthuan.trackingbus.Adapter.DetailDirectionAdapter;
import com.nhtthuan.trackingbus.Model.directions.Routes;
import com.nhtthuan.trackingbus.Model.directions.Steps;

import java.util.ArrayList;
import java.util.List;

public class DetailDirectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FloatingActionButton actionButton;
    private MapView mapView;
    private GoogleMap mMaps;
    private boolean isOnGPS = false;

    private ListView lsView;
    private Routes routes;
    private List<Steps> lsData;
    private DetailDirectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_direction);
        initView();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        routes = getIntent().getParcelableExtra("routes");
        lsData = routes.getSteps();
        adapter = new DetailDirectionAdapter(routes, this);
        lsView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        lsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lsData.get(i).getStartLocation(), 16);
                mMaps.animateCamera(cameraUpdate);
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnGPS) {
                    if (ActivityCompat.checkSelfPermission(DetailDirectionActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(DetailDirectionActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMaps.setMyLocationEnabled(true);
                    if (mMaps.getMyLocation() != null) {
                        actionButton.setImageResource(R.drawable.ic_my_location_20dp_blue_light);
                        isOnGPS = true;
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mMaps.getMyLocation().getLatitude(), mMaps.getMyLocation().getLongitude()), 16);
                        mMaps.animateCamera(cameraUpdate);
                    }
                } else {
                    isOnGPS = false;
                    mMaps.setMyLocationEnabled(false);
                    actionButton.setImageResource(R.drawable.ic_my_location_20dp_gray68);
                }
            }
        });
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lộ trình");
        mapView = (MapView) findViewById(R.id.maps);
        lsView = (ListView) findViewById(R.id.lsView);
        actionButton = (FloatingActionButton) findViewById(R.id.fbtGPS);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMaps = googleMap;
        ArrayList<Polyline> polylineList = new ArrayList<>();
        mMaps.addMarker(new MarkerOptions()
                .position(routes.getStartLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_start_trip))
                .title(routes.getStartAddress()));
        mMaps.addMarker(new MarkerOptions()
                .position(routes.getEndLocation())
                .title(routes.getEndAddress())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_end_trip)));
        for (Steps steps : routes.getSteps()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .width(10);
            if (steps.getTravelMode().equals("WALKING"))
                polylineOptions.addAll(steps.getPoints())
                        .color(Color.GRAY);

            if (steps.getTravelMode().equals("TRANSIT")) {
                polylineOptions.addAll(steps.getPoints())
                        .color(Color.RED);
                mMaps.addMarker(new MarkerOptions()
                        .position(steps.getStartLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_station_bus)));
                mMaps.addMarker(new MarkerOptions()
                        .position(steps.getEndLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_station_bus)));
            }
            polylineList.add(googleMap.addPolyline(polylineOptions));
        }
        zoomToAllMarker(routes.getStartLocation(), routes.getEndLocation());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMaps.getUiSettings().setMyLocationButtonEnabled(false);
        mMaps.setMyLocationEnabled(true);
    }

    public void zoomToAllMarker(LatLng lngA, LatLng lngB) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(lngA);
        builder.include(lngB);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.10);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMaps.moveCamera(cameraUpdate);
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
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}

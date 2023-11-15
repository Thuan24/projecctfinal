package com.nhtthuan.trackingbus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhtthuan.trackingbus.Adapter.StopBusAdapter;
import com.nhtthuan.trackingbus.MyBottomSheet.BottomSheetBehaviorGoogleMapsLike;
import com.nhtthuan.trackingbus.Model.Bus;
import com.nhtthuan.trackingbus.TimeLineView.Orientation;
import com.nhtthuan.trackingbus.TimeLineView.TimeLineAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsBusActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMaps;
    MapView mapView;

    ListView lsView;
    Bus bus;
    List<String> lsData;
    FloatingActionButton fbExpand;

    private RecyclerView mRecyclerView;
    private TimeLineAdapter mTimeLineAdapter;
    private Orientation mOrientation = Orientation.vertical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_bus);
        init();
        bus = getIntent().getParcelableExtra("bus");
        getSupportActionBar().setTitle(bus.getBusname());
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        String[] listData = bus.getBusitinerary().split("-");
        lsData = new ArrayList<>(Arrays.asList(listData));

        mTimeLineAdapter = new TimeLineAdapter(lsData, mOrientation);
        mRecyclerView.setAdapter(mTimeLineAdapter);
        mTimeLineAdapter.notifyDataSetChanged();
    }

    public void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = (MapView) findViewById(R.id.maps);
        fbExpand = (FloatingActionButton) findViewById(R.id.fbExpand);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(getLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true);
    }

    private LinearLayoutManager getLinearLayoutManager() {

        if (mOrientation == Orientation.horizontal) {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            return linearLayoutManager;
        } else {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            return linearLayoutManager;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMaps = googleMap;
        String s = bus.getPolyline();
        final List<LatLng> listPoint = decodePolyLine(s);
        PolylineOptions polylineOptions = new PolylineOptions()
                .geodesic(true)
                .color(Color.RED)
                .addAll(listPoint)
                .width(10);
        mMaps.addPolyline(polylineOptions);

        zoomToAllMarker(listPoint);
        fbExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomToAllMarker(listPoint);
            }
        });
    }


    public void zoomToAllMarker(List<LatLng> listPoint) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : listPoint)
            builder.include(latLng);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.05);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMaps.moveCamera(cameraUpdate);
    }

    public List<LatLng> decodePolyLine(final String poly) {
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        int index = 0;
        while (index < poly.length()) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(lat / 100000d, lng / 100000d));
        }
        return decoded;
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
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}

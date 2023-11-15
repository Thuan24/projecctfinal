package com.nhtthuan.trackingbus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.nhtthuan.trackingbus.Adapter.ResultRoutesAdapter;
import com.nhtthuan.trackingbus.Model.directions.ArrivalStop;
import com.nhtthuan.trackingbus.Model.directions.ArrivalTime;
import com.nhtthuan.trackingbus.Model.directions.Distance;
import com.nhtthuan.trackingbus.Model.directions.Duration;
import com.nhtthuan.trackingbus.Model.directions.Fare;
import com.nhtthuan.trackingbus.Model.directions.Routes;
import com.nhtthuan.trackingbus.Model.directions.Steps;
import com.nhtthuan.trackingbus.Model.directions.TransitDetails;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DirectionActivity extends AppCompatActivity {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDoWUkOp1to9-gQma4sR7RXoD1i9Jti2Tk";

    private TextView tvOrigin, tvDestination;
    private ListView lvData;

     private int option = 1;
    private String origin, destination;
    private ArrayList<Routes> lsRoutes = new ArrayList<>();
    private ResultRoutesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dánh sách các tuyến đường");

        tvOrigin = (TextView) findViewById(R.id.tvOrigin);
        tvDestination = (TextView) findViewById(R.id.tvDestination);
        lvData = (ListView) findViewById(R.id.lsView);

        Bundle bundle = getIntent().getExtras();
        origin = bundle.getString("origin");
        destination = bundle.getString("destination");
        tvOrigin.setText(bundle.getString("tvorigin"));
        tvDestination.setText(bundle.getString("tvdestination"));
        option = bundle.getInt("option");

        sendRequest();
    }

    private void sendRequest() {
        String strUrl = creadUrl();
        new LoadData().execute(strUrl);
    }

    public String creadUrl() {
        return DIRECTION_URL_API + "origin=place_id:" + origin
                + "&destination=place_id:" + destination
                + "&mode=transit&alternatives=true&language=vi&region=vi"
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
                lsRoutes = parseJSon(data);
                if (lsRoutes != null) {
                    if (option == 0)
                        Collections.sort(lsRoutes, new Comparator<Routes>() {
                            @Override
                            public int compare(Routes routes, Routes t1) {
                                if (routes.getDistance().getValue() < t1.getDistance().getValue())
                                    return -1;
                                else {
                                    if (routes.getDistance().getValue() == t1.getDistance().getValue())
                                        return 0;
                                    else return 1;
                                }
                            }
                        });
                    else if (option == 1)
                        Collections.sort(lsRoutes, new Comparator<Routes>() {
                            @Override
                            public int compare(Routes routes, Routes t1) {
                                if (getDistanceWalking(routes) < getDistanceWalking(t1))
                                    return -1;
                                else {
                                    if (getDistanceWalking(routes) == getDistanceWalking(t1))
                                        return 0;
                                    else return 1;
                                }
                            }
                        });
                    else Collections.sort(lsRoutes, new Comparator<Routes>() {
                            @Override
                            public int compare(Routes routes, Routes t1) {
                                if (getCountStep(routes) < getCountStep(t1))
                                    return -1;
                                else if (getCountStep(routes) == getCountStep(t1))
                                    return 0;
                                else return 1;
                            }
                        });

                    adapter = new ResultRoutesAdapter(lsRoutes, DirectionActivity.this);
                    lvData.setAdapter(adapter);
                    lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Routes routes = lsRoutes.get(position);
                            Intent intent = new Intent(DirectionActivity.this, DetailDirectionActivity.class);
                            intent.putExtra("routes", routes);
                            startActivity(intent);
                        }
                    });
                    adapter.notifyDataSetChanged();
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }

    public int getDistanceWalking(Routes routes) {
        int results = 0;
        List<Steps> lsSteps = routes.getSteps();
        for (int j = 0; j < lsSteps.size(); j++) {
            Steps steps = lsSteps.get(j);
            if (steps.getTravelMode().equals("WALKING")) {
                results = results + steps.getDistance().getValue();
            }
        }
        return results;
    }

    public int getCountStep(Routes routes) {
        return routes.getSteps().size();
    }

    public ArrayList<Routes> parseJSon(String data) throws JSONException {
        if (data == null)
            return null;
        ArrayList<Routes> lsRoutes = new ArrayList<Routes>();
        JSONObject jsData = new JSONObject(data);
        String status = jsData.getString("status");

        if (status.equals("OK")) {
            JSONArray jsLsRoutess = jsData.getJSONArray("routes");

            for (int i = 0; i < jsLsRoutess.length(); i++) {
                JSONObject jsRoutes = jsLsRoutess.getJSONObject(i);

                JSONObject jsFare = jsRoutes.getJSONObject("fare");
                JSONObject jsOverviewPolylineJson = jsRoutes.getJSONObject("overview_polyline");
                JSONArray jsLsLegs = jsRoutes.getJSONArray("legs");
                JSONObject jsLeg = jsLsLegs.getJSONObject(0);

                JSONObject jsArrivalTimeLeg = jsLeg.getJSONObject("arrival_time");
                JSONObject jsDepartureTimeLeg = jsLeg.getJSONObject("departure_time");
                JSONObject jsDistance = jsLeg.getJSONObject("distance");
                JSONObject jsDuration = jsLeg.getJSONObject("duration");
                JSONObject jsEndLocation = jsLeg.getJSONObject("end_location");
                JSONObject jsStartLocation = jsLeg.getJSONObject("start_location");

                List<Steps> lsSteps = new ArrayList<Steps>();
                JSONArray jsLsSteps = jsLeg.getJSONArray("steps");
                for (int j = 0; j < jsLsSteps.length(); j++) {
                    JSONObject jsSteps = jsLsSteps.getJSONObject(j);
                    Steps steps = new Steps();

                    JSONObject jsStepDistance = jsSteps.getJSONObject("distance");
                    JSONObject jsStepDuration = jsSteps.getJSONObject("duration");
                    JSONObject jsStepEndLocation = jsSteps.getJSONObject("end_location");
                    JSONObject jsStepStartLocation = jsSteps.getJSONObject("start_location");
                    JSONObject jsStepPolyLine = jsSteps.getJSONObject("polyline");

                    if (jsSteps.getString("travel_mode").equals("TRANSIT")) {
                        TransitDetails transitDetails = new TransitDetails();
                        JSONObject jsStepTransitDetail = jsSteps.getJSONObject("transit_details");
                        JSONObject jsArrivalStop = jsStepTransitDetail.getJSONObject("arrival_stop");
                        JSONObject jsArrivalStopLocation = jsArrivalStop.getJSONObject("location");
                        transitDetails.setArrivalStop(new ArrivalStop(new LatLng(jsArrivalStopLocation.getDouble("lat"), jsArrivalStopLocation.getDouble("lng")), jsArrivalStop.getString("name")));

                        JSONObject jsDepartureStop = jsStepTransitDetail.getJSONObject("departure_stop");
                        JSONObject jsDepartureStopLocation = jsDepartureStop.getJSONObject("location");
                        transitDetails.setDepartureStop(new ArrivalStop(new LatLng(jsDepartureStopLocation.getDouble("lat"), jsDepartureStopLocation.getDouble("lng")), jsDepartureStop.getString("name")));

                        JSONObject jsArrivalTime = jsStepTransitDetail.getJSONObject("arrival_time");
                        transitDetails.setArrivalTime(jsArrivalTime.getString("text"));

                        JSONObject jsDepartureTime = jsStepTransitDetail.getJSONObject("departure_time");
                        transitDetails.setDepartureTime(jsDepartureTime.getString("text"));

                        transitDetails.setHeadsign(jsStepTransitDetail.getString("headsign"));
                        transitDetails.setNumStops(jsStepTransitDetail.getInt("num_stops"));

                        JSONObject jsLineName = jsStepTransitDetail.getJSONObject("line");
                        transitDetails.setLineName(jsLineName.getString("name"));
                        steps.setTransitDetails(transitDetails);
                    }

                    steps.setDistance(new Distance(jsStepDistance.getString("text"), jsStepDistance.getInt("value")));
                    steps.setDuration(new Duration(jsStepDuration.getString("text"), jsStepDuration.getInt("value")));
                    steps.setHtmlInstructions(jsSteps.getString("html_instructions"));
                    steps.setEndLocation(new LatLng(jsStepEndLocation.getDouble("lat"), jsStepEndLocation.getDouble("lng")));
                    steps.setStartLocation(new LatLng(jsStepStartLocation.getDouble("lat"), jsStepStartLocation.getDouble("lng")));
                    steps.setTravelMode(jsSteps.getString("travel_mode"));
                    steps.setPoints(decodePolyLine(jsStepPolyLine.getString("points")));
                    lsSteps.add(steps);
                }

                Routes routes = new Routes();
                routes.setFare(new Fare(jsFare.getString("currency"), jsFare.getString("text"), jsFare.getString("value")));
                routes.setArrivalTime(new ArrivalTime(jsArrivalTimeLeg.getString("text"), jsArrivalTimeLeg.getInt("value")));
                routes.setDepartureTime(new ArrivalTime(jsDepartureTimeLeg.getString("text"), jsDepartureTimeLeg.getInt("value")));
                routes.setDistance(new Distance(jsDistance.getString("text"), jsDistance.getInt("value")));
                routes.setDuration(new Duration(jsDuration.getString("text"), jsDuration.getInt("value")));
                routes.setEndAddress(jsLeg.getString("end_address"));
                routes.setEndLocation(new LatLng(jsEndLocation.getDouble("lat"), jsEndLocation.getDouble("lng")));
                routes.setStartAddress(jsLeg.getString("start_address"));
                routes.setStartLocation(new LatLng(jsStartLocation.getDouble("lat"), jsStartLocation.getDouble("lng")));
                routes.setPoints(decodePolyLine(jsOverviewPolylineJson.getString("points")));
                routes.setSteps(lsSteps);

                lsRoutes.add(routes);
            }
        }
        return lsRoutes;
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
}

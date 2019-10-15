package com.example.go_car;

import helper.SQLiteHandler;
import helper.SessionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    Button btnLogout;
    ListView list;
    ImageView image;
    private static final String TAG = MapsActivity.class.getSimpleName();
    public Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    FusedLocationProviderClient mFusedLocationProviderClient;
    double userLatitude;
    double userLongtitude;
    double earthRadius = 3958.75;
    String Latitude;
    String Longitude;
    car currentcar;
    String carLat;
    String catLng;
    private SQLiteHandler db;
    private SessionManager session;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btnLogout = (Button) findViewById(R.id.logout);
        list = (ListView) findViewById(R.id.list);
        getJSON("http://172.20.10.3/android_login_api/vehciles.php");

        image = (ImageView) findViewById(R.id.imageView);

        getLocationPermission();
        getDeviceLocation();


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int arg2, long arg3) {
                //  Toast.makeText(ListActivity.this, results[position], Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MapsMarkerActivity.class);
                currentcar = (car) list.getItemAtPosition(arg2);
                carLat = currentcar.getLatitude();
                catLng = currentcar.getLongitude();
                intent.putExtra("lat", carLat);
                intent.putExtra("lng", catLng);
                startActivity(intent);


            }
        });


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

        HashMap<String, String> cars = db.getUserDetails();

        String Name = cars.get("Name");
        String Fuel_Level = cars.get("Fuel Level");
        String Longitude = cars.get("Longitude");
        String Latitude = cars.get("Latitude");
        String Prod_Year = cars.get("Prod_Year");


        // Displaying the user details on the screen
        //txtName.setText(name);
        //txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

//        maps.setOnClickListener(new View.OnClickListener() {
//
//
//            public void onClick(View v) {
//                movetoLocation();
//            }
//        });

    }


//    private void movetoLocation() {
//        Intent i = new Intent(MainActivity.this, MapsActivity.class);
//        startActivity(i);
//        finish();
//    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                System.out.println("nado" + mFusedLocationProviderClient);
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            userLatitude = mLastKnownLocation.getLatitude() ;
                            userLongtitude = mLastKnownLocation.getLongitude() ;
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }



    private void getJSON(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    loadIntoListView(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }


    private void loadIntoListView(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<car> vehicles = new ArrayList<>();
        //String[] imagepath = new String[jsonArray.length()];
        System.out.println("myLocation"+userLatitude+","+userLongtitude);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Latitude = obj.getString("Latitude");
            Longitude = obj.getString("Longitude");
            double dLat = Math.toRadians(userLatitude - Double.parseDouble(Latitude));
            double dLng = Math.toRadians(userLongtitude - Double.parseDouble(Longitude));
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(Double.parseDouble(Latitude))) * Math.cos(Math.toRadians(userLatitude)) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double dist = earthRadius * c;

            // imagepath[i] =  obj.getString("Image_Path" ) ;
            vehicles.add(new car(
                    obj.getString("Name"),
                    obj.getString("Fuel_Level"),
                    obj.getString("Prod_Year"),
                    dist,
                    obj.getString("Image_Path"),
                    obj.getString("Latitude"),
                    obj.getString("Longitude")
            ));


            Collections.sort(vehicles);


        }
        CustomAdapter arrayAdapter = new CustomAdapter(this, vehicles);
        list.setAdapter(arrayAdapter);
    }

    class CustomAdapter extends ArrayAdapter<car> {
        private final Activity context;
        private final ArrayList<car> nameArray;
        //private final String[] imageArray ;

        public CustomAdapter(Activity context, ArrayList<car> items) {
            super(context, R.layout.list_row, items);
            this.context = context;
            this.nameArray = items;
            //this.imageArray = imageArray ;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View CustomView = inflater.inflate(R.layout.list_row, null, false);
            TextView itemText = CustomView.findViewById(R.id.textView);
            ImageView image = CustomView.findViewById(R.id.imageView);

            Picasso.with(getApplicationContext()).load("http://172.20.10.3/" + nameArray.get(position).getImage_Path()).into(image);
            String output = "";
            output += nameArray.get(position).getName();
            output += "\n";
            output += "Fuel Level : " + nameArray.get(position).getFuel_Level();
            output += "\n";
            output += "Production Year : " + nameArray.get(position).getProd_Year();
            output += "\n";
            output += "Distance : " + nameArray.get(position).getDistance();
            output += "\n";


            itemText.setText(output);
            return CustomView;
        }
    }


}



package com.example.go_car;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import app.AppController;
import app.AppConfig;
import helper.SQLiteHandler;
import helper.SessionManager;


public class DeviceDetailActivity extends Activity {
    private static final String TAG = DeviceDetailActivity.class.getSimpleName();
    private TextView list ;
    private EditText review;
    private Button addReview;
    private Button maps;
   // private ListView list;
    private SessionManager session;
    private SQLiteHandler db;
    private ProgressDialog pDialog;
    String user_id;
    String car_id;
    String output ;
    TextView name ;
    String car_name ;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        name = (TextView)findViewById(R.id.car) ;
        list = (TextView)findViewById(R.id.text_r) ;
        review = (EditText) findViewById(R.id.review);
        addReview = (Button) findViewById(R.id.addReview);
        maps = (Button) findViewById(R.id.navigate);
        //list = (ListView) findViewById(R.id.list);

        Intent i = getIntent();
        car_id = i.getStringExtra("car_id");
        car_name = i.getStringExtra("car_name") ;
        getReviews();

        name.setText(car_name);

        //getJSON("http://172.20.10.3/android_login_api/getReview.php");


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        user_id = user.get(SessionManager.KEY_userid);
        System.out.println(user_id + "USER IN DEVICE");


        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        // Register Button Click event
        addReview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String NewReview = review.getText().toString().trim();
                if (!NewReview.isEmpty()) {
                    StoreReview(user_id, car_id, NewReview);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter review!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        maps.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        MyLoc_to_Des.class);
                startActivity(i);
                finish();
            }
        });

    }




    private void getReviews() {
        // Tag used to cancel the request
        final String tag_string_req = "req_review";
        //"http://172.20.10.3/android_login_api/getReview.php"
//        pDialog.setMessage("Adding Review ...");
//        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_GETREVIEWS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                System.out.println(response + "RESPONSEEEE");
                Log.d(tag_string_req, "Review Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQ

//                        String uid = jObj.getString("unique_id");

                        //JSONObject data = jObj.getJSONObject("review");
                        //System.out.println(review + "REVIEW");
                        JSONArray reviews = jObj.getJSONArray("reviews");
                        System.out.println(reviews + "REVIEW ARRAY ");
                        ArrayList<Review> reviewList = new ArrayList<Review>();
                        for (int i = 0; i < reviews.length(); i++) {
                            JSONObject reviewObject = reviews.getJSONObject(i).getJSONObject("reviewObject");
                            System.out.println(reviewObject + "ONE OBJECT");
                            output = "" ;
                            output += reviewObject.get("name") ;
                            output += ": " ;
                            output += reviewObject.get("review") ;
                            output +="\n" ;

                            list.setText(output);

                            //reviewList.add(new Review(reviewObject.getString("name"), reviewObject.getString("review")));


                        }





                        //MyAdapter arrayAdapter = new MyAdapter(DeviceDetailActivity.this, reviewList);
                        //list.setAdapter(arrayAdapter);



                        for (int i = 0; i < reviewList.size(); i++) {
                            reviewList.get(i).print();
                        }
//                        String user_id = Review.getString("user_id");
//                        String car_id = Review.getString("car_id");
//                        String review = Review.getString("review");

                        Toast.makeText(getApplicationContext(), "Reviews successfully received  !", Toast.LENGTH_LONG).show();

                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error + "ERROR");
                Log.e(tag_string_req, "Error Adding Review: " + error.getMessage());
//                Toast.makeText(getApplicationContext(),
//                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                System.out.println(user_id + "USERR");
                // System.out.println(LoginActivity.user_id + "REVIEW"
                params.put("car_id", MainActivity.car_id);


                return params;
            }

        };

        // Adding request to request queue
        System.out.println(AppController.getInstance() + "???");
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void StoreReview(final String user_id, final String car_id, final String review) {
        // Tag used to cancel the request
        final String tag_string_req = "req_review";

        pDialog.setMessage("Adding Review ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_STOREREVIEWS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                System.out.println(response + "RESPONSEEEE");
                Log.d(tag_string_req, "Review Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQ

//                        String uid = jObj.getString("unique_id");

//                        JSONObject Review = jObj.getJSONObject("Review");
//                        String user_id = Review.getString("user_id");
//                        String car_id = Review.getString("car_id");
//                        String review = Review.getString("review");

                        Toast.makeText(getApplicationContext(), "Review successfully added !", Toast.LENGTH_LONG).show();

                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error + "ERROR");
                Log.e(tag_string_req, "Error Adding Review: " + error.getMessage());
//                Toast.makeText(getApplicationContext(),
//                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                System.out.println(user_id + "USERR");
                // System.out.println(LoginActivity.user_id + "REVIEW");
                params.put("user_id", user_id);
                params.put("car_id", MainActivity.car_id);
                params.put("review", review);


                return params;
            }

        };

        // Adding request to request queue
        System.out.println(AppController.getInstance() + "???");
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

//
//    private void getJSON(final String urlWebService) {
//
//        class GetJSON extends AsyncTask<Void, Void, String> {
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                // Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
////                try {
////                    loadIntoListView(s);
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
//                if (!TextUtils.isEmpty(s) && s!=null) {
//                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//                    try {
//                        loadIntoListView(s);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            protected String doInBackground(Void... voids) {
//                try {
//                    URL url = new URL(urlWebService);
//                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                    StringBuilder sb = new StringBuilder();
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                    String json;
//                    while ((json = bufferedReader.readLine()) != null) {
//                        sb.append(json + "\n");
//                    }
//                    return sb.toString().trim();
//                } catch (Exception e) {
//                    return null;
//                }
//            }
//        }
//        GetJSON getJSON = new GetJSON();
//        getJSON.execute();
//    }


//    private void loadIntoListView(String json) throws JSONException {
//        JSONArray jsonArray = new JSONArray(json);
//        ArrayList<Review> reviews = new ArrayList<>();
//
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject obj = jsonArray.getJSONObject(i);
//
//            Review r = new Review(obj.getJSONObject(obj.get));
//            reviews.add(r);
//
//        }
//        MyAdapter arrayAdapter = new MyAdapter(DeviceDetailActivity.this, reviews);
//        list.setAdapter(arrayAdapter);
//    }

//    class MyAdapter extends ArrayAdapter<Review> {
//        private final Activity context;
//        private final ArrayList<Review> nameArray;
//
//
//        public MyAdapter(Activity context, ArrayList<Review> items) {
//            super(context, R.layout.list_review, items);
//            this.context = context;
//            this.nameArray = items;
//        }
//
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            LayoutInflater inflater = context.getLayoutInflater();
//            View CustomView = inflater.inflate(R.layout.list_row, null, false);
//            TextView itemText = CustomView.findViewById(R.id.new_review);
//
//            String output = "";
//            output += nameArray.get(position).getName() ;
//            output += ": " ;
//            output +=  nameArray.get(position).getReview();
//
//
//
//            itemText.setText(output);
//            return CustomView;
//        }
//
//
//}
}


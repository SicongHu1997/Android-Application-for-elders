/* Code developed by Team Morgaint
* for Subject IT Project COMP30022
* Team member:
* Chengyao Xu
* Jin Wei Loh
* Philip Cervenjak
* Qianqian Zheng
* Sicong Hu
*/
package com.example.ecare_client;

import com.example.ecare_client.mainpageview.*;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecare_client.settings.PersonalInfoActivity;
import com.example.ecare_client.textchat.MsgAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity implements OnClickListener {


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    static private HashMap<String, MsgAdapter> contacts = new HashMap<String, MsgAdapter>();
    private ProgressDialog mSpinner;
    private String email = "null";
    private static final String TAG = "MainActivity";
    private TextView temp_view, city_view, description_view, date_view;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Location location;
    private double Lat;
    private double Lon;

    private String phoneNo = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_layout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            email = user.getEmail();
            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
        }

        SexangleImageView chatActivity = (SexangleImageView) findViewById(R.id.chat_activity);
        chatActivity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        openContactListActivity();
                }
                return false;
            }
        });

        SexangleImageView helpActivity = (SexangleImageView) findViewById(R.id.help);
        helpActivity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        doSendSMS();
                }
                return false;
            }
        });


        SexangleImageView MapsView = (SexangleImageView) findViewById(R.id.btnOpenMaps);
        MapsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        openMapsActivity();
                }
                return false;
            }
        });

        SexangleImageView ChecklistView = (SexangleImageView) findViewById(R.id.btnOpenSettings);
        ChecklistView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        openChecklistActivity();
                }
                return false;

            }
        });


        SexangleImageView PersonalInfoView = (SexangleImageView) findViewById(R.id.btnOpenPersonalInfo);
        PersonalInfoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        openPersonalInfoActivity();
                }
                return false;
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Lat = location.getLatitude();
                Lon = location.getLongitude();
                find_weather(Lat, Lon);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        };

        temp_view = (TextView) findViewById(R.id.temp_text);
        city_view = (TextView) findViewById(R.id.city_text);
        description_view = (TextView) findViewById(R.id.descrip_text);
        date_view = (TextView) findViewById(R.id.date_text);

        try {
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        }

        catch (SecurityException e) {
            Toast.makeText(getApplicationContext(),
                    "Location request failed.", Toast.LENGTH_LONG).show();
        }

        location = beginLocation();
        if (location!=null){
            Lat = location.getLatitude();
            Lon = location.getLongitude();
            find_weather(Lat,Lon);
            Log.d(TAG, "onCreate:"+Lat);
            Log.d(TAG, "onCreate:"+Lon);
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final ArrayList<String> contactIDs = new ArrayList<>();

        //-----------------------------------------------------------------------------
        final FirebaseUser currentUser = auth.getCurrentUser();

        final DatabaseReference userRef =
                database.getReference().child("Users").child(currentUser.getUid());

        Query queryContacts = userRef.child("Contacts").orderByKey();


        queryContacts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "work1");

                    // dataSnapshot is the "Users" node with all children with contactEmail.
                    for (DataSnapshot matchingContact : dataSnapshot.getChildren()) {
                        Log.d(TAG, "newLoop");
                        // Very bad way of doing this perhaps.
                        if (!(matchingContact.getKey().equals("Null"))) {
                            Log.d(TAG, matchingContact.getKey());
                            contactIDs.add(matchingContact.getKey());
                        }

                    }
                    Log.d(TAG, "work2");
                    createMsgAdapter(contactIDs);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });


    }

    @Override
    public void onClick(View v) {
    }


    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }

    private void openMapsActivity() {
        Intent MapsActivity = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(MapsActivity);
    }

    private void openChecklistActivity() {
        Intent ChecklistActivity = new Intent(getApplicationContext(), ToolboxActivity.class);
        startActivity(ChecklistActivity);
    }

    private void openContactListActivity() {
        Intent ContactListActivity = new Intent(getApplicationContext(), com.example.ecare_client.settings.ContactListActivity.class);
        startActivity(ContactListActivity);
    }

    private void openPersonalInfoActivity() {
        Intent personalInfoActivity = new Intent(getApplicationContext(), PersonalInfoActivity.class);
        startActivity(personalInfoActivity);
    }

    private void find_weather(double lat, double lon) {
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=f4d6dace6e140800a81c7003610b7de2&units=Imperial";

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temp = String.valueOf(main_object.getDouble("temp"));
                    String description = object.getString("description");
                    String city = response.getString("name");

                    city_view.setText(city);
                    description_view.setText(description);
                    ImageView image = (ImageView) findViewById(R.id.weather_imageView);
                    changeImage(description, image);

                    Calendar calendar = Calendar.getInstance();
                    DateFormat sdf = SimpleDateFormat.getDateInstance();
                    String formatted_date = sdf.format(calendar.getTime());

                    date_view.setText(formatted_date);

                    double temp_int = Double.parseDouble(temp);
                    double centi = (temp_int - 32) / 1.8000;
                    centi = Math.round(centi);
                    int i = (int) centi;
                    temp_view.setText(String.valueOf(i));


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);


    }


    private void changeImage(String s, ImageView image) {
        switch (s) {
            case "broken clouds":
            case "overcast clouds":
                image.setImageResource(R.drawable.icon_brokenclouds);
                break;
            case "sky is clear":
                image.setImageResource(R.drawable.icon_clearsky);
                break;
            case "light rain":
            case "moderate rain":
            case "heavy intensity rain":
                image.setImageResource(R.drawable.icon_rain);
                break;
            case "snow":
            case "light snow":
            case "moderate snow":
                image.setImageResource(R.drawable.icon_snow);
                break;
            case "mist":
                image.setImageResource(R.drawable.icon_mist);
                break;
            case "few clouds":
                image.setImageResource(R.drawable.icon_fewclouds);
                break;
            case "thunder storm":
                image.setImageResource(R.drawable.icon_thunderstorm);
                break;
            case "shower rain":
                image.setImageResource(R.drawable.icon_showerrain);
                break;
            default:
                image.setImageResource(R.drawable.icon_clearsky);
                break;
        }
    }


    private void createMsgAdapter(ArrayList<String> contact) {
        Log.d(TAG, "createMsgAdapter:work");
        for (String people : contact) {
            Log.d(TAG, "forloop work");
            database = FirebaseDatabase.getInstance();
            DatabaseReference user = database.getReference().child("Users").child(people);
            ValueEventListener userEventListener = new ValueEventListener() {
                @Override
                // THE DATA SNAPSHOT IS AT THE CHILD!! NOT THE ROOT NODE!!!!
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange:work ");
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Log.d(TAG, "work");
                        if (child.getKey().equals("Email")) {
                            contacts.put(child.getValue(String.class), new MsgAdapter(getApplicationContext()));
                            Log.d(TAG, child.getValue(String.class));
                        }
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) { }

            };
            user.addValueEventListener(userEventListener);
        }
    }

    public static MsgAdapter getAdapter(String key) {
        return contacts.get(key);
    }

    private String judgeProvider(LocationManager locationManager) {
        List<String> prodiverlist = locationManager.getProviders(true);
        if(prodiverlist.contains(LocationManager.NETWORK_PROVIDER)){
            return LocationManager.NETWORK_PROVIDER;
        }else if(prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }else{
            Toast.makeText(getApplicationContext(),"Location provider does not exist",Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public Location beginLocation() {
        Log.d(TAG, "beginLocation:used ");
        //access lication service
        //if we have location provider
        if (judgeProvider(locationManager)!= null) {
            //to avoid getLastKnownLocation  warning
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return locationManager.getLastKnownLocation(judgeProvider(locationManager));
        }else{
            Toast.makeText(getApplicationContext(),"Location provider does not exist",Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void doSendSMS(){
        String userID = auth.getCurrentUser().getUid();
        DatabaseReference carerNoRef = database.getReference().
                child("Users").child(userID).child("Info").child("carerPhone");
        Query queryCarerNo = carerNoRef;
        queryCarerNo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    phoneNo = "+" + dataSnapshot.getValue(String.class);

                    String myCurrentlocation = "https://www.google.com.au/maps/place/" + Lat + "+" + Lon;
                    String message = "I need Help, I am at this location:" + myCurrentlocation;
                    if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNo)){
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNo));
                        intent.putExtra("sms_body",message);
                        startActivity(intent);
                    }

                    else {
                        Toast.makeText(getApplicationContext(),
                                "You must enter a valid Primary Carer Phone No. in Personal Info.", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Then run the SMS anyway, but using the default number.
                String myCurrentlocation = "https://www.google.com.au/maps/place/" + Lat + "+" + Lon;
                String message = "I need Help, I am at this location:" + myCurrentlocation;
                if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNo)){
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNo));
                    intent.putExtra("sms_body",message);
                    startActivity(intent);
                }
            }
        });
    }
}

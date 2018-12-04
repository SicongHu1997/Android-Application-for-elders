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

import com.example.ecare_client.videochat.*;
import com.example.ecare_client.textchat.*;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

public class ChatActivity extends BaseActivity implements MessageClientListener{

    private EditText inputText;
    private Button send;
    private Button videochat;
    private Button sendCurrentLocation;
    private Button more;
    private RecyclerView msgRecyclerView;
    private String makeCallTo;
    private MsgAdapter adapter;
    private static final String TAG = "ChatActivity";
    private TextView leftMsg;
    private TextView rightMsg;
    private Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String Lat;
    private String Lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_page);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }


        TitleLayout titleLayout = (TitleLayout) findViewById(R.id.chat_title);
        titleLayout.setTitleText("Chat");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Lat = String.valueOf(location.getLatitude());
                Lon = String.valueOf(location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        makeCallTo = getIntent().getExtras().getString("ContactName");

        if (MainActivity.getAdapter(makeCallTo)!=null){
            adapter = MainActivity.getAdapter(makeCallTo);
            Log.d(TAG, "Use main");
        }else {
            adapter = new MsgAdapter(getApplicationContext());
            Log.d(TAG, "own");
            Log.d(TAG, makeCallTo);
        }


        rightMsg = (TextView) findViewById(R.id.right_msg);
        leftMsg = (TextView) findViewById(R.id.left_msg);
        sendCurrentLocation = (Button) findViewById(R.id.send_location);
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        videochat = (Button) findViewById(R.id.video_call);
        more = (Button) findViewById(R.id.more);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        msgRecyclerView.setAdapter(adapter);

        videochat.setOnClickListener(buttonClickListener);
        send.setOnClickListener(buttonClickListener);
        sendCurrentLocation.setOnClickListener(buttonClickListener);
        more.setOnClickListener(buttonClickListener);
        if(rightMsg != null && leftMsg !=null) {
            rightMsg.setOnClickListener(buttonClickListener);
            leftMsg.setOnClickListener(buttonClickListener);
        }

        location = beginLocation();
        if (location!=null){
            Lat = String.valueOf(location.getLatitude());
            Lon = String.valueOf(location.getLongitude());
            //find_weather(Lat,Lon);
            Log.d(TAG, "onCreate:"+Lat);
            Log.d(TAG, "onCreate:"+Lon);
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    }


    private void sendMsg(){
        String content = inputText.getText().toString();
        if (!"".equals(content)) {
            getSinchServiceInterface().sendMessage(makeCallTo, content);
            inputText.setText("");//clear text input；
        }

    }

    //to place the call to the entered name
    private void callButtonClicked() {

        Call call = getSinchServiceInterface().callUserVideo(makeCallTo);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_call:
                    callButtonClicked();
                    break;
                case R.id.send:
                    sendMsg();
                    break;
                case R.id.send_location:
                    sendCurrentLocation();
                    break;
                case R.id.more:
                        sendCurrentLocation.setVisibility(View.VISIBLE);
                        sendCurrentLocation.setClickable(true);
                        videochat.setVisibility(View.VISIBLE);
                        videochat.setClickable(true);
                        more.setContentDescription("back");
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().removeMessageClientListener(this);
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    @Override
    public void onIncomingMessage(MessageClient client, Message message) {
        adapter.addMessage(message, adapter.DIRECTION_INCOMING);
        // update recycler view
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
        msgRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onMessageSent(MessageClient client, Message message, String recipientId) {
        adapter.addMessage(message, adapter.DIRECTION_OUTGOING);
        // update recycler view
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
        msgRecyclerView.scrollToPosition(adapter.getItemCount() - 1);

    }

    @Override
    public void onServiceConnected() {
        getSinchServiceInterface().addMessageClientListener(this);
    }

    @Override
    public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {
        // Left blank intentionally
    }

    @Override
    public void onMessageFailed(MessageClient client, Message message,
                                MessageFailureInfo failureInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sending failed: ")
                .append(failureInfo.getSinchError().getMessage());

        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        Log.d(TAG, sb.toString());
    }

    @Override
    public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
        Log.d(TAG, "onDelivered");
    }

    private void sendCurrentLocation(){
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        String message = "Click to see my current location";
        if (!"".equals(message)) {
            getSinchServiceInterface().sendLocation(makeCallTo,message,Lat,Lon);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("adapter",adapter);
        super.onSaveInstanceState(outState);
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
        Log.d(TAG, "beginLocatioon:used ");
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

}

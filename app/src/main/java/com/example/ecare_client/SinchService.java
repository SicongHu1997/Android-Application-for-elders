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
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

public class SinchService extends Service {

    private static final String APP_KEY = "8d24e3b6-39e6-47f1-9844-6c5bce024bbd";
    private static final String APP_SECRET = "VURNfOPOik6tNhbzYmTNJg==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();

    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private SinchClient mSinchClient;
    private String mUserId;

    private StartFailedListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mSinchClient != null && mSinchClient.isStarted()) {
            mSinchClient.terminate();
        }
        super.onDestroy();
    }

    private void start(String userName) {
        if (mSinchClient == null) {
            mUserId = userName;
            mSinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(userName)
                    .applicationKey(APP_KEY)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT).build();

            mSinchClient.setSupportCalling(true);
            mSinchClient.setSupportMessaging(true);
            mSinchClient.startListeningOnActiveConnection();

            mSinchClient.addSinchClientListener(new MySinchClientListener());
            mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
            mSinchClient.getMessageClient().addMessageClientListener(new SinchMessageClientListener());
            mSinchClient.start();
        }
    }

    private void stop() {
        if (mSinchClient != null) {
            mSinchClient.terminate();
            mSinchClient = null;
        }
    }

    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mSinchServiceInterface;
    }

    public class SinchServiceInterface extends Binder {

        public Call callUserVideo(String userId) {
            return mSinchClient.getCallClient().callUserVideo(userId);
        }

        public String getUserName() {
            return mUserId;
        }

        public boolean isStarted() {
            return SinchService.this.isStarted();
        }

        public void startClient(String userName) {
            start(userName);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId) {
            return mSinchClient.getCallClient().getCall(callId);
        }

        public VideoController getVideoController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getVideoController();
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }

        public void sendMessage(String recipientUserId, String textBody) {
            SinchService.this.sendMessage(recipientUserId, textBody);
        }
        public void sendLocation(String recipientUserId, String textBody, String lat, String lon){
            SinchService.this.sendLocation(recipientUserId,textBody,lat,lon);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            SinchService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            SinchService.this.removeMessageClientListener(listener);
        }
    }

    public interface StartFailedListener {

        void onStartFailed(SinchError error);

        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if (mListener != null) {
                mListener.onStartFailed(error);
            }
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient client) {
            Log.d(TAG, "SinchClient started");
            if (mListener != null) {
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient client) {
            Log.d(TAG, "SinchClient stopped");
        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient client,
                                                      ClientRegistration clientRegistration) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            Log.d(TAG, "Incoming call");
            Intent intent = new Intent(SinchService.this, IncomingCallScreenActivity.class);
            intent.putExtra(CALL_ID, call.getCallId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SinchService.this.startActivity(intent);
        }
    }
    private class SinchMessageClientListener implements MessageClientListener{
        @Override
        public void onIncomingMessage(MessageClient messageClient, Message message) {
            String title = message.getSenderId();
            String content = message.getTextBody();
            Intent intent = new Intent(SinchService.this,ChatActivity.class);
            Bundle options = new Bundle();
            options.putString("ContactName", title);
            intent.putExtras(options);
            PendingIntent pi = PendingIntent.getActivity(SinchService.this,0,intent,0);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pi)
                    .build();
            manager.notify(1, notification);
        }

        @Override
        public void onMessageSent(MessageClient messageClient, Message message, String s) {

        }

        @Override
        public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {

        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {

        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {

        }
    }
    public void sendMessage(String recipientUserId, String textBody) {
        if (isStarted()) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            mSinchClient.getMessageClient().send(message);
        }
    }

    public void sendLocation(String recipientUserId, String textBody,String lat,String lon){
        if (isStarted()){
            WritableMessage message = new WritableMessage();
            message.addRecipient(recipientUserId);
            message.setTextBody(textBody);
            message.addHeader("lat",lat);
            message.addHeader("lon",lon);
            mSinchClient.getMessageClient().send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener) {
        if (mSinchClient != null) {
            mSinchClient.getMessageClient().addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener) {
        if (mSinchClient != null) {
            mSinchClient.getMessageClient().removeMessageClientListener(listener);
        }
    }
}
package io.lokkit;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * This class is somehow required to load libstatusgoraw and libstatusgo.
 */
public class StatusService extends Service {

    private static final String TAG = "StatusService";

    public StatusService() {
        super();
    }

    private static class IncomingHandler extends Handler {

        private final WeakReference<StatusService> service;

        IncomingHandler(StatusService service) {

            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message message) {

            StatusService service = this.service.get();
            if (service != null) {
                if (!service.handleMessage(message)) {
                    super.handleMessage(message);
                }
            }
        }
    }

    private final Messenger serviceMessenger = new Messenger(new IncomingHandler(this));

    private static Messenger applicationMessenger = null;

    private boolean handleMessage(Message message) {
        Log.d(TAG, "Received service message." + message.toString());
        applicationMessenger = message.replyTo;

        return true;
    }

    public static void signalEvent(String jsonEvent) {

        Log.d(TAG, "Signal event: " + jsonEvent);
        Bundle replyData = new Bundle();
        replyData.putString("event", jsonEvent);

        Message replyMessage = Message.obtain(null, 0, 0, 0, null);
        replyMessage.setData(replyData);
        sendReply(applicationMessenger, replyMessage);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Status Service created!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Status Service stopped!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private static void sendReply(Messenger messenger, Message message) {
        try {
            Log.d(TAG, "before sendReply " + (messenger != null));
            if (messenger != null) {
                messenger.send(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception sending message id: " + message.what, e);
        }
    }
}

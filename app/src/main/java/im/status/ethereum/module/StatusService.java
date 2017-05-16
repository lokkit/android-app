package im.status.ethereum.module;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.status_im.status_go.cmd.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Handles the hooks provided by the Statusgo library.
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

    /**
     * This hook is called by the Statusgo native library.
     * @param jsonEvent
     */
    public static void signalEvent(String jsonEvent) {
        Log.d(TAG, "Signal event: " + jsonEvent);
        try {
            JSONObject jsonObject = new JSONObject(jsonEvent);
            switch (jsonObject.getString("type")) {
                case "transaction.queued":
                    Statusgo.CompleteTransaction(jsonObject.getJSONObject("event").getString("id"), "hirzel");// todo: intent and get password from app
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleTransaction(String id){
        // todo: intent to activity to ask for password.

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
}

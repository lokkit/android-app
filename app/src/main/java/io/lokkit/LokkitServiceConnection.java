package io.lokkit;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import im.status.ethereum.module.StatusService;

/**
 * Created by Nick on 23.05.2017.
 */

public class LokkitServiceConnection implements ServiceConnection {
    private StatusService lokkitService;
    private static final String TAG = "LokkitServiceConnection";
    private LokkitActivity activity;
    private ServiceBoundEvent event;

    public LokkitServiceConnection(LokkitActivity context) {
        this.activity = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        StatusService.LocalBinder b = (StatusService.LocalBinder) binder;
        lokkitService = b.getService();
        try {
            lokkitService.startNode(activity.getApplicationInfo().dataDir + "/ethereum/data/");
            OnBindEvent();
        } catch (LokkitException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.d(TAG, "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        OnUnbindEvent();
        Log.d(TAG, "onServiceDisconnected");
    }

    public void setServiceBoundEvent(ServiceBoundEvent event) {
        this.event = event;
    }

    private void OnBindEvent() {
        if (event != null) {
            event.serviceBound();
        }
    }

    private void OnUnbindEvent() {
        if (event != null) {
            event.serviceUnbound();
        }
    }

    public StatusService getLokkitService() {
        return this.lokkitService;
    }
}

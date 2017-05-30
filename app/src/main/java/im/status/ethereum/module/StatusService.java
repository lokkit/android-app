package im.status.ethereum.module;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.status_im.status_go.cmd.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.lokkit.LokkitAuthenticationFailedException;
import io.lokkit.LokkitException;
import io.lokkit.LokkitIntents;
import io.lokkit.Web3Bridge;

/**
 * Handles the hooks provided by the Statusgo library.
 */
public class StatusService extends Service {
    private final String lokkitGenesis = "{\"config\":{\"chainId\":42,\"homesteadBlock\":0,\"eip155Block\":0,\"eip158Block\":0},\"difficulty\":\"0x20000\",\"gasLimit\":\"0x80000000\",\"alloc\":{\"0xe0a83a8b5ba5c9acc140f89296187f96a163cf43\":{\"balance\":\"20000000000000000000\"},\"0x677c9e0a30ba472eec4ea0f4ed6dcfb1c51d6bf1\":{\"balance\":\"20000000000000000000\"},\"0xa26efbc2634c81900b3d2f604e6b427dfe6e1764\":{\"balance\":\"20000000000000000000\"},\"0xaf75fcb29d58549b9c451a52a64e9020a66bdf6e\":{\"balance\":\"20000000000000000000\"},\"0x9fffb27287898a20857531d7aae0942184e7d56e\":{\"balance\":\"20000000000000000000\"},\"0x183d9685e49367c07dc63f0938d112a74945e411\":{\"balance\":\"20000000000000000000\"},\"0x57f5d12a63025e819bb51e973be075717d923c15\":{\"balance\":\"20000000000000000000\"},\"0xf55fb78f02ac5ecc9333b35b4287609140690517\":{\"balance\":\"20000000000000000000\"},\"0xb5ede4a54dddec0fc345b5dc11d9db077015d686\":{\"balance\":\"20000000000000000000\"},\"0x179972bea45078eac67ac60c8de2257e6af33e27\":{\"balance\":\"20000000000000000000\"}}}";
    private final static Collection<String> peers = Collections.unmodifiableCollection(Arrays.asList(
            "enode://288b97262895b1c7ec61cf314c2e2004407d0a5dc77566877aad1f2a36659c8b698f4b56fd06c4a0c0bf007b4cfb3e7122d907da3b005fa90e724441902eb19e@192.168.43.166:30303",
            "enode://5f74d479eee44164e1b884fb3e05c22fd3177f990ceed45e03cf1058aab1619ed88e4f0d9feef35c7c4c9b2098735c75a00bade50f9137606725f99f6853cea7@192.168.43.166:30308",
            "enode://0d1253bb5f62a0ea42e312d9b352abf1742ca2b060b1f5b1e401d7e39fbd8081a04b8e060ef3430d0382e08c74891d345189f3fe64292f1efbcef4ac65ea8c7c@192.168.43.166:30305"));


    private static final String TAG = "LokkitService";
    private static WeakReference<StatusService> weakService;
    private Web3Bridge web3;
    private final LocalBinder localBinder = new LocalBinder();
    private boolean nodeRunning;
    private static final String mnemonicKey = "io.lokkit.MNEMONIC";
    private static final String addressKey = "io.lokkit.ADDRESS";
    private static final String accountPreferencesName = "io.lokkit.ACCOUNT_PREFERENCES";
    private List<BroadcastReceiver> receivers = new ArrayList<>();
    private String password; // is kept in the app while it's running. needs to be re-entered on app restart.

    private SharedPreferences getAccountPreferences() {
        return getSharedPreferences(accountPreferencesName, MODE_PRIVATE);
    }

    private String getMnemonic() {
        return getAccountPreferences().getString(mnemonicKey, null);
    }

    private String getPassword() {
        return password;
    }

    private String getAddress() {
        return getAccountPreferences().getString(addressKey, null);
    }

    private void setMnemonic(String mnemonic) {
        SharedPreferences.Editor edit = getAccountPreferences().edit();
        edit.putString(mnemonicKey, mnemonic);
        edit.apply();
    }

    private void setAddress(String address) {
        SharedPreferences.Editor edit = getAccountPreferences().edit();
        edit.putString(addressKey, address);
        edit.apply();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BroadcastReceiver completeTransactionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String id = intent.getExtras().getString(LokkitIntents.ID_EXTRA);
                String pw = intent.getExtras().getString(LokkitIntents.PASSWORD_EXTRA);
                String completeTransactionResponseString = Statusgo.CompleteTransaction(id, pw);
                try {
                    JSONObject completeTransactionResponse = new JSONObject(completeTransactionResponseString);
                    if (completeTransactionResponse.has("error")
                            && !completeTransactionResponse.getString("error").equals("")) {
                        Intent failIntent = new Intent(LokkitIntents.COMPLETE_TRANSACTION_FAILED);
                        failIntent.putExtra(LokkitIntents.ID_EXTRA, id);
                        failIntent.putExtra(LokkitIntents.REASON_EXTRA, completeTransactionResponse.getString("error"));
                        sendBroadcast(failIntent);
                    } else {
                        Intent successIntent = new Intent(LokkitIntents.COMPLETE_TRANSACTION_SUCCESSFUL);
                        successIntent.putExtra(LokkitIntents.ID_EXTRA, id);
                        sendBroadcast(successIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(completeTransactionReceiver, new IntentFilter(LokkitIntents.COMPLETE_TRANSACTION));
        receivers.add(completeTransactionReceiver);

        BroadcastReceiver discardTransactionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String id = intent.getExtras().getString(LokkitIntents.ID_EXTRA);
                String s1 = Statusgo.DiscardTransaction(id);
            }
        };
        registerReceiver(discardTransactionReceiver, new IntentFilter(LokkitIntents.DISCARD_TRANSACTION));
        receivers.add(discardTransactionReceiver);

        BroadcastReceiver loginReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String password = intent.getExtras().getString(LokkitIntents.PASSWORD_EXTRA);
                Intent response = new Intent();
                if (getMnemonic() == null
                        || getAddress() == null) {
                    createAndPersistAccount(password);
                }
                try {
                    login(password);
                    StatusService.this.password = password;
                    response.setAction(LokkitIntents.LOGIN_SUCCESSFUL);
                } catch (LokkitAuthenticationFailedException e) {
                    recoverAccount();
                }
                sendBroadcast(response);
            }
        };
        registerReceiver(loginReceiver, new IntentFilter(LokkitIntents.LOGIN));
        receivers.add(loginReceiver);

        System.loadLibrary("statusgoraw");
        System.loadLibrary("statusgo");
        this.web3 = new Web3Bridge();
        weakService = new WeakReference<>(this);
        Log.d(TAG, "Lokkit Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (BroadcastReceiver receiver : receivers) {
            unregisterReceiver(receiver);
        }

        stopNode();
        Log.d(TAG, "Lokkit Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private JSONObject getNodeConfig() {
        try {
            JSONObject jsonConfig = new JSONObject();
            jsonConfig.put("LogEnabled", true);
            jsonConfig.put("LogFile", "geth.log");
            jsonConfig.put("LogLevel", "DEBUG");
            jsonConfig.put("Name", "lokkit");
            jsonConfig.put("DataDir", getNodeDataDir().getAbsolutePath());
            jsonConfig.put("NetworkId", 42);
            JSONObject lightEthConfig = jsonConfig.put("LightEthConfig", new JSONObject());
            lightEthConfig.put("Enabled", true);
            lightEthConfig.put("Genesis", lokkitGenesis);
            return jsonConfig;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private File getNodeDataDir() {
        return new File(this.getApplicationInfo().dataDir + "/ethereum/data");
    }

    public void startNode(String dir) throws LokkitException {
        if (nodeRunning) {
            return;
        }
        final File dataDirectory = getNodeDataDir();
        final File f = new File(dir);
        if (!f.exists()
                && !f.mkdirs()) {
            throw new LokkitException("Could not create datadir: " + dataDirectory);
        }
        Log.d(TAG, "Lokkit node starting in folder: " + dataDirectory.getAbsolutePath());

        String startResult = Statusgo.StartNode(getNodeConfig().toString());
        Log.d(TAG, startResult);
        for (String peer : peers) {
            Log.d(TAG, Statusgo.AddPeer(peer));
        }
        Log.d(TAG, Statusgo.StartNodeRPCServer());

        if (getMnemonic() == null
                || getAddress() == null) {
            requireAccount();
        } else {
            recoverAccount();
        }
        nodeRunning = true;
        Log.d(TAG, "Lokkit node started");
    }

    /**
     * Requires a new account to be created.
     */
    private void requireAccount() {
        sendBroadcast(new Intent(LokkitIntents.REQUIRE_ACCOUNT));
    }

    /**
     * Recovers the existing account.
     */
    private void recoverAccount() {
        Intent intent = new Intent(LokkitIntents.RECOVER_ACCOUNT);
        intent.putExtra(LokkitIntents.ADDRESS_EXTRA, getAddress());
        intent.putExtra(LokkitIntents.MNEMONIC_EXTRA, getMnemonic());
        sendBroadcast(intent);
    }

    private String createAndPersistAccount(String password) {
        String s = Statusgo.CreateAccount(password);
        try {
            JSONObject o = new JSONObject(s);
            if (o.has("error") && o.getString("error").equals("")) {
                setMnemonic(o.getString("mnemonic"));
                setAddress(o.getString("address"));
                return getMnemonic();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void login(String password) throws LokkitAuthenticationFailedException {
        if (getMnemonic() != null
                && getAddress() != null) {
            String response = Statusgo.Login(getAddress(), password);
            try {
                JSONObject o = new JSONObject(response);
                if (o.has("error")
                        && !o.getString("error").equals("")) {
                    throw new LokkitAuthenticationFailedException(o.getString("error"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopNode() {
        Log.d(TAG, "Lokkit node stopping");
        Log.d(TAG, Statusgo.StopNodeRPCServer());
        Log.d(TAG, Statusgo.StopNode());
        nodeRunning = false;
        Log.d(TAG, "Lokkit node stopped");
    }

    /**
     * This hook is called by the Statusgo native library.
     *
     * @param jsonEvent
     */
    public static void signalEvent(String jsonEvent) {
        Log.d(TAG, "Signal event: " + jsonEvent);
        try {
            JSONObject jsonObject = new JSONObject(jsonEvent);
            switch (jsonObject.getString("type")) {
                case "transaction.queued":
                    StatusService service = weakService.get();
                    if (service.password == null
                            || service.password.equals("")) {
                        service.requireAccount(); // todo: wait for response and unlock transaction
                    } else {
                        Statusgo.CompleteTransaction(jsonObject.getJSONObject("event").getString("id"), service.password);
                        Intent intent = new Intent(LokkitIntents.COMPLETE_TRANSACTION);
                        intent.putExtra(LokkitIntents.ID_EXTRA, jsonObject.getJSONObject("event").getString("id"));


                        /*Intent intent = new Intent(LokkitIntents.TRANSACTION_QUEUED);
                        JSONObject args = jsonObject.getJSONObject("event").getJSONObject("args");
                        intent.putExtra(LokkitIntents.FROM_EXTRA, args.getString("from"));
                        intent.putExtra(LokkitIntents.TO_EXTRA, args.getString("to"));
                        BigInteger value = new BigInteger(args.getString("value").substring(2), 16);
                        intent.putExtra(LokkitIntents.VALUE_EXTRA, value);
                        intent.putExtra(LokkitIntents.GAS_EXTRA, args.getString("gas"));
                        intent.putExtra(LokkitIntents.GASPRICE_EXTRA, args.getString("gasPrice"));
                        intent.putExtra(LokkitIntents.ID_EXTRA, jsonObject.getJSONObject("event").getString("id"));
                        weakService.get().sendBroadcast(intent);*/
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public StatusService getService() {
            return StatusService.this;
        }
    }
}

package io.lokkit;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

import im.status.ethereum.module.StatusService;

public class LokkitActivity extends AppCompatActivity {

    private static final String TAG = "LokkitActivity";
    protected final LokkitServiceConnection lokkitServiceConnection = new LokkitServiceConnection(this);
    private final Uri lokkitWebAppUri = Uri.parse("http://192.168.43.166:8080"); //todo: http://lokkit.io
    private ProgressDialog dialog;
    private int lokkitRunningStickyNotificationId = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean(LokkitIntents.STARTED_FROM_STICKY_NOTIFICATION)) {
            // return; // shortcircuit in case the activity was started from the sticky notification icon
            // todo: maybe restore ui?
        }

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LokkitActivity.this.finish();
            }
        }, new IntentFilter(LokkitIntents.STOP_LOKKIT));

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                Intent intent = new Intent(LokkitActivity.this, TransactionConfirmationActivity.class);
                intent.putExtra(LokkitIntents.FROM_EXTRA, received.getExtras().getString(LokkitIntents.FROM_EXTRA));
                intent.putExtra(LokkitIntents.TO_EXTRA, received.getExtras().getString(LokkitIntents.TO_EXTRA));
                intent.putExtra(LokkitIntents.VALUE_EXTRA, received.getExtras().getString(LokkitIntents.VALUE_EXTRA));
                intent.putExtra(LokkitIntents.ID_EXTRA, received.getExtras().getString(LokkitIntents.ID_EXTRA));
                startActivity(intent);
            }
        }, new IntentFilter(LokkitIntents.TRANSACTION_QUEUED));

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                LokkitActivity.this.dialog.setTitle("create account");
                LokkitActivity.this.dialog.show();
                final EditText input = new EditText(LokkitActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                AlertDialog a = new AlertDialog.Builder(LokkitActivity.this)
                        .setTitle("lokkit account")
                        .setIcon(R.drawable.ic_lokkit)
                        .setView(input)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendLoginIntent(input.getText().toString());
                            }
                        })
                        .setCancelable(false)
                        .setMessage("No account found. Please specify a password. If this dialog is cancelled, a empty password will be used.")
                        .show();
            }

            private void sendLoginIntent(String password) {
                Intent intent = new Intent(LokkitIntents.LOGIN);
                intent.putExtra(LokkitIntents.PASSWORD_EXTRA, password);
                sendBroadcast(intent);
            }
        }, new IntentFilter(LokkitIntents.REQUIRE_ACCOUNT));

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                LokkitActivity.this.dialog.setTitle("recover account");
                LokkitActivity.this.dialog.show();
                final EditText input = new EditText(LokkitActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                String address = received.getExtras().getString(LokkitIntents.ADDRESS_EXTRA);
                String mnemonic = received.getExtras().getString(LokkitIntents.MNEMONIC_EXTRA);
                AlertDialog a = new AlertDialog.Builder(LokkitActivity.this)
                        .setTitle("lokkit account")
                        .setIcon(R.drawable.ic_lokkit)
                        .setView(input)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendLoginIntent(input.getText().toString());
                            }
                        })
                        .setCancelable(false)
                        .setMessage("Found account: " + mnemonic.substring(0, 15) + "...\n\nPlease provide the password.")
                        .show();
            }

            private void sendLoginIntent(String password) {
                Intent intent = new Intent(LokkitIntents.LOGIN);
                intent.putExtra(LokkitIntents.PASSWORD_EXTRA, password);
                sendBroadcast(intent);
            }
        }, new IntentFilter(LokkitIntents.RECOVER_ACCOUNT));


        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                tryReloadWebapp();
            }
        }, new IntentFilter(LokkitIntents.LOGIN_SUCCESSFUL));

        setContentView(R.layout.activity_lokkit_splash_screen);
        Log.d(TAG, "Lokkit Activity created");
        dialog = ProgressDialog.show(this, "starting node", "trust no one, just lokkit", true, false);
        dialog.setIcon(R.drawable.ic_lokkit);
        lokkitServiceConnection.setServiceBoundEvent(new ServiceBoundEvent() {
            @Override
            public void serviceBound() {
                makeStickyNotification();
                dialog.dismiss();
            }

            @Override
            public void serviceUnbound() {
                LokkitActivity.this.finish();
            }

        });
        final Intent intent = new Intent(this, StatusService.class);
        bindService(intent, this.lokkitServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this.lokkitServiceConnection);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(lokkitRunningStickyNotificationId);
        notificationManager.cancelAll();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        boolean wentBack = false;
        try {
            final WebView webView = (WebView) findViewById(R.id.lokkitWebView);
            if (webView.canGoBack()) {
                webView.goBack();
                wentBack = true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (!wentBack) {
            moveTaskToBack(true);
        }
    }

    /**
     * Inspired by: https://singztechmusings.wordpress.com/2011/05/26/java-how-to-check-if-a-web-page-exists-and-is-available/
     *
     * @param timeout
     * @throws LokkitException
     */
    public void tryLokkitConnection(int timeout) throws LokkitUnavailableException {
        try {
            final HttpURLConnection httpUrlConn = (HttpURLConnection) new URL(lokkitWebAppUri.toString()).openConnection();
            httpUrlConn.setRequestMethod("GET"); // todo: make VueJS support HEAD requests for checking availability.
            httpUrlConn.setConnectTimeout(timeout);
            httpUrlConn.setReadTimeout(timeout);
            final int responseCode = httpUrlConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new LokkitUnavailableException("HTTP response code is not " + HttpURLConnection.HTTP_OK + " but is " + responseCode + ".");
            }
        } catch (Exception e) {
            throw new LokkitUnavailableException(e.getMessage(), e);
        }
    }

    private void configureLokkitWebView(WebView webView) {
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(LokkitActivity.this, "Error: " + description, Toast.LENGTH_LONG).show();
            }
        });
        webView.loadUrl(lokkitWebAppUri.toString());
    }

    private void tryReloadWebapp() {
        dialog.setTitle("connecting to lokkit");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag = false;
                try {
                    tryLokkitConnection(3000);
                    flag = true;
                } catch (LokkitUnavailableException e) {
                    Log.e(TAG, "Cannot connect to lokkit: " + e.getMessage());
                    flag = false;
                }
                final boolean connectedToLokkit = flag;
                new Handler(LokkitActivity.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (connectedToLokkit) {
                            setContentView(R.layout.activity_lokkit_webapp);
                            WebView webView = (WebView) findViewById(R.id.lokkitWebView);
                            configureLokkitWebView(webView);
                        } else {
                            setContentView(R.layout.activity_lokkit_splash_screen);
                            TextView statusBox = ((TextView) findViewById(R.id.statusBox));
                            statusBox.setText("Cannot connect to lokkit. Please connect to the lokkit WIFI and try again by clicking the lokk.");
                        }
                    }
                });
                dialog.dismiss();
            }
        }).start();
    }

    public void splashScreenClicked(View view) {
        tryReloadWebapp();
    }

    private void makeStickyNotification() {
        Intent intent = new Intent(this, LokkitActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopNodeIntent = new Intent(LokkitIntents.STOP_LOKKIT);
        PendingIntent stopNodePendingIntent = PendingIntent.getBroadcast(this, 0, stopNodeIntent, 0);
        Notification.Action.Builder stopNodeAction = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_stop_icon), "stop node", stopNodePendingIntent);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("lokkit node is running")
                .setContentText("Please visit the lokkit booth for more information!")
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_lokkit)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lokkit))
                .addAction(stopNodeAction.build());

        Notification n = builder.build();
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        n.color = getResources().getColor(R.color.colorAccent, null);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(lokkitRunningStickyNotificationId, n);
    }
}
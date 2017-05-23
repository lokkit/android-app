package io.lokkit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

import im.status.ethereum.module.StatusService;

public class LokkitActivity extends AppCompatActivity {

    private static final String TAG = "LokkitActivity";
    private boolean lokkitServiceBound = false;
    protected final LokkitServiceConnection lokkitServiceConnection = new LokkitServiceConnection(this);
    private final Uri lokkitWebAppUri = Uri.parse("http://webqr.com"); //"http://192.168.43.166:8080"
    private boolean connectedToLokkit = false;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "intenteeee");
            }
        }, new IntentFilter(LokkitIntents.transactionQueuedAction));

        setContentView(R.layout.activity_lokkit_splash_screen);
        Log.d(TAG, "Lokkit Activity created");
        dialog = ProgressDialog.show(this, "starting node", "trust no one, just lokkit", true, false);
        final TextView statusBox = (TextView) findViewById(R.id.statusBox);
        statusBox.setText("starting node");
        lokkitServiceConnection.setServiceBoundEvent(new ServiceBoundEvent() {
            @Override
            public void serviceBound() {
                makeNotification(LokkitActivity.this);
                LokkitActivity.this.lokkitServiceBound = true;
                statusBox.setText("Connecting to lokkit...");
                lokkitServiceConnection.getLokkitService().setTransactionHandler(new TransactionHandler() {
                    @Override
                    public void handleTransaction(String id, String from, String to, String value) {
                        Intent intent = new Intent(LokkitActivity.this, TransactionConfirmationActivity.class);
                        intent.putExtra(LokkitIntents.fromExtra, from);
                        intent.putExtra(LokkitIntents.toExtra, to);
                        intent.putExtra(LokkitIntents.valueExtra, value);
                        intent.putExtra(LokkitIntents.idExtra, id);
                        LokkitActivity.this.startActivity(intent);
                    }
                });
                tryReloadWebapp();
            }

            @Override
            public void serviceUnbound() {
                LokkitActivity.this.lokkitServiceBound = false;
                LokkitActivity.this.connectedToLokkit = false;
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
        if (this.lokkitServiceBound) {
            unbindService(this.lokkitServiceConnection);
            this.lokkitServiceBound = false;
        }
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
            httpUrlConn.setRequestMethod("GET"); // todo: make VUEJS support HEAD requests for checking availability.
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
                boolean flag;
                try {
                    tryLokkitConnection(3000);
                    flag = true;
                } catch (LokkitUnavailableException e) {
                    Log.e(TAG, "Cannot connect to lokkit: " + e.getMessage());
                    flag = false;
                }

                final boolean lokkitConnected = flag;
                new Handler(LokkitActivity.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (lokkitConnected) {
                            setContentView(R.layout.activity_lokkit_webapp);
                            WebView webView = (WebView) findViewById(R.id.lokkitWebView);
                            configureLokkitWebView(webView);
                        } else {
                            setContentView(R.layout.activity_lokkit_splash_screen);
                            final TextView statusBox = (TextView) findViewById(R.id.statusBox);
                            statusBox.setText("Cannot connect to lokkit. Please connect to the lokkit chain and try again by clicking the lokk.");
                        }
                        connectedToLokkit = lokkitConnected;
                    }
                });
                dialog.dismiss();
            }
        }).start();
    }

    public void splashScreenClicked(View view) {
        tryReloadWebapp();
    }

    public void testButtonDings(View view) {
        StatusService.signalEvent("{\"type\":\"transaction.queued\",\"event\":{\"id\":\"12\"}}");
    }

    private void makeNotification(Context context) {
        Intent intent = new Intent(context, LokkitActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                42, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("lokkit node is running")
                .setContentText("Please visit the lokkit booth for more information!")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_lokkit)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lokkit));
        Notification n = builder.build();
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(42, n);
    }
}
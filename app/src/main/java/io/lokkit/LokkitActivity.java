package io.lokkit;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Browser;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LokkitActivity extends AppCompatActivity {

    private static final String TAG = "LokkitActivity";
    private ServiceConnection lokkitServiceConnection;
    private final Uri lokkitWebAppUri = Uri.parse("https://webapp.lokkit.io");
    private ProgressDialog dialog;
    private int lokkitRunningStickyNotificationId = 42;
    private List<BroadcastReceiver> receivers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trustAllHosts();

        BroadcastReceiver stopLokkitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LokkitActivity.this.cleanup();
                LokkitActivity.this.finish();
            }
        };
        registerReceiver(stopLokkitReceiver, new IntentFilter(LokkitIntents.STOP_LOKKIT));
        receivers.add(stopLokkitReceiver);

        BroadcastReceiver transactionQueuedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                Intent intent = new Intent(LokkitActivity.this, TransactionConfirmationActivity.class);
                intent.putExtra(LokkitIntents.FROM_EXTRA, received.getExtras().getString(LokkitIntents.FROM_EXTRA));
                intent.putExtra(LokkitIntents.TO_EXTRA, received.getExtras().getString(LokkitIntents.TO_EXTRA));
                intent.putExtra(LokkitIntents.VALUE_EXTRA, (BigInteger) received.getExtras().get(LokkitIntents.VALUE_EXTRA));
                intent.putExtra(LokkitIntents.ID_EXTRA, received.getExtras().getString(LokkitIntents.ID_EXTRA));
                startActivity(intent);
            }
        };
        registerReceiver(transactionQueuedReceiver, new IntentFilter(LokkitIntents.TRANSACTION_QUEUED));
        receivers.add(transactionQueuedReceiver);

        BroadcastReceiver requireAccountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                LokkitActivity.this.dialog.setTitle("creating account");
                LokkitActivity.this.dialog.show();
                final EditText input = new EditText(LokkitActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                AlertDialog a = new AlertDialog.Builder(LokkitActivity.this)
                        .setTitle("new account")
                        .setIcon(R.drawable.ic_lokkit)
                        .setView(input)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendLoginIntent(input.getText().toString());
                            }
                        })
                        .setCancelable(false)
                        .setMessage("No account found. Please specify a password.")
                        .show();
            }

            private void sendLoginIntent(String password) {
                Intent intent = new Intent(LokkitIntents.LOGIN);
                intent.putExtra(LokkitIntents.PASSWORD_EXTRA, password);
                sendBroadcast(intent);
            }
        };
        registerReceiver(requireAccountReceiver, new IntentFilter(LokkitIntents.REQUIRE_ACCOUNT));
        receivers.add(requireAccountReceiver);

        BroadcastReceiver recoverAccountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                LokkitActivity.this.dialog.setTitle("recover account");
                LokkitActivity.this.dialog.show();
                final EditText input = new EditText(LokkitActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                String address = received.getExtras().getString(LokkitIntents.ADDRESS_EXTRA);
                String mnemonic = received.getExtras().getString(LokkitIntents.MNEMONIC_EXTRA);
                AlertDialog a = new AlertDialog.Builder(LokkitActivity.this)
                        .setTitle("unlock account")
                        .setIcon(R.drawable.ic_lokkit)
                        .setView(input)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendLoginIntent(input.getText().toString());
                            }
                        })
                        .setCancelable(false)
                        .setMessage("\nEnter Password for: " + mnemonic.substring(0, 15) + "...")
                        .show();
            }

            private void sendLoginIntent(String password) {
                Intent intent = new Intent(LokkitIntents.LOGIN);
                intent.putExtra(LokkitIntents.PASSWORD_EXTRA, password);
                sendBroadcast(intent);
            }
        };
        registerReceiver(recoverAccountReceiver, new IntentFilter(LokkitIntents.RECOVER_ACCOUNT));
        receivers.add(recoverAccountReceiver);

        BroadcastReceiver loginSuccessfulReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent received) {
                tryReloadWebapp();
            }
        };
        registerReceiver(loginSuccessfulReceiver, new IntentFilter(LokkitIntents.LOGIN_SUCCESSFUL));
        receivers.add(loginSuccessfulReceiver);

        setContentView(R.layout.activity_lokkit_splash_screen);
        Log.d(TAG, "Lokkit Activity created");
        dialog = ProgressDialog.show(this, "starting node", "trust no one, just lokkit", true, false);
        dialog.setIcon(R.drawable.ic_lokkit);
        lokkitServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                LokkitService.LocalBinder b = (LokkitService.LocalBinder) binder;
                LokkitService lokkitService = b.getService();
                try {
                    lokkitService.startNode(LokkitActivity.this.getApplicationInfo().dataDir + "/ethereum/data/");
                    makeStickyNotification();
                    dialog.dismiss();
                } catch (LokkitException e) {
                    Log.e(TAG, e.getMessage());
                }
                Log.d(TAG, "onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                LokkitActivity.this.cleanup();
                LokkitActivity.this.finish();
                Log.d(TAG, "onServiceDisconnected");
            }
        };
        final Intent intent = new Intent(this, LokkitService.class);
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
        cleanup();
    }

    private void cleanup() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(lokkitRunningStickyNotificationId);
            notificationManager.cancelAll();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        for (BroadcastReceiver receiver : this.receivers) {
            unregisterReceiver(receiver);
        }
        receivers.clear();
        try {
            unbindService(this.lokkitServiceConnection);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
        settings.setDomStorageEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(LokkitActivity.this, "Error: " + description, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });
        webView.loadUrl(lokkitWebAppUri.toString());
    }

    private class LokkitHostVerifierTrustManager implements HostnameVerifier, X509TrustManager {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            for (X509Certificate cert : x509Certificates) {
                cert.checkValidity();
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            for (X509Certificate cert : x509Certificates) {
                cert.checkValidity();
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private void trustAllHosts() {
        try {
            LokkitHostVerifierTrustManager verifierTrustmanager = new LokkitHostVerifierTrustManager();
            HttpsURLConnection.setDefaultHostnameVerifier(verifierTrustmanager);
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] myTrustmangers = new TrustManager[1];
            myTrustmangers[0] = verifierTrustmanager;
            context.init(null, myTrustmangers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

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
                            //switchToWebsite();
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

    private void switchToWebsite() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(LokkitActivity.this.lokkitWebAppUri);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pi.send();
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void splashScreenClicked(View view) {
        tryReloadWebapp();
    }

    private void makeStickyNotification() {
        Intent intent = new Intent(this, LokkitActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Intent stopNodeIntent = new Intent(LokkitIntents.STOP_LOKKIT);
        PendingIntent stopNodePendingIntent = PendingIntent.getBroadcast(this, 0, stopNodeIntent, 0);


        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("lokkit node is running")
                .setContentText("Please visit the lokkit booth for more information!")
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_lokkit)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lokkit));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Notification.Action.Builder stopNodeAction = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_stop_icon), "stop lokkit", stopNodePendingIntent);
            builder = builder.addAction(stopNodeAction.build())
                    .setColor(getResources().getColor(R.color.colorAccent, null))
                    .setLights(getResources().getColor(R.color.colorAccent, null), 1000, 0);
        } else {
            builder.addAction(R.drawable.ic_stop_icon, "stop lokkit", stopNodePendingIntent);
        }
        Notification n = builder.build();

        n.flags |= Notification.FLAG_NO_CLEAR;
        n.defaults |= Notification.FLAG_SHOW_LIGHTS;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(lokkitRunningStickyNotificationId, n);
    }
}
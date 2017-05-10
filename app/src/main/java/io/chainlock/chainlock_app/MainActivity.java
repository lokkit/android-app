package io.chainlock.chainlock_app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.status_im.status_go.cmd.GoInterface;
import com.github.status_im.status_go.cmd.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Web3Bridge web3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        System.loadLibrary("statusgoraw");
        System.loadLibrary("statusgo");

        doStartNode();
        web3 = new Web3Bridge();
        Statusgo.StartNodeRPCServer();

        Toast.makeText(this, "started zeugs", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void doStartNode() {
        final String TAG = "StatusZeug";
        Activity currentActivity = this;

        String andisGenesis = "{\n" +
                "  \"config\": {\n" +
                "    \"chainId\": 42,\n" +
                "    \"homesteadBlock\": 0,\n" +
                "    \"eip155Block\": 0,\n" +
                "    \"eip158Block\": 0\n" +
                "  },\n" +
                "  \"difficulty\" : \"0x20000\",\n" +
                "  \"gasLimit\"   : \"0x80000000\",\n" +
                "  \"alloc\": {\n" +
                "    \"0xe0a83a8b5ba5c9acc140f89296187f96a163cf43\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0x677c9e0a30ba472eec4ea0f4ed6dcfb1c51d6bf1\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0xa26efbc2634c81900b3d2f604e6b427dfe6e1764\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0xaf75fcb29d58549b9c451a52a64e9020a66bdf6e\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0x9fffb27287898a20857531d7aae0942184e7d56e\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0x183d9685e49367c07dc63f0938d112a74945e411\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0x57f5d12a63025e819bb51e973be075717d923c15\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0xf55fb78f02ac5ecc9333b35b4287609140690517\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0xb5ede4a54dddec0fc345b5dc11d9db077015d686\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    },\n" +
                "    \"0x179972bea45078eac67ac60c8de2257e6af33e27\": {\n" +
                "      \"balance\": \"20000000000000000000\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        String root = currentActivity.getApplicationInfo().dataDir;
        String dataFolder = root + "/ethereum/lokkit5";
        Log.d(TAG, "Starting Geth node in folder: " + dataFolder);

        try {
            final File newFile = new File(dataFolder);

            // todo handle error?
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        /*final String lokkitFlag = root + "/lokkit_flag";
        final File ropstenFlag = new File(lokkitFlag);
        if (!ropstenFlag.exists()) {
            try {*/
        final String chaindDataFolderPath = dataFolder + "/lightchaindata";
        final File lightChainFolder = new File(chaindDataFolderPath);
        if (lightChainFolder.isDirectory()) {
            String[] children = lightChainFolder.list();
            for (String child : children) {
                new File(lightChainFolder, child).delete();
            }
        }
        lightChainFolder.delete();
                /*ropstenFlag.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        String config;
        String defaultConfig = "";

        if (false)
            try {
                defaultConfig = Statusgo.GenerateConfig(dataFolder, 42);

                JSONObject jsonConfig = new JSONObject(defaultConfig);
                jsonConfig.put("LogEnabled", true);
                jsonConfig.put("LogFile", "geth.log");
                jsonConfig.put("LogLevel", "DEBUG");
                jsonConfig.put("Name", "lokkit");


                JSONObject lightEthConfig = jsonConfig.getJSONObject("LightEthConfig");
                lightEthConfig.put("Enabled", true);
                lightEthConfig.put("Genesis", andisGenesis);

                JSONObject chainConfig = new JSONObject();
                chainConfig.put("ChainId", 42);
                chainConfig.put("HomesteadBlock", 0);
                chainConfig.put("EIP155Block", 0);
                chainConfig.put("EIP158Block", 0);
                jsonConfig.put("ChainConfig", chainConfig);

                config = jsonConfig.toString();
            } catch (JSONException e) {
                Log.d(TAG, "Something went wrong " + e.getMessage());
                Log.d(TAG, "Default configuration will be used");

                config = defaultConfig;
            }

        JSONObject j = new JSONObject();
        try {
            j.put("DataDir", dataFolder);
            j.put("NetworkId", 42);
            JSONObject lightEthConfig = j.put("LightEthConfig", new JSONObject());
            lightEthConfig.put("Enabled", true);
            lightEthConfig.put("Genesis", andisGenesis);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String s = Statusgo.StartNode(j.toString());
        String peer2 = Statusgo.AddPeer("enode://288b97262895b1c7ec61cf314c2e2004407d0a5dc77566877aad1f2a36659c8b698f4b56fd06c4a0c0bf007b4cfb3e7122d907da3b005fa90e724441902eb19e@192.168.43.166:30303");

        Log.d(TAG, "Geth node started");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void doSomething(View view) throws Exception {
        TextView logBox = ((TextView) findViewById(R.id.textbox));
        EditText addr = ((EditText) findViewById(R.id.addressBox));
        EditText pswd = ((EditText) findViewById(R.id.passwordBox));

        Intent i = new Intent(this, ChainlockService.class);
        i.putExtra("data", "start");
        startService(i);
    }

    public void onBtnClicked(View view) throws Exception {
        TextView logBox = ((TextView) findViewById(R.id.textbox));
        EditText addr = ((EditText) findViewById(R.id.addressBox));
        EditText pswd = ((EditText) findViewById(R.id.passwordBox));

        String adi = String.valueOf(addr.getText());
        String pw = String.valueOf(pswd.getText());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String logon = Statusgo.Login("2cedf28382dce01f62b1aceab2f031424c363a11", "hirzel");
                String response = web3.sendRequest("{\"method\":\"eth_accounts\",\"id\":0}");
                String r = response.toString();
                web3.sendRequest("{method:eth_sendTransaction, params:[]}");
            }
        }).start();

    }
}
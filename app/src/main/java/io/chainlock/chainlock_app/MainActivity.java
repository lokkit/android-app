package io.chainlock.chainlock_app;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Address;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Header;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Subscription;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static String enode = "enode://15f05de0e9b6b29f4c81c91f3910515ae68e43ef10ac480fe29804aee060fe9e5dddf9a0f1217cd1976aa96365f10b3c3a63c47a13e6be72b58577227a1b72ff@192.168.43.166:30303";
    private static String genesis = "{    \"nonce\": \"0x0000000000001244234\",    \"timestamp\": \"0x0\",    \"parentHash\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",    \"extraData\": \"0x0\",    \"gasLimit\": \"0x800000000000000000\",    \"difficulty\": \"0x64\",    \"mixhash\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",    \"coinbase\": \"0x3333333333333333333333333333333333333333\",    \"alloc\": {\t\t\"0x86d2ca00492fec79ffaa83fb1cde776678cc522d\": { \"balance\": \"2000000000000000000000000\" }     }}";
    private static String abi = "[{\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'currentRenter',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'address'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'location',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'string'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [{\n" +
            "        name: 'time',\n" +
            "        type: 'uint256'\n" +
            "    }],\n" +
            "    name: 'occupiedAt',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'bool'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'description',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'string'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: false,\n" +
            "    inputs: [{\n" +
            "        name: 'start',\n" +
            "        type: 'uint256'\n" +
            "    }, {\n" +
            "        name: 'end',\n" +
            "        type: 'uint256'\n" +
            "    }],\n" +
            "    name: 'rent',\n" +
            "    outputs: [],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'owner',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'address'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'locked',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'bool'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [{\n" +
            "        name: 'start',\n" +
            "        type: 'uint256'\n" +
            "    }, {\n" +
            "        name: 'end',\n" +
            "        type: 'uint256'\n" +
            "    }],\n" +
            "    name: 'occupiedBetween',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'bool'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'allReservations',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'uint256[3][]'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: true,\n" +
            "    inputs: [],\n" +
            "    name: 'pricePerTime',\n" +
            "    outputs: [{\n" +
            "        name: '',\n" +
            "        type: 'uint256'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    constant: false,\n" +
            "    inputs: [{\n" +
            "        name: 'newOwner',\n" +
            "        type: 'address'\n" +
            "    }],\n" +
            "    name: 'transferOwnership',\n" +
            "    outputs: [],\n" +
            "    payable: false,\n" +
            "    type: 'function'\n" +
            "}, {\n" +
            "    inputs: [{\n" +
            "        name: 'pdescription',\n" +
            "        type: 'string'\n" +
            "    }, {\n" +
            "        name: 'plocation',\n" +
            "        type: 'string'\n" +
            "    }, {\n" +
            "        name: 'ppricePerTime',\n" +
            "        type: 'uint256'\n" +
            "    }, {\n" +
            "        name: 'deposit',\n" +
            "        type: 'uint256'\n" +
            "    }],\n" +
            "    payable: false,\n" +
            "    type: 'constructor'\n" +
            "}, {\n" +
            "    anonymous: false,\n" +
            "    inputs: [{\n" +
            "        indexed: false,\n" +
            "        name: 'start',\n" +
            "        type: 'uint256'\n" +
            "    }, {\n" +
            "        indexed: false,\n" +
            "        name: 'end',\n" +
            "        type: 'uint256'\n" +
            "    }, {\n" +
            "        indexed: false,\n" +
            "        name: 'renter',\n" +
            "        type: 'address'\n" +
            "    }],\n" +
            "    name: 'OnReserve',\n" +
            "    type: 'event'\n" +
            "}]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**/
        KeyStore keyStore = Geth.newKeyStore(this.getFilesDir().getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
        Account myAccount;
        try {
            myAccount = keyStore.getAccounts().get(0);
            TextView logBox = ((TextView) findViewById(R.id.textbox));
            EditText addr = ((EditText) findViewById(R.id.addressBox));
            logBox.setText(logBox.getText() + "\nFound account 0: " + myAccount.getAddress().getHex());
            addr.setText(myAccount.getAddress().getHex());
        } catch (Exception e1) {
            Toast.makeText(this.getApplicationContext(), "Could not find any account. Please create a new one!", Toast.LENGTH_LONG).show();
        }
    }

    private Account getAccount(KeyStore keyStore, Address address) {
        Accounts as = keyStore.getAccounts();
        for (long i = 0; i < as.size(); i++) {
            try {
                Account acc = as.get(i);
                if (acc.getAddress().getHex().equals(address.getHex())) {
                    return acc;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Address newLocalAccount(String pw) {
        KeyStore keyStore = Geth.newKeyStore(this.getFilesDir().getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
        try {
            return keyStore.newAccount(pw).getAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

        KeyStore keyStore = Geth.newKeyStore(this.getFilesDir().getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
        Account acc;
        try {
            if (!adi.equals("")) {
                acc = getAccount(keyStore, new Address(adi));
                keyStore.unlock(acc, pw);
                Toast.makeText(this, "acc unlocked!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e1) {
            if (!pw.equals("")) {
                Address newAdi = newLocalAccount(pw);
                addr.setText(newAdi.getHex());
                Account newAcc = getAccount(keyStore, newAdi);
                String t = new String(keyStore.exportKey(newAcc, pw, pw));
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("new account", t);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "copied new identity to clipboard", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "please enter pw to unlock account.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
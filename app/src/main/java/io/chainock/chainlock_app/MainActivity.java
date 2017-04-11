package io.chainock.chainlock_app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Block;
import org.ethereum.geth.BoundContract;
import org.ethereum.geth.CallMsg;
import org.ethereum.geth.CallOpts;
import org.ethereum.geth.ChainConfig;
import org.ethereum.geth.Context;
import org.ethereum.geth.Enode;
import org.ethereum.geth.Enodes;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.AccountManager;
import org.ethereum.geth.Hash;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.PeerInfos;
import org.ethereum.geth.Subscription;
import org.ethereum.geth.SyncProgress;
import org.ethereum.geth.Transaction;

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

        TextView box = ((TextView) findViewById(R.id.textbox));
        /**/
        try {
            Address accountAddress = new Address("0x61478d52c136d357f9a262ba594e7fd7e290ac2f");
            String accountPassphrase = "hirzel";
            AccountManager am = Geth.newAccountManager(this.getFilesDir().getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
            Account a = getAccount(am, accountAddress);
            if (a == null) {
                a = am.newAccount(accountPassphrase);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String t = new String(am.exportKey(a, "hirzel", "hirzel"));
                ClipData clip = ClipData.newPlainText("bc identity", t);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "copied identity to clipboard", Toast.LENGTH_SHORT).show();
            }

            NodeConfig conf = Geth.newNodeConfig();
            Enode enode = Geth.newEnode(MainActivity.enode);
            Enodes enodes = Geth.newEnodes(1);
            enodes.set(0, enode);
            conf.setBootstrapNodes(enodes);
            conf.setEthereumGenesis(genesis);
            conf.setEthereumNetworkID(3034);
            conf.setWhisperEnabled(true);
            conf.setEthereumEnabled(true);
            conf.setMaxPeers(5);
            Node node = Geth.newNode(getFilesDir() + "/.ethereum", conf);
            node.start();
            Thread.sleep(4000);

            //BigInt balanceAt = node.getEthereumClient().getBalanceAt(Geth.newContext(), Geth.newAddressFromHex("0x86d2ca00492fec79ffaa83fb1cde776678cc522d"), -1);
            box.setText(box.getText()+"\nenode: " + node.getNodeInfo().getEnode());

            EthereumClient ethereumClient = node.getEthereumClient();
            Block block = ethereumClient.getBlockByNumber(Geth.newContext(), -1);
            box.setText(box.getText() + "\n" + block.getNumber());


            /*EthereumClient ethereumClient = Geth.newEthereumClient("http://192.168.43.166:8545");
            Toast.makeText(this, "le connection éxiste!", Toast.LENGTH_LONG).show();

            Block block = ethereumClient.getBlockByNumber(Geth.newContext(), -1);
            box.setText(box.getText() + "\n" + block.getNumber());

            BoundContract locker = Geth.bindContract(new Address("0xf3776738429681a0ebf0158078e9ea11827b5dcf"), abi, ethereumClient);
            CallMsg m = Geth.newCallMsg();
            m.setFrom(a.getAddress());
            m.setTo(locker.getAddress());
            m.setData("owner".getBytes());
            byte[] result = ethereumClient.callContract(Geth.newContext(), m, 0);
            Toast.makeText(this, new String(result), Toast.LENGTH_SHORT).show();*/

            //locker.call(Geth.newCallOpts(),Geth.newInterfaces(0),"owner", Geth.newInterfaces(0));


        } catch (Exception e) {
            e.printStackTrace();
            box.setText(e.getMessage());
        }
    }

    private Account getAccount(AccountManager am, Address address) {
        Accounts as = am.getAccounts();
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

    private String newLocalAccount(String pw) {
        AccountManager am = Geth.newAccountManager(this.getFilesDir().getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
        Account newAcc = null;
        try {
            newAcc = am.newAccount(pw);
            return newAcc.getAddress().getHex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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
}
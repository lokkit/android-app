package io.chainlock.chainlock_app;

import android.app.IntentService;
import android.content.Intent;
import android.icu.util.ChineseCalendar;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.ethereum.geth.Address;
import org.ethereum.geth.BoundContract;
import org.ethereum.geth.CallMsg;
import org.ethereum.geth.CallOpts;
import org.ethereum.geth.ChainConfig;
import org.ethereum.geth.Context;
import org.ethereum.geth.Enode;
import org.ethereum.geth.Enodes;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Header;
import org.ethereum.geth.Interface;
import org.ethereum.geth.Interfaces;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.PeerInfo;
import org.ethereum.geth.Subscription;
import org.ethereum.geth.TransactOpts;
import org.ethereum.geth.Transaction;

/**
 * Created by Nick on 16.04.2017.
 */

public class ChainlockService extends IntentService {

    private static String rentableAbi = "[{    constant: true,    inputs: [],    name: \"currentRenter\",    outputs: [{        name: \"\",        type: \"address\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [],    name: \"location\",    outputs: [{        name: \"\",        type: \"string\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [{        name: \"time\",        type: \"uint256\"    }],    name: \"occupiedAt\",    outputs: [{        name: \"\",        type: \"bool\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [],    name: \"description\",    outputs: [{        name: \"\",        type: \"string\"    }],    payable: false,    type: \"function\"}, {    constant: false,    inputs: [{        name: \"start\",        type: \"uint256\"    }, {        name: \"end\",        type: \"uint256\"    }],    name: \"rent\",    outputs: [],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [],    name: \"owner\",    outputs: [{        name: \"\",        type: \"address\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [],    name: \"locked\",    outputs: [{        name: \"\",        type: \"bool\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [{        name: \"start\",        type: \"uint256\"    }, {        name: \"end\",        type: \"uint256\"    }],    name: \"occupiedBetween\",    outputs: [{        name: \"\",        type: \"bool\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [],    name: \"allReservations\",    outputs: [{        name: \"\",        type: \"uint256[3][]\"    }],    payable: false,    type: \"function\"}, {    constant: true,    inputs: [],    name: \"pricePerTime\",    outputs: [{        name: \"\",        type: \"uint256\"    }],    payable: false,    type: \"function\"}, {    constant: false,    inputs: [{        name: \"newOwner\",        type: \"address\"    }],    name: \"transferOwnership\",    outputs: [],    payable: false,    type: \"function\"}, {    inputs: [{        name: \"pdescription\",        type: \"string\"    }, {        name: \"plocation\",        type: \"string\"    }, {        name: \"ppricePerTime\",        type: \"uint256\"    }, {        name: \"deposit\",        type: \"uint256\"    }],    payable: false,    type: \"constructor\"}, {    anonymous: false,    inputs: [{        indexed: false,        name: \"start\",        type: \"uint256\"    }, {        indexed: false,        name: \"end\",        type: \"uint256\"    }, {        indexed: false,        name: \"renter\",        type: \"address\"    }],    name: \"OnReserve\",    type: \"event\"}]";
    private static String bootstrapEnode = "enode://288b97262895b1c7ec61cf314c2e2004407d0a5dc77566877aad1f2a36659c8b698f4b56fd06c4a0c0bf007b4cfb3e7122d907da3b005fa90e724441902eb19e@192.168.43.166:30303";
    private static String genesis = "{    \"nonce\": \"0x0000000000001244234\",    \"timestamp\": \"0x0\",    \"parentHash\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",    \"extraData\": \"0x0\",    \"gasLimit\": \"0x800000000000000000\",    \"difficulty\": \"0x64\",    \"mixhash\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",    \"coinbase\": \"0x03f92c229e49286420e70824d5f043ec26fb498d\",    \"alloc\": {\t\t\"0x03f92c229e49286420e70824d5f043ec26fb498d\": { \"balance\": \"2000000000000000000000000\" }     }}";
    private static Address testLocker = new Address("0x8f9ccd5ecb20d3d5dd82c30a8eae410d7ed04cd2");

    private static boolean nodeStarted = false;
    public static Node node;

    public ChainlockService() {
        super("ChainlockService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String s = intent.getStringExtra("data");
        if (s.equals("start") && !nodeStarted) {
            start();
        } else if (s.equals("stop") && nodeStarted) {
            stop();
        }


        try {
            Subscription subscription = node.getEthereumClient().subscribeNewHead(Geth.newContext(), new NewHeadHandler() {
                @Override
                public void onError(String s) {
                    Toast.makeText(ChainlockService.this, "ERROR: " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNewHead(Header header) {
                    Toast.makeText(ChainlockService.this, "HEAD: " + header.getHash().getHex(), Toast.LENGTH_SHORT).show();
                }
            }, 10);
            BoundContract locker = Geth.bindContract(testLocker, rentableAbi, node.getEthereumClient());
            Address address = locker.getAddress();
            Transaction deployer = locker.getDeployer();
            CallOpts co = Geth.newCallOpts();
            co.setContext(Geth.newContext());
            co.setPending(true);
            co.setGasLimit(10000000000l);
            Interface returnValue = Geth.newInterface();
            returnValue.setString("return value");
            Interfaces outputs = Geth.newInterfaces(1);
            outputs.set(0, returnValue);
            locker.call(co, outputs, "owner", Geth.newInterfaces(0));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void stop() {
    }

    private Enodes bootstrapEnodes() throws Exception {
        Enode enode = null;
        enode = Geth.newEnode(bootstrapEnode);
        Enodes enodes = Geth.newEnodes(1);
        enodes.set(0, enode);
        return enodes;
    }

    private void start() {
        if (nodeStarted) {
            return;
        }
        try {
            if (node != null) {
                node.stop();
                node = null;
            }
            NodeConfig conf = Geth.newNodeConfig();
            conf.setMaxPeers(5);
            conf.setBootstrapNodes(bootstrapEnodes());
            conf.setEthereumGenesis(genesis);
            conf.setEthereumNetworkID(3034);
            conf.setWhisperEnabled(true);
            conf.setEthereumEnabled(true);
            ChainConfig cc = Geth.newChainConfig();
            cc.setChainID(3034);
            conf.setEthereumChainConfig(cc);
            node = Geth.newNode(getFilesDir() + "/.ethereum", conf);
            node.start();
            nodeStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package io.chainlock.chainlock_app;

import android.app.IntentService;
import android.content.Intent;
import android.icu.util.ChineseCalendar;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by Nick on 16.04.2017.
 */

public class ChainlockService extends IntentService {

    private static String rentableAbi = "[{\"constant\":false,\"inputs\":[],\"name\":\"withdrawRefundedDepsoits\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"currentRenter\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"location\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"time\",\"type\":\"uint256\"}],\"name\":\"occupiedAt\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"description\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"start\",\"type\":\"uint256\"},{\"name\":\"end\",\"type\":\"uint256\"}],\"name\":\"rent\",\"outputs\":[],\"payable\":true,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"start\",\"type\":\"uint256\"},{\"name\":\"end\",\"type\":\"uint256\"},{\"name\":\"renter\",\"type\":\"address\"}],\"name\":\"refundReservationDeposit\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"costPerMinute\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"mins\",\"type\":\"uint256\"}],\"name\":\"rentNowForMinutes\",\"outputs\":[],\"payable\":true,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"deposit\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"start\",\"type\":\"uint256\"},{\"name\":\"end\",\"type\":\"uint256\"}],\"name\":\"costInWei\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"start\",\"type\":\"uint256\"},{\"name\":\"end\",\"type\":\"uint256\"}],\"name\":\"occupiedBetween\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"allReservations\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256[3][]\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"end\",\"type\":\"uint256\"}],\"name\":\"rentNowUntil\",\"outputs\":[],\"payable\":true,\"type\":\"function\"},{\"inputs\":[{\"name\":\"pdescription\",\"type\":\"string\"},{\"name\":\"plocation\",\"type\":\"string\"},{\"name\":\"pcostPerMinute\",\"type\":\"uint256\"},{\"name\":\"pdeposit\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"start\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"end\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"renter\",\"type\":\"address\"}],\"name\":\"OnReserve\",\"type\":\"event\"}]";
    private static String bootstrapEnode = "enode://288b97262895b1c7ec61cf314c2e2004407d0a5dc77566877aad1f2a36659c8b698f4b56fd06c4a0c0bf007b4cfb3e7122d907da3b005fa90e724441902eb19e@192.168.0.1:30303";
    private static String genesis = "{  \"config\": {    \"chainId\": 42,    \"homesteadBlock\": 0,    \"eip155Block\": 0,    \"eip158Block\": 0  },  \"difficulty\" : \"0x20000\",  \"gasLimit\"   : \"0x80000000\",  \"alloc\": {    \"0xe0a83a8b5ba5c9acc140f89296187f96a163cf43\": {      \"balance\": \"20000000000000000000\"    },    \"0x677c9e0a30ba472eec4ea0f4ed6dcfb1c51d6bf1\": {      \"balance\": \"20000000000000000000\"    },    \"0xa26efbc2634c81900b3d2f604e6b427dfe6e1764\": {      \"balance\": \"20000000000000000000\"    },    \"0xaf75fcb29d58549b9c451a52a64e9020a66bdf6e\": {      \"balance\": \"20000000000000000000\"    },    \"0x9fffb27287898a20857531d7aae0942184e7d56e\": {      \"balance\": \"20000000000000000000\"    },    \"0x183d9685e49367c07dc63f0938d112a74945e411\": {      \"balance\": \"20000000000000000000\"    },    \"0x57f5d12a63025e819bb51e973be075717d923c15\": {      \"balance\": \"20000000000000000000\"    },    \"0xf55fb78f02ac5ecc9333b35b4287609140690517\": {      \"balance\": \"20000000000000000000\"    },    \"0xb5ede4a54dddec0fc345b5dc11d9db077015d686\": {      \"balance\": \"20000000000000000000\"    },    \"0x179972bea45078eac67ac60c8de2257e6af33e27\": {      \"balance\": \"20000000000000000000\"    }  }}";
    //private static Address testLocker = new Address("0xf16801293f34fc16470729f4ac91185595aa6e10");

    private static boolean nodeStarted = false;
    //public static Node node;

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
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void stop() {
    }

    private void bootstrapEnodes() throws Exception {
        /*Enode enode = null;
        enode = Geth.newEnode(bootstrapEnode);
        Enodes enodes = Geth.newEnodes(1);
        enodes.set(0, enode);
        return enodes;*/
    }

    private void start() {
        /*if (nodeStarted) {
            return;
        }
        try {
            if (node != null) {
                try {
                    node.stop();
                }catch(Exception ignored){

                }
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
        }*/
    }
}

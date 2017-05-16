package io.lokkit;

import org.json.JSONObject;

/**
 * Created by Nick on 15.05.2017.
 */

@Deprecated
public class LokkitWhisper {
    private Web3Bridge web3;
    private String host;

    public LokkitWhisper(String host) {
        this.web3 = new Web3Bridge();
        this.host = host;
    }

    private String generateSymmetricKey() {
        return "";
    }

    public void sendCommand(String address, String command) {
        JSONObject j = new JSONObject();

        return;
    }

    public void unlock(String address) {

    }
}

package io.lokkit;

/**
 * Created by Nick on 23.05.2017.
 */

public interface TransactionHandler {

    void handleTransaction(String id, String from, String to, String amount);
}

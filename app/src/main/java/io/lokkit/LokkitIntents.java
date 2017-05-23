package io.lokkit;

/**
 * Created by Nick on 23.05.2017.
 */

public class LokkitIntents {
    public final static String transactionQueuedAction = "io.lokkit.TRANSACTION_QUEUED";
    public final static String discardTransactionAction = "io.lokkit.DISCARD_TRANSACTION";
    public final static String completeTransactionAction = "io.lokkit.COMPLETE_TRANSACTION";
    public final static String completeTransactionSuccessfulAction = "io.lokkit.COMPLETE_TRANSACTION_SUCCESSFUL";
    public final static String completeTransactionFailedAction = "io.lokkit.COMPLETE_TRANSACTION_FAILED";

    public final static int confirmTransactionCode = 1337;

    public final static String idExtra = "id";
    public final static String fromExtra = "from";
    public final static String toExtra = "to";
    public final static String valueExtra = "value";
    public final static String passwordExtra = "password";
    public final static String reasonExtra = "reason";

}

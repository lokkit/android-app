package io.lokkit;

/**
 * Created by Nick on 23.05.2017.
 */

public class LokkitIntents {
    public final static String TRANSACTION_QUEUED = "io.lokkit.TRANSACTION_QUEUED";
    public final static String DISCARD_TRANSACTION = "io.lokkit.DISCARD_TRANSACTION";
    public final static String COMPLETE_TRANSACTION = "io.lokkit.COMPLETE_TRANSACTION";
    public final static String COMPLETE_TRANSACTION_SUCCESSFUL = "io.lokkit.COMPLETE_TRANSACTION_SUCCESSFUL";
    public final static String COMPLETE_TRANSACTION_FAILED = "io.lokkit.COMPLETE_TRANSACTION_FAILED";
    public final static String STARTED_FROM_STICKY_NOTIFICATION = "io.lokkit.STARTED_FROM_STICKY_NOTIFICATION";
    public final static String STOP_LOKKIT = "io.lokkit.STOP_LOKKIT";
    public final static String RESUME_LOKKIT = "io.lokkit.STOP_LOKKIT";
    public final static String LOGIN = "io.lokkit.LOGIN"; // provide password to lokkit service [create], recover, login.
    public final static String REQUIRE_ACCOUNT = "io.lokkit.REQUIRE_ACCOUNT"; // service requires an account
    public final static String RECOVER_ACCOUNT = "io.lokkit.RECOVER_ACCOUNT"; // service wants to recovar an account. Service sends this as long as the login fails.
    public final static String LOGIN_SUCCESSFUL = "io.lokkit.LOGIN_SUCCESSFUL"; // service successfully logged in with the account


    public final static String ID_EXTRA = "id";
    public final static String FROM_EXTRA = "from";
    public final static String TO_EXTRA = "to";
    public final static String VALUE_EXTRA = "value";
    public final static String PASSWORD_EXTRA = "password";
    public final static String REASON_EXTRA = "reason";
    public final static String ADDRESS_EXTRA = "address";
    public final static String MNEMONIC_EXTRA = "mnemonic";

}

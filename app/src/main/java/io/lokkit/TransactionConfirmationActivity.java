package io.lokkit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that allows to complete/discard transactions. todo: show transaction details.
 */
public class TransactionConfirmationActivity extends Activity {

    private EditText passwordTextEdit;
    private EditText fromAddressText;
    private TextView toAddressTextView;
    private TextView valueTextView;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private String from;
    private String id;
    private String to;
    private BigInteger value;
    private List<BroadcastReceiver> receivers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation);
        passwordTextEdit = (EditText) findViewById(R.id.password);
        toAddressTextView = (TextView) findViewById(R.id.toAddress);
        valueTextView = (TextView) findViewById(R.id.value);
        fromAddressText = (EditText) findViewById(R.id.fromAddress);

        IntentFilter filter = new IntentFilter();
        filter.addAction(LokkitIntents.COMPLETE_TRANSACTION_FAILED);
        filter.addAction(LokkitIntents.COMPLETE_TRANSACTION_SUCCESSFUL);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (id.equals(intent.getExtras().getString(LokkitIntents.ID_EXTRA))) {
                    switch (intent.getAction()) {
                        case LokkitIntents.COMPLETE_TRANSACTION_FAILED:
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(TransactionConfirmationActivity.this,
                                    "Complete failed: " + intent.getExtras().getString(LokkitIntents.REASON_EXTRA),
                                    Toast.LENGTH_LONG).show();// todo: properly report fail
                            passwordTextEdit.setText("");
                            passwordTextEdit.requestFocus();
                            break;
                        case LokkitIntents.COMPLETE_TRANSACTION_SUCCESSFUL:
                            finish();
                            break;
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
        receivers.add(broadcastReceiver);

        Intent intent = getIntent();
        from = intent.getExtras().getString(LokkitIntents.FROM_EXTRA);
        id = intent.getExtras().getString(LokkitIntents.ID_EXTRA);
        to = intent.getExtras().getString(LokkitIntents.TO_EXTRA);
        value = (BigInteger) intent.getExtras().get(LokkitIntents.VALUE_EXTRA);

        fromAddressText.setText(from);
        toAddressTextView.setText(to);
        valueTextView.setText(value.toString(10) + " Ether");

        passwordTextEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    processTransaction(true);
                    return true;
                }
                return false;
            }
        });
        //progressBar = (ProgressBar) findViewById(R.id.login_progress);

        Button completeTransactionButton = (Button) findViewById(R.id.complete_transaction_button);
        completeTransactionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                processTransaction(true);
            }
        });

        Button discardTransactionButton = (Button) findViewById(R.id.discard_transaction_button);
        discardTransactionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                processTransaction(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (BroadcastReceiver receiver : this.receivers) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onBackPressed() {
        processTransaction(false);
    }

    private void processTransaction(boolean complete) {
        progressDialog = ProgressDialog.show(this, "Completing transaction", "trust no one, just lokkit.");
        Intent intent = new Intent();
        intent.putExtra(LokkitIntents.ID_EXTRA, id);
        if (complete) {
            intent.setAction(LokkitIntents.COMPLETE_TRANSACTION);
            intent.putExtra(LokkitIntents.PASSWORD_EXTRA, passwordTextEdit.getText().toString());
            //progressBar.setVisibility(View.VISIBLE);
        } else {
            intent.setAction(LokkitIntents.DISCARD_TRANSACTION);
            progressDialog.dismiss();
            finish();
        }
        sendBroadcast(intent);
    }
}


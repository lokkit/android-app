package io.lokkit;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that allows to complete/discard transactions
 */
public class TransactionConfirmationActivity extends Activity {

    private EditText mPasswordView;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private String from;
    private String id;
    private String to;
    private String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation);

        IntentFilter filter = new IntentFilter();
        filter.addAction(LokkitIntents.completeTransactionFailedAction);
        filter.addAction(LokkitIntents.completeTransactionSuccessfulAction);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (id.equals(intent.getExtras().getString(LokkitIntents.idExtra))) {
                    switch (intent.getAction()) {
                        case LokkitIntents.completeTransactionFailedAction:
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(TransactionConfirmationActivity.this,
                                    "Complete failed: " + intent.getExtras().getString(LokkitIntents.reasonExtra),
                                    Toast.LENGTH_LONG).show();// todo: properly report fail
                            mPasswordView.setText("");
                            mPasswordView.requestFocus();
                            break;
                        case LokkitIntents.completeTransactionSuccessfulAction:
                            finish();
                            break;
                    }
                }
            }
        }, filter);

        Intent intent = getIntent();
        from = intent.getExtras().getString(LokkitIntents.fromExtra);
        id = intent.getExtras().getString(LokkitIntents.idExtra);
        to = intent.getExtras().getString(LokkitIntents.toExtra);
        value = intent.getExtras().getString(LokkitIntents.valueExtra);

        EditText addressView = (EditText) findViewById(R.id.address);
        addressView.setText(from);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    processTransaction(true);
                    return true;
                }
                return false;
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.login_progress);

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

    private void processTransaction(boolean complete) {
        Intent intent = new Intent();
        intent.putExtra(LokkitIntents.idExtra, id);
        if (complete) {
            intent.setAction(LokkitIntents.completeTransactionAction);
            intent.putExtra(LokkitIntents.passwordExtra, mPasswordView.getText().toString());
            progressDialog = ProgressDialog.show(this, "Completing transaction", "trust no one, just lokkit.");
            //progressBar.setVisibility(View.VISIBLE);
        } else {
            intent.setAction(LokkitIntents.discardTransactionAction);
            finish();
        }
        sendBroadcast(intent);
    }
}


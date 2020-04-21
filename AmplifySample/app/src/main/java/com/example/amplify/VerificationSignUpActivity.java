package com.example.amplify;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignUpResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VerificationSignUpActivity extends AppCompatActivity {

    private static final String TAG = VerificationSignUpActivity.class.getSimpleName();

    private TextView mConfirmationEmail;
    private EditText mUsernameEt;
    private EditText mCodeEt;
    private Button mConfirmButton;

    private ProgressDialog mProgressDialog;

    private boolean isFromSignUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_confirm);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_confirm_account));
        }
        initializeViews();
        setConfirmOnClickListener();

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            String emailDestination = bundle.getString("destination");
            isFromSignUp = bundle.getBoolean("isFromSignUp");
            mConfirmationEmail.setText(getResources().getString(R.string.text_instructions_confirm_email, emailDestination));
        }
    }

    private void initializeViews() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        mConfirmationEmail = findViewById(R.id.text_welcome);
        mUsernameEt = findViewById(R.id.et_username);
        mCodeEt = findViewById(R.id.et_verification_code);
        mConfirmButton = findViewById(R.id.button_confirm);
    }

    /**
     * Request AWS Resend Sign Up
     **/
    private void setConfirmOnClickListener() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameEt.getText().toString();
                String code = mCodeEt.getText().toString();

                showDialog(getResources().getString(R.string.message_evaluate_code));

                AWSMobileClient.getInstance().confirmSignUp(username, code, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult result) {
                        Log.i(TAG, result.toString());
                        showAlert("Confirm Account", "Sign up confirmation succeeded", false);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
            }
        });
    }

    /**
     * Show progress dialog
     **/
    private void showDialog(String message) {
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    /**
     * Close progress dialog
     **/
    private void dismissDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void showAlert(String title, String message, boolean hasNegativeButton) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Nice!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        if(isFromSignUp) {
                            finish();
                        }
                    }
                });
        if (!hasNegativeButton) {
            alertDialogBuilder.setNegativeButton("", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }
        alertDialogBuilder.show();
    }

}

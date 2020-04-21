package com.example.amplify;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private final String TAG = SignUpActivity.class.getSimpleName();

    private EditText mUsernameEt;
    private EditText mPasswordEt;
    private EditText mGivenName;
    private EditText mEmailAddress;
    private EditText mPhoneNumber;
    private Button mSignUpButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_sign_up));
        }
        initializeViews();
        setSignUpOnClickListener();
    }

    private void initializeViews() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        mUsernameEt = findViewById(R.id.et_username);
        mPasswordEt = findViewById(R.id.et_password);
        mGivenName = findViewById(R.id.et_given_name);
        mEmailAddress = findViewById(R.id.et_email_address);
        mPhoneNumber = findViewById(R.id.et_phone_number);
        mSignUpButton = findViewById(R.id.button_sign_up);
    }

    /**
     * Request AWS Sign Up
     **/
    private void setSignUpOnClickListener() {
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUsernameEt.getText().toString();
                String password = mPasswordEt.getText().toString();
                String giveName = mGivenName.getText().toString();
                final String emailAddress = mEmailAddress.getText().toString();
                String phoneNumber = mPhoneNumber.getText().toString();

                showDialog(getResources().getString(R.string.message_sign_up));
                Map<String, String> userAttributes = new HashMap<>();
                userAttributes.put("given_name", giveName);
                userAttributes.put("email", emailAddress);
                userAttributes.put("phone_number", phoneNumber);

                AWSMobileClient.getInstance().signUp(username, password, userAttributes, null, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult result) {
                        dismissDialog();
                        Log.i(TAG, result.toString());
                        confirmEmail(result.getUserCodeDeliveryDetails().getDestination());
                    }

                    @Override
                    public void onError(final Exception e) {
                        dismissDialog();
                        Log.e(TAG, e.toString());
                        if (e instanceof UsernameExistsException) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showAlert("You are already registered", "Click Resend to generate new confirmation code to " + emailAddress, true, username);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void confirmEmail(String destination) {
        Bundle bundle = new Bundle();
        bundle.putString("destination", destination);
        bundle.putBoolean("isFromSignUp", true);
        Intent intent = new Intent(getApplicationContext(), VerificationSignUpActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
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

    private void showAlert(String title, String message, boolean hasNegativeButton, final String data) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Resend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // resendSignUp for email confirmation
                        AWSMobileClient.getInstance().resendSignUp(data, new Callback<SignUpResult>() {
                            @Override
                            public void onResult(SignUpResult result) {
                                dismissDialog();
                                Log.i(TAG, result.toString());
                                confirmEmail(result.getUserCodeDeliveryDetails().getDestination());
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        });
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

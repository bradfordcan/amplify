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
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = LoginActivity.class.getSimpleName();

    private EditText mUsernameEt;
    private EditText mPasswordEt;
    private Button mLoginButton;
    private TextView mLinkCreateAccountText;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_sign_in));
        }
        initializeViews();
        showDialog(getResources().getString(R.string.message_initializing_user_state));
        initializeUserState();
        setLoginOnClickListener();
        setCreateAccountListener();
    }

    private void initializeViews() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        mUsernameEt = findViewById(R.id.et_username);
        mPasswordEt = findViewById(R.id.et_password);
        mLoginButton = findViewById(R.id.button_login);
        mLinkCreateAccountText = findViewById(R.id.link_text_sign_up);
    }

    /**
     * Request AWS Sign In
     **/
    private void setLoginOnClickListener() {
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUsernameEt.getText().toString();
                String password = mPasswordEt.getText().toString();
                showDialog(getResources().getString(R.string.message_logging_in));
                AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
                    @Override
                    public void onResult(SignInResult result) {
                        dismissDialog();
                        Log.i(TAG, result.getSignInState().toString());
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(final Exception e) {
                        dismissDialog();
                        Log.e(TAG, e.toString());
                        if (e instanceof UserNotFoundException) {
                            final UserNotFoundException userNotFoundException = (UserNotFoundException) e;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), userNotFoundException.getErrorMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if(e instanceof UserNotConfirmedException) { // User email not confirmed, resendSignUp
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showAlert("You are already registered", "Click Resend to generate new confirmation code to your email", true, username);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    /**
     * Launch Sign Up Activity
     **/
    private void setCreateAccountListener() {
        mLinkCreateAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });
    }

    /**
     * Fetch user signIn state
     **/
    private void initializeUserState() {
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                dismissDialog();
                switch (userStateDetails.getUserState()) {
                    case SIGNED_IN:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        break;
                    case SIGNED_OUT:
                        showSignIn();
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        showSignIn();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                dismissDialog();
                Log.e(TAG, e.toString());
            }
        });
    }

    /**
     * Display default AWS UI for Sign In
     **/
    private void showSignIn() {
        /*try {
            AWSMobileClient.getInstance().showSignIn(this, SignInUIOptions.builder().nextActivity(MainActivity.class).build());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }*/
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

    private void confirmEmail(String destination) {
        Bundle bundle = new Bundle();
        bundle.putString("destination", destination);
        bundle.putBoolean("isFromSignUp", false);
        Intent intent = new Intent(getApplicationContext(), VerificationSignUpActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
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

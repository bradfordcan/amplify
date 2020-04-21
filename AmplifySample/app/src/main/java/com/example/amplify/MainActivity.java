package com.example.amplify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.Tokens;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView mWelcomeText;
    private TextView mTokenText;
    private Button mLogoutButton;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
        initializeViews();
        setWelcomeMessage();
        fetchIdToken();
        setLogoutOnClickListener();
    }

    private void initializeViews() {
        mWelcomeText = findViewById(R.id.text_welcome);
        mTokenText = findViewById(R.id.text_token);
        mLogoutButton = findViewById(R.id.button_logout);
    }

    /**
     * Request AWS Sign Out
     **/
    private void setLogoutOnClickListener() {
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AWSMobileClient.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    /**
     * Displays the logged-in username
     **/
    private void setWelcomeMessage() {
        String welcomeMessage = "Welcome back, " + AWSMobileClient.getInstance().getUsername() + "!";
        mWelcomeText.setText(welcomeMessage);
    }

    /**
     * Displays the username's idToken
     **/
    private void fetchIdToken() {
        AWSMobileClient.getInstance().getTokens(new Callback<Tokens>() {
            @Override
            public void onResult(final Tokens result) {
                Log.i(TAG, result.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTokenText.setText(result.getIdToken().getTokenString());
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

}

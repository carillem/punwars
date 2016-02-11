package com.example.watson.punwarz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.*;
import com.facebook.*;
import com.parse.*;


public class Login extends AppCompatActivity {

    private CallbackManager callbackManager;
    private TextView info;
    private LoginButton loginButton;

    private ProfileTracker mProfileTracker;
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "5UQqqOAeFhEDsGhrMMka0a1vKWNxpu4IlNonVn4z", "STbqcRcr7FcJxkmjEiz8Qs2qgq8SjsPVOtqnMDgG");
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        info = (TextView)findViewById(R.id.info);
        loginButton = (LoginButton)findViewById(R.id.login_button);



        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                info.setText(loginResult.getAccessToken().getToken() + "\n" +
                        loginResult.getAccessToken().getUserId());

                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            Log.v("facebook - profile", profile2.getFirstName());
                            printName();
                            mProfileTracker.stopTracking();
                        }
                    };
                    mProfileTracker.startTracking();
                }
                else {
                    Profile profile = Profile.getCurrentProfile();
                    Log.v("facebook - profile", profile.getFirstName());
                    printName();
                }

                //printName();
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt cancelled.");
            }

            @Override
            public void onError(FacebookException error) {
                info.setText("Login failed.");
            }
        });

        // [Optional] Power your app with Local Datastore. For more info, go to
        // https://parse.com/docs/android/guide#local-datastore
        //Parse.enableLocalDatastore(this);

        //Parse.initialize(this);
        // Note from Dorothy: Commented out for now, not sure if we need Local Datastores?
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //callbackManager.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    private void printName(){
        info.setText("\nName: " + Profile.getCurrentProfile().getName());
        run();
    }

    private void run(){
        ParseObject trial = new ParseObject("TheTest");
        trial.put("foo", "bar");
        trial.saveInBackground();
        //Parse functionality test
    }
}

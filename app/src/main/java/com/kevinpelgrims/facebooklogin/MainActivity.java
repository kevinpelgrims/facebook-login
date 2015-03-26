package com.kevinpelgrims.facebooklogin;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    private CallbackManager callbackManager;

    private TextView tokenView, userView;
    private Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        tokenView = (TextView) findViewById(R.id.token);
        userView = (TextView) findViewById(R.id.user);
        logOutButton = (Button) findViewById(R.id.log_out);

        showEmptyData();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login);
        loginButton.setReadPermissions("public_profile", "email");
        loginButton.registerCallback(callbackManager, fbCallback);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
            }
        });

        if (AccessToken.getCurrentAccessToken() != null) {
            getUserFacebookData(AccessToken.getCurrentAccessToken());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showEmptyData() {
        logOutButton.setVisibility(View.GONE);
        tokenView.setText("Not logged in");
        userView.setText("No data available");
    }

    private final FacebookCallback<LoginResult> fbCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            getUserFacebookData(loginResult.getAccessToken());
        }

        @Override
        public void onCancel() {
            userView.setText("You cancelled, what up?");
        }

        @Override
        public void onError(FacebookException e) {
            userView.setText("Something went wrong");
        }
    };

    private void getUserFacebookData(final AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                tokenView.setText(accessToken.getToken());
                FacebookUser user = new Gson().fromJson(jsonObject.toString(), FacebookUser.class);
                String userString = String.format("%s%n%s %s%n%s",
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail() != null ? user.getEmail() : "(no email)");
                userView.setText(userString);
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }
}

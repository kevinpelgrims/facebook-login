package com.kevinpelgrims.facebooklogin;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class MainActivity extends ActionBarActivity {
    private Session fbSession;
    private UiLifecycleHelper uiHelper;

    private TextView tokenView, userView;
    private Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tokenView = (TextView) findViewById(R.id.token);
        userView = (TextView) findViewById(R.id.user);
        logOutButton = (Button) findViewById(R.id.log_out);

        showEmptyData();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login);
        loginButton.setReadPermissions("public_profile", "email");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = Session.getActiveSession();
                if (!session.isOpened() && !session.isClosed()) {
                    session.openForRead(new Session.OpenRequest(MainActivity.this)
                            .setPermissions("public_profile", "email")
                            .setCallback(statusCallback));
                } else {
                    Session.openActiveSession(MainActivity.this, true, statusCallback);
                }
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fbSession != null) fbSession.closeAndClearTokenInformation();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(MainActivity.this, requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception e) {
            fbSession = session;
            if (state.isOpened()) {
                logOutButton.setVisibility(View.VISIBLE);
                tokenView.setText(session.getAccessToken());

                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            String userString = String.format("%s%n%s %s%n%s", user.getId(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getProperty("email") != null ? user.getProperty("email").toString() : "no email");
                            userView.setText(userString);
                        }
                    }
                });
                request.executeAsync();
            }
            else if (state.isClosed()) {
                showEmptyData();
            }
            else {
                logOutButton.setVisibility(View.GONE);
            }
        }
    };

    private void showEmptyData() {
        logOutButton.setVisibility(View.GONE);
        tokenView.setText("Not logged in");
        userView.setText("No data available");
    }
}

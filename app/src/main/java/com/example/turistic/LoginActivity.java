package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    public static final String sTAG = "LoginActivity";
    private EditText mEtLoginUsername;
    private EditText mEtLoginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button btnLoginFacebook;

        if(ParseUser.getCurrentUser() != null){
            goFeedActivity();
        }

        mEtLoginPassword = findViewById(R.id.etLoginPassword);
        mEtLoginUsername = findViewById(R.id.etLoginUsername);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);

        btnLogin.setOnClickListener(v -> {
            String password = mEtLoginPassword.getText().toString();
            String username = mEtLoginUsername.getText().toString();
            loginUser(username, password);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnLoginFacebook.setOnClickListener(v -> {
            final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("Please, wait a moment.");
            dialog.setMessage("Logging in...");
            dialog.show();
            Collection<String> permissions = Arrays.asList("public_profile", "email");
            ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, (user, err) -> {
                dialog.dismiss();
                if (err != null) {
                    Log.e(sTAG, "done: ", err);
                    Toasty.error(LoginActivity.this, Objects.requireNonNull(err.getMessage()), Toast.LENGTH_LONG).show();
                } else if (user == null) {
                    Toasty.error(LoginActivity.this, "The user cancelled the Facebook login.", Toast.LENGTH_LONG).show();
                    Log.d(sTAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Toasty.success(LoginActivity.this, "User signed up and logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d(sTAG, "User signed up and logged in through Facebook!");
                    getUserDetailFromFB();
                } else {
                    Toasty.success(LoginActivity.this, "User logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d(sTAG, "User logged in through Facebook!");
                    goFeedActivity();
                }
            });
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if(e != null){
                Log.e(sTAG, "Issue with Login: ", e);
                Toasty.error(LoginActivity.this, "Issue with Login", Toast.LENGTH_SHORT).show();
                return;
            }
            Toasty.success(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
            goFeedActivity();
        });
    }

    private void goFeedActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void getUserDetailFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
            ParseUser user = ParseUser.getCurrentUser();
            try {
                if (object.has("name")) {
                    String name = object.getString("name");
                    String [] arrName = name.split(" ");
                    user.setUsername(name);
                    user.put("lastName", arrName[1]);
                    user.put("name", arrName[0]);
                }
                if (object.has("email"))
                    user.setEmail(object.getString("email"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            user.saveInBackground(e -> {
                if (e == null) {
                    goFeedActivity();
                } else
                    Log.e(sTAG, e.getMessage());
            });
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
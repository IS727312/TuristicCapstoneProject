package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String sTAG = "LoginActivity";
    private EditText mEtLoginUsername;
    private EditText mEtLoginPassword;
    private Button mBtnLogin;
    private Button mBtnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null){
            goFeedActivity();
        }

        mEtLoginPassword = findViewById(R.id.etLoginPassword);
        mEtLoginUsername = findViewById(R.id.etLoginUsername);
        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnSignUp = findViewById(R.id.btnSignUp);

        mBtnLogin.setOnClickListener(v -> {
            String password = mEtLoginPassword.getText().toString();
            String username = mEtLoginUsername.getText().toString();
            loginUser(username, password);
        });

        mBtnSignUp.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(i);
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if(e != null){
                Log.e(sTAG, "Issue with Login: ", e);
                Toast.makeText(LoginActivity.this, "Issue with Login", Toast.LENGTH_SHORT).show();
                return;
            }
            goFeedActivity();
            Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
        });
    }

    private void goFeedActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
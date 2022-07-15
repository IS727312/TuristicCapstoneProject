package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;

import es.dmoral.toasty.Toasty;

public class SignupActivity extends AppCompatActivity {

    public static final String sTAG = "SignupActivity";
    public static final String sKEY_NAME = "name";
    public static final String sKEY_LAST_NAME = "lastName";
    private EditText mEtSignupUsername;
    private EditText mEtSignupPass;
    private EditText mEtSignupConfirmPass;
    private EditText mEtSignupName;
    private EditText mEtSignupLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mEtSignupUsername = findViewById(R.id.etSignupUsername);
        mEtSignupPass = findViewById(R.id.etSignupPass);
        mEtSignupConfirmPass = findViewById(R.id.etSignupConfirmPass);
        mEtSignupName = findViewById(R.id.etSignupName);
        mEtSignupLastName = findViewById(R.id.etSignupLastName);
        Button btnSignupRegister = findViewById(R.id.btnSignupRegister);

        btnSignupRegister.setOnClickListener(v -> {
            String username = mEtSignupUsername.getText().toString();
            String pass = mEtSignupPass.getText().toString();
            String confirmPass = mEtSignupConfirmPass.getText().toString();
            String name = mEtSignupName.getText().toString();
            String lastName = mEtSignupLastName.getText().toString();

            if(username.isEmpty() || pass.isEmpty() ||confirmPass.isEmpty() || name.isEmpty() || lastName.isEmpty()){
                Log.e(sTAG, "There are empty fills");
                Toasty.error(SignupActivity.this, "There are empty fills", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!pass.equals(confirmPass)){
                Log.e(sTAG, "Passwords don't match");
                Toasty.error(SignupActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            ParseUser newUser = new ParseUser();
            newUser.setUsername(username);
            newUser.setPassword(pass);
            newUser.put(sKEY_NAME, name);
            newUser.put(sKEY_LAST_NAME, lastName);
            newUser.signUpInBackground(e -> {
                if(e == null){
                    Intent i = new Intent(SignupActivity.this, MainActivity.class);
                    Toasty.success(SignupActivity.this, "Successfully sign up", Toast.LENGTH_SHORT).show();
                    startActivity(i);
                } else {
                    Log.e(sTAG, "Error with signing up" + e);
                }
            });
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
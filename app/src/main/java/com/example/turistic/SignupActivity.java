package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_NAME = "lastName";
    private EditText etSignupUsername;
    private EditText etSignupPass;
    private EditText etSignupConfirmPass;
    private EditText etSignupName;
    private EditText etSignupLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etSignupUsername = findViewById(R.id.etSignupUsername);
        etSignupPass = findViewById(R.id.etSignupPass);
        etSignupConfirmPass = findViewById(R.id.etSignupConfirmPass);
        etSignupName = findViewById(R.id.etSignupName);
        etSignupLastName = findViewById(R.id.etSignupLastName);
        Button btnSignupRegister = findViewById(R.id.btnSignupRegister);

        btnSignupRegister.setOnClickListener(v -> {
            String username = etSignupUsername.getText().toString();
            String pass = etSignupPass.getText().toString();
            String confirmPass = etSignupConfirmPass.getText().toString();
            String name = etSignupName.getText().toString();
            String lastName = etSignupLastName.getText().toString();

            if(username.isEmpty() || pass.isEmpty() ||confirmPass.isEmpty() || name.isEmpty() || lastName.isEmpty()){
                Log.e(TAG, "There are empty fills");
                Toast.makeText(SignupActivity.this, "There are empty fills", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!pass.equals(confirmPass)){
                Log.e(TAG, "Passwords don't match");
                Toast.makeText(SignupActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            ParseUser newUser = new ParseUser();
            newUser.setUsername(username);
            newUser.setPassword(pass);
            newUser.put(KEY_NAME, name);
            newUser.put(KEY_LAST_NAME, lastName);
            newUser.signUpInBackground(e -> {
                if(e == null){
                    Intent i = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Log.e(TAG, "Error with signing up" + e);
                }

            });
        });
    }
}
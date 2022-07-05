package com.example.turistic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditActivity extends AppCompatActivity {

    public static final String TAG = "EditActivity";
    public static final int SELECT_PICTURE_REQUEST_CODE = 200;
    private Spinner spnPrivacyMode;
    private EditText etEditUsername;
    private EditText etEditName;
    private EditText etEditLastName;
    private ImageView ivEditProfilePicture;
    private String spnValue;
    private int privacyModeValue;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private ParseUser user;
    private Boolean photoChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        etEditUsername = findViewById(R.id.etEditUsername);
        etEditName = findViewById(R.id.etEditName);
        etEditLastName = findViewById(R.id.etEditLastName);
        ivEditProfilePicture = findViewById(R.id.ivEditProfilePicture);
        user = ParseUser.getCurrentUser();
        Button btnEditSave = findViewById(R.id.btnEditSave);


        spnPrivacyMode = findViewById(R.id.spnEditPrivacyMode);
        privacyModeValue = 0;

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.privacyMode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spnPrivacyMode.setAdapter(adapter);

        setUserInfo();

        ivEditProfilePicture.setOnClickListener(v -> {
            onPickPhoto(v);
            photoChanged = true;
        });

        btnEditSave.setOnClickListener(v -> {
            spnValue = spnPrivacyMode.getSelectedItem().toString();
            switch (spnValue){
                case "Public":privacyModeValue = 0;
                break;
                case "Followers Only": privacyModeValue = 1;
                break;
                case "Friends Only": privacyModeValue = 2;
                break;
                case "Private":
                default: privacyModeValue = 3;
                    break;
            }
            if(!etEditName.getText().toString().isEmpty()) {
                user.put("name", etEditName.getText().toString());
            }
            if(!etEditLastName.getText().toString().isEmpty()){
                user.put("lastName", etEditLastName.getText().toString());
            }
            if(!etEditUsername.getText().toString().isEmpty()){
                user.setUsername(etEditUsername.getText().toString());
            }
            if(photoChanged){
                user.put("profilePicture",new ParseFile(photoFile));
            }
            user.put("profileMode", spnValue);
            user.saveInBackground(e -> {
                if(e != null){
                    Log.e(TAG, "Issue with updating data");
                }
                Toast.makeText(EditActivity.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditActivity.this, MainActivity.class);
                startActivity(i);
            });
        });
    }

    private void setUserInfo(){
        ParseFile profilePicture = user.getParseFile("profilePicture");

        etEditName.setHint(user.getString("name"));
        etEditLastName.setHint(user.getString("lastName"));
        etEditUsername.setHint(user.getUsername());
        assert profilePicture != null;
        Glide.with(EditActivity.this).load(profilePicture.getUrl()).into(ivEditProfilePicture);
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE_REQUEST_CODE);

    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            // on newer versions of Android, use the new decodeBitmap method
            ImageDecoder.Source source = ImageDecoder.createSource(EditActivity.this.getContentResolver(), photoUri);
            image = ImageDecoder.decodeBitmap(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                assert data != null;
                Uri photoUri = data.getData();

                // Load the image located at photoUri into selectedImage
                Bitmap selectedImage = loadFromUri(photoUri);

                //create a file to write bitmap data
                File f = new File(EditActivity.this.getCacheDir(), photoFileName);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    assert fos != null;
                    fos.write(bitmapdata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                photoFile = f;

                ivEditProfilePicture.setImageBitmap(selectedImage);
            }
        }
    }

}
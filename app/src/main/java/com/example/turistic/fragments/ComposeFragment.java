package com.example.turistic.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.turistic.BitmapScaler;
import com.example.turistic.MainActivity;
import com.example.turistic.R;
import com.example.turistic.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final int SELECT_PICTURE = 200;
    private EditText etComposeTitle;
    private ImageView ivComposePictureToPost;
    private EditText etComposeCaption;
    private File photoFile;
    public String photoFileName = "photo.jpg";
    public static final int PICTURE_TAKEN = 0;
    public static final int PICTURE_SUBMITTED = 0;
    private int pictureOrigin;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "COMPOSE FRAGMENT");
        Button btnComposeSubmitPicture = view.findViewById(R.id.btnComposeSubmitPicture);
        Button btnComposeSubmitPost = view.findViewById(R.id.btnComposeSubmitPost);
        Button btnComposeTakePicture = view.findViewById(R.id.btnComposeTakePicture);
        etComposeTitle = view.findViewById(R.id.etComposeTitle);
        etComposeCaption = view.findViewById(R.id.etComposeCaption);
        ivComposePictureToPost = view.findViewById(R.id.ivComposePictureToPost);

        btnComposeTakePicture.setOnClickListener(v -> launchCamera());

        btnComposeSubmitPost.setOnClickListener(v -> {
            String caption = etComposeCaption.getText().toString();
            String title = etComposeTitle.getText().toString();
            Log.i(TAG, caption);
            Log.i(TAG, title);
            if(caption.isEmpty() || title.isEmpty()){
                Toast.makeText(getContext(), "Empty fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if(photoFile == null || ivComposePictureToPost.getDrawable() == null){
                Toast.makeText(getContext(), "There is no photo", Toast.LENGTH_SHORT).show();
            }
            ParseUser currentUser = ParseUser.getCurrentUser();
            savePosts(caption, title, currentUser, photoFile);
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
        });

        btnComposeSubmitPicture.setOnClickListener(v -> Log.i(TAG, "Upload pictures"));
    }

    private void savePosts(String caption, String title, ParseUser currentUser, File photoFile) {
            Post post = new Post();
            post.setTitle(title);
            post.setPicture(new ParseFile(photoFile));
            post.setCaption(caption);
            post.setOwner(currentUser);
            post.saveInBackground(e -> {
                if(e != null){
                    Log.e(TAG, "Error while saving the post: ", e);
                    return;
                }
                Log.i(TAG, "Save successful");
                etComposeTitle.setText("");
                etComposeCaption.setText("");
                ivComposePictureToPost.setImageResource(0);
            });
    }

    private void launchCamera(){
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);
        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(requireContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                //All the code below helps to resize the image
                Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
                // by this point we have the camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 400);

                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(resizedFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // Write the bytes of the bitmap to file
                try {
                    assert fos != null;
                    fos.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pictureOrigin = PICTURE_TAKEN;
                ivComposePictureToPost.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    pictureOrigin = PICTURE_SUBMITTED;
                    ivComposePictureToPost.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(i, SELECT_PICTURE);
    }

}
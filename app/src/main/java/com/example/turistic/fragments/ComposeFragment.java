package com.example.turistic.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
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

public class ComposeFragment extends Fragment {

    public static final String sTAG = "ComposeFragment";
    public static final int sCAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final int sSELECT_PICTURE_REQUEST_CODE = 200;
    private EditText mEtComposeTitle;
    private ImageView mIvComposePictureToPost;
    private EditText mEtComposeCaption;
    private File mPhotoFile;
    public String mPhotoFileName = "photo.jpg";

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(sTAG, "COMPOSE FRAGMENT");
        Button btnComposeSubmitPicture = view.findViewById(R.id.btnComposeSubmitPicture);
        Button btnComposeSubmitPost = view.findViewById(R.id.btnComposeSubmitPost);
        Button btnComposeTakePicture = view.findViewById(R.id.btnComposeTakePicture);
        mEtComposeTitle = view.findViewById(R.id.etComposeTitle);
        mEtComposeCaption = view.findViewById(R.id.etComposeCaption);
        mIvComposePictureToPost = view.findViewById(R.id.ivComposePictureToPost);

        btnComposeTakePicture.setOnClickListener(v -> launchCamera());

        btnComposeSubmitPost.setOnClickListener(v -> {
            String caption = mEtComposeCaption.getText().toString();
            String title = mEtComposeTitle.getText().toString();
            if(caption.isEmpty() || title.isEmpty()){
                Toast.makeText(getContext(), "Empty fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mPhotoFile == null || mIvComposePictureToPost.getDrawable() == null){
                Toast.makeText(getContext(), "There is no photo", Toast.LENGTH_SHORT).show();
            }
            ParseUser currentUser = ParseUser.getCurrentUser();
            savePosts(caption, title, currentUser, mPhotoFile);
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
        });

        btnComposeSubmitPicture.setOnClickListener(this::onPickPhoto);
    }

    private void savePosts(String caption, String title, ParseUser currentUser, File photoFile) {
            Post post = new Post();
            post.setTitle(title);
            post.setCaption(caption);
            post.setPicture(new ParseFile(photoFile));
            post.setOwner(currentUser);
            post.saveInBackground(e -> {
                if(e != null){
                    Log.e(sTAG, "Error while saving the post: ", e);
                    return;
                }

                post.saveInBackground(e1 -> {
                    if(e1 != null){
                        Log.e(sTAG, "Error while saving the post image: ", e1);
                        return;
                    }
                    Log.i(sTAG, "PHOTO SAVED");
                });
                Log.i(sTAG, "Save successful");
                mEtComposeTitle.setText("");
                mEtComposeCaption.setText("");
                mIvComposePictureToPost.setImageResource(0);
            });
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, sSELECT_PICTURE_REQUEST_CODE);


    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            // on newer versions of Android, use the new decodeBitmap method
            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
            image = ImageDecoder.decodeBitmap(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoFile = getPhotoFileUri(mPhotoFileName);

        Uri fileProvider = FileProvider.getUriForFile(requireContext(), "com.codepath.fileprovider", mPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(intent, sCAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), sTAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(sTAG, "failed to create directory");
        }

        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == sCAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = rotateBitmapOrientation(mPhotoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(mPhotoFileName));
                // by this point we have the camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 400);

                // Configure byte output stream
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] bitmapdata = bos.toByteArray();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);

                File resizedFile = getPhotoFileUri(mPhotoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(resizedFile);
                    fos.write(bitmapdata);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mIvComposePictureToPost.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == sSELECT_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Uri photoUri = data.getData();

                // Load the image located at photoUri into selectedImage
                Bitmap selectedImage = loadFromUri(photoUri);

                //create a file to write bitmap data
                File f = new File(getContext().getCacheDir(), mPhotoFileName);
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
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mPhotoFile = f;

                mIvComposePictureToPost.setImageBitmap(selectedImage);
            }
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data -> Get the orientation the camera is originally in, and rotate the image based on that
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90: rotationAngle = 90;
            break;
            case ExifInterface.ORIENTATION_ROTATE_180: rotationAngle = 180;
            break;
            case ExifInterface.ORIENTATION_ROTATE_270: rotationAngle = 270;
            break;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
    }

}
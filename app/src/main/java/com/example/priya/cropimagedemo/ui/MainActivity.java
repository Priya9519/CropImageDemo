package com.example.priya.cropimagedemo.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.priya.cropimagedemo.R;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

import static com.example.priya.cropimagedemo.AppConstants.Constants.REQUEST_CODE_CAMERA;
import static com.example.priya.cropimagedemo.AppConstants.Constants.REQUEST_CODE_GALLARY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView, ivCamera, ivGallary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        imageView = (ImageView) findViewById(R.id.image_view);
        ivCamera = (ImageView) findViewById(R.id.camera_crop);
        ivGallary = (ImageView) findViewById(R.id.gallary_crop);
        ivCamera.setOnClickListener(this);
        ivGallary.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        imageView.setImageBitmap(null);
        String permission[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (view.getId() == R.id.gallary_crop) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission[0]) == PackageManager.PERMISSION_GRANTED) {
                Crop.pickImage(this);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission[0]}, REQUEST_CODE_GALLARY);
            }
        } else if (view.getId() == R.id.camera_crop) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission[1]) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            } else
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission[1]}, REQUEST_CODE_CAMERA);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String permission[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLARY && resultCode == Activity.RESULT_OK && data != null) {
               Crop.pickImage(this);
        }
        else if(requestCode == REQUEST_CODE_GALLARY && resultCode != Activity.RESULT_OK){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission[0]}, REQUEST_CODE_GALLARY);
        }
        else if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            Log.e("bitmap", photo + "");
            Uri imageUri = getImageUri(this, photo);
            Log.e("Uri", imageUri + "");
            beginCrop(imageUri);
        } else if(requestCode == REQUEST_CODE_CAMERA && resultCode != Activity.RESULT_OK){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission[1]}, REQUEST_CODE_CAMERA);
            }
         else if (resultCode == Activity.RESULT_CANCELED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission[0]}, REQUEST_CODE_GALLARY);
        } else if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
            Log.e("data2",data.getData()+"");
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }
    private void handleCrop(int resultCode, Intent result) {
        Bitmap bmp = null;
        int n = 10000;
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(Crop.getOutput(result));
            try {
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(Crop.getOutput(result)));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Random generator = new Random();
            n = generator.nextInt(n);
            createDirectoryAndSaveFile(bmp, "image" + n);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {
        File direct = new File(Environment.getExternalStorageDirectory() + "/SavedImage");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/SavedImage/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/SavedImage/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,"profile", null);
        return Uri.parse(path);
    }
}




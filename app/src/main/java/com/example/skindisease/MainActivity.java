package com.example.skindisease;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private WebView browser;
    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int REQUEST_CODE_GALLERY = 200;
    private static final int REQUEST_CODE_PERMISSION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        browser = findViewById(R.id.webView);
        browser.setWebViewClient(new MyBrowser());
        browser.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        browser.addJavascriptInterface(new WebViewInterface(), "AndroidInterface");
        browser.loadUrl("https://health-privilege-maker-regulatory.trycloudflare.com");

        // Request storage permission if not granted
        requestStoragePermission();
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private class WebViewInterface {
        @JavascriptInterface
        public void openCamera() {
            // Open camera
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }

        @JavascriptInterface
        public void openGallery() {
            // Open gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            // Handle camera result
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            saveImage(imageBitmap);
        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            // Handle gallery result
            if (data != null) {
                Uri selectedImageUri = data.getData();
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveImage(imageBitmap);
            }
        }
    }

    private void saveImage(Bitmap imageBitmap) {
        if (imageBitmap != null) {
            // Save image to external storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request storage permission if not granted
                requestStoragePermission();
            } else {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";
                File storageDir = new File(Environment.getExternalStorageDirectory() + "/media/images/");
                storageDir.mkdirs(); // Create the directory if it doesn't exist
                if (storageDir.exists()) {
                    File imageFile = new File(storageDir, imageFileName);
                    try {
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                        // Image saved successfully, you can proceed with further operations
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Directory creation failed, handle this scenario accordingly
                }
            }
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with saving the image
            } else {
                // Permission denied, handle this scenario accordingly
            }
        }
    }
}
/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.face.bluetooth.Bluetooth;
import com.google.android.gms.samples.vision.face.patch.SafeFaceDetector;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.rohitarya.glide.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.glide.facedetection.transformation.core.GlideFaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates basic usage of the GMS vision face detector by running face landmark detection on a
 * photo and displaying the photo with associated landmarks in the UI.
 */
public class PhotoViewerActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "PhotoViewerActivity";
    Button gallery,camera,open;
    ImageView imageView;
    private String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
    ,Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int MULTIPLE_PERMISSIONS = 101;
    static final int getCamera=2001;
    static final int getGallery=2002;
    static Bluetooth bluetooth;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideFaceDetector.releaseDetector();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        if(checkPermissions())
            init();
        GlideFaceDetector.initialize(this);

        setBluetooth();


    }
    public void setBluetooth(){
        bluetooth = new Bluetooth(this);
        bluetooth.checkBluetooth();
    }
    public static void sendData(String param){
        bluetooth.sendData(param);
    }
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                                break;
                            }
                        }
                        if(i==permissions.length-1)
                            init();
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }


            }
        }
    }
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this,getString(R.string.limit),Toast.LENGTH_SHORT).show();
        init();
    }
    void init(){
        gallery=findViewById(R.id.gallery);
        gallery.setOnClickListener(this);
        camera=findViewById(R.id.camera);
        camera.setOnClickListener(this);
        imageView=findViewById(R.id.imageView);
        open=findViewById(R.id.open);
        open.setOnClickListener(this);

    }

    void run(Bitmap image){

        //InputStream stream = getResources().openRawResource(R.raw.face);
        //Bitmap bitmap = BitmapFactory.decodeStream(stream);

        // A new face detector is created for detecting the face and its landmarks.
        //
        // Setting "tracking enabled" to false is recommended for detection with unrelated
        // individual images (as opposed to video or a series of consecutively captured still
        // images).  For detection on unrelated individual images, this will give a more accurate
        // result.  For detection on consecutive images (e.g., live video), tracking gives a more
        // accurate (and faster) result.
        //
        // By default, landmark detection is not enabled since it increases detection time.  We
        // enable it here in order to visualize detected landmarks.
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        // This is a temporary workaround for a bug in the face detector with respect to operating
        // on very small images.  This will be fixed in a future release.  But in the near term, use
        // of the SafeFaceDetector class will patch the issue.
        Detector<Face> safeDetector = new SafeFaceDetector(detector);

        // Create a frame from the bitmap and run face detection on the frame.
        //Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        Frame frame = new Frame.Builder().setBitmap(image).build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        if (!safeDetector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));


            }
        }

//        FaceView overlay = (FaceView) findViewById(R.id.faceView);
//        overlay.setContent(image, faces);
       // overlay.setContent(bitmap, faces);
        // Although detector may be used multiple times for different images, it should be released
        // when it is no longer needed in order to free native resources.
        safeDetector.release();
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.camera:
                intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, getCamera);
                break;
            case R.id.gallery:

                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/gallery");
                startActivityForResult(intent, getGallery);
                break;
            case R.id.open:
                sendData("1");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm=null;
        ByteArrayOutputStream stream =null;
        if(resultCode==RESULT_OK){
            switch(requestCode){
                case getCamera:

                    bm=(Bitmap) data.getExtras().get("data");
                    stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                    run(bm);
                    Glide.with(this)
                            .load(stream.toByteArray())
                            .asBitmap()
                            .transform(new FaceCenterCrop())
                            .into(imageView);
                    //이미지뷰
                    break;
                case getGallery:
                    try {
                        bm = MediaStore.Images.Media.getBitmap( getContentResolver(), data.getData());
                        stream = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }catch(OutOfMemoryError e){
                        Toast.makeText(getApplicationContext(), "이미지 용량이 너무 큽니다.", Toast.LENGTH_SHORT).show();
                    }
//                    run(bm);
                    Glide.with(this)

                            .load(stream.toByteArray())

                            .asBitmap()

                            .transform(new FaceCenterCrop())
                            .into(imageView);


                    break;

                default:

                    break;

            }
        }


    }
}

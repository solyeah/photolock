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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.face.bluetooth.Bluetooth;
import com.google.android.gms.samples.vision.face.firebase.Firebase;
import com.google.android.gms.samples.vision.face.firebase.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rohitarya.glide.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.glide.facedetection.transformation.core.GlideFaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates basic usage of the GMS vision face detector by running face landmark detection on a
 * photo and displaying the photo with associated landmarks in the UI.
 */
public class PhotoViewerActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "PhotoViewerActivity";
    Button gallery,camera,open;
    ImageView imageView;
    FirebaseUser user;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String imageFilePath;
    private Uri photoUri;


    private ValueEventListener mPostListener;
    private DatabaseReference mPostReference;



    //    private String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
//    ,Manifest.permission.READ_EXTERNAL_STORAGE};
//    private static final int MULTIPLE_PERMISSIONS = 101;
    static final int getCamera=2001;
    static final int getGallery=2002;
    static Bluetooth bluetooth;

    private StorageReference mStorageRef;
    static Firebase mFirebase;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideFaceDetector.releaseDetector();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
//        if(checkPermissions())
            init();
        setBluetooth();
        GlideFaceDetector.initialize(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String name = user.getEmail();
        }

        FirebaseDatabase.getInstance().getReference().child("server").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if(post.getUserName().equals(user.getEmail())){
                    Log.d("me", "equal mail");
                    if(post.getPermission().equals("y")){
                        Log.d("me", "yes");
                        sendData("1");
                    }else if(post.getPermission().equals("n")){
                        Log.d("me", "No");
                    }else{
                        Log.d("me", post.getPermission());
                    }
                }else{
                    Log.d("me", "not equal");
                    Log.d("me", post.getUserName());
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        mFirebase = new Firebase();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

//        mStorageRef = FirebaseStorage.getInstance().getReference();
//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                Post post = dataSnapshot.getValue(Post.class);
//                if(dataSnapshot.getValue() != null){
////                    Log.d("me", dataSnapshot.getValue().toString());
//                    Post post = dataSnapshot.getValue(Post.class);
//                    Log.w(TAG, "userName : "+ post.getUserName());
//                    Log.w(TAG, "permission : "+ post.getPermission());
//
//                    Toast.makeText(PhotoViewerActivity.this, "door OK.",
//                            Toast.LENGTH_SHORT).show();
//                }
//
//
//
//
//
//                // [START_EXCLUDE]
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//                // [START_EXCLUDE]
//                Toast.makeText(PhotoViewerActivity.this, "Failed to load post.",
//                        Toast.LENGTH_SHORT).show();
//                // [END_EXCLUDE]
//
//            }
//        };
//
//        mPostReference.addValueEventListener(postListener);


    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, getCamera);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }


        public void setBluetooth(){
        bluetooth = new Bluetooth(this);
        bluetooth.checkBluetooth();
    }
    public static void sendData(String param){
        bluetooth.sendData(param);
    }
//    private boolean checkPermissions() {
//        int result;
//        List<String> permissionList = new ArrayList<>();
//        for (String pm : permissions) {
//            result = ContextCompat.checkSelfPermission(this, pm);
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                permissionList.add(pm);
//            }
//        }
//        if (!permissionList.isEmpty()) {
//            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
//            return false;
//        }
//        return true;
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MULTIPLE_PERMISSIONS: {
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < permissions.length; i++) {
//                        if (permissions[i].equals(this.permissions[0])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//                                break;
//                            }
//                        }
//                        if(i==permissions.length-1)
//                            init();
//                    }
//                } else {
//                    showNoPermissionToastAndFinish();
//                }
//
//
//            }
//        }
//    }
//    private void showNoPermissionToastAndFinish() {
//        Toast.makeText(this,getString(R.string.limit),Toast.LENGTH_SHORT).show();
//        init();
//    }
    void init(){
        gallery=findViewById(R.id.gallery);
        gallery.setOnClickListener(this);
        camera=findViewById(R.id.camera);
        camera.setOnClickListener(this);
        imageView=findViewById(R.id.imageView);
        open=findViewById(R.id.open);
        open.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.camera:
                sendTakePhotoIntent();
                break;
            case R.id.gallery:

                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/gallery");
                startActivityForResult(intent, getGallery);
                break;
            case R.id.open:
 /*이미지 저장***************************************************************************************************
 * 현재시간(yyyyMMddHHmmss)을 이름으로 얼굴만 크롭해서 갤러리에 저장합니다. */
                Bitmap bitmap;
                FileOutputStream out;
                imageView.setDrawingCacheEnabled(false);
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                bitmap = imageView.getDrawingCache();
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String getTime = sdf.format(date);
                Log.d("fileName",getTime);

                try{
                    out=new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath()+"/"+getTime+".png"));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+Environment.getExternalStorageDirectory().getPath()+"/"+getTime+".png")));

                    Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();

                }
                catch(FileNotFoundException e)

                {
                    e.printStackTrace();
                }
/*블루투스 send***************************************************************************************************
* 아두이노에 도어락 열라는 신호!! */
//                sendData("1");

                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + getTime + ".png");


//                mFirebase.uploadImage(user.getEmail(), file);
                mFirebase.writeNewRequest(1, user.getEmail(), file.getName());
//                imageView.setImageResource(android.R.color.transparent);



//                Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+"/"+getTime+".png"));
//                StorageReference riversRef = mStorageRef.child("images/"+file.getLastPathSegment());
//
//                riversRef.putFile(file)
//                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                // Get a URL to the uploaded content
////                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                                Log.d("my", "success");
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception exception) {
//                                // Handle unsuccessful uploads
//                                // ...
//                                Log.d("my", "fail");
//                            }
//                        });
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm=null;
        ByteArrayOutputStream stream =null;
        if(resultCode==RESULT_OK){
            Drawable d;
//            Bitmap bitmap;
            FileOutputStream out;
            switch(requestCode){
                case getCamera:

                    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                    ExifInterface exif = null;

                    try {
                        exif = new ExifInterface(imageFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int exifOrientation;
                    int exifDegree;

                    if (exif != null) {
                        exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        exifDegree = exifOrientationToDegrees(exifOrientation);
                    } else {
                        exifDegree = 0;
                    }

                    imageView.setImageBitmap(rotate(bitmap, exifDegree));


                    bm=rotate(bitmap, exifDegree);
                    stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Glide.with(this)
                            .load(stream.toByteArray())
                            .asBitmap()
                            .transform(new FaceCenterCrop())
                            .into(imageView);


                    break;

//                    HERE=========================================================
//                    bm=(Bitmap) data.getExtras().get("data");
//                    stream = new ByteArrayOutputStream();
//                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                    Glide.with(this)
//                            .load(stream.toByteArray())
//                            .asBitmap()
//                            .transform(new FaceCenterCrop())
//                            .into(imageView);
//                    break;
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

package com.google.android.gms.samples.vision.face.firebase;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class Firebase {

    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;

    public Firebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = database.getReference("message");
    }

    public void uploadImage(String userName, File inputFile){
        Uri file = Uri.fromFile(inputFile);
        StorageReference riversRef = mStorageRef.child(userName+"/"+file.getLastPathSegment());

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
//                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("my", "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("my", "fail");
                    }
                });
    }

    public void writeNewRequest(int type, String userName, String imageName){
        Request request = new Request(type, userName, imageName);

//        mDatabase.setValue("Hello, World!");
        mDatabase.child("app").push().setValue(request);

    }


}

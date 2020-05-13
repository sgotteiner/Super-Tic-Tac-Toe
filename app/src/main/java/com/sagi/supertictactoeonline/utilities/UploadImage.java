package com.sagi.supertictactoeonline.utilities;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;

import java.io.ByteArrayOutputStream;

/**
 * Created by User on 01/03/2019.
 */

public class UploadImage {

    private String imageName;
    private Bitmap bitmap;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private IUploadImage mListener;

    public UploadImage(String imageName, Bitmap bitmap, IUploadImage mListener) {
        this.imageName = imageName;
        this.bitmap = bitmap;
        this.mListener = mListener;
    }

    public void startUpload() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask  = mStorageRef.child(FireBaseConstant.PROFILES).child(imageName).putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (mListener != null)
                    mListener.onSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (mListener != null)
                    mListener.onFail(e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                float progress = (taskSnapshot.getBytesTransferred()/(float)taskSnapshot.getTotalByteCount())*100;
                if (mListener != null)
                    mListener.onProgress((int)progress);
            }
        });


    }

    public interface IUploadImage {
        void onSuccess();

        void onFail(String error);

        void onProgress(int progress);
    }

}

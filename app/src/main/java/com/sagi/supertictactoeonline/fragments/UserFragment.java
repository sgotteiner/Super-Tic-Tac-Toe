package com.sagi.supertictactoeonline.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.interfaces.IUserFragmentGetEventFromMain;
import com.sagi.supertictactoeonline.utilities.DownloadImage;
import com.sagi.supertictactoeonline.utilities.ImageUtils;
import com.sagi.supertictactoeonline.utilities.Patch;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.UploadImage;
import com.sagi.supertictactoeonline.utilities.Utils;
import com.squareup.picasso.Picasso;

public class UserFragment extends Fragment implements IUserFragmentGetEventFromMain {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 20;
    private OnFragmentInteractionListener mListener;
    private Button btnSave;
    private ImageView imgProfilePicture;
    private final int IMG_FROM_GALLERY = 2;
    private Bitmap bitmapProfile = null;
    private User user;
    private TextView txtEmail, txtRank, txtName;
    private ProgressDialog progressDialogDownload;
    private ProgressDialog progressDialogUpload;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {

            boolean isOk = true;
            for (int i = 0; i < grantResults.length; i++) {
                boolean isAccept = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                if (!isAccept)
                    isOk = false;
            }
            if (isOk) {
                showGallery();
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = SharedPreferencesHelper.getInstance(getContext()).getUser();
        loadAllFields(view);
        loadImageProfile();
        setListeners();
    }

    private void showGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMG_FROM_GALLERY);
    }

    private void showDialogUpload() {
        progressDialogUpload = new ProgressDialog(getContext());
        progressDialogUpload.setMessage("Uploading...");
        progressDialogUpload.setTitle("Uploading started");
        progressDialogUpload.setCancelable(false);
        progressDialogUpload.setIcon(R.drawable.x); //TODO make logo
        progressDialogUpload.show();
    }

    private void showDialogDownload() {
        progressDialogDownload = new ProgressDialog(getContext());
        progressDialogDownload.setMessage("Downloading...");
        progressDialogDownload.setTitle("Downloading started");
        progressDialogDownload.setCancelable(false);
        progressDialogDownload.setIcon(R.drawable.x);
        progressDialogDownload.show();
    }

    private void uploadImage() {
        new UploadImage(Patch.PROFILES, user.getEmail(), bitmapProfile, new UploadImage.IUploadImage() {
            @Override
            public void onSuccess() {
                progressDialogUpload.dismiss();
                mListener.showHomePage();
            }

            @Override
            public void onFail(String error) {
                progressDialogUpload.dismiss();
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int progress) {
                progressDialogUpload.setMessage("Uploading " + progress + "%");
            }
        }).startUpload();
    }

    private void loadImageProfile() {
        showDialogDownload();
        new DownloadImage(Patch.PROFILES, user.getEmail(), new DownloadImage.IDownloadImage() {
            @Override
            public void onSuccess(Uri uri) {
                stopProgressBar();
                if (getContext() != null)
                    Picasso.with(getContext()).load(uri).fit().into(imgProfilePicture);
            }

            @Override
            public void onFail(String error) {
                stopProgressBar();
            }
        }).startLoading();
    }

    private void loadAllFields(View view) {
        txtName = view.findViewById(R.id.txtName);
        txtName.setText(getUserFirstNameWithUpperCase(user.getFirstName()));
        txtEmail = view.findViewById(R.id.txtEmail);
        txtEmail.setText(user.getEmail());
        txtRank = view.findViewById(R.id.txtRank);
        txtRank.setText(String.valueOf(user.getRank()));
        imgProfilePicture = view.findViewById(R.id.imgProfilePicture);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmapProfile != null) {
                    showDialogUpload();
                    mListener.updateProfile(user);
                    uploadImage();

                } else {
                    mListener.updateProfileWithoutBitmap(user);
                }
            }

        });
        imgProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showGallery();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
            }
        });
    }

    private String getUserFirstNameWithUpperCase(String firstName) {
        return Utils.geteFirstLettersUpperCase(firstName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_FROM_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            Uri uriImageGallery = data.getData();
            imgProfilePicture.setBackgroundResource(0);
            bitmapProfile = ImageUtils.handleImageGallery(uriImageGallery, getContext());
            imgProfilePicture.setImageBitmap(bitmapProfile);
            bitmapProfile = ImageUtils.scaleDown(bitmapProfile, 200, false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.registerEventFromMain(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.registerEventFromMain(null);
        mListener = null;
    }

    @Override
    public void onBackPressedInActivity() {
        mListener.showHomePage();
    }

    @Override
    public void onDownloadUri(Uri uriProfile) {
        Picasso.with(getContext()).load(uriProfile).fit().into(imgProfilePicture);
    }

    @Override
    public void stopProgressBar() {
        progressDialogDownload.dismiss();
    }

    public interface OnFragmentInteractionListener {
        void registerEventFromMain(IUserFragmentGetEventFromMain iUserFragmentGetEventFromMain);

        void updateProfileWithoutBitmap(User user);

        void showHomePage();

        void updateProfile(User user);
    }
}

package com.sagi.supertictactoeonline.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.interfaces.IWaitingProgressBar;
import com.sagi.supertictactoeonline.utilities.ImageUtils;
import com.sagi.supertictactoeonline.utilities.Utils;

import java.util.Calendar;

public class RegisterFragment extends Fragment implements IWaitingProgressBar {


    private static final int REQUEST_CODE_STORAGE_PERMISSION = 20;
    private EditText edtEmail, edtPass, edtFirstName, edtLastName;
    private CheckBox checkBoxRememberMe;
    private Button btnRegister, btnCancel, btnDate;
    private boolean isRememberMe = false;
    private long dateBirthDay = -1;
    private ImageView imgProfile;
    private final int PICK_IMAGE = 1;
    private Bitmap newProfilePic = null;
    private User user;
    private ProgressDialog progressDialog;


    private OnFragmentInteractionListener mListener;

    public RegisterFragment() {
        // Required empty public constructor
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
                getImageFromGallery();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getContext());
        loadViews(view);
        loadListeners();
    }

    private void loadViews(View view) {
        edtEmail = view.findViewById(R.id.edtEmailRegister);
        edtPass = view.findViewById(R.id.edtPassRgister);
        edtFirstName = view.findViewById(R.id.edtFirstName);
        edtLastName = view.findViewById(R.id.edtLastName);
        checkBoxRememberMe = view.findViewById(R.id.checkBoxRememberMeRegister);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDate = view.findViewById(R.id.btnDate);
        imgProfile = view.findViewById(R.id.imgProfile);

    }

    private void loadListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString().trim();
                String pass = edtPass.getText().toString().trim();
                String fName = edtFirstName.getText().toString().trim().toLowerCase();
                String lName = edtLastName.getText().toString().trim();

                if (!Utils.isValid(email, pass, fName, lName, dateBirthDay, getContext()))
                    return;
                user = new User(edtFirstName.getText().toString(),email, System.currentTimeMillis(),1000);
                showProgressDialod();
                mListener.createUser(user, edtPass.getText().toString(), isRememberMe, newProfilePic);
            }
        });
        checkBoxRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                isRememberMe = isCheck;
            }
        });
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dateBirthDay = Utils.getTimeStampFromDate(year, month, day);
                    }
                }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showLoginFragment();
            }
        });
    }

    private void showProgressDialod() {
        progressDialog.setMessage("try login your profile");
        progressDialog.setTitle("Waiting");
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.x);
        progressDialog.show();
    }

    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uriImageGallery = data.getData();

            imgProfile.setBackgroundResource(0);
            newProfilePic = ImageUtils.handleImageGallery(uriImageGallery, getContext());
            imgProfile.setImageBitmap(newProfilePic);
            newProfilePic = ImageUtils.scaleDown(newProfilePic, 200, false);

        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.registerEventFromRegisterLogin(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.registerEventFromRegisterLogin(null);
        mListener = null;
    }

    @Override
    public void stopProgressBar() {
        progressDialog.dismiss();
    }

    public interface OnFragmentInteractionListener {
        void createUser(User user, String password, boolean isRememberMe, Bitmap newProfilePic);

        void showLoginFragment();

        void registerEventFromRegisterLogin(IWaitingProgressBar iWaitingProgressBar);
    }

}

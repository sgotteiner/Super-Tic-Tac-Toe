package com.sagi.supertictactoeonline.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.utilities.ImageUtils;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.UploadImage;
import com.sagi.supertictactoeonline.R;

import static com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant.USERS_TABLE;

public class RegisterLoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private SignInButton btnSignIn;
    private ImageView imgProfile;
    private EditText edtNickname;
    private final int PICK_IMAGE = 1;
    private Bitmap newProfilePic = null;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 20;
    private DatabaseReference myRef;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog progressDialogUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);

        myRef = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (getIntent().getBooleanExtra("isSignOut", false))
            signOut();


        btnSignIn = findViewById(R.id.btnSignIn);
        edtNickname = findViewById(R.id.edtNickname);
        imgProfile = findViewById(R.id.imgProfile);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(RegisterLoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        || ContextCompat.checkSelfPermission(RegisterLoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut();
    }

    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uriImageGallery = data.getData();

            imgProfile.setBackgroundResource(0);
            newProfilePic = ImageUtils.handleImageGallery(uriImageGallery, this);
            imgProfile.setImageBitmap(newProfilePic);
            newProfilePic = ImageUtils.scaleDown(newProfilePic, 200, false);

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                boolean isNew;
                if (getIntent().getBundleExtra("isSignOut") == null)
                    isNew = true;
                else isNew = getIntent().getBooleanExtra("isSignOut", false);
                goToMain(account, isNew);
            }
        } catch (ApiException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SharedPreferencesHelper.getInstance(this).isAlreadyLogin())
            startActivity(new Intent(RegisterLoginActivity.this, MainActivity.class));
    }

    private void goToMain(final GoogleSignInAccount account, boolean isNew) {
        if (edtNickname.getText().toString().equals("")) {
            isNew = false;
        }
        if (isNew) {
            myRef.child(USERS_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isValidName = true;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        if (snapshot.getValue(User.class).getName().equals(edtNickname.getText().toString()) && !snapshot.getValue(User.class).getKey().equals(account.getId())) {
                            isValidName = false;
                            Toast.makeText(RegisterLoginActivity.this, "Name already exists", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    if (isValidName) {
                        User user = new User(edtNickname.getText().toString(), account.getId(), System.currentTimeMillis(), 1000);
                        myRef.child(USERS_TABLE).child(user.getName()).setValue(user);
                        SharedPreferencesHelper.getInstance(RegisterLoginActivity.this).setUser(user);
                        if (newProfilePic != null)
                            uploadBitmap(newProfilePic, user.getName());
                        else showMainActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            myRef.child(USERS_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        user = snapshot.getValue(User.class);
                        if (user.getKey().equals(account.getId())) {
                            SharedPreferencesHelper.getInstance(RegisterLoginActivity.this).setUser(user);
                            if (newProfilePic != null)
                                uploadBitmap(newProfilePic, user.getName());
                            else showMainActivity();
                            return;
                        }
                    }
                    Toast.makeText(RegisterLoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void showDialogUpload() {
        progressDialogUpload = new ProgressDialog(this);
        progressDialogUpload.setMessage("Uploading...");
        progressDialogUpload.setTitle("Uploading started");
        progressDialogUpload.setCancelable(false);
        progressDialogUpload.setIcon(R.drawable.x); //TODO make logo
        progressDialogUpload.show();
    }

    private void uploadBitmap(Bitmap bitmapProfile, String key) {
        showDialogUpload();
        new UploadImage(key, bitmapProfile, new UploadImage.IUploadImage() {
            @Override
            public void onSuccess() {
                progressDialogUpload.dismiss();
                showMainActivity();
            }

            @Override
            public void onFail(String error) {
                progressDialogUpload.dismiss();
                Toast.makeText(RegisterLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                showMainActivity();
            }

            @Override
            public void onProgress(int progress) {
                progressDialogUpload.setMessage("Uploading " + progress + "%");
            }
        }).startUpload();
    }

    private void showMainActivity() {
        SharedPreferencesHelper.getInstance(this).setIsAlreadyLogin(true);
        Intent intent = new Intent(RegisterLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

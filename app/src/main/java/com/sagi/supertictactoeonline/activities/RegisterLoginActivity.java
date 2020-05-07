package com.sagi.supertictactoeonline.activities;

import android.Manifest;
import android.app.Activity;
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
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.fragments.LoginFragment;
import com.sagi.supertictactoeonline.fragments.RegisterFragment;
import com.sagi.supertictactoeonline.interfaces.IWaitingProgressBar;
import com.sagi.supertictactoeonline.utilities.ImageUtils;
import com.sagi.supertictactoeonline.utilities.Patch;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.UploadImage;
import com.sagi.supertictactoeonline.utilities.Utils;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;
import com.sagi.supertictactoeonline.R;

import static com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant.USERS_TABLE;

public class RegisterLoginActivity extends AppCompatActivity
        implements RegisterFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private Fragment fragment;
    private SignInButton btnSignIn;
    private ImageView imgProfile;
    private EditText edtNickname;
    private final int PICK_IMAGE = 1;
    private Bitmap newProfilePic = null;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 20;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private IWaitingProgressBar iWaitingProgressBar;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);

        mAuth = FirebaseAuth.getInstance();
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

//        fragment = new LoginFragment();
//        showFragment(fragment);
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
            if (account != null){
                boolean isNew;
                if(getIntent().getBundleExtra("isSignOut") == null)
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
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null)
            goToMain(account, false);
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
                            uploadBitmap(newProfilePic, user.getName(), true);
                        showMainActivity(true);
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
                                uploadBitmap(newProfilePic, user.getName(), true);
                            showMainActivity(true);
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

    private void uploadBitmap(Bitmap bitmapProfile, String key, final boolean isRememberMe) {
        new UploadImage(Patch.PROFILES, key, bitmapProfile, new UploadImage.IUploadImage() {
            @Override
            public void onSuccess() {
                if (iWaitingProgressBar != null)
                    iWaitingProgressBar.stopProgressBar();
            }

            @Override
            public void onFail(String error) {
                if (iWaitingProgressBar != null)
                    iWaitingProgressBar.stopProgressBar();
            }

            @Override
            public void onProgress(int progress) {
            }
        }).startUpload();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.frameLayoutContainerLogin, fragment)
                .commit();
    }

    @Override
    public void createUser(final User user, String password, final boolean isRememberMe, final Bitmap newProfilePic) {
        mAuth.createUserWithEmailAndPassword(user.getName(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferencesHelper.getInstance(RegisterLoginActivity.this).setUser(user);
                            myRef.child(USERS_TABLE).child(user.getName()).setValue(user);
                            if (newProfilePic != null)
                                uploadBitmap(newProfilePic, user.getName(), isRememberMe);
                            else {
                                if (iWaitingProgressBar != null)
                                    iWaitingProgressBar.stopProgressBar();
                                showMainActivity(isRememberMe);
                            }
                        } else {
                            Toast.makeText(RegisterLoginActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void signIn(final String email, String password, final boolean isRememberMe) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getUserFromFirebase(email, isRememberMe);
                        } else {
                            Toast.makeText(RegisterLoginActivity.this, "ERROR USER NOT FOUND", Toast.LENGTH_LONG).show();
                            if (iWaitingProgressBar != null)
                                iWaitingProgressBar.stopProgressBar();
                        }
                    }
                });
    }

    private void getUserFromFirebase(String key, final boolean isRememberMe) {
        User user = new User();
        user.setName(key);
        myRef.child(FireBaseConstant.USERS_TABLE).child(user.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userProfile = dataSnapshot.getValue(User.class);
                SharedPreferencesHelper.getInstance(RegisterLoginActivity.this).setUser(userProfile);
                if (iWaitingProgressBar != null)
                    iWaitingProgressBar.stopProgressBar();
                showMainActivity(isRememberMe);
                Toast.makeText(RegisterLoginActivity.this, "OK", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegisterLoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                if (iWaitingProgressBar != null)
                    iWaitingProgressBar.stopProgressBar();
            }
        });
    }

    @Override
    public void showRegisterFragment() {
        fragment = new RegisterFragment();
        showFragment(fragment);
    }

    @Override
    public void showLoginFragment() {
        fragment = new LoginFragment();
        showFragment(fragment);
    }

    @Override
    public void registerEventFromRegisterLogin(IWaitingProgressBar iWaitingProgressBar) {
        this.iWaitingProgressBar = iWaitingProgressBar;
    }

    private void showMainActivity(boolean isRememberMe) {
//        SharedPreferencesHelper.getInstance(this).setIsAlreadyLogin(isRememberMe);
        Intent intent = new Intent(RegisterLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

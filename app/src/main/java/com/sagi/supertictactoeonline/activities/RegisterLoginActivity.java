package com.sagi.supertictactoeonline.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
import com.sagi.supertictactoeonline.fragments.UserFragment;
import com.sagi.supertictactoeonline.interfaces.IUserFragmentGetEventFromMain;
import com.sagi.supertictactoeonline.interfaces.IWaitingProgressBar;
import com.sagi.supertictactoeonline.utilities.Patch;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.UploadImage;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;
import com.sagi.supertictactoeonline.R;

import static com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant.USERS_TABLE;

public class RegisterLoginActivity extends AppCompatActivity
        implements RegisterFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener {

    private Fragment fragment;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private IWaitingProgressBar iWaitingProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        fragment = new LoginFragment();
        showFragment(fragment);
    }


    private void uploadBitmap(Bitmap bitmapProfile, User user, final boolean isRememberMe) {
        new UploadImage(Patch.PROFILES, user.getEmail(), bitmapProfile, new UploadImage.IUploadImage() {
            @Override
            public void onSuccess() {
                if (iWaitingProgressBar != null)
                    iWaitingProgressBar.stopProgressBar();
                showMainActivity(isRememberMe);
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
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferencesHelper.getInstance(RegisterLoginActivity.this).setUser(user);
                            myRef.child(USERS_TABLE).child(user.textEmailForFirebase()).setValue(user);
                            if (newProfilePic != null)
                                uploadBitmap(newProfilePic, user, isRememberMe);
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

    private void getUserFromFirebase(String email, final boolean isRememberMe) {
        User user = new User();
        user.setEmail(email);
        myRef.child(FireBaseConstant.USERS_TABLE).child(user.textEmailForFirebase()).addListenerForSingleValueEvent(new ValueEventListener() {
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
        SharedPreferencesHelper.getInstance(this).setIsAlreadyLogin(isRememberMe);
        Intent intent = new Intent(RegisterLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

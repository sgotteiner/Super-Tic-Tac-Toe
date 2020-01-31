package com.sagi.supertictactoeonline.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.Game;
import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.fragments.HomeFragment;
import com.sagi.supertictactoeonline.fragments.PlayFragment;
import com.sagi.supertictactoeonline.fragments.UserFragment;
import com.sagi.supertictactoeonline.interfaces.IPlayFragmentUpdateGameChanges;
import com.sagi.supertictactoeonline.interfaces.IUserFragmentGetEventFromMain;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.constants.Constants;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;

import static com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant.USERS_TABLE;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        PlayFragment.OnFragmentInteractionListener,
        UserFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener {

    private Fragment fragment;
    private StorageReference mStorageRef;
    private DatabaseReference myRef;
    private NavigationView mNavigationView;
    private IUserFragmentGetEventFromMain iUserFragmentGetEventFromMain;
    private IPlayFragmentUpdateGameChanges iPlayFragmentUpdateGameChanges;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawerListener(drawer);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        fragment = new HomeFragment();
        showFragment(fragment);
    }

    private void drawerListener(final DrawerLayout drawer) {
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                closeKeyBoard();
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });
    }

    public void closeKeyBoard() {
        if (getCurrentFocus() == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            if (fragment instanceof HomeFragment)
                return false;
            fragment = new HomeFragment();
            showFragment(fragment);
        } else if (id == R.id.nav_profile) {
            if (fragment instanceof UserFragment)
                return false;
            fragment = new UserFragment();
            showFragment(fragment);
        } else if (id == R.id.nav_logout) {
            logOutFromApp();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.frameLayoutContainerMain, fragment)
                .commit();
    }

    private void logOutFromApp() {
        updateIsUserActiveInApp(false);
        SharedPreferencesHelper.getInstance(this).resetSharedPreferences();
        Intent intent = new Intent(this, RegisterLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateIsUserActiveInApp(boolean isUserActive) {
        User user = SharedPreferencesHelper.getInstance(MainActivity.this).getUser();
        if (user == null)
            return;
        myRef.child(USERS_TABLE).child(user.textEmailForFirebase()).child(FireBaseConstant.IS_USER_ACTIVE).setValue(isUserActive);
    }

    @Override
    public void rematch(Constants.MODE mode, OnlineGame game, int level) {
        showFragment(PlayFragment.newInstance(mode, game, level));
    }

    @Override
    public void updateGameState(OnlineGame game) {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(game.getKeyGame()).setValue(game);
    }

    @Override
    public void listenToGame(String keyGame) {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(keyGame).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                OnlineGame game = dataSnapshot.getValue(OnlineGame.class);
                iPlayFragmentUpdateGameChanges.updateGameChanges(game);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void registerGameEvent(IPlayFragmentUpdateGameChanges iPlayFragmentUpdateGameChanges) {
        this.iPlayFragmentUpdateGameChanges=iPlayFragmentUpdateGameChanges;
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void registerEventFromMain(IUserFragmentGetEventFromMain iUserFragmentGetEventFromMain) {
        this.iUserFragmentGetEventFromMain = iUserFragmentGetEventFromMain;
    }

    public void updateProfile(User user) {
        myRef.child(USERS_TABLE).child(user.textEmailForFirebase()).setValue(user);
    }

    @Override
    public void updateProfileWithoutBitmap(User user) {
        myRef.child(USERS_TABLE).child(user.textEmailForFirebase()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (iUserFragmentGetEventFromMain != null) {
                    iUserFragmentGetEventFromMain.stopProgressBar();
                    fragment = new HomeFragment();
                    showFragment(fragment);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (iUserFragmentGetEventFromMain != null) {
                    iUserFragmentGetEventFromMain.stopProgressBar();
                    fragment = new HomeFragment();
                    showFragment(fragment);
                }
            }
        });
    }

    @Override
    public void showHomePage() {
        fragment = new PlayFragment();
        showFragment(fragment);
    }

    @Override
    public void showPlayFragment(Constants.MODE mode, OnlineGame game, int level) {
        showFragment(PlayFragment.newInstance(mode, game, level));
    }

    @Override
    public OnlineGame findGame() {
        final OnlineGame[] game = new OnlineGame[1];
        myRef.child(FireBaseConstant.GAMES_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    game[0] = snapshot.getValue(OnlineGame.class);
                    if (game[0].getGetEmailPlayer2().equals(""))
                        return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (game[0] != null)
            if (!game[0].getGetEmailPlayer2().equals("")) {
                game[0].setPlayer2Connected(true);
                return game[0];
            }
        String keyGame = myRef.child(FireBaseConstant.GAMES_TABLE).push().getKey();
        game[0] = new OnlineGame(14, keyGame,
                SharedPreferencesHelper.getInstance(this).getUser().getEmail());
        myRef.child(FireBaseConstant.GAMES_TABLE).child(keyGame).setValue(game[0]);
        return game[0];
    }


}

package com.sagi.supertictactoeonline.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

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
import com.sagi.supertictactoeonline.BuildConfig;
import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.fragments.HomeFragment;
import com.sagi.supertictactoeonline.fragments.PlayFragment;
import com.sagi.supertictactoeonline.fragments.UserFragment;
import com.sagi.supertictactoeonline.interfaces.IHomePage;
import com.sagi.supertictactoeonline.interfaces.IPlayFragmentUpdateGameChanges;
import com.sagi.supertictactoeonline.interfaces.IUserFragmentGetEventFromMain;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.Utils;
import com.sagi.supertictactoeonline.utilities.constants.Constants;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;

import java.util.ArrayList;

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
    private IHomePage iHomePage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawerListener(drawer);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        if (intent != null) {
            String key = intent.getDataString();
            if (key == null)
                showHomePage();
            else
                openGame(key.split("=")[1]);
        }
    }

    private void openGame(String key) {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(FireBaseConstant.FRIENDS_GAMES_TABLE)
                .child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                OnlineGame game = dataSnapshot.getValue(OnlineGame.class);
                if (game != null)
                    joinGame(game, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        this.fragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainerMain, fragment)
                .commitAllowingStateLoss();
    }

    private void logOutFromApp() {
        updateIsUserActiveInApp(false);
        SharedPreferencesHelper.getInstance(this).resetSharedPreferences();
        Intent intent = new Intent(this, RegisterLoginActivity.class);
        intent.putExtra("isSignOut", true);
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
    public void rematch(OnlineGame game) {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(game.getKeyGame()).setValue(game);
    }

    @Override
    public void updateGameState(String key, int moveId, boolean isRandom) {
        String gameTable = gameTable(isRandom);
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable).child(key).child("lastMoveId").setValue(moveId);
    }

    private ValueEventListener valueEventListenerGame = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            OnlineGame game = dataSnapshot.getValue(OnlineGame.class);
            if (iPlayFragmentUpdateGameChanges != null)
                iPlayFragmentUpdateGameChanges.updateGameChanges(game);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void listenToGame(String keyGame, boolean isRandom) {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable(isRandom)).child(keyGame).addValueEventListener(valueEventListenerGame);
    }

    private String gameTable(boolean isRandom) {
        return isRandom ? FireBaseConstant.RANDOM_GAMES_TABLE : FireBaseConstant.FRIENDS_GAMES_TABLE;
    }

    @Override
    public void leaveGame(final String keyGame, boolean isX, Constants.MODE mode, final boolean isRandom) {
        if (mode == Constants.MODE.ONLINE) {
            myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable(isRandom)).child(keyGame).removeEventListener(valueEventListenerGame);
            notifyGameILeft(keyGame, isX, isRandom);
            myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable(isRandom)).child(keyGame).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    OnlineGame game = dataSnapshot.getValue(OnlineGame.class);
                    if (game != null)
                        if (!game.isPlayer1Connected() && !game.isPlayer2Connected())
                            deleteGame(keyGame, isRandom);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        showHomePage();
    }

    private void notifyGameILeft(String keyGame, boolean isX, boolean isRandom) {
        String player = isX ? "player1Connected" : "player2Connected";
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable(isRandom)).child(keyGame).child(player).setValue(false);
    }

    private void deleteGame(String keyGame, boolean isRandom) {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable(isRandom)).child(keyGame).removeValue();
    }

    @Override
    public void updateScore(int rank1, int rank2, String email2) {
        myRef.child(USERS_TABLE).child(SharedPreferencesHelper.getInstance(this).getUser().textEmailForFirebase()).child("rank").setValue(rank1);
        myRef.child(USERS_TABLE).child(email2).child("rank").setValue(rank2);
    }

    @Override
    public void getOtherPlayer(String email) {
        myRef.child(USERS_TABLE).child(Utils.textEmailForFirebase(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                iPlayFragmentUpdateGameChanges.setOtherPlayer(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void registerGameEvent(IPlayFragmentUpdateGameChanges iPlayFragmentUpdateGameChanges) {
        this.iPlayFragmentUpdateGameChanges = iPlayFragmentUpdateGameChanges;
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
        showFragment(new HomeFragment());
    }

    @Override
    public void showPlayFragment(Constants.MODE mode, OnlineGame game, int level, boolean isRandom, long startTimeMillis) {
        showFragment(PlayFragment.newInstance(mode, game, level, isRandom, startTimeMillis));
    }

    @Override
    public void findGame(final long startTimeMillis) {
        final String[] key = new String[1];
        myRef.child(FireBaseConstant.GAMES_TABLE).child(FireBaseConstant.RANDOM_GAMES_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isFound = false;
                OnlineGame game;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    game = snapshot.getValue(OnlineGame.class);
                    if (!game.isPlayer2Connected() && game.getStartTimeMillis() == startTimeMillis) {
                        joinGame(game, true);
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    key[0] = myRef.child(FireBaseConstant.GAMES_TABLE).push().getKey();
                    createGame(SharedPreferencesHelper.getInstance(MainActivity.this).getUser(), key[0], true, startTimeMillis);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void joinGame(OnlineGame game, boolean isRandom) {
        String email = SharedPreferencesHelper.getInstance(MainActivity.this).getUser().getEmail();
        game.setEmailPlayer2(email);
        game.setPlayer2Connected(true);
        String gameTable = gameTable(isRandom);
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable).child(game.getKeyGame()).child("emailPlayer2").setValue(email);
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable).child(game.getKeyGame()).child("player2Connected").setValue(true);
        showPlayFragment(Constants.MODE.ONLINE, game, 0, isRandom, game.getStartTimeMillis());
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable).child(game.getKeyGame()).child("player1Connected").addValueEventListener(valueEventListenerConnection);
    }

    private void createGame(User user, String key, boolean isRandom, long startTimeMillis) {
        OnlineGame game = new OnlineGame(14, key,
                user.getEmail(), startTimeMillis);
        String gameTable = gameTable(isRandom);
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable).child(key).setValue(game);
        showPlayFragment(Constants.MODE.ONLINE, game, 0, isRandom, startTimeMillis);
        myRef.child(FireBaseConstant.GAMES_TABLE).child(gameTable).child(key).child("player2Connected").addValueEventListener(valueEventListenerConnection);
    }

    private ValueEventListener valueEventListenerConnection = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (iPlayFragmentUpdateGameChanges != null)
                iPlayFragmentUpdateGameChanges.onOtherPlayerConnectionEvent(dataSnapshot.getValue(boolean.class));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void sendInvitation(User user, String email, boolean isWhatsapp, long startTimeMillis) {
        String key = Utils.textEmailForFirebase(email) + "/" + user.textEmailForFirebase();
        createGame(user, key, false, startTimeMillis);
        if (isWhatsapp)
            sendWhatsappLink(key);
    }

    private void sendWhatsappLink(String key) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage("com.whatsapp");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Super Tic Tac Toe Online");
        String strShareMessage = "Super Tic Tac Toe\nJoin " + SharedPreferencesHelper.getInstance(this).getUser().getFirstName() + " here\n";
        strShareMessage += "www.supertictactoeonline.com/play=" + key;
        strShareMessage += "\n\nor if you haven't downloaded the app yet you can do it here\n" +
                "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.super_tic_tac_toe_online_icon);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), logo, "title", null);
        if (path != null) {
            Uri logoUri = Uri.parse(path);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, logoUri);
        }
        intent.putExtra(Intent.EXTRA_TEXT, strShareMessage);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(intent, "whatsapp"));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void loadFriends() {
        myRef.child(FireBaseConstant.FRIENDS_TABLE).child(SharedPreferencesHelper.getInstance(this).getUser().textEmailForFirebase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<User> users = new ArrayList<>();
                long i = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final long finalI = --i;
                    myRef.child(USERS_TABLE).child(snapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            users.add(dataSnapshot.getValue(User.class));
                            if (finalI == 0)
                                if (iHomePage != null)
                                    iHomePage.setFriends(users);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void loadInvitations() {
        myRef.child(FireBaseConstant.GAMES_TABLE).child(FireBaseConstant.FRIENDS_GAMES_TABLE)
                .child(SharedPreferencesHelper.getInstance(this).getUser().textEmailForFirebase()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<User> users = new ArrayList<>();
                long i = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final long finalI = --i;
                    myRef.child(USERS_TABLE).child(snapshot.getValue(OnlineGame.class).getKeyGame().split("/")[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            users.add(dataSnapshot.getValue(User.class));
                            if (finalI == 0)
                                if (iHomePage != null)
                                    iHomePage.setInvitations(users);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void joinGame(final User user, final String email) {
        myRef.child(FireBaseConstant.FRIENDS_TABLE).child(user.textEmailForFirebase()).child(Utils.textEmailForFirebase(email)).setValue(Utils.textEmailForFirebase(email));
        myRef.child(FireBaseConstant.FRIENDS_TABLE).child(Utils.textEmailForFirebase(email)).child(user.textEmailForFirebase()).setValue(user.textEmailForFirebase());
        myRef.child(FireBaseConstant.GAMES_TABLE).child(FireBaseConstant.FRIENDS_GAMES_TABLE).child(user.textEmailForFirebase()).child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                joinGame(dataSnapshot.getValue(OnlineGame.class), false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void registerEventFromMain(IHomePage iHomePage) {
        this.iHomePage = iHomePage;
    }
}

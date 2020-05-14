package com.sagi.supertictactoeonline.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.adapters.AdapterUser;
import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.interfaces.IHomePage;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.constants.Constants;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements IHomePage {

    private OnFragmentInteractionListener mListener;
    private ImageView imgPlayOfflineFriend, imgPlayOnlineRandom, imgPlayOnlineFriend, imgPlayOfflineComputer;
    private Spinner spnTimeLimit;
    public static final String[] TIME_LIMIT = new String[]{"none", "1 minute", "3 minutes", "5 minutes"};

    public HomeFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadViews(view);
        loadListeners();
    }

    private void loadViews(View view) {
        imgPlayOfflineFriend = view.findViewById(R.id.imgPlayOfflineFriend);
        imgPlayOnlineRandom = view.findViewById(R.id.imgPlayOnlineRandom);
        imgPlayOnlineFriend = view.findViewById(R.id.imgPlayOnlineFriend);
        imgPlayOfflineComputer = view.findViewById(R.id.imgPlayOfflineComputer);
        spnTimeLimit = view.findViewById(R.id.spnTimeLimit);
        ArrayAdapter<String> adapterTimeLimit = new ArrayAdapter<>(getContext(), R.layout.item_spinner, TIME_LIMIT);
        spnTimeLimit.setAdapter(adapterTimeLimit);
        spnTimeLimit.setSelection(SharedPreferencesHelper.getInstance(getContext()).getStartTime());
    }

    private void loadListeners() {
        imgPlayOfflineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showPlayFragment(Constants.MODE.OFFLINE_FRIEND, null, 0, false, startTimeMillis());
            }
        });
        imgPlayOnlineRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.findGame(startTimeMillis());
            }
        });
        imgPlayOnlineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialOnlineFriendDialog();
                dialogOnlineFriendGame.show();
            }
        });
        imgPlayOfflineComputer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showPlayFragment(Constants.MODE.OFFLINE_COMPUTER, null, 2, false, 0);
            }
        });
        spnTimeLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferencesHelper.getInstance(getContext()).setStartTime(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private long startTimeMillis() {
        switch ((String)spnTimeLimit.getSelectedItem()){
            case "1 minute":
                return 60000;
            case "3 minutes":
                return 180000;
            case "5 minutes":
                return 300000;
        }
        return 0;
    }

    private Dialog dialogOnlineFriendGame;
    private TextView txtJoin;
    private CheckBox checkboxWhatsappInvitation;
    private RecyclerView recyclerJoin, recyclerInvite;
    private AdapterUser adapterJoin, adapterInvite;
    private ArrayList<User> arrJoin = new ArrayList<>(), arrFriends = new ArrayList<>();

    private void initialOnlineFriendDialog() {
        dialogOnlineFriendGame = new Dialog(getContext());
        dialogOnlineFriendGame.setContentView(R.layout.dialog_online_friend_game);
        txtJoin = dialogOnlineFriendGame.findViewById(R.id.txtJoin);
        checkboxWhatsappInvitation = dialogOnlineFriendGame.findViewById(R.id.checkboxWhatsappInvitation);
        txtJoin.setVisibility(View.GONE);
        adapterJoin = new AdapterUser(arrJoin, getContext(), new AdapterUser.CallbackAdapterUser() {
            @Override
            public void onPick(User user, boolean isJoin) {
                mListener.joinGame(user.getName());
                dialogOnlineFriendGame.dismiss();
            }
        }, true);
        recyclerJoin = dialogOnlineFriendGame.findViewById(R.id.recyclerJoin);
        recyclerJoin.setHasFixedSize(true);
        recyclerJoin.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerJoin.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerJoin.setAdapter(adapterJoin);
        adapterInvite = new AdapterUser(arrFriends, getContext(), new AdapterUser.CallbackAdapterUser() {
            @Override
            public void onPick(User user, boolean isJoin) {
                mListener.sendInvitation(user.getName(), checkboxWhatsappInvitation.isChecked(), startTimeMillis());
                dialogOnlineFriendGame.dismiss();
            }
        }, false);
        recyclerInvite = dialogOnlineFriendGame.findViewById(R.id.recyclerInvite);
        recyclerInvite.setHasFixedSize(true);
        recyclerInvite.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerInvite.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerInvite.setAdapter(adapterInvite);
        final EditText edtSearch = dialogOnlineFriendGame.findViewById(R.id.edtSearch);
        ImageView imgSearch = dialogOnlineFriendGame.findViewById(R.id.imgSearch);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.sendInvitation(edtSearch.getText().toString(), checkboxWhatsappInvitation.isChecked(),
                        startTimeMillis());
                dialogOnlineFriendGame.dismiss();
                mListener.closeKeyBoard();
            }
        });
        mListener.loadInvitations();
        mListener.loadFriends();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
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
    public void onResume() {
        super.onResume();
        if (getContext() instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) getContext();
            mListener.registerEventFromMain(this);
        } else {
            throw new RuntimeException(getContext().toString()
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
    public void setGame(OnlineGame game, boolean isRandom) {
        mListener.showPlayFragment(Constants.MODE.ONLINE, game, 0, isRandom, startTimeMillis());
    }

    @Override
    public void setFriends(ArrayList<User> friends) {
        arrFriends.clear();
        arrFriends.addAll(friends);
        updateFriends();
    }

    private void updateFriends() {
        for (int i = 0; i < arrJoin.size(); i++) {
            for (int j = 0; j < arrFriends.size(); j++) {
                if (arrJoin.get(i).getName().equals(arrFriends.get(j).getName())) {
                    arrFriends.remove(i);
                }
            }
        }
        adapterInvite.notifyDataSetChanged();
    }

    @Override
    public void setInvitations(ArrayList<User> invitations) {
        if (txtJoin == null)
            return;
        txtJoin.setVisibility(View.VISIBLE);
        arrJoin.clear();
        arrJoin.addAll(invitations);
        adapterJoin.notifyDataSetChanged();
        updateFriends();
    }

    public interface OnFragmentInteractionListener {

        void showPlayFragment(Constants.MODE mode, OnlineGame game, int level, boolean isRandom, long startTimeMillis);

        void findGame(long l);

        void registerEventFromMain(IHomePage iHomePage);

        void sendInvitation(String name, boolean isWhatsapp, long startTimeMillis);

        void loadFriends();

        void loadInvitations();

        void joinGame(String name);

        void closeKeyBoard();
    }
}

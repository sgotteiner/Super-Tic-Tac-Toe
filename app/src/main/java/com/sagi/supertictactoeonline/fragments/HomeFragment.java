package com.sagi.supertictactoeonline.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.Game;
import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.utilities.constants.Constants;

public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ImageView imgPlayOfflineFriend, imgPlayOnline, imgPlayOfflineComputer;

    public HomeFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadViews(view);
        loadListeners();
    }

    private void loadViews(View view){
        imgPlayOfflineFriend = view.findViewById(R.id.imgPlayOfflineFriend);
        imgPlayOnline = view.findViewById(R.id.imgPlayOnline);
        imgPlayOfflineComputer = view.findViewById(R.id.imgPlayOfflineComputer);
    }

    private void loadListeners() {
        imgPlayOfflineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showPlayFragment(Constants.MODE.OFFLINE_FRIEND, null, 0);
            }
        });
        imgPlayOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnlineGame game = mListener.findGame();
                mListener.showPlayFragment(Constants.MODE.ONLINE, game, 0);
            }
        });
        imgPlayOfflineComputer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showPlayFragment(Constants.MODE.OFFLINE_COMPUTER, null, 2);
            }
        });
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
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {

        void showPlayFragment(Constants.MODE mode, OnlineGame game, int level);

        OnlineGame findGame();
    }
}

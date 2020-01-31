package com.sagi.supertictactoeonline.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.ComputerGame;
import com.sagi.supertictactoeonline.entities.Game;
import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.interfaces.IPlayFragmentUpdateGameChanges;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.constants.Constants;

import java.io.Serializable;

public class PlayFragment extends Fragment implements View.OnClickListener,
    IPlayFragmentUpdateGameChanges {

    private static final String GAME = "key";
    private static String MODE = "mode";
    private static String LEVEL = "level";
    private String keyGame;
    private Constants.MODE mode;
    private Game game;
    int boardSize;
    int sign;
    private PlayViewModel mViewModel;
    private TableLayout tlBoard;
    int bestMoveId = 0;
    private ImageView imgLastTurn;
    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.play_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadBundle();
//        initialGame();
        boardSize = game.getBoardSize();
        tlBoard = view.findViewById(R.id.tlBoard);
        initialBoard();
    }

    private void initialGame() {
        switch (mode) {
            case OFFLINE_FRIEND:
                game = new Game(14);
                break;
            case OFFLINE_COMPUTER:
                game = new ComputerGame(14);
                break;
            case ONLINE:
                game = new OnlineGame(14, keyGame,
                        SharedPreferencesHelper.getInstance(getContext()).getUser().getEmail());
                mListener.listenToGame(((OnlineGame)game).getKeyGame());
                break;
        }
    }

    public static PlayFragment newInstance(Constants.MODE mode, OnlineGame game, int level) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MODE, mode);
        bundle.putSerializable(GAME, (Serializable) game);
        bundle.putInt(LEVEL, level);
        PlayFragment fragment = new PlayFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void loadBundle() {
        Bundle bundle = getArguments();
        game = (OnlineGame) bundle.getSerializable(GAME);
        mode = (Constants.MODE) bundle.getSerializable(MODE);
    }

    private void initialBoard() {
        for (int i = 0; i < boardSize; i++) {
            TableRow tableRow = new TableRow(getContext());

            for (int j = 0; j < boardSize; j++) {
                final ImageView imgSign = new ImageView(getContext());
                imgSign.setBackgroundResource(R.drawable.empty_cell);
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                int pixels = display.getWidth();
                imgSign.setLayoutParams(new TableRow.LayoutParams(pixels / boardSize, pixels / boardSize));
                imgSign.setId(i * boardSize + j);
                imgSign.setOnClickListener(this);
                tableRow.addView(imgSign);
            }
            tlBoard.addView(tableRow);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int i = id / boardSize;
        int j = id % boardSize;
        makeTurn(i, j);
        if (mode == Constants.MODE.OFFLINE_COMPUTER) {
            int moveId = ((ComputerGame) game).getNextMoveId();
            makeTurn(moveId / boardSize, moveId % boardSize);
        }
        if (mode == Constants.MODE.ONLINE) {
            mListener.updateGameState((OnlineGame)game);
        }
    }

    private void makeTurn(int i, int j) {
        sign = game.getSign(i, j);
        game.makeTurn(i, j);
        changeGUI(i, j);
    }

    private void changeGUI(int i, int j) {
        ImageView imgSign = tlBoard.findViewById(i * boardSize + j);
        if (sign == 0) {
            if (imgLastTurn != null)
                imgLastTurn.setBackgroundColor(Color.WHITE);
            imgSign.setBackgroundColor(Color.YELLOW);

            if (!game.isXTurn())
                imgSign.setImageResource(R.drawable.x);
            else
                imgSign.setImageResource(R.drawable.o);
            if (game.isOver())
                win();
            imgLastTurn = imgSign;
        } else
            Toast.makeText(getContext(), "cant", Toast.LENGTH_SHORT).show();
    }

    private void win() {
        String winner;
        if (!game.isXTurn()) {
            winner = "X";
        } else
            winner = "O";
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(R.drawable.trophy);
        builder.setTitle(winner + " won!!!");
        builder.setPositiveButton("rematch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mode == Constants.MODE.ONLINE)
                    if (mListener != null) {
                        game.setXTurn(true);
                        game.setOver(false);
                        mListener.rematch(mode, (OnlineGame) game, 2);
                    } else if (mListener != null)
                        mListener.rematch(mode, null, 2);
            }
        });
        builder.create().show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.registerGameEvent(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.registerGameEvent(null);
        mListener = null;
    }

    @Override
    public void updateGameChanges(OnlineGame game) {
        int move = game.getLastMoveId();
        makeTurn(move/boardSize,move%boardSize);
    }

    public interface OnFragmentInteractionListener {
        void rematch(Constants.MODE mode, OnlineGame game, int level);

        void updateGameState(OnlineGame game);

        void listenToGame(String keyGame);

        void registerGameEvent(IPlayFragmentUpdateGameChanges iPlayFragmentUpdateGameChanges);
    }
}

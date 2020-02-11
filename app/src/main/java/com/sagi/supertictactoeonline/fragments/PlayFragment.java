package com.sagi.supertictactoeonline.fragments;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.ComputerGame;
import com.sagi.supertictactoeonline.entities.Game;
import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.interfaces.IPlayFragmentUpdateGameChanges;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.constants.Constants;

import java.io.Serializable;

public class PlayFragment extends Fragment implements View.OnClickListener,
        IPlayFragmentUpdateGameChanges {

    private static final String GAME = "key";
    private static String MODE = "mode";
    private static String LEVEL = "level";
    private boolean isX;
    private Constants.MODE mode;
    private Game game;
    int boardSize;
    int sign;
    private PlayViewModel mViewModel;
    private TableLayout tlBoard;
    private ImageView imgLastTurn;
    private OnFragmentInteractionListener mListener;
    private User otherPlayer;
    private boolean isGameStarted = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.play_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadBundle();
        boardSize = game.getBoardSize();
        tlBoard = view.findViewById(R.id.tlBoard);
        initialBoard();
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
        mode = (Constants.MODE) bundle.getSerializable(MODE);
        game = (OnlineGame) bundle.getSerializable(GAME);
        if (game == null)
            if (mode == Constants.MODE.OFFLINE_FRIEND)
                game = new Game(14);
            else game = new ComputerGame(14);
        else {
            if (!((OnlineGame) game).getEmailPlayer1().equals("") && !((OnlineGame) game).getEmailPlayer2().equals(""))
                isGameStarted = true;
            isX = ((OnlineGame) game).getEmailPlayer1().equals(SharedPreferencesHelper.getInstance(getContext()).getUser().getEmail());
            mListener.listenToGame(((OnlineGame) game).getKeyGame());
        }
        game.initialSigns();
        game.initialWeights();
    }

    private void initialBoard() {
        for (int i = 0; i < boardSize; i++) {
            TableRow tableRow = new TableRow(getContext());

            for (int j = 0; j < boardSize; j++) {
                final ImageView imgSign = new ImageView(getContext());
                imgSign.setBackgroundResource(R.drawable.empty_cell);
//                imgSign.setBackgroundColor(Color.WHITE);
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
        if (!isGameStarted) {
            Toast.makeText(getContext(), "Other player is not connected yet", Toast.LENGTH_SHORT).show();
            return;
        }
        int id = v.getId();
        int i = id / boardSize;
        int j = id % boardSize;
        if (game.isLegalMove(i, j)) {
            if (game.isXTurn() != isX && mode == Constants.MODE.ONLINE) {
                Toast.makeText(getContext(), "not your turn", Toast.LENGTH_SHORT).show();
                return;
            }
            makeTurn(i, j);
            if (mode == Constants.MODE.OFFLINE_COMPUTER) {
                int moveId = ((ComputerGame) game).getNextMoveId();
                makeTurn(moveId / boardSize, moveId % boardSize);
            }
            if (mode == Constants.MODE.ONLINE) {
                mListener.updateGameState(((OnlineGame) game).getKeyGame(), id);
            }
        } else Toast.makeText(getContext(), "can't", Toast.LENGTH_SHORT).show();
    }

    private void makeTurn(int i, int j) {
        game.makeTurn(i, j);
        sign = game.getSign(i, j);
        changeGUI(i, j);
    }

    private void changeGUI(int i, int j) {
        ImageView imgSign = tlBoard.findViewById(i * boardSize + j);
        if (imgLastTurn != null)
            imgLastTurn.setBackgroundColor(Color.WHITE);
        imgSign.setBackgroundColor(Color.YELLOW);

        if (!game.isXTurn())
            imgSign.setImageResource(R.drawable.x);
        else
            imgSign.setImageResource(R.drawable.o);
        if (game.isOver())
            win(false);
        imgLastTurn = imgSign;
    }

    private void win(final boolean isOtherPlayerLeft) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_win);
        TextView txtTitleWin = dialog.findViewById(R.id.txtTitleWin);
        txtTitleWin.setText(getWinnerName());
        if (game instanceof OnlineGame) {
            mListener.updateScore(SharedPreferencesHelper.getInstance(getContext()).getUser(), otherPlayer, !((OnlineGame) game).isXTurn());
        }
        TextView txtRematch = dialog.findViewById(R.id.txtRematch);
        if (isOtherPlayerLeft)
            txtRematch.setVisibility(View.INVISIBLE);
        else
            txtRematch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    game.setXTurn(true);
                    game.setOver(false);
                    game.initialSigns();
                    if (mListener != null && mode == Constants.MODE.ONLINE) {
                        ((OnlineGame) game).setLastMoveId(-1);
                        mListener.rematch((OnlineGame) game);
                    }
                    tlBoard.removeAllViews();
                    initialBoard();
                    dialog.cancel();
                }
            });
        TextView txtLeaveGame = dialog.findViewById(R.id.txtLeaveGame);
        txtLeaveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    String key="";
                    if(game instanceof OnlineGame){
                        key = ((OnlineGame) game).getKeyGame();
                        ((OnlineGame) game).setPlayer1Connected(false);
                        ((OnlineGame) game).setPlayer2Connected(false);
                    }
                    mListener.leaveGame(key, isX, mode, isOtherPlayerLeft);
                }
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private String getWinnerName() {
        String winner;
        if (!game.isXTurn()) {
            if (mode == Constants.MODE.ONLINE)
                if (isX)
                    winner = "You";
                else winner = otherPlayer.getFirstName() + " " + otherPlayer.getLastName();
            else
                winner = "X";
        } else {
            if (mode == Constants.MODE.ONLINE)
                if (!isX)
                    winner = "You";
                else winner = otherPlayer.getFirstName() + " " + otherPlayer.getLastName();            else
                winner = "O";
        }
        return winner;
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
        ((OnlineGame) this.game).setEmailPlayer2(game.getEmailPlayer2());
        ((OnlineGame) this.game).setPlayer2Connected(game.isPlayer2Connected());
        int move = game.getLastMoveId();
        if (move != -1)
            if (this.game.isLegalMove(move / boardSize, move % boardSize))
                makeTurn(move / boardSize, move % boardSize);
    }

    @Override
    public void onOtherPlayerConnectionEvent(boolean isConneted) {
        if (isConneted) {
            isGameStarted = true;
            mListener.getOtherPlayer(!isX ? ((OnlineGame) game).getEmailPlayer1() : ((OnlineGame) game).getEmailPlayer2());
        } else {
            if (((OnlineGame) game).getLastMoveId() != -1) {
                Toast.makeText(getContext(), "Your opponent has left the game", Toast.LENGTH_SHORT).show();
                game.setXTurn(isX);
                if(!((OnlineGame)game).isPlayer1Connected() || !((OnlineGame)game).isPlayer2Connected())
                    return;
                win(true);
            }
        }
    }

    @Override
    public void setOtherPlayer(User user) {
        this.otherPlayer = user;
    }

    public interface OnFragmentInteractionListener {
        void rematch(OnlineGame game);

        void updateGameState(String key, int moveId);

        void listenToGame(String keyGame);

        void registerGameEvent(IPlayFragmentUpdateGameChanges iPlayFragmentUpdateGameChanges);

        void leaveGame(String keyGame, boolean isX, Constants.MODE mode, boolean isOtherPlayerLeft);

        void updateScore(User emailPlayer1, User emailPlayer11, boolean xTurn);

        void getOtherPlayer(String email);
    }
}

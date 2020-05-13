package com.sagi.supertictactoeonline.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.Locale;

public class PlayFragment extends Fragment implements View.OnClickListener,
        IPlayFragmentUpdateGameChanges {

    private static final String GAME = "key";
    private static String MODE = "mode";
    private static String LEVEL = "level";
    private static String IS_RANDOM = "is random";
    private static String START_TIME_MILLIS = "start time millis";
    private boolean isX, isCreator, isRandom;
    private Constants.MODE mode;
    private Game game;
    int boardSize;
    int sign;
    private TableLayout tlBoard;
    private TextView txtTurnX, txtTurnO, txtTimeX, txtTimeO, txtRankX, txtRankO;
    private CountDownTimer countDownTimerX, countDownTimerO;
    private long timeLeftX, timeLeftO, startTimeMillis;
    private boolean isPauseX = true, isPauseO = true;
    private ImageView imgLastTurn;
    private OnFragmentInteractionListener mListener;
    private User otherPlayer;
    private boolean isGameStarted = false, isRematch = false;
    private boolean isOtherPlayerLeft = false;
    private ImageView imgZoomOut, imgZoomIn, imgShowDialog;

    public PlayFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadBundle();
        boardSize = game.getBoardSize();
        loadViews(view);
        initialTextViews();
        initialBoard();
        dialog = new Dialog(getContext());
    }

    private void initialTextViews() {
        if (mode == Constants.MODE.ONLINE || mode == Constants.MODE.OFFLINE_FRIEND) {
            if (mode == Constants.MODE.ONLINE) {
                User user = SharedPreferencesHelper.getInstance(getContext()).getUser();
                if (isX) {
                    txtTurnX.setText(user.getName());
                    txtRankX.setText(String.valueOf(user.getRank()));
                    if (otherPlayer == null)
                        txtTurnO.setText("Coming");
                    else {
                        txtTurnO.setText(otherPlayer.getName());
                        txtRankO.setText(String.valueOf(otherPlayer.getRank()));
                    }
                } else {
                    if (otherPlayer == null)
                        txtTurnX.setText("Coming");
                    else {
                        txtTurnX.setText(otherPlayer.getName());
                        txtRankX.setText(String.valueOf(otherPlayer.getRank()));
                    }
                    txtTurnO.setText(user.getName());
                    txtRankO.setText(String.valueOf(user.getRank()));
                }
            } else {
                txtTurnX.setText("X");
                moveToCenter(txtTurnX);
                txtTurnO.setText("O");
                moveToCenter(txtTurnO);
                txtRankX.setVisibility(View.INVISIBLE);
                txtRankO.setVisibility(View.INVISIBLE);
            }

            if (startTimeMillis != 0) {
                updateText(startTimeMillis, true);
                updateText(startTimeMillis, false);
            } else {
                txtTimeX.setVisibility(View.INVISIBLE);
                txtTimeO.setVisibility(View.INVISIBLE);
            }
        } else {
            txtTurnX.setText("X: You");
            txtTurnO.setText("O: Computer");
            moveToCenter(txtTurnX);
            moveToCenter(txtTurnO);
            txtRankX.setVisibility(View.INVISIBLE);
            txtRankO.setVisibility(View.INVISIBLE);
            txtTimeX.setVisibility(View.INVISIBLE);
            txtTimeO.setVisibility(View.INVISIBLE);
        }
    }

    private void moveToCenter(TextView txt) {
        RelativeLayout.LayoutParams params;
        params = (RelativeLayout.LayoutParams) txt.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        txt.setLayoutParams(params);
    }

    private void startTimer(final boolean isTimerX) {
        if (isTimerX) {
            countDownTimerX = new CountDownTimer(timeLeftX, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isPauseX)
                        cancel();
                    else {
                        timeLeftX = millisUntilFinished;
                        updateText(timeLeftX, true);
                    }
                }

                @Override
                public void onFinish() {
                    isPauseX = isPauseO = true;
                    game.setXTurn(true);
                    win();
                }
            }.start();
        } else {
            countDownTimerO = new CountDownTimer(timeLeftO, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isPauseO)
                        cancel();
                    else {
                        timeLeftO = millisUntilFinished;
                        updateText(timeLeftO, false);
                    }
                }

                @Override
                public void onFinish() {
                    isPauseX = isPauseO = true;
                    game.setXTurn(false);
                    win();
                }
            }.start();
        }
    }

    private void updateText(long timeLeft, boolean isTxtX) {
        int minutes = (int) timeLeft / 60000;
        int seconds = (int) (timeLeft % 60000) / 1000;
        String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        if (isTxtX)
            txtTimeX.setText(time);
        else
            txtTimeO.setText(time);
    }

    private void loadViews(View view) {
        tlBoard = view.findViewById(R.id.tlBoard);
        txtTurnX = view.findViewById(R.id.txtTurnX);
        txtTurnO = view.findViewById(R.id.txtTurnO);
        txtTimeX = view.findViewById(R.id.txtTimeX);
        txtTimeO = view.findViewById(R.id.txtTimeO);
        txtRankX = view.findViewById(R.id.txtRankX);
        txtRankO = view.findViewById(R.id.txtRankO);
        imgZoomIn = view.findViewById(R.id.imgZoomIn);
        imgZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoom(true);
            }
        });
        imgZoomOut = view.findViewById(R.id.imgZoomOut);
        imgZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoom(false);
            }
        });
        imgShowDialog = view.findViewById(R.id.imgShowDialog);
        imgShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null)
                    dialog.show();
            }
        });
    }

    private void zoom(boolean isIn) {
        ImageView temp;
        temp = tlBoard.findViewById(0);
        int size = isIn ? 20 : -20;
        size += temp.getWidth();
        if ((isIn && size >= 140) || (!isIn && size <= 60))
            return;
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(size, size);
        for (int i = 0; i < boardSize * boardSize; i++) {
            temp = tlBoard.findViewById(i);
            temp.setLayoutParams(layoutParams);
        }
    }

    public static PlayFragment newInstance(Constants.MODE mode, OnlineGame game, int level, boolean isRandom, long startTimeMillis) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MODE, mode);
        bundle.putSerializable(GAME, (Serializable) game);
        bundle.putInt(LEVEL, level);
        bundle.putBoolean(IS_RANDOM, isRandom);
        bundle.putLong(START_TIME_MILLIS, startTimeMillis);
        PlayFragment fragment = new PlayFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void loadBundle() {
        Bundle bundle = getArguments();
        mode = (Constants.MODE) bundle.getSerializable(MODE);
        game = (OnlineGame) bundle.getSerializable(GAME);
        if (game == null) {
            if (mode == Constants.MODE.OFFLINE_FRIEND) {
                game = new Game(14);
                startTimeMillis = timeLeftX = timeLeftO = bundle.getLong(START_TIME_MILLIS);
            } else game = new ComputerGame(14);
            isGameStarted = true;
        } else {
            isX = isCreator = ((OnlineGame) game).getKeyPlayer1().equals(SharedPreferencesHelper.getInstance(getContext()).getUser().getName());
            isRandom = bundle.getBoolean(IS_RANDOM);
            startTimeMillis = timeLeftX = timeLeftO = bundle.getLong(START_TIME_MILLIS);
            mListener.listenToGame(((OnlineGame) game).getKeyGame(), isRandom);
        }
        game.initialSigns();
        game.initialWeights();
    }

    private void initialBoard() {
        int pixels = screenWidth();
        for (int i = 0; i < boardSize; i++) {
            TableRow tableRow = new TableRow(getContext());
            for (int j = 0; j < boardSize; j++) {
                final ImageView imgSign = new ImageView(getContext());
                imgSign.setBackgroundResource(R.drawable.empty_cell);
                imgSign.setLayoutParams(new TableRow.LayoutParams(pixels / boardSize, pixels / boardSize));
                imgSign.setId(i * boardSize + j);
                imgSign.setOnClickListener(this);
                tableRow.addView(imgSign);
            }
            tlBoard.addView(tableRow);
        }
    }

    private int screenWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getWidth();
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
            if ((mode == Constants.MODE.ONLINE || mode == Constants.MODE.OFFLINE_FRIEND) && timeLeftX != 0) {
                startTimer(game.isXTurn());
                if (!game.isOver() && timeLeftX != 0)
                    switchClock();
            }
            if (mode == Constants.MODE.ONLINE)
                mListener.updateGameState((OnlineGame) game, isRandom);
        } else Toast.makeText(getContext(), "can't", Toast.LENGTH_SHORT).show();
    }

    private void switchClock() {
        if (!game.isXTurn()) {
            isPauseX = true;
            isPauseO = false;
        } else {
            isPauseX = false;
            isPauseO = true;
        }
    }

    private void makeTurn(int i, int j) {
        game.makeTurn(i, j);
        sign = game.getSign(i, j);
        changeGUI(i, j);
    }

    private void changeGUI(int i, int j) {
        ImageView imgSign = tlBoard.findViewById(i * boardSize + j);
        if (imgLastTurn != null)
            imgLastTurn.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        imgSign.setBackgroundColor(getResources().getColor(R.color.colorShape));
        if (!game.isXTurn()) {
            imgSign.setImageResource(R.drawable.x);
        } else {
            imgSign.setImageResource(R.drawable.o);
        }
        if (game.isOver()) {
            win();
        }
        imgLastTurn = imgSign;
    }

    private void win() {
        isPauseX = isPauseO = true;
        isGameStarted = false;
        initialWinDialog();
        dialog.show();
        imgShowDialog.setVisibility(View.VISIBLE);
    }

    private Dialog dialog;
    private TextView txtRematch;

    private void initialWinDialog() {
        if (dialog.isShowing())
            return;
        dialog.setContentView(R.layout.dialog_win);
        TextView txtTitleWin = dialog.findViewById(R.id.txtTitleWin);
        txtTitleWin.setText(getWinnerName());
        if (game instanceof OnlineGame && ((OnlineGame) game).getLastMoveId() > -1) {
            if (isX || isOtherPlayerLeft)
                updateScore(true);
            else updateScore(false);
        }
        txtRematch = dialog.findViewById(R.id.txtRematch);
        if (isOtherPlayerLeft)
            txtRematch.setVisibility(View.INVISIBLE);
        else
            txtRematch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isRematch = true;
                    game.setXTurn(true);
                    isX = !isX;
                    game.setOver(false);
                    if (mListener != null && mode == Constants.MODE.ONLINE) {
                        if (!isGameStarted)
                            isOtherPlayerLeft = true;
                        ((OnlineGame) game).setLastMoveId(isCreator ? -1 : -2);
                        mListener.rematch((OnlineGame) game, isRandom);
                    }
                    tlBoard.removeAllViews();
                    game.initialSigns();
                    initialBoard();
                    dialog.cancel();
                    imgShowDialog.setVisibility(View.INVISIBLE);
                    timeLeftX = timeLeftO = startTimeMillis;
                    initialTextViews();
                }
            });
        TextView txtLeaveGame = dialog.findViewById(R.id.txtLeaveGame);
        txtLeaveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    String key = "";
                    if (game instanceof OnlineGame) {
                        key = ((OnlineGame) game).getKeyGame();
                    }
                    mListener.leaveGame(key, isCreator, mode, isRandom);
                }
                dialog.cancel();
            }
        });
    }

    private void updateScore(boolean isUpdateFirebase) {
        int rankX, rankO;
        if (isCreator) {
            rankX = SharedPreferencesHelper.getInstance(getContext()).getUser().getRank();
            rankO = otherPlayer.getRank();
        } else {
            rankX = otherPlayer.getRank();
            rankO = SharedPreferencesHelper.getInstance(getContext()).getUser().getRank();
        }

        int difScore = (rankX - rankO) / 50;
        if (8 - Math.abs(difScore) < 2) {
            if (rankX > rankO)
                difScore = 2;
            else difScore = -2;
        }

        if (!game.isXTurn()) {
            rankX += 8 - difScore;
            rankO -= 8 - difScore;
        } else {
            rankX -= 8 + difScore;
            rankO += 8 + difScore;
        }
        rankX = rankX < 0 ? 0 : rankX;
        rankO = rankO < 0 ? 0 : rankO;

        if (isCreator) {
            SharedPreferencesHelper.getInstance(getContext()).setRank(rankX);
            otherPlayer.setRank(rankO);
        } else {
            SharedPreferencesHelper.getInstance(getContext()).setRank(rankO);
            otherPlayer.setRank(rankX);
        }

        txtRankX.setText(String.valueOf(rankX));
        txtRankO.setText(String.valueOf(rankO));

        if (isUpdateFirebase && mListener != null)
            if (isCreator)
                mListener.updateScore(rankX, rankO, otherPlayer.getName());
            else mListener.updateScore(rankO, rankX, otherPlayer.getName());
    }

    private String getWinnerName() {
        if (isOtherPlayerLeft)
            return "Your Opponent Left";
        String winner;
        if (!game.isXTurn()) {
            if (mode == Constants.MODE.ONLINE)
                if (isX)
                    winner = "You";
                else winner = otherPlayer.getName();
            else if (mode == Constants.MODE.OFFLINE_FRIEND)
                winner = "X";
            else winner = "You";
        } else {
            if (mode == Constants.MODE.ONLINE)
                if (!isX)
                    winner = "You";
                else winner = otherPlayer.getName();
            else if (mode == Constants.MODE.OFFLINE_FRIEND)
                winner = "O";
            else winner = "Computer";
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
        isPauseX = isPauseO = true;
        if (game instanceof OnlineGame)
            mListener.leaveGame(((OnlineGame) game).getKeyGame(), isCreator, mode, isRandom);
        mListener.registerGameEvent(null);
        mListener = null;
    }

    @Override
    public void updateGameChanges(OnlineGame game) {
        int move = game.getLastMoveId();
        if (move > -1) {
            if (timeLeftX != 0)
                startTimer(isX);
            makeTurn(move / boardSize, move % boardSize);
            if (!game.isOver())
                switchClock();
        } else {
            if (isRematch)
                isGameStarted = true;
            isOtherPlayerLeft = false;
            ((OnlineGame) this.game).setKeyPlayer2(game.getKeyPlayer2());
            ((OnlineGame) this.game).setPlayer2Connected(game.isPlayer2Connected());
        }
    }

    @Override
    public void onOtherPlayerConnectionEvent(boolean isConnected) {
        if (isConnected) {
            isGameStarted = true;
            isOtherPlayerLeft = false;
            if (otherPlayer == null)
                mListener.getOtherPlayer(!isX ? ((OnlineGame) game).getKeyPlayer1() : ((OnlineGame) game).getKeyPlayer2());
        } else {
            if (isGameStarted) {
                isOtherPlayerLeft = true;
                if (((OnlineGame) game).getLastMoveId() > -1) {
                    game.setXTurn(!isCreator);
                    win();
                } else {
                    initialWinDialog();
                    dialog.show();
                }
            }
            if (txtRematch != null) {
                txtRematch.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setOtherPlayer(User user) {
        this.otherPlayer = user;
        if (isX) {
            txtTurnO.setText(user.getName());
            txtRankO.setText(String.valueOf(user.getRank()));
        } else {
            txtTurnX.setText(user.getName());
            txtRankX.setText(String.valueOf(user.getRank()));
        }
    }

    @Override
    public void onBackPressedInActivity() {
        mListener.showHomePage();
    }

    public interface OnFragmentInteractionListener {
        void rematch(OnlineGame game, boolean isRandom);

        void updateGameState(OnlineGame game, boolean isRandom);

        void listenToGame(String keyGame, boolean aBoolean);

        void registerGameEvent(IPlayFragmentUpdateGameChanges iPlayFragmentUpdateGameChanges);

        void leaveGame(String keyGame, boolean isX, Constants.MODE mode, boolean isRandom);

        void updateScore(int rank, int rank2, String key);

        void getOtherPlayer(String email);

        void showHomePage();
    }
}

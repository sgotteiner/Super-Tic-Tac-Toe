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
import androidx.lifecycle.ViewModelProviders;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.Game;
import com.sagi.supertictactoeonline.utilities.constants.Constants;

public class PlayFragment extends Fragment implements View.OnClickListener {

    private static final String GAME_KEY = "key";
    private static final String IS_JOINING = "is_joining";
    private static String MODE = "mode";
    private String keyGame;
    private boolean isJoining;
    private Constants.MODE mode;
    private Game game;
    private PlayViewModel mViewModel;
    private TableLayout tlBoard;
    private boolean isXTurn = true;
    private int boardSize = 14;
    private int[][] arrSigns = new int[boardSize][boardSize];
    private int[][] arrWeights = new int[boardSize][boardSize];
    private int[][] arrPotentials = new int[boardSize][boardSize];
    int bestMoveId = 0;
    private ImageView imgLastTurn;
    private OnFragmentInteractionListener mListener;
    private boolean isOver = false;

    public static PlayFragment newInstance() {
        return new PlayFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.play_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tlBoard = view.findViewById(R.id.tlBoard);
        initialBoard();
        calculateWeights();
    }


    public static PlayFragment newInstance(Constants.MODE mode, boolean isJoining, String keyGame) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MODE, mode);
        bundle.putBoolean(IS_JOINING, isJoining);
        bundle.putString(GAME_KEY, keyGame);
        PlayFragment fragment = new PlayFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void loadBundle() {
        Bundle bundle = getArguments();
        isJoining = bundle.getBoolean(IS_JOINING);
        keyGame = bundle.getString(GAME_KEY);
        mode = (Constants.MODE)bundle.getSerializable(MODE);
    }


    private void calculateWeights() {
        for (int i = 0; i < boardSize / 2; i++) {
            arrWeights[i][i] = i + 1;
            arrWeights[boardSize - i - 1][boardSize - i - 1] = i + 1;
            arrWeights[i][boardSize - i - 1] = i + 1;
            arrWeights[boardSize - i - 1][i] = i + 1;
            arrPotentials[i][i] = i + 1;
            arrPotentials[boardSize - i - 1][boardSize - i - 1] = i + 1;
            arrPotentials[i][boardSize - i - 1] = i + 1;
            arrPotentials[boardSize - i - 1][i] = i + 1;
            for (int j = i - 1; j >= 0; j--) {
                arrWeights[i][j] = j + 1;
                arrWeights[j][i] = j + 1;
                arrWeights[boardSize - i - 1][boardSize - j - 1] = j + 1;
                arrWeights[boardSize - j - 1][boardSize - i - 1] = j + 1;
                arrWeights[i][boardSize - j - 1] = j + 1;
                arrWeights[j][boardSize - i - 1] = j + 1;
                arrWeights[boardSize - i - 1][j] = j + 1;
                arrWeights[boardSize - j - 1][i] = j + 1;
                arrPotentials[i][j] = j + 1;
                arrPotentials[j][i] = j + 1;
                arrPotentials[boardSize - i - 1][boardSize - j - 1] = j + 1;
                arrPotentials[boardSize - j - 1][boardSize - i - 1] = j + 1;
                arrPotentials[i][boardSize - j - 1] = j + 1;
                arrPotentials[j][boardSize - i - 1] = j + 1;
                arrPotentials[boardSize - i - 1][j] = j + 1;
                arrPotentials[boardSize - j - 1][i] = j + 1;
            }
        }
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PlayViewModel.class);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int i = id / boardSize;
        int j = id % boardSize;
        makeTurn(i, j);
        if (!isOver && !isXTurn)
            computerTurn(i, j);
    }

    private void makeTurn(int i, int j) {
        ImageView imgSign = tlBoard.findViewById(i * boardSize + j);
        if (arrSigns[i][j] == 0) {

            if (imgLastTurn != null)
                imgLastTurn.setBackgroundColor(Color.WHITE);
            imgSign.setBackgroundColor(Color.YELLOW);

            if (isXTurn) {
                arrSigns[i][j] = Constants.PLAYER_1;
                imgSign.setImageResource(R.drawable.x);
            } else {
                arrSigns[i][j] = Constants.PLAYER_2;
                imgSign.setImageResource(R.drawable.o);
            }
            isOver = checkWin(i, j);
            if (isOver)
                win();
            isXTurn = !isXTurn;
            imgLastTurn = imgSign;
        } else
            Toast.makeText(getContext(), "cant", Toast.LENGTH_SHORT).show();
    }

    private void computerTurn(int i, int j) {
        int startI = i - 4;
        startI = startI < 0 ? 0 : startI;
        int endI = i + 4;
        endI = endI >= boardSize ? boardSize - 1 : endI;
        int startJ = j - 4;
        startJ = startJ < 0 ? 0 : startJ;
        int endJ = j + 4;
        endJ = endJ >= boardSize ? boardSize - 1 : endJ;
//        int keepStartJ = startJ;
//        for (; startI <= endI; startI++) {
//            startJ = keepStartJ;
//            for (; startJ <= endJ; startJ++) {
//                arrPotentials[startI][startJ] = calculatePotentialCell(startI, startJ);
//            }
//        }
        int maxGrade = 0, grade;
        for (startI = 0; startI < boardSize; startI++) {
            for (startJ = 0; startJ < boardSize; startJ++) {
                grade = Math.abs(calculatePotentialCell(startI, startJ));
                if (grade > maxGrade) {
                    maxGrade = grade;
                    bestMoveId = startI * boardSize + startJ;
                }
                arrPotentials[startI][startJ] = grade;
            }
        }
        makeTurn(bestMoveId / boardSize, bestMoveId % boardSize);
    }

    private int calculatePotentialCell(int i, int j) {

        if (arrSigns[i][j] != 0)
            return 0;

        arrSigns[i][j] = Constants.COMPUTER;
        if (checkWin(i, j)) {
            arrSigns[i][j] = 0;
            return 10000000;
        }
        arrSigns[i][j] = Constants.PLAYER_1;
        if (checkWin(i, j)) {
            arrSigns[i][j] = 0;
            return 1000000;
        }

        arrSigns[i][j] = Constants.COMPUTER;
        int totalPotentialComputer = checkPotentialCellWithPlayerLevel2(i, j);
        arrSigns[i][j] = Constants.PLAYER_1;
        int totalPotentialPlayer = checkPotentialCellWithPlayerLevel2(i, j);
        arrSigns[i][j] = 0;

        if (totalPotentialComputer != totalPotentialPlayer)
            return totalPotentialComputer - totalPotentialPlayer;
        else return arrWeights[i][j];
    }

    private int checkPotentialCellWithPlayerLevel1(int i, int j) {

        int potential, totalPotentialPlayer = 0;
        int player = arrSigns[i][j];

        potential = checkLeftUpPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkLeftPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkLeftDownPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkUpPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkDownPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkRightUpPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkRightPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];
        potential = checkRightDownPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential * arrWeights[i][j];

        return totalPotentialPlayer;
    }

    private int checkPotentialCellWithPlayerLevel2(int i, int j) {

        int potential=0, maxPotential, totalPotentialPlayer = 0;
        int player = arrSigns[i][j];
        int weight = arrWeights[i][j];
        int m;

        m = minDifference(i, j);
        maxPotential = 0;
        for (; m >= 0; m--) {
            if (arrSigns[i - m][j - m] == player)
                potential = checkLeftUpPotential(i - m, j - m, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        m = minIndex(j);
        maxPotential = 0;
        for (; m <= j; m++) {
            if (arrSigns[i][m] == player)
                potential = checkLeftPotential(i, m, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        int k = minDifference(i, j);
        int l = maxDifference(i, j);
        m = k > l ? l : k;
        maxPotential = 0;
        for (; m >= 0; m--) {
            if (arrSigns[i + m][j - m] == player)
                potential = checkLeftDownPotential(i + m, j - m, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        m = minIndex(i);
        maxPotential = 0;
        for (; m <= i; m++) {
            if (arrSigns[m][j] == player)
                potential = checkUpPotential(m, j, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        m = maxIndex(i);
        maxPotential = 0;
        for (; m >= i; m--) {
            if (arrSigns[m][j] == player)
                potential = checkDownPotential(m, j, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        k = minDifference(i, j);
        l = maxDifference(i, j);
        m = k > l ? l : k;
        maxPotential = 0;
        for (; m >= 0; m--) {
            if (arrSigns[i - m][j + m] == player)
                potential = checkRightUpPotential(i - m, j + m, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        m = maxIndex(j);
        maxPotential = 0;
        for (; m >= j; m--) {
            if (arrSigns[i][m] == player)
                potential = checkRightPotential(i, m, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        m = maxDifference(i, j);
        maxPotential = 0;
        for (; m >= 0; m--) {
            if (arrSigns[i + m][j + m] == player)
                potential = checkRightDownPotential(i + m, j + m, player);
            if (potential > maxPotential)
                maxPotential = potential;
        }
        totalPotentialPlayer += maxPotential * weight;

        return totalPotentialPlayer;
    }

    boolean checkWin(int i, int j) {
        int startI = minIndex(i), startJ = minIndex(j);
        int endI = maxIndex(i), endJ = maxIndex(j);
        int keepJ = startJ;
        for (; startI <= endI; startI++) {
            for (startJ = keepJ; startJ <= endJ; startJ++) {
                if (arrSigns[startI][startJ] == arrSigns[i][j])
                    if (checkPotentialCellWithPlayerLevel1(startI, startJ) >= 200000)
                        return true;
            }
        }
        return false;
    }

    private int minIndex(int index) {
        return index - 4 < 0 ? 0 : index - 4;
    }

    private int maxIndex(int index) {
        return index + 4 >= boardSize ? boardSize - 1 : index + 4;
    }

    private int minDifference(int i, int j) {
        int k = minIndex(i);
        int l = minIndex(j);
        if ((i - k) > (j - l))
            return j - l;
        else return i - k;
    }

    private int maxDifference(int i, int j) {
        int k = maxIndex(i);
        int l = maxIndex(j);
        if ((k - i) > (l - j))
            return l - j;
        else return k - i;
    }

    private int checkLeftUpPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, i++, j++) {
            if (j >= boardSize || i >= boardSize || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }

        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkLeftPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, j++) {
            if (j >= boardSize || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkLeftDownPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, i--, j++) {
            if (j >= boardSize || i < 0 || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkUpPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, i++) {
            if (i >= boardSize || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkDownPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, i--) {
            if (i < 0 || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkRightUpPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, i++, j--) {
            if (j < 0 || i >= boardSize || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkRightPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, j--) {
            if (j < 0 || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private int checkRightDownPotential(int i, int j, int player) {
        int lineGrade = 0;
        int inRow = 0;
        int otherPlayer = player == Constants.PLAYER_1 ? Constants.PLAYER_2 : Constants.PLAYER_1;
        for (int k = 0; k < 5; k++, i--, j--) {
            if (j < 0 || i < 0 || arrSigns[i][j] == otherPlayer)
                return 0;
            if (arrSigns[i][j] == player)
                inRow++;
            else {
                if (inRow != 0) {
                    lineGrade += Math.pow(10, inRow);
                    inRow = 0;
                }
            }
        }
        if (inRow > 0)
            lineGrade += Math.pow(10, inRow);
        return lineGrade;
    }

    private void win() {
        String winner;
        if (isXTurn) {
            winner = "X";
        } else
            winner = "O";
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(R.drawable.trophy);
        builder.setTitle(winner + " won!!!");
        builder.setPositiveButton("rematch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null)
                    mListener.rematch();
            }
        });
        builder.create().show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        void rematch();
    }
}

package com.sagi.supertictactoeonline.entities;

import com.sagi.supertictactoeonline.utilities.constants.Constants;

import java.io.Serializable;

public class Game implements Serializable {

    protected boolean isXTurn;
    protected int boardSize = 14;
    protected int[][] arrSigns;
    protected int[][] arrWeights;
    protected boolean isOver;

    public Game() {
    }

    public Game(int boardSize) {
        this.boardSize = boardSize;
        isXTurn = true;
        isOver = false;
    }

    public void initialSigns() {
        arrSigns = new int[boardSize][boardSize];
    }

    public void initialWeights() {
        arrWeights = new int[boardSize][boardSize];
        calculateWeights();
    }

    public void makeTurn(int i, int j) {
        changeGameParameters(i, j);
    }

    protected void changeGameParameters(int i, int j) {
        if (isXTurn)
            arrSigns[i][j] = Constants.PLAYER_1;
        else arrSigns[i][j] = Constants.PLAYER_2;
        isOver = checkWin(i, j);
        isXTurn = !isXTurn;
    }

    public boolean isLegalMove(int i, int j) {
        if (arrSigns[i][j] == 0)
            return true;
        else return false;
    }

    public boolean isXTurn() {
        return isXTurn;
    }

    public void setXTurn(boolean XTurn) {
        isXTurn = XTurn;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getSign(int i, int j) {
        return arrSigns[i][j];
    }

    public void setSign(int i, int j, int sign) {
        this.arrSigns[i][j] = sign;
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }

    protected void calculateWeights() {
        for (int i = 0; i < boardSize / 2; i++) {
            arrWeights[i][i] = i + 1;
            arrWeights[boardSize - i - 1][boardSize - i - 1] = i + 1;
            arrWeights[i][boardSize - i - 1] = i + 1;
            arrWeights[boardSize - i - 1][i] = i + 1;
//            arrPotentials[i][i] = i + 1;
//            arrPotentials[boardSize - i - 1][boardSize - i - 1] = i + 1;
//            arrPotentials[i][boardSize - i - 1] = i + 1;
//            arrPotentials[boardSize - i - 1][i] = i + 1;
            for (int j = i - 1; j >= 0; j--) {
                arrWeights[i][j] = j + 1;
                arrWeights[j][i] = j + 1;
                arrWeights[boardSize - i - 1][boardSize - j - 1] = j + 1;
                arrWeights[boardSize - j - 1][boardSize - i - 1] = j + 1;
                arrWeights[i][boardSize - j - 1] = j + 1;
                arrWeights[j][boardSize - i - 1] = j + 1;
                arrWeights[boardSize - i - 1][j] = j + 1;
                arrWeights[boardSize - j - 1][i] = j + 1;
//                arrPotentials[i][j] = j + 1;
//                arrPotentials[j][i] = j + 1;
//                arrPotentials[boardSize - i - 1][boardSize - j - 1] = j + 1;
//                arrPotentials[boardSize - j - 1][boardSize - i - 1] = j + 1;
//                arrPotentials[i][boardSize - j - 1] = j + 1;
//                arrPotentials[j][boardSize - i - 1] = j + 1;
//                arrPotentials[boardSize - i - 1][j] = j + 1;
//                arrPotentials[boardSize - j - 1][i] = j + 1;
            }
        }
    }

    public boolean checkWin(int i, int j) {
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

    protected int checkPotentialCellWithPlayerLevel1(int i, int j) {

        int potential, totalPotentialPlayer = 0;
        int player = arrSigns[i][j];

        potential = checkLeftUpPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkLeftPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkLeftDownPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkUpPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkDownPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkRightUpPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkRightPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;
        potential = checkRightDownPotential(i, j, player);
        if (potential > 0)
            totalPotentialPlayer += potential;

        return totalPotentialPlayer * arrWeights[i][j];
    }

    protected int minIndex(int index) {
        return index - 4 < 0 ? 0 : index - 4;
    }

    protected int maxIndex(int index) {
        return index + 4 >= boardSize ? boardSize - 1 : index + 4;
    }

    protected int checkLeftUpPotential(int i, int j, int player) {
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

    protected int checkLeftPotential(int i, int j, int player) {
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

    protected int checkLeftDownPotential(int i, int j, int player) {
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

    protected int checkUpPotential(int i, int j, int player) {
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

    protected int checkDownPotential(int i, int j, int player) {
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

    protected int checkRightUpPotential(int i, int j, int player) {
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

    protected int checkRightPotential(int i, int j, int player) {
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

    protected int checkRightDownPotential(int i, int j, int player) {
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
}

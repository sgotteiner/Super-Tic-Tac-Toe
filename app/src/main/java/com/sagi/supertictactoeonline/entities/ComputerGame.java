package com.sagi.supertictactoeonline.entities;

import com.sagi.supertictactoeonline.utilities.constants.Constants;

public class ComputerGame extends Game {

    //    private int[][] arrPotentials = new int[boardSize][boardSize];
    int nextMoveId;

    public ComputerGame(int boardSize) {
        super(boardSize);
    }

    public void makeTurn(int i, int j){
        if (arrSigns[i][j] == 0) {
            changeGameParameters(i, j);
            if (!isOver && !isXTurn)
                nextMoveId = computerTurn(i, j);
        }
    }

    public int getNextMoveId() {
        return nextMoveId;
    }

    public int computerTurn(int i, int j) {
        int startI = minIndex(i);
        int endI = maxIndex(i);
        int startJ = minIndex(j);
        int endJ = maxIndex(j);
//        int keepStartJ = startJ;
//        for (; startI <= endI; startI++) {
//            startJ = keepStartJ;
//            for (; startJ <= endJ; startJ++) {
//                arrPotentials[startI][startJ] = calculatePotentialCell(startI, startJ);
//            }
//        }
        int maxGrade = 0, grade, bestMoveId=0;
        for (startI = 0; startI < boardSize; startI++) {
            for (startJ = 0; startJ < boardSize; startJ++) {
                grade = Math.abs(calculatePotentialCell(startI, startJ));
                if (grade > maxGrade) {
                    maxGrade = grade;
                    bestMoveId = startI * boardSize + startJ;
                }
//                arrPotentials[startI][startJ] = grade;
            }
        }
        return bestMoveId;
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

}

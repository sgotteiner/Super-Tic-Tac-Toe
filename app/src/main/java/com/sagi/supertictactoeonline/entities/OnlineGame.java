package com.sagi.supertictactoeonline.entities;

import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;

public class OnlineGame extends Game {

    private String keyGame, emailPlayer1, emailPlayer2;
    private int lastMoveId;
    private boolean isPlayer1Connected, isPlayer2Connected;
    private long startTimeMillis;

    public OnlineGame(){}

    public OnlineGame(int boardSize, String keyGame, String emailPlayer1, long startTimeMillis) {
        super(boardSize);
        this.keyGame = keyGame;
        this.emailPlayer1 = emailPlayer1;
        emailPlayer2 = "";
        lastMoveId = -1;
        isPlayer1Connected = true;
        isPlayer2Connected = false;
        this.startTimeMillis = startTimeMillis;
    }

    public void makeTurn(int i, int j){
        if (arrSigns[i][j] == 0) {
            changeGameParameters(i, j);
            lastMoveId = i * boardSize + j;

        }
    }

    public String getKeyGame() {
        return keyGame;
    }

    public void setKeyGame(String keyGame) {
        this.keyGame = keyGame;
    }

    public String getEmailPlayer1() {
        return emailPlayer1;
    }

    public void setEmailPlayer1(String emailPlayer1) {
        this.emailPlayer1 = emailPlayer1;
    }

    public String getEmailPlayer2() {
        return emailPlayer2;
    }

    public void setEmailPlayer2(String emailPlayer2) {
        this.emailPlayer2 = emailPlayer2;
    }

    public int getLastMoveId() {
        return lastMoveId;
    }

    public void setLastMoveId(int lastMoveId) {
        this.lastMoveId = lastMoveId;
    }

    public boolean isPlayer1Connected() {
        return isPlayer1Connected;
    }

    public void setPlayer1Connected(boolean player1Connected) {
        isPlayer1Connected = player1Connected;
    }

    public boolean isPlayer2Connected() {
        return isPlayer2Connected;
    }

    public void setPlayer2Connected(boolean player2Connected) {
        isPlayer2Connected = player2Connected;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }
}

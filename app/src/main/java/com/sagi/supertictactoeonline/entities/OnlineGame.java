package com.sagi.supertictactoeonline.entities;

public class OnlineGame extends Game {

    private String keyGame, keyPlayer1, keyPlayer2;
    private int lastMoveId;
    private boolean isPlayer1Connected, isPlayer2Connected;
    private long startTimeMillis;

    public OnlineGame(){}

    public OnlineGame(int boardSize, String keyGame, String keyPlayer1, long startTimeMillis) {
        super(boardSize);
        this.keyGame = keyGame;
        this.keyPlayer1 = keyPlayer1;
        keyPlayer2 = "";
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

    public String getKeyPlayer1() {
        return keyPlayer1;
    }

    public void setKeyPlayer1(String keyPlayer1) {
        this.keyPlayer1 = keyPlayer1;
    }

    public String getKeyPlayer2() {
        return keyPlayer2;
    }

    public void setKeyPlayer2(String keyPlayer2) {
        this.keyPlayer2 = keyPlayer2;
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

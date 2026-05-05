package pcd.poool.model;

import pcd.poool.view.GameStateViewInfo;

public class GameState {

    private int playerScore;
    private int botScore;
    private boolean gameOver = false;
    private String gameResult = "";

    public synchronized int getPlayerScore() {
        return playerScore;
    }

    public synchronized int getBotScore() {
        return botScore;
    }

    public synchronized void addPlayerScore() {
        playerScore++;
    }

    public synchronized void addBotScore() {
        botScore++;
    }

    public synchronized boolean isGameOver() {
        return gameOver;
    }

    public synchronized String getGameResult() {
        return gameResult;
    }

    public synchronized void endGame(String result) {
        gameOver = true;
        gameResult = result;
    }

    public synchronized GameStateViewInfo getGameStateViewInfo() {
        return new GameStateViewInfo(playerScore, botScore);
    }
}

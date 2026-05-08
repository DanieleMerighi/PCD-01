package pcd.pooolThreadOriginal.view;

import java.util.ArrayList;

public class ViewModel {

	private BoardViewInfo boardViewInfo;
	private GameStateViewInfo gameStateViewInfo;
	private long tickPerSec;
	private String gameOverMessage = null;

	public ViewModel(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo) {
		tickPerSec = 0;
		this.boardViewInfo = boardViewInfo;
		this.gameStateViewInfo = gameStateViewInfo;
	}
	
	public synchronized void update(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec) {
		this.boardViewInfo = boardViewInfo;
		this.gameStateViewInfo = gameStateViewInfo;
		this.tickPerSec = tickPerSec;
	}

	synchronized ArrayList<BallViewInfo> getBalls(){
		return new ArrayList<>(gameStateViewInfo.balls());
	}

	public synchronized long getTickPerSec() {
		return tickPerSec;
	}

	synchronized BallViewInfo getHumanBall() {
		return boardViewInfo.humanBall();
	}

	synchronized BallViewInfo getBotBall() {
		return boardViewInfo.botBall();
	}

	synchronized ArrayList<HoleViewInfo> getHoles() {
		return new ArrayList<>(boardViewInfo.holes());
	}

	synchronized int getHumanScore() {
		return gameStateViewInfo.humanScore();
	}

	synchronized int getBotScore() {
		return gameStateViewInfo.botScore();
	}

	public synchronized void setGameOver(String message) {
		this.gameOverMessage = message;
	}

	public synchronized String getGameOverMessage() {
		return gameOverMessage;
	}

}

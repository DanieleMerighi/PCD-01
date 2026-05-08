package pcd.pooolThreadOriented.view;

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

	public synchronized ArrayList<BallViewInfo> getBalls(){
		return new ArrayList<>(gameStateViewInfo.balls());
	}

	public synchronized long getTickPerSec() {
		return tickPerSec;
	}

	public synchronized BallViewInfo getHumanBall() {
		return boardViewInfo.humanBall();
	}

	public synchronized BallViewInfo getBotBall() {
		return boardViewInfo.botBall();
	}

	public synchronized ArrayList<HoleViewInfo> getHoles() {
		return new ArrayList<>(boardViewInfo.holes());
	}

	public synchronized int getHumanScore() {
		return gameStateViewInfo.humanScore();
	}

	public synchronized int getBotScore() {
		return gameStateViewInfo.botScore();
	}

	public synchronized void setGameOver(String message) {
		this.gameOverMessage = message;
	}

	public synchronized String getGameOverMessage() {
		return gameOverMessage;
	}

}

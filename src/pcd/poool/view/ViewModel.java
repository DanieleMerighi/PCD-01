package pcd.poool.view;

import java.util.ArrayList;

public class ViewModel {

	private BoardViewInfo boardViewInfo;
	private GameStateViewInfo gameStateViewInfo;
	private long framePerSec;
	private String gameOverMessage = null;

	public ViewModel(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo) {
		framePerSec = 0;
		this.boardViewInfo = boardViewInfo;
		this.gameStateViewInfo = gameStateViewInfo;
	}
	
	public synchronized void update(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long framePerSec) {
		this.boardViewInfo = boardViewInfo;
		this.gameStateViewInfo = gameStateViewInfo;
		this.framePerSec = framePerSec;
	}

	synchronized ArrayList<BallViewInfo> getBalls(){
		return new ArrayList<>(boardViewInfo.balls());
	}

	public synchronized long getFramePerSec() {
		return framePerSec;
	}

	synchronized BallViewInfo getPlayerBall() {
		return boardViewInfo.player();
	}

	synchronized BallViewInfo getBotBall() {
		return boardViewInfo.bot();
	}

	synchronized ArrayList<HoleViewInfo> getHoles() {
		return new ArrayList<>(boardViewInfo.holes());
	}

	synchronized int getPlayerScore() {
		return gameStateViewInfo.playerScore();
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

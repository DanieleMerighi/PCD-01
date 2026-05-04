package pcd.poool.view;

import java.util.ArrayList;

public class ViewModel {

	private BoardViewInfo boardViewInfo;
	private int framePerSec;
	private String gameOverMessage = null;

	public ViewModel(BoardViewInfo boardViewInfo) {
		framePerSec = 0;
		this.boardViewInfo = boardViewInfo;
	}
	
	public synchronized void update(BoardViewInfo boardViewInfo, int framePerSec) {
		this.boardViewInfo = boardViewInfo;
		this.framePerSec = framePerSec;
	}

	synchronized ArrayList<BallViewInfo> getBalls(){
		return new ArrayList<>(boardViewInfo.balls());
	}

	public synchronized int getFramePerSec() {
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
		return boardViewInfo.playerScore();
	}

	synchronized int getBotScore() {
		return boardViewInfo.botScore();
	}

	public synchronized void setGameOver(String message) {
		this.gameOverMessage = message;
	}

	public synchronized String getGameOverMessage() {
		return gameOverMessage;
	}

}

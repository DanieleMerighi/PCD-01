package pcd.pooolTaskOriented.view;

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
	
	public void update(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec) {
		this.boardViewInfo = boardViewInfo;
		this.gameStateViewInfo = gameStateViewInfo;
		this.tickPerSec = tickPerSec;
	}

	public ArrayList<BallViewInfo> getBalls() {
		return new ArrayList<>(gameStateViewInfo.balls());
	}

	public long getTickPerSec() {
		return tickPerSec;
	}

	public BallViewInfo getHumanBall() {
		return boardViewInfo.humanBall();
	}

	public BallViewInfo getBotBall() {
		return boardViewInfo.botBall();
	}

	public ArrayList<HoleViewInfo> getHoles() {
		return new ArrayList<>(boardViewInfo.holes());
	}

	public int getHumanScore() {
		return gameStateViewInfo.humanScore();
	}

	public int getBotScore() {
		return gameStateViewInfo.botScore();
	}

	public void setGameOver(String message) {
		this.gameOverMessage = message;
	}

	public String getGameOverMessage() {
		return gameOverMessage;
	}

}

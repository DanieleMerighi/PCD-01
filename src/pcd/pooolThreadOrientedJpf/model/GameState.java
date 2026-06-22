package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOriented.util.AtomicReference;
import pcd.pooolThreadOriented.util.AtomicReferenceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GameState {

    private final AtomicReference<Integer> humanScore = new AtomicReferenceImpl<>(0);
    private final AtomicReference<Integer> botScore = new AtomicReferenceImpl<>(0);
    private final AtomicReference<Boolean> gameOver = new AtomicReferenceImpl<>(false);
    private final AtomicReference<String> gameResult = new AtomicReferenceImpl<>("");

    public int getHumanScore() {
        return humanScore.get();
    }

    public int getBotScore() {
        return botScore.get();
    }

    public void addHumanScore() {
        humanScore.map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer value) {
                return value + 1;
            }
        });
    }

    public void addBotScore() {
        botScore.map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer value) {
                return value + 1;
            }
        });
    }

    public boolean isGameOver() {
        return gameOver.get();
    }

    public String getGameResult() {
        return gameResult.get();
    }

    public void endGame(String result) {
        gameOver.set(true);
        gameResult.set(result);
    }

}

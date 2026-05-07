package pcd.poool.model;

import pcd.poool.view.BallViewInfo;
import pcd.poool.view.BoardViewInfo;
import pcd.poool.view.HoleViewInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

    private static final double KICK_SPEED = 1.5;

    private final Boundary bounds;
    private final List<Hole> holes;
    private final Ball playerBall;
    private final Ball botBall;
    private final GameState state;
    private final Random random;

    public Board(BoardConf conf){
        playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
        state = new GameState(playerBall, botBall, conf.getSmallBalls());
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        random = new Random(System.currentTimeMillis());
    }

    public void kickPlayerBall(Direction direction) {
        var velocity = direction.getVector().mul(KICK_SPEED);
        playerBall.kick(velocity);
    }

    public void kickBotBall() {
        var botPos = botBall.getPos();
        double angle;
        int attempts = 0;
        do {
            angle = random.nextDouble() * Math.PI * 2;
            attempts++;
        } while (attempts < 20 && isAngleDangerous(angle, botPos));
        var v = new V2d(Math.cos(angle), Math.sin(angle)).mul(KICK_SPEED);
        botBall.kick(v);
    }

    private boolean isAngleDangerous(double angle, P2d botPos) {
        for (var hole : holes) {
            double dx = hole.pos().x() - botPos.x();
            double dy = hole.pos().y() - botPos.y();
            double dist = Math.hypot(dx, dy);
            double toHole = Math.atan2(dy, dx); // [-π, π]
            double diff = ((angle - toHole + Math.PI) % (2 * Math.PI)) - Math.PI;
            double dangerHalfAngle = Math.min(Math.PI * 0.6, 0.5 / dist);
            if (Math.abs(diff) < dangerHalfAngle) return true;
        }
        return false;
    }

    public Boundary getBounds(){
        return this.bounds;
    }

    public List<Hole> getHoles() {
        return List.copyOf(holes);
    }

    public GameState getState() {
        return this.state;
    }

    public BoardViewInfo getBoardViewInfo() {
        var player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
        var bot = new BallViewInfo(botBall.getPos(), botBall.getRadius());
        var holes = new ArrayList<HoleViewInfo>();
        for (var hole : this.holes) {
            holes.add(new HoleViewInfo(hole.pos(), hole.radius()));
        }
        return new BoardViewInfo(player, bot, holes);
    }
}

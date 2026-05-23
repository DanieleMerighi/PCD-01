package pcd.pooolThreadOrientedJpf.model;

public class Ball {

    private static final double FRICTION_FACTOR = 0.25;
    private static final double RESTITUTION_FACTOR = 1;

    private P2d pos;
    private V2d vel;
    private final double radius;
    private final double mass;
    private final BallType type;
    private BallType hitCredit;

    public Ball(P2d pos, double radius, double mass, V2d vel, BallType type) {
        this.pos = pos;
        this.radius = radius;
        this.mass = mass;
        this.vel = vel;
        this.type = type;
        this.hitCredit = BallType.SMALL_BALL;
    }

    public Ball(P2d pos, double radius, double mass, V2d vel) {
        this(pos, radius, mass, vel, BallType.SMALL_BALL);
    }

    public synchronized void updateState(long dt, Board ctx) {
        double speed = vel.abs();
        double dt_scaled = dt * 0.001;
        if (speed > 0.001) {
            double dec = FRICTION_FACTOR * dt_scaled;
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
            vel = new V2d(0, 0);
        }
        pos = pos.sum(vel.mul(dt_scaled));
        applyBoundaryConstraints(ctx);
    }

    public synchronized void kick(V2d vel) {
        this.vel = vel;
    }

    private void applyBoundaryConstraints(Board ctx) {
        Boundary bounds = ctx.getBounds();
        if (pos.x() + radius > bounds.x1()) {
            pos = new P2d(bounds.x1() - radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.x() - radius < bounds.x0()) {
            pos = new P2d(bounds.x0() + radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.y() + radius > bounds.y1()) {
            pos = new P2d(pos.x(), bounds.y1() - radius);
            vel = vel.getSwappedY();
        } else if (pos.y() - radius < bounds.y0()) {
            pos = new P2d(pos.x(), bounds.y0() + radius);
            vel = vel.getSwappedY();
        }
    }

    public static void resolveCollision(Ball a, Ball b) {
        P2d aPos = a.getPos();
        P2d bPos = b.getPos();
        double dx = bPos.x() - aPos.x();
        double dy = bPos.y() - aPos.y();
        double dist = Math.hypot(dx, dy);
        double minD = a.getRadius() + b.getRadius();

        if (dist < minD && dist > 1e-6) {
            double nx = dx / dist;
            double ny = dy / dist;

            double overlap = minD - dist;
            double totalM = a.getMass() + b.getMass();

            double a_factor = overlap * (b.getMass() / totalM);
            double a_deltax = nx * a_factor;
            double a_deltay = ny * a_factor;
            a.setPos(new P2d(a.getPos().x() - a_deltax, a.getPos().y() - a_deltay));

            double b_factor = overlap * (a.getMass() / totalM);
            double b_deltax = nx * b_factor;
            double b_deltay = ny * b_factor;
            b.setPos(new P2d(b.getPos().x() + b_deltax, b.getPos().y() + b_deltay));

            double dvx = b.getVel().x() - a.getVel().x();
            double dvy = b.getVel().y() - a.getVel().y();
            double dvn = dvx * nx + dvy * ny;

            if (dvn <= 0) {
                double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / a.getMass() + 1.0 / b.getMass());
                a.setVel(new V2d(a.getVel().x() - (imp / a.getMass()) * nx, a.getVel().y() - (imp / a.getMass()) * ny));
                b.setVel(new V2d(b.getVel().x() + (imp / b.getMass()) * nx, b.getVel().y() + (imp / b.getMass()) * ny));
            }

            a.setHitCredit(b.getType());
            b.setHitCredit(a.getType());
        }
    }

    public static void resolveHole(Ball ball, Hole hole, GameState gameState) {
        double dx = ball.getPos().x() - hole.pos().x();
        double dy = ball.getPos().y() - hole.pos().y();
        if (Math.hypot(dx, dy) < hole.radius()) {
            switch (ball.getType()) {
                case HUMAN:
                    gameState.endGame("Bot wins! Human fell in a hole.");
                    break;
                case BOT:
                    gameState.endGame("Human wins! Bot fell in a hole.");
                    break;
                case SMALL_BALL:
                    switch (ball.getHitCredit()) {
                        case BOT:
                            gameState.addBotScore();
                            break;
                        case HUMAN:
                            gameState.addHumanScore();
                            break;
                        default:
                            break;
                    }
                    gameState.removeSmallBall(ball);
                    break;
            }
        }
    }

    public synchronized P2d getPos() {
        return pos;
    }

    public synchronized void setPos(P2d pos) {
        this.pos = pos;
    }

    public synchronized double getMass() {
        return mass;
    }

    public synchronized V2d getVel() {
        return vel;
    }

    public synchronized void setVel(V2d vel) {
        this.vel = vel;
    }

    public synchronized double getRadius() {
        return radius;
    }

    public synchronized BallType getType() {
        return type;
    }

    public synchronized BallType getHitCredit() {
        return this.hitCredit;
    }

    public synchronized void setHitCredit(BallType hitCredit) {
        this.hitCredit = hitCredit;
    }
}

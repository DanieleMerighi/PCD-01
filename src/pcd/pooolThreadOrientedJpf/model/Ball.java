package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOrientedJpf.util.AtomicReference;
import pcd.pooolThreadOrientedJpf.util.AtomicReferenceImpl;

import java.util.function.Function;

public class Ball {

	private static final double FRICTION_FACTOR = 0.25; 	/* 0 minimum */
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

	public void updateState(long dt, Board ctx) {
		var speed = this.vel.abs();
		var dt_scaled = dt*0.001;
		if (speed > 0.001) {
			var dec    = FRICTION_FACTOR * dt_scaled;
			var factor = Math.max(0, speed - dec) / speed;
			vel = vel.mul(factor);
		} else {
			this.vel.set(new V2d(0,0));
		}
		pos = pos.sum(vel.mul(dt_scaled));
		applyBoundaryConstraints(ctx);
	}

	/**
	 * Keep the ball inside the boundaries, updating the velocity in the case of bounces
	 */
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

	public void kick(V2d vel) {
		this.vel = vel;
	}

	/**
	 * Resolving collision between 2 balls, updating their position and velocity
	 */
	public static void resolveCollision(Ball a, Ball b) {
		/* check if there is a collision */
		double dx = b.pos.x() - a.pos.x();
		double dy = b.pos.y() - a.pos.y();
		double dist = Math.hypot(dx, dy);
		double minD = a.radius + b.radius;

		/*
		 * There is a collision if the distance between the two balls is less than the sum of the radii
		 *
		 */
		if (dist < minD && dist > 1e-6) {
			/*
			 * Collision case - what to do:
			 *
			 * 1) solve overlaps, moving balls
			 * 2) update velocities
			 *
			 */

			/* dvn = V2d(nx,ny) = dv unit vector */

			double nx = dx / dist;
			double ny = dy / dist;

			/*
			 *
			 * Update positions to solve overlaps, moving balls along dvn
			 * - the displacements is proportional to the mass
			 *
			 */
			double overlap = minD - dist;
			double totalM = a.mass + b.mass;

			double a_factor = overlap * (b.mass / totalM);
			double a_deltax = nx * a_factor;
			double a_deltay = ny * a_factor;
			a.pos = new P2d(a.pos.x() - a_deltax, a.pos.y() - a_deltay);

			double b_factor = overlap * (a.mass / totalM);
			double b_deltax = nx * b_factor;
			double b_deltay = ny * b_factor;
			b.pos = new P2d(b.pos.x() + b_deltax, b.pos.y() + b_deltay);

			/* Update velocities  */

			/* relative speed along the normal vector*/
			double dvx = b.vel.x() - a.vel.x();
			double dvy = b.vel.y() - a.vel.y();
			double dvn = dvx * nx + dvy * ny;

			if (dvn <= 0) { /* if not already separating, update velocities */
				double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / a.mass + 1.0 / b.mass);
				a.vel = new V2d(a.vel.x() - (imp / a.mass) * nx, a.vel.y() - (imp / a.mass) * ny);
				b.vel = new V2d(b.vel.x() + (imp / b.mass) * nx, b.vel.y() + (imp / b.mass) * ny);
			}

			a.hitCredit = b.type;
			b.hitCredit = a.type;
		}
	}

    public static void resolveHole(Ball ball, Hole hole, Board board, GameState gameState) {
        double dx = ball.getPos().x() - hole.pos().x();
        double dy = ball.getPos().y() - hole.pos().y();
        if (Math.hypot(dx, dy) < hole.radius()) {
            switch (ball.type) {
                case HUMAN:
                    gameState.endGame("Bot wins! Human fell in a hole.");
                    break;
                case BOT:
                    gameState.endGame("Human wins! Bot fell in a hole.");
                    break;
                case SMALL_BALL:
                    switch (ball.hitCredit) {
                        case BOT:
                            gameState.addBotScore();
                            break;
                        case HUMAN:
                            gameState.addHumanScore();
                            break;
                        default:
                            break;
                    }
                    board.removeSmallBall(ball);
                    break;
            }
        }
    }

	public P2d getPos() {
		return pos;
	}

	public double getRadius() {
		return radius;
	}

}

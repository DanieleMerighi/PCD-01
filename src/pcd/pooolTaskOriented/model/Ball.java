package pcd.pooolTaskOriented.model;

import pcd.pooolTaskOriented.util.AtomicReference;
import pcd.pooolTaskOriented.util.AtomicReferenceImpl;

public class Ball {

	private static final double FRICTION_FACTOR = 0.25; 	/* 0 minimum */
	private static final double RESTITUTION_FACTOR = 1;

	private final AtomicReference<P2d> pos;
	private final AtomicReference<V2d> vel;
	private final double radius;
	private final double mass;
	private final BallType type;
	private final AtomicReference<BallType> hitCredit;

	public Ball(P2d pos, double radius, double mass, V2d vel, BallType type) {
		this.pos = new AtomicReferenceImpl<>(pos);
		this.radius = radius;
		this.mass = mass;
		this.vel = new AtomicReferenceImpl<>(vel);
		this.type = type;
		this.hitCredit = new AtomicReferenceImpl<>(BallType.NONE);
	}

	public Ball(P2d pos, double radius, double mass, V2d vel) {
		this(pos, radius, mass, vel, BallType.SMALL_BALL);
	}

	public void updateState(long dt, Board ctx) {
		var speed = this.vel.get().abs();
		var dt_scaled = dt*0.001;
		if (speed > 0.001) {
			var dec    = FRICTION_FACTOR * dt_scaled;
			var factor = Math.max(0, speed - dec) / speed;
			this.vel.map(value -> value.mul(factor));
		} else {
			this.vel.set(new V2d(0,0));
		}
		this.pos.map(value -> value.sum(this.vel.get().mul(dt_scaled)));
		applyBoundaryConstraints(ctx);
	}

	/**
	 * Keep the ball inside the boundaries, updating the velocity in the case of bounces
	 */
	private void applyBoundaryConstraints(Board ctx) {
		var bounds = ctx.getBounds();
		P2d pos = this.pos.get();
		if (pos.x() + radius > bounds.x1()) {
			this.pos.set(new P2d(bounds.x1() - radius, pos.y()));
			vel.map(V2d::getSwappedX);
		} else if (pos.x() - radius < bounds.x0()) {
			this.pos.set(new P2d(bounds.x0() + radius, pos.y()));
			vel.map(V2d::getSwappedX);
		} else if (pos.y() + radius > bounds.y1()) {
			this.pos.set(new P2d(pos.x(), bounds.y1() - radius));
			vel.map(V2d::getSwappedY);
		} else if (pos.y() - radius < bounds.y0()) {
			this.pos.set(new P2d(pos.x(), bounds.y0() + radius));
			vel.map(V2d::getSwappedY);
		}
	}

	public void kick(V2d vel) {
		this.vel.set(vel);
	}

	/**
	 * Resolving collision between 2 balls, updating their position and velocity
	 */
	public static void resolveCollision(Ball a, Ball b) {
		/* check if there is a collision */
		P2d aPos = a.pos.get();
		P2d bPos = b.pos.get();
		double dx = bPos.x() - aPos.x();
		double dy = bPos.y() - aPos.y();
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
			a.pos.set(new P2d(aPos.x() - a_deltax, aPos.y() - a_deltay));

			double b_factor = overlap * (a.mass / totalM);
			double b_deltax = nx * b_factor;
			double b_deltay = ny * b_factor;
			b.pos.set(new P2d(bPos.x() + b_deltax, bPos.y() + b_deltay));

			/* Update velocities  */

			/* relative speed along the normal vector*/
			V2d aVel = a.vel.get();
			V2d bVel = b.vel.get();
			double dvx = bVel.x() - aVel.x();
			double dvy = bVel.y() - aVel.y();
			double dvn = dvx * nx + dvy * ny;

			if (dvn <= 0) { /* if not already separating, update velocities */
				double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / a.mass + 1.0 / b.mass);
				a.vel.set(new V2d(aVel.x() - (imp / a.mass) * nx, aVel.y() - (imp / a.mass) * ny));
				b.vel.set(new V2d(bVel.x() + (imp / b.mass) * nx, bVel.y() + (imp / b.mass) * ny));
			}

			a.hitCredit.set(b.type);
			b.hitCredit.set(a.type);
		}
	}

	public static void resolveHole(Ball ball, Hole hole, Board board, GameState gameState) {
		var dx = ball.pos.get().x() - hole.pos().x();
		var dy = ball.pos.get().y() - hole.pos().y();
		if (Math.hypot(dx, dy) < hole.radius()) {
			switch (ball.type) {
				case HUMAN -> gameState.endGame("Bot wins! Human fell in a hole.");
				case BOT -> gameState.endGame("Human wins! Bot fell in a hole.");
				case SMALL_BALL -> {
					switch (ball.hitCredit.get()) {
						case BOT -> gameState.addBotScore();
						case HUMAN -> gameState.addHumanScore();
					}
					board.removeSmallBall(ball);
				}
			}
		}
	}

	public P2d getPos() {
		return pos.get();
	}

	public double getRadius() {
		return radius;
	}

}

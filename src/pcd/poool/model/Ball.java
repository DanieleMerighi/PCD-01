package pcd.poool.model;

public class Ball {
    
    private P2d pos;
    private V2d vel;
    private final double radius;
    private final double mass;
	private final HitCredit role;
    private HitCredit hitCredit;
    
    private static final double FRICTION_FACTOR = 0.25; 	/* 0 minimum */
    private static final double RESTITUTION_FACTOR = 1;

    public Ball(P2d pos, double radius, double mass, V2d vel, HitCredit role){
       this.pos = pos;
       this.radius = radius;
       this.mass = mass;
       this.vel = vel;
	   this.role = role;
       this.hitCredit = HitCredit.NONE;
    }

	public Ball(P2d pos, double radius, double mass, V2d vel){
		this(pos, radius, mass, vel, HitCredit.NONE);
	}

    public synchronized void updateState(long dt, Board ctx){
        double speed = vel.abs();
        double dt_scaled = dt*0.001;
    	if (speed > 0.001) {
            double dec    = FRICTION_FACTOR * dt_scaled;
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
        	vel = new V2d(0,0);
        }
        pos = pos.sum(vel.mul(dt_scaled));
     	applyBoundaryConstraints(ctx);
    }
    
    public synchronized void kick(V2d vel) {
    	this.vel = vel;
    }

    /**
     * Keep the ball inside the boundaries, updating the velocity in the case of bounces
     */
    private void applyBoundaryConstraints(Board ctx){
        Boundary bounds = ctx.getBounds();
        if (pos.x() + radius > bounds.x1()){
            pos = new P2d(bounds.x1() - radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.x() - radius < bounds.x0()){
            pos = new P2d(bounds.x0() + radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.y() + radius > bounds.y1()){
            pos = new P2d(pos.x(), bounds.y1() - radius);
            vel = vel.getSwappedY();
        } else if (pos.y() - radius < bounds.y0()){
            pos = new P2d(pos.x(), bounds.y0() + radius);
            vel = vel.getSwappedY();
        }
    }

    /**
     * Resolving collision between 2 balls, updating their position and velocity
     */
    public static void resolveCollision(Ball a, Ball b) {

    	/* check if there is a collision */
    	
    	/* compute dv = b.pos - a.pos vector */

    	double dx   = b.pos.x() - a.pos.x();
        double dy   = b.pos.y() - a.pos.y();
        double dist = Math.hypot(dx, dy);
        double minD = a.radius + b.radius;
        
        /* 
         * There is a collision if the distance between the two balls is less than the sum of the radii 
         * 
         */
        if (dist < minD && dist > 1e-6)  {

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
	        double totalM  = a.getMass() + b.getMass();
	
	        double a_factor = overlap * (b.getMass() / totalM);
	        double a_deltax = nx * a_factor; 
	        double a_deltay = ny * a_factor; 
	        
	        a.setPos(new P2d(a.getPos().x() - a_deltax, a.getPos().y() - a_deltay));
	        
	        double b_factor = overlap * (a.getMass() / totalM);
	        double b_deltax = nx * b_factor; 
	        double b_deltay = ny * b_factor; 
	
	        b.setPos(new P2d(b.getPos().x() + b_deltax, b.getPos().y() + b_deltay));
	
	        /* Update velocities  */
	        
	        /* relative speed along the normal vector*/
	
	        double dvx = b.getVel().x() - a.getVel().x();
	        double dvy = b.getVel().y() - a.getVel().y();
	        double dvn = dvx * nx + dvy * ny;
	
	        if (dvn <= 0) { /* if not already separating, update velocities */
	        	
	        	double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0/a.getMass() + 1.0/b.getMass());        
	        	a.setVel(new V2d(a.getVel().x() - (imp / a.getMass()) * nx, a.getVel().y() - (imp / a.getMass()) * ny));
	        	b.setVel(new V2d(b.getVel().x() + (imp / b.getMass()) * nx, b.getVel().y() + (imp / b.getMass()) * ny));
	        }

			a.setHitCredit(b.getRole());
			b.setHitCredit(a.getRole());
        }
    }

    public static void resolveHole(Ball ball, Hole hole, Board board) {
        var dx = ball.getPos().x() - hole.pos().x();
        var dy = ball.getPos().y() - hole.pos().y();
        if (Math.hypot(dx, dy) < hole.radius()) {
			switch (ball.getRole()) {
				case PLAYER -> board.endGame("Bot wins! Player fell in a hole.");
				case BOT -> board.endGame("Player wins! Bot fell in a hole.");
				case NONE -> {
					switch (ball.getHitCredit()){
						case BOT -> board.addBotScore();
						case PLAYER -> board.addPlayerScore();
					}
					board.removeBall(ball);
				}
			}
		}
    }
    
    public synchronized P2d getPos(){
    	return pos;
    }

	public synchronized void setPos(P2d pos){
		this.pos = pos;
	}
    
    public synchronized double getMass() {
    	return mass;
    }
    
    public synchronized V2d getVel() {
    	return vel;
    }

	public synchronized void setVel(V2d vel){
		this.vel = vel;
	}
    
    public synchronized double getRadius() {
    	return radius;
    }

	public synchronized HitCredit getRole() {
		return role;
	}

    public synchronized HitCredit getHitCredit() {
        return this.hitCredit;
    }

    public synchronized void setHitCredit(HitCredit hitCredit) {
        this.hitCredit = hitCredit;
    }

}

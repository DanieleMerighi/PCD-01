package pcd.pooolThreadOrientedJpf.model;

public final class Hole {

    private final P2d pos;
    private final double radius;

    public Hole(P2d pos, double radius) {
        this.pos = pos;
        this.radius = radius;
    }

    public P2d pos() {
        return pos;
    }

    public double radius() {
        return radius;
    }
}

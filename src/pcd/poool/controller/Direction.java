package pcd.poool.controller;

import pcd.poool.model.V2d;

public enum Direction {
    UP(0,1),
    LEFT(-1, 0),
    DOWN(0, -1),
    RIGHT(1, 0);

    private final V2d vector;

    Direction(double x, double y) {
        vector = new V2d(x, y);
    }

    public V2d getVector() {
        return vector;
    }
}

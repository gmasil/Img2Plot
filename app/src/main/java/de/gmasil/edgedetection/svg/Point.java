package de.gmasil.edgedetection.svg;

public class Point {
    public float x;
    public float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        this(0, 0);
    }

    public float distance(Point other) {
        return (float) Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    @Override
    public String toString() {
        return String.format("[%.6f,%.6f]", x, y);
    }
}

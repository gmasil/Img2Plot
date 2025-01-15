package de.gmasil.edgedetection.svg;

public class Point {
    public float x;
    public float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point() {
        this(0, 0);
    }

    public float distance(Point other) {
        return (float) Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public Point addToCopy(Point o) {
        return new Point(x+o.x, y+o.y);
    }

    public void translate(Point p) {
        x += p.x;
        y += p.y;
    }

    public void scale(Point p) {
        x *= p.x;
        y *= p.y;
    }

    @Override
    public String toString() {
        return String.format("[%.6f,%.6f]", x, y);
    }
}

package de.gmasil.edgedetection.svg.element;

import de.gmasil.edgedetection.svg.Point;

import java.util.List;

public abstract class SvgElement {

    protected float strokeWidth = 1;
    protected String strokeColor = "#000000";

    public abstract String toSvgString();

    public abstract void invert();

    public abstract Point getFirstPoint();

    public abstract Point getLastPoint();

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public abstract void translate(Point p);

    public abstract void scale(Point p);

    public abstract float getLength();

    public static float calculateLength(List<Point> points) {
        double length = 0;
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            length += Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        }
        return (float) length;
    }
}

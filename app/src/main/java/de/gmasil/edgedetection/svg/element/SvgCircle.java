package de.gmasil.edgedetection.svg.element;

import de.gmasil.edgedetection.svg.Point;

public class SvgCircle extends SvgElement {

    private static final String TEMPLATE = "<circle stroke-width=\"%s\" style=\"fill:%s; stroke:%s;\" cx=\"%f\" cy=\"%f\" r=\"%f\" />";

    protected Point center;
    private float radius;

    public SvgCircle(Point center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public SvgCircle(Point center, float radius, float strokeWidth, String strokeColor) {
        this(center, radius);
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public String toSvgString() {
        return String.format(TEMPLATE, strokeWidth, strokeColor, strokeColor, center.x, center.y, radius);
    }

    @Override
    public void invert() {
        throw new UnsupportedOperationException("Cannot invert a circle");
    }

    @Override
    public Point getFirstPoint() {
        return new Point(center.x - radius, center.y);
    }

    @Override
    public Point getLastPoint() {
        return new Point(center.x - radius, center.y);
    }

    @Override
    public void translate(Point translate) {
        center.translate(translate);
    }

    @Override
    public void scale(Point scale) {
        // cannot scale, nothing to do
    }

    @Override
    public float getLength() {
        return 0;
    }
}

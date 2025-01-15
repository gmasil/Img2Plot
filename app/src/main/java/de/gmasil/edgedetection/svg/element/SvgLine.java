package de.gmasil.edgedetection.svg.element;

import de.gmasil.edgedetection.svg.Point;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SvgLine extends SvgElement {

    private static final String TEMPLATE = """
            <path stroke-width="%f" stroke="%s" fill="none" d="%s"/>
            """.replace("\n", "").trim();

    private final List<Point> points;
    private boolean travel = false;

    public SvgLine(Point startPoint, Point endPoint) {
        this(List.of(startPoint, endPoint));
    }

    public SvgLine(Point startPoint, Point endPoint, float strokeWidth, String strokeColor) {
        this(List.of(startPoint, endPoint), strokeWidth, strokeColor);
    }

    public SvgLine(List<Point> points) {
        this.points = new LinkedList<>(points);
    }

    public SvgLine(List<Point> points, float strokeWidth, String strokeColor) {
        this.points = new LinkedList<>(points);
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
    }

    public void addLine(SvgLine line) {
        this.points.addAll(line.getPoints());
    }

    public List<Point> getPoints() {
        return points;
    }

    public boolean isTravel() {
        return travel;
    }

    public SvgLine travel() {
        this.travel = true;
        return this;
    }

    @Override
    public String toSvgString() {
        StringBuilder path = new StringBuilder();
        path.append("M ");
        path.append(points.getFirst().x);
        path.append(" ");
        path.append(points.getFirst().y);
        path.append(" ");
        for (int i = 1; i < points.size(); i++) {
            path.append("L ");
            path.append(points.get(i).x);
            path.append(" ");
            path.append(points.get(i).y);
            path.append(" ");
        }
        return TEMPLATE.formatted(strokeWidth, strokeColor, path.toString());
    }

    @Override
    public void invert() {
        Collections.reverse(points);
    }

    @Override
    public Point getFirstPoint() {
        return points.getFirst();
    }

    @Override
    public Point getLastPoint() {
        return points.getLast();
    }

    @Override
    public void translate(Point translate) {
        points.forEach(p -> p.translate(translate));
    }

    @Override
    public void scale(Point scale) {
        points.forEach(p -> p.scale(scale));
    }

    @Override
    public float getLength() {
        return calculateLength(getPoints());
    }
}

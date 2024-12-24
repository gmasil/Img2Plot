package de.gmasil.edgedetection.svg.element;

import de.gmasil.edgedetection.svg.Point;

import java.util.Collections;
import java.util.List;

public class SvgLine extends SvgElement {

    private static final String TEMPLATE = """
            <path stroke-width="%f" style="fill:none; stroke:%s;" d="%s"/>
            """.replace("\n", "").trim();

    private final List<Point> points;

    public SvgLine(Point startPoint, Point endPoint) {
        this(List.of(startPoint, endPoint));
    }

    public SvgLine(Point startPoint, Point endPoint, float strokeWidth, String strokeColor) {
        this(List.of(startPoint, endPoint), strokeWidth, strokeColor);
    }

    public SvgLine(List<Point> points) {
        this.points = points;
    }

    public SvgLine(List<Point> points, float strokeWidth, String strokeColor) {
        this(points);
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
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
}

package de.gmasil.edgedetection.svg.element;

import de.gmasil.edgedetection.svg.Point;

import java.util.LinkedList;
import java.util.List;

/**
 * See <a href="https://www.jasondavies.com/animated-bezier/">Animated Bezier</a>
 */
public class SvgBezierCurve extends SvgElement {

    private Point start;
    private Point control1;
    private Point control2;
    private Point end;

    public SvgBezierCurve(Point start, Point control1, Point control2, Point end) {
        this.start = start;
        this.control1 = control1;
        this.control2 = control2;
        this.end = end;
    }

    public List<SvgElement> getBezierVisualizationPaths(){
        List<SvgElement> elements = new LinkedList<>();
        elements.add(new SvgLine(start, control1, 1f, "#ff0000"));
        elements.add(new SvgLine(end, control2, 1f, "#ff0000"));
        elements.add(new SvgCircle(control1, 3, 1, "#ff0000"));
        elements.add(new SvgCircle(control2, 3, 1, "#ff0000"));
        return elements;
    }

    public SvgLine toLine(int resolution){
        List<Point> points = new LinkedList<>();
        float step = 1.0f / resolution;
        Point previous = start;
        for (float f = step; f < 1; f += step){
            Point p = calculateBezierPoint(f);
            points.add(p);
            previous = p;
        }
        points.add(previous);
        return new SvgLine(points);
    }

    private Point calculateBezierPoint(float f){
        // first iteration
        Point i1p1 = calculateBezierPoint(start, control1, f);
        Point i1p2 = calculateBezierPoint(control1, control2, f);
        Point i1p3 = calculateBezierPoint(control2, end, f);

        // second iteration
        Point i2p1 = calculateBezierPoint(i1p1, i1p2, f);
        Point i2p2 = calculateBezierPoint(i1p2, i1p3, f);

        return calculateBezierPoint(i2p1, i2p2, f);
    }

    private Point calculateBezierPoint(Point a, Point b, float f) {
        return new Point(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f);
    }

    @Override
    public String toSvgString() {
        return toLine(100).toSvgString();
    }

    @Override
    public void invert() {
        Point temp = start;
        start = end;
        end = temp;
        temp = control1;
        control1 = control2;
        control2 = temp;
    }

    @Override
    public Point getFirstPoint() {
        return start;
    }

    @Override
    public Point getLastPoint() {
        return end;
    }
}

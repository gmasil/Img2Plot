package de.gmasil.edgedetection.svg;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Path {
    private static final String PATH_TEMPLATE = """
            <path stroke-width="%f" style="fill:none; stroke:%s;" d="%s"/>
            """.replace("\n", "").trim();

    private List<String> args;
    private List<Point> points;
    private float length;
    private float strokeWidth = 1;
    private String strokeColor = "#000000";
    private boolean isTravelLine = false;

    public Path(Object... args) {
        this(Arrays.stream(args).map(Object::toString).toList());
    }

    public Path(List<String> args) {
        if(args.isEmpty()) {
            throw new IllegalArgumentException("Path args cannot be empty");
        }
        this.args = args;
        this.points = extractPoints(args);
        this.length = calculateLength(points);
    }

    public Path(List<String> args, float strokeWidth, String strokeColor, boolean isTravelLine) {
        this(args);
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
        this.isTravelLine = isTravelLine;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void invert() {
        List<String> invertedArgs = new LinkedList<>();
        invertedArgs.add("M");
        invertedArgs.add(args.get(args.size() - 2));
        invertedArgs.add(args.getLast());
        // TODO: handle z
        for (int i = args.size() - 1; i >= 2; i--) {
            if(args.get(i).equals("L") || args.get(i).equals("M")) {
                invertedArgs.add("L");
                invertedArgs.add(args.get(i - 2));
                invertedArgs.add(args.get(i - 1));
            } else if(args.get(i).equals("C")) {
                invertedArgs.add("C");
                invertedArgs.add(args.get(i + 3));
                invertedArgs.add(args.get(i + 4));
                invertedArgs.add(args.get(i + 1));
                invertedArgs.add(args.get(i + 2));
                invertedArgs.add(args.get(i - 2));
                invertedArgs.add(args.get(i - 1));
            }
        }
        this.args = invertedArgs;
        this.points = extractPoints(invertedArgs);
        this.length = calculateLength(points);
    }

    public Point getFirstPoint() {
        return points.getFirst();
    }

    public Point getLastPoint() {
        return points.getLast();
    }

    public List<String> getArgs() {
        return args;
    }

    public float getLength() {
        return length;
    }

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

    public boolean isTravelLine() {
        return isTravelLine;
    }

    public void setTravelLine(boolean travelLine) {
        isTravelLine = travelLine;
    }

    public String toSvgString() {
        return String.format(PATH_TEMPLATE, strokeWidth, strokeColor, String.join(" ", args));
    }

    @Override
    public String toString() {
        return toSvgString();
    }

    public static List<Point> extractPoints(List<String> args) {
        List<Point> points = new LinkedList<>();
        boolean returnToStart = args.getLast().endsWith("z");
        int i = 0;
        while (i < args.size()) {
            String c = args.get(i);
            if (c.equals("M") || c.equals("L")) {
                points.add(new Point(Float.parseFloat(args.get(i + 1)), Float.parseFloat(args.get(i + 2).replace("z", ""))));
                i += 3;
            } else if (c.equals("C")) {
                points.add(new Point(Float.parseFloat(args.get(i + 5)), Float.parseFloat(args.get(i + 6).replace("z", ""))));
                i += 7;
            }
        }
        if (returnToStart) {
            points.add(points.getFirst());
        }
        return points;
    }

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

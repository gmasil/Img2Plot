package de.gmasil.edgedetection.gcode;

import de.gmasil.edgedetection.svg.Point;
import de.gmasil.edgedetection.svg.Svg;
import de.gmasil.edgedetection.svg.element.SvgLine;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Gcode {
    public static final List<String> HEADER = List.of(
            "G21", // Set units to millimeters
            "G90", // Absolute Positioning
            "G0 F36000", // Set feedrate to 36000 mm/min (600 mm/s)
            "G0 Z10", // Move Z to 1cm
            "G0 X0 Y0" // Move to 0,0
    );

    private final Point offset;
    private final float factor;

    public Gcode(Point offset, float factor) {
        this.offset = offset;
        this.factor = factor;
    }

    public void saveSvgToGCode(Svg svg, String outputFile) throws Exception {

        System.out.println("Min: " + getMinPoint(svg.getSvgLines()));
        System.out.println("Max: " + getMaxPoint(svg.getSvgLines()));

        List<SvgLine> lines = svg.getSvgLines();
        List<String> gCodeLines = new LinkedList<>(HEADER);
        gCodeLines.addAll(lines.stream().flatMap(this::lineToGCode).toList());
        String content = String.join("\n", gCodeLines);
        PrintWriter writer = new PrintWriter(outputFile, StandardCharsets.UTF_8);
        writer.println(content);
        writer.close();
    }

    public Stream<String> lineToGCode(SvgLine line) {
        return line.getPoints().stream().map(this::pointToGCode);
    }

    public String pointToGCode(Point point) {
        Point p = transformPoint(point);
        return "G0 X" + p.x + " Y" + p.y;
    }

    public Point getMinPoint(List<SvgLine> lines) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        for (SvgLine line : lines) {
            for (Point point : line.getPoints()) {
                Point p = transformPoint(point);
                if (p.x < minX) {
                    minX = p.x;
                }
                if (p.y < minY) {
                    minY = p.y;
                }
            }
        }
        return new Point(minX, minY);
    }

    public Point getMaxPoint(List<SvgLine> lines) {
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (SvgLine line : lines) {
            for (Point point : line.getPoints()) {
                Point p = transformPoint(point);
                if (p.x > maxX) {
                    maxX = p.x;
                }
                if (p.y > maxY) {
                    maxY = p.y;
                }
            }
        }
        return new Point(maxX, maxY);
    }

    public Point transformPoint(Point p) {
        return new Point(p.x * factor + offset.x, p.y * factor + offset.y);
    }
}

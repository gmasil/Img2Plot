package de.gmasil.edgedetection.svg;

import de.gmasil.edgedetection.svg.element.SvgBezierCurve;
import de.gmasil.edgedetection.svg.element.SvgElement;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

class SvgBezierCurveTest {

    @Test
    void testBezierRendering() throws Exception {
        SvgBezierCurve bezierCurve = new SvgBezierCurve(new Point(50, 100), new Point(70, 150), new Point(150, 140), new Point(150, 100));
//        List<SvgElement> paths = new LinkedList<>();
////        Path path = bezierCurve.toPath();
////        paths.addAll(bezierCurve.getBezierVisualizationPaths());
////        paths.add(path);
//        paths.addAll(bezierCurve.toLine());
        Svg svg = new Svg(200,200, List.of(bezierCurve));
        svg.save("bezier.svg");
    }
}

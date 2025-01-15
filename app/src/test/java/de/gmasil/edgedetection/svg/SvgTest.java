package de.gmasil.edgedetection.svg;

import de.gmasil.edgedetection.svg.element.SvgBezierCurve;
import de.gmasil.edgedetection.svg.element.SvgElement;
import de.gmasil.edgedetection.svg.element.SvgLine;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

class SvgTest {
    @Test
    void testOrderReverse() throws Exception {
        Svg svg = Svg.loadSvg("src/test/resources/order-test.svg");
        svg.orderPaths(true);
        svg.setStrokeWidth(4);
        new File("build/out").mkdirs();
        svg.save("build/out/order-test-result.svg");
    }

    @Test
    void testMerge() throws Exception {
        Svg svg = Svg.loadSvg("src/test/resources/order-test.svg");
        svg.orderPaths(true);
        svg.setStrokeWidth(4);
        svg.mergeClosePaths(55);
//        svg.addTravelPaths();
        new File("build/out").mkdirs();
        svg.save("build/out/merge-test-result.svg");
    }

    @Test
    void test() throws Exception {
        Svg svg = Svg.loadSvg("../out-02-traced.svg");
        svg.setStrokeWidth(10);
        svg.save("../out-05-test.svg");
    }
}

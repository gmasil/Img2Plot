package de.gmasil.edgedetection.svg;

import org.junit.jupiter.api.Test;

import java.io.File;

class SvgTest {
    @Test
    void testOrderReverse() throws Exception {
        Svg svg = Svg.loadSvg("src/test/resources/order-test.svg");
        svg.orderPaths();
//        svg.setStrokeWidth(4);
//        svg.addTravelPaths();
        new File("build/out").mkdirs();
        svg.save("build/out/order-test-result.svg");
    }
}

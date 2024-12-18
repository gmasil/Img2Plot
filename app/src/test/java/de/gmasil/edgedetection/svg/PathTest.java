package de.gmasil.edgedetection.svg;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathTest {

    @Test
    void test(){
        String pathContent = "M 1935 1388 C 1895.81 1408.06 1873.69 1407.44 1867 1394 C 1532.53 1414.52 1540.31 1413 1545 1413 L 1639 1413";
        Path path = new Path(List.of(pathContent.split(" +")));
        System.out.println(path);
        System.out.println(String.join(" ", path.getPoints().stream().map(Point::toString).toList()));
        path.invert();
        System.out.println(path);
        System.out.println(String.join(" ", path.getPoints().stream().map(Point::toString).toList()));
        String expected = "<path stroke-width=\"1.000000\" style=\"fill:none; stroke:#000000;\" d=\"M 1639 1413 L 1545 1413 C 1540.31 1413 1532.53 1414.52 1867 1394 C 1873.69 1407.44 1895.81 1408.06 1935 1388\"/>";
        assertThat(path.toString()).isEqualTo(expected);
    }
}

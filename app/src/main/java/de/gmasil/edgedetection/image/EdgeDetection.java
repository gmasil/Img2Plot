package de.gmasil.edgedetection.image;

import de.gmasil.edgedetection.svg.Svg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.util.Arrays;

public class EdgeDetection {

    public static void handleImage(String inputImageFile, int blurRadius, int edgeThreshold) {
        try {
            BufferedImage image = ImageIO.read(new File(inputImageFile));

            if (blurRadius > 0) {
                image = blurImage(image, blurRadius);
            }
            image = detectEdges(image, edgeThreshold, true);
            ImageIO.write(image, "png", new File("out-01-edges.png"));

            runAutotrace("out-01-edges.png", "out-02-traced.svg");
            int lengthThreshold = Math.min(image.getWidth(), image.getHeight()) / 30;
            Svg svg = Svg.loadSvg("out-02-traced.svg");
            svg.filter(lengthThreshold);
            float travelLength = svg.getTravelLength();
            svg.orderPaths();
            int maxDistance = Math.min(image.getWidth(), image.getHeight()) / 100;
            svg.mergeClosePaths(maxDistance);
            float newTravelLength = svg.getTravelLength();
            System.out.println("Travel length: " + travelLength + " -> " + newTravelLength + " (improvement: " + (100 - (100 * newTravelLength / travelLength)) + "%)");

            // travel must be last
            svg.setStrokeWidth(1);
            svg.save("out-04-result.svg");
            svg.addTravelPaths();
            svg.save("out-03-filtered.svg");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage detectEdges(BufferedImage image, int threshold, boolean binary) {
        BufferedImage edgeImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < image.getWidth() - 1; x++) {
            for (int y = 0; y < image.getHeight() - 1; y++) {

                int p00 = image.getRGB(x, y) & 0xFF;
                int p01 = image.getRGB(x + 1, y) & 0xFF;
                int p10 = image.getRGB(x, y + 1) & 0xFF;

                int brightness = Math.abs(p00 - p01) + Math.abs(p00 - p10);
                brightness = Math.min(brightness, 255);
                brightness = 255 - brightness;
                brightness = brightness < threshold ? (binary ? 0 : brightness) : 255;
                edgeImage.setRGB(x, y, brightness << 16 | brightness << 8 | brightness);
            }
        }
        return edgeImage;
    }

    public static BufferedImage blurImage(BufferedImage image, int blurSize) {
        float weight = 1.0f / (blurSize * blurSize);
        float[] data = new float[blurSize * blurSize];
        Arrays.fill(data, weight);
        Kernel kernel = new Kernel(blurSize, blurSize, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        return op.filter(image, null);
    }

    public static void runAutotrace(String input, String output) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("autotrace",
                input,
                "-output-file", output,
                "-background-color", "FFFFFF"
//                "-despeckle-level", "0",
//                "-despeckle-tightness", "8.0",
//                "-filter-iterations", "4",
//                "-error-threshold", "20.0",
//                "-color-count", "0",
//                "-noise-removal", "0.99"
        );
        Process p = pb.start();
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            String result = new String(p.getErrorStream().readAllBytes());
            System.out.println("Autotrace output:\n" + result);
            throw new RuntimeException("Autotrace failed with exit code " + exitCode);
        }
    }
}

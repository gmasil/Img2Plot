package de.gmasil.edgedetection;

import de.gmasil.edgedetection.image.EdgeDetection;

public class App {

    public static void main(String[] args) throws Exception {
        String image = "image.png";
        if(args.length > 0){
            image = args[0];
        }
        int blurRadius = 3;
        if (args.length > 1) {
            blurRadius = Integer.parseInt(args[1]);
        }
        int edgeThreshold = 250;
        if (args.length > 2) {
            edgeThreshold = Integer.parseInt(args[2]);
        }
        EdgeDetection.handleImage(image, blurRadius, edgeThreshold);
        // allow another image for agent run
        if(args.length > 3){
            image = args[3];
            EdgeDetection.handleImage(image, blurRadius, edgeThreshold);
        }
    }
}

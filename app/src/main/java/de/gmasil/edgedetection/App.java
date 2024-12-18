package de.gmasil.edgedetection;

import de.gmasil.edgedetection.image.EdgeDetection;

public class App {

    public static void main(String[] args) throws Exception {
        String image = "image.png";
        if(args.length > 0){
            image = args[0];
        }
        EdgeDetection.handleImage(image);
    }
}

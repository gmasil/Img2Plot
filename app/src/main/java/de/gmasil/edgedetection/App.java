package de.gmasil.edgedetection;

import de.gmasil.edgedetection.image.EdgeDetection;
import de.gmasil.edgedetection.ui.DemoFrame;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "EdgeDetection")
public class App implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Files to process.")
    File[] files;

    @Option(names = {"-b", "--blur-radius"}, description = "Blur radius to remove speckles and details", defaultValue = "3")
    int blurRadius = 3;

    @Option(names = {"-e", "--edge-threshold"}, description = "Threshold for edge detection (1..255)\nStart with 250 and decrease until lines are straight", defaultValue = "250")
    int edgeThreshold = 250;

    @Option(names = {"-gui"}, description = "Open GUI", defaultValue = "false")
    boolean ui = false;

    @Option(names = {"--instrumentation-run-only"}, description = "Close application automatically for instrumentation run", defaultValue = "false", hidden = true)
    boolean instrumentationRunOnly = false;

    @Override
    public Integer call() throws Exception {
        if(ui) {
            if(files.length > 1) {
                System.err.println("Cannot open GUI with multiple files");
                return 1;
            }
            new DemoFrame(!instrumentationRunOnly);
            return 0;
        }
        Arrays.asList(files).forEach(file -> EdgeDetection.handleImage(file.getAbsolutePath(), blurRadius, edgeThreshold));
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}

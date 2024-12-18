package de.gmasil.edgedetection.svg;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Svg {

    private static final List<String> SVG_HEADER = Arrays.asList("""
            <?xml version="1.0" standalone="yes"?>
            """.replace("\n", "").trim(), """
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d">
            """.replace("\n", "").trim(), """
            <rect width="%d" height="%d" x="0" y="0" fill="#ffffff" />
            """.replace("\n", "").trim());
    private static final List<String> SVG_FOOTER = Arrays.asList("""
            </svg>
            """.replace("\n", "").trim());

    private final int width;
    private final int height;
    private List<Path> paths;

    public Svg(int width, int height, List<Path> paths) {
        this.width = width;
        this.height = height;
        this.paths = paths;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void filter(int lengthThreshold) {
        paths = paths.stream().filter(x -> x.getLength() >= lengthThreshold).toList();
    }

    public float getTravelLength(){
        float length = 0;
        for (int i = 0; i < paths.size() - 1; i++) {
            Path p1 = paths.get(i);
            Path p2 = paths.get(i + 1);
            length += p1.getLastPoint().distance(p2.getFirstPoint());
        }
        return length;
    }

    public void removeTravelPaths() {
        List<Path> filteredPaths = new LinkedList<>();
        for (Path path : paths) {
            if (!path.isTravelLine()) {
                filteredPaths.add(path);
            }
        }
        paths = filteredPaths;
    }

    public void addTravelPaths() {
        removeTravelPaths();
        List<Path> travelPaths = new LinkedList<>();
        for (int i = 0; i < paths.size() - 1; i++) {
            Path p1 = paths.get(i);
            Path p2 = paths.get(i + 1);
            travelPaths.add(new Path(List.of("M", "" + p1.getLastPoint().x, "" + p1.getLastPoint().y, "L", "" + p2.getFirstPoint().x, "" + p2.getFirstPoint().y), 1, "#ff0000", true));
        }
        this.paths.addAll(travelPaths);
    }

    public void invertPaths() {
        paths.forEach(Path::invert);
    }

    public void setStrokeWidth(float strokeWidth) {
        for (Path path : paths) {
            path.setStrokeWidth(strokeWidth);
        }
    }

    public void orderPaths() {
        List<PathChainElement> pathChain = getPaths().stream().map(PathChainElement::new).toList();
        paths = new LinkedList<>();
        while(pathChain.stream().filter(x -> x.getPreviousElement() == null || x.getNextElement() == null).count() > 2) {
            for (PathChainElement currentElement : pathChain) {
                if (currentElement.getNextElement() != null) {
                    continue;
                }
                PathChainElement closestPathElement = null;
                float closestDistance = Float.MAX_VALUE;
                boolean reversed = false;
                for (PathChainElement element : pathChain) {
                    if (element != currentElement) {
                        // check path in normal order
                        if (element.getPreviousElement() == null&& !currentElement.isInChainBackwards(element)) {
                            float distance = currentElement.getPath().getLastPoint().distance(element.getPath().getFirstPoint());
                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestPathElement = element;
                                reversed = false;
                            }
                        }
                        // check path in reverse order
                        if (element.getNextElement() == null) {
                            float distance = currentElement.getPath().getLastPoint().distance(element.getPath().getLastPoint());
                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestPathElement = element;
                                reversed = true;
                            }
                        }
                    }
                }
                if (closestPathElement != null) {
                    if (reversed) {
                        if (closestPathElement.getNextElement() != null) {
                            throw new RuntimeException("PathChainElement may not have a next element");
                        }
                        closestPathElement.reverseChainBackwards(new HashSet<>());
                    }
                    currentElement.setNextElement(closestPathElement);
                    closestPathElement.setPreviousElement(currentElement);
                }
            }
        }
        PathChainElement currentElement = pathChain.getFirst();
        while (currentElement.getPreviousElement() != null) {
            currentElement = currentElement.getPreviousElement();
        }
        while (currentElement != null) {
            paths.add(currentElement.getPath());
            currentElement = currentElement.getNextElement();
        }
    }

    public void mergeClosePaths(int maxDistance) {
        removeTravelPaths();
        List<Path> mergedPaths = new LinkedList<>();
        boolean mergedLastPath = false;
        for (int i = 0; i < paths.size() - 1; i++) {
            Path p1 = paths.get(i);
            Path p2 = paths.get(i + 1);
            if (p1.getLastPoint().distance(p2.getFirstPoint()) < maxDistance) {
                List<String> args = new LinkedList<>();
                args.addAll(p1.getArgs());
                args.addAll(p2.getArgs().subList(3, p2.getArgs().size()));
                mergedPaths.add(new Path(args));
                mergedLastPath = true;
                i++;
            } else {
                mergedPaths.add(p1);
                mergedLastPath = false;
            }
        }
        if(!mergedLastPath) {
            mergedPaths.add(paths.getLast());
        }
        paths = mergedPaths;
    }

    @Override
    public String toString() {
        List<String> lines = new LinkedList<>(SVG_HEADER.stream().map(x -> x.formatted(width, height)).toList());
        lines.addAll(paths.stream().map(Path::toString).toList());
        lines.addAll(SVG_FOOTER);
        return String.join("\n", lines);
    }

    public void save(String outputFile) throws Exception {
        PrintWriter writer = new PrintWriter(outputFile, StandardCharsets.UTF_8);
        writer.println(this);
        writer.close();
    }

    public static Svg loadSvg(String inputFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        File file = new File(inputFile);
        Document doc = builder.parse(file);
        Node svg = doc.getElementsByTagName("svg").item(0);
        int width = Integer.parseInt(svg.getAttributes().getNamedItem("width").getTextContent());
        int height = Integer.parseInt(svg.getAttributes().getNamedItem("height").getTextContent());
        NodeList nodes = svg.getChildNodes();
        List<String> contents = new LinkedList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase("path")) {
                String content = node.getAttributes().getNamedItem("d").getTextContent();
                content = content.replace("M", " M ");
                content = content.replace("L", " L ");
                content = content.replace("C", " C ");
                contents.add(content.trim());
            }
        }
        List<Path> paths = new LinkedList<>();
        for (String content : contents) {
            String[] tokens = content.split(" +");
            List<String> args = new LinkedList<>();
            for (String token : tokens) {
                if (token.equals("M")) {
                    if (!args.isEmpty()) {
                        paths.add(new Path(args));
                    }
                    args = new LinkedList<>();
                    args.add(token);
                } else {
                    args.add(token);
                }
            }
            if (!args.isEmpty()) {
                paths.add(new Path(args));
            }
        }
        return new Svg(width, height, paths);
    }
}

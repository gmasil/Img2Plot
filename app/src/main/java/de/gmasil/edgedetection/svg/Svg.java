package de.gmasil.edgedetection.svg;

import de.gmasil.edgedetection.svg.element.SvgBezierCurve;
import de.gmasil.edgedetection.svg.element.SvgElement;
import de.gmasil.edgedetection.svg.element.SvgLine;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Svg {

    private static final List<String> SVG_HEADER = List.of("""
            <?xml version="1.0" standalone="yes"?>
            """.replace("\n", "").trim(), """
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d">
            """.replace("\n", "").trim(), """
            <rect width="%d" height="%d" x="0" y="0" fill="#ffffff" />
            """.replace("\n", "").trim());
    private static final List<String> SVG_FOOTER = List.of("""
            </svg>
            """.replace("\n", "").trim());

    private final int width;
    private final int height;
    private List<SvgElement> paths;

    public Svg(int width, int height, List<SvgElement> paths) {
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

    public List<SvgElement> getPaths() {
        return paths;
    }

    public List<SvgLine> getSvgLines() {
        return paths.stream().map(el -> {
            if (el instanceof SvgLine line) {
                return line;
            } else if (el instanceof SvgBezierCurve curve) {
                return curve.toLine(20);
            } else {
                throw new IllegalStateException("Invalid element type: " + el.getClass().getSimpleName());
            }
        }).toList();
    }

    public void filter(int lengthThreshold) {
        paths = paths.stream().filter(el -> {
            return el.getLength() >= lengthThreshold;
        }).toList();
    }

    public float getTravelLength() {
        float length = 0;
        for (int i = 0; i < paths.size() - 1; i++) {
            SvgElement p1 = paths.get(i);
            SvgElement p2 = paths.get(i + 1);
            length += p1.getLastPoint().distance(p2.getFirstPoint());
        }
        return length;
    }

    public void removeTravelPaths() {
        List<SvgElement> filteredPaths = new LinkedList<>();
        for (SvgElement element : paths) {
            if (!(element instanceof SvgLine line) || !line.isTravel()) {
                filteredPaths.add(element);
            }
        }
        paths = filteredPaths;
    }

    public void addTravelPaths() {
        removeTravelPaths();
        List<SvgLine> travelPaths = new LinkedList<>();
        for (int i = 0; i < paths.size() - 1; i++) {
            SvgElement p1 = paths.get(i);
            SvgElement p2 = paths.get(i + 1);
            travelPaths.add(new SvgLine(p1.getLastPoint(), p2.getFirstPoint(), 20.0f, "#ff0000").travel());
        }
        this.paths.addAll(travelPaths);
    }

    public void invertPaths() {
        paths.forEach(SvgElement::invert);
    }

    public void setStrokeWidth(float strokeWidth) {
        for (SvgElement path : paths) {
            path.setStrokeWidth(strokeWidth);
        }
    }

    public void translate(Point p) {
        paths.forEach(element -> element.translate(p));
    }

    public void scale(Point p) {
        paths.forEach(element -> element.scale(p));
    }

    public void orderPaths(boolean allowReversing) {
        List<PathChainElement> pathChain = getPaths().stream().map(PathChainElement::new).toList();
        paths = new LinkedList<>();
        while (pathChain.stream().filter(x -> x.getPreviousElement() == null || x.getNextElement() == null).count() > 2) {
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
                        if (element.getPreviousElement() == null && !currentElement.isInChainBackwards(element)) {
                            float distance = currentElement.getElement().getLastPoint().distance(element.getElement().getFirstPoint());
                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestPathElement = element;
                                reversed = false;
                            }
                        }
                        // check path in reverse order
                        if (allowReversing && element.getNextElement() == null) {
                            float distance = currentElement.getElement().getLastPoint().distance(element.getElement().getLastPoint());
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
            paths.add(currentElement.getElement());
            currentElement = currentElement.getNextElement();
        }
    }

    public void mergeClosePaths(float maxDistance) {
        removeTravelPaths();
        List<SvgLine> lines = getSvgLines();
        List<SvgLine> mergedLines = new LinkedList<>();
        mergedLines.add(lines.getFirst());
        for (int i = 1; i < lines.size(); i++) {
            SvgLine l1 = mergedLines.getLast();
            SvgLine l2 = lines.get(i);
            System.out.println("distance: "+l1.getLastPoint().distance(l2.getFirstPoint()));
            if (l1.getLastPoint().distance(l2.getFirstPoint()) < maxDistance) {
                l1.addLine(l2);
            } else {
                mergedLines.add(l2);
            }
        }
        paths = new LinkedList<>();
        paths.addAll(mergedLines);
    }

    @Override
    public String toString() {
        List<String> lines = new LinkedList<>(SVG_HEADER.stream().map(x -> x.formatted(width, height)).toList());
        lines.addAll(paths.stream().map(SvgElement::toSvgString).toList());
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
        int width = Integer.parseInt(svg.getAttributes().getNamedItem("width").getTextContent().replaceAll("\\..*", ""));
        int height = Integer.parseInt(svg.getAttributes().getNamedItem("height").getTextContent().replaceAll("\\..*", ""));
        List<Node> pathNodes = stream(svg.getChildNodes()).flatMap(Svg::filterPath).toList();
        List<String> contents = new LinkedList<>();
        for (Node pathNode : pathNodes) {
            String content = pathNode.getAttributes().getNamedItem("d").getTextContent();
            contents.add(separateTokens(content));
        }
        List<String> tokens = contents.stream().flatMap(s -> Stream.of(s.split(" +"))).map(s -> s.replace("z", "")).toList();
        List<SvgElement> elements = extractSvgElements(tokens);
        return new Svg(width, height, elements);
    }

    private static String separateTokens(String content) {
        String s = content;
        for (String c : List.of("M", "L", "C")) {
            s = s.replace(c.toUpperCase(), " " + c.toUpperCase() + " ");
            s = s.replace(c.toLowerCase(), " " + c.toLowerCase() + " ");
        }
        return s.trim();
    }

    private static List<SvgElement> extractSvgElements(List<String> tokens) {
        // TODO: handle z
        List<SvgElement> elements = new LinkedList<>();
        int i = 0;
        String lastCommand = "M";
        Point lastAbsolutePoint = null;
        while (i < tokens.size()) {
            String c = tokens.get(i);
            if (c.equals("M")) {
                lastCommand = c;
                i++;
                lastAbsolutePoint = handleAbsoluteMove(tokens, i);
                i += 2;
            } else if (c.equals("m")) {
                lastCommand = c;
                i++;
                lastAbsolutePoint = handleRelativeMove(lastAbsolutePoint, tokens, i, elements);
                i += 2;
            } else if (c.equals("L")) {
                lastCommand = c;
                i++;
                lastAbsolutePoint = handleAbsoluteLine(lastAbsolutePoint, tokens, i, elements);
                i += 2;
            } else if (c.equals("l")) {
                lastCommand = c;
                i++;
                lastAbsolutePoint = handleRelativeLine(lastAbsolutePoint, tokens, i, elements);
                i += 2;
            } else if (c.equals("C")) {
                lastCommand = c;
                i++;
                lastAbsolutePoint = handleAbsoluteBezier(lastAbsolutePoint, tokens, i, elements);
                i += 6;
            } else if (c.equals("c")) {
                lastCommand = c;
                i++;
                lastAbsolutePoint = handleRelativeBezier(lastAbsolutePoint, tokens, i, elements);
                i += 6;
            } else {
                if (lastCommand.equals("L")) {
                    lastAbsolutePoint = handleAbsoluteLine(lastAbsolutePoint, tokens, i, elements);
                    i += 2;
                } else if (lastCommand.equals("l")) {
                    lastAbsolutePoint = handleRelativeLine(lastAbsolutePoint, tokens, i, elements);
                    i += 2;
                } else if (lastCommand.equals("C")) {
                    lastAbsolutePoint = handleAbsoluteBezier(lastAbsolutePoint, tokens, i, elements);
                    i += 6;
                } else if (lastCommand.equals("c")) {
                    lastAbsolutePoint = handleRelativeBezier(lastAbsolutePoint, tokens, i, elements);
                    i += 6;
                } else {
                    throw new IllegalStateException("Token not recognized: " + c);
                }
            }
        }
        return elements;
    }

    private static Point handleAbsoluteMove(List<String> tokens, int i) {
        return new Point(Float.parseFloat(tokens.get(i)), Float.parseFloat(tokens.get(i + 1)));
    }

    private static Point handleRelativeMove(Point lastAbsolutePoint, List<String> tokens, int i, List<SvgElement> elements) {
        return lastAbsolutePoint.addToCopy(new Point(Float.parseFloat(tokens.get(i)), Float.parseFloat(tokens.get(i + 1))));
    }

    private static Point handleAbsoluteLine(Point lastAbsolutePoint, List<String> tokens, int i, List<SvgElement> elements) {
        Point end = new Point(Float.parseFloat(tokens.get(i)), Float.parseFloat(tokens.get(i + 1)));
        elements.add(new SvgLine(lastAbsolutePoint, end));
        return new Point(end);
    }

    private static Point handleRelativeLine(Point lastAbsolutePoint, List<String> tokens, int i, List<SvgElement> elements) {
        Point end = lastAbsolutePoint.addToCopy(new Point(Float.parseFloat(tokens.get(i)), Float.parseFloat(tokens.get(i + 1))));
        elements.add(new SvgLine(lastAbsolutePoint, end));
        return new Point(end);
    }

    private static Point handleAbsoluteBezier(Point lastAbsolutePoint, List<String> tokens, int i, List<SvgElement> elements) {
        Point c1 = new Point(Float.parseFloat(tokens.get(i)), Float.parseFloat(tokens.get(i + 1)));
        Point c2 = new Point(Float.parseFloat(tokens.get(i + 2)), Float.parseFloat(tokens.get(i + 3)));
        Point end = new Point(Float.parseFloat(tokens.get(i + 4)), Float.parseFloat(tokens.get(i + 5)));
        elements.add(new SvgBezierCurve(lastAbsolutePoint, c1, c2, end));
        return new Point(end);
    }

    private static Point handleRelativeBezier(Point lastAbsolutePoint, List<String> tokens, int i, List<SvgElement> elements) {
        Point c1 = lastAbsolutePoint.addToCopy(new Point(Float.parseFloat(tokens.get(i)), Float.parseFloat(tokens.get(i + 1))));
        Point c2 = lastAbsolutePoint.addToCopy(new Point(Float.parseFloat(tokens.get(i + 2)), Float.parseFloat(tokens.get(i + 3))));
        Point end = lastAbsolutePoint.addToCopy(new Point(Float.parseFloat(tokens.get(i + 4)), Float.parseFloat(tokens.get(i + 5))));
        elements.add(new SvgBezierCurve(lastAbsolutePoint, c1, c2, end));
        return new Point(end);
    }

    private static Stream<Node> filterPath(Node node) {
        if (node.getNodeName().equalsIgnoreCase("path")) {
            return Stream.of(node);
        } else if (node.getNodeName().equalsIgnoreCase("g")) {
            return stream(node.getChildNodes());
        }
        return Stream.of();
    }

    private static Stream<Node> stream(NodeList nodes) {
        List<Node> list = new LinkedList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i));
        }
        return list.stream();
    }
}

package de.gmasil.edgedetection.svg;

import java.util.Set;

public class PathChainElement {
    private final Path path;
    private PathChainElement previousElement;
    private PathChainElement nextElement;

    public PathChainElement(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public PathChainElement getPreviousElement() {
        return previousElement;
    }

    public void setPreviousElement(PathChainElement previousElement) {
        this.previousElement = previousElement;
    }

    public PathChainElement getNextElement() {
        return nextElement;
    }

    public void setNextElement(PathChainElement nextElement) {
        this.nextElement = nextElement;
    }

    public void reverseChainBackwards(Set<PathChainElement> visitedElements) {
        if(visitedElements.contains(this)) {
            throw new RuntimeException("There cannot be loops in the path chain");
        }
        visitedElements.add(this);
        path.invert();
        if(previousElement != null) {
            previousElement.reverseChainBackwards(visitedElements);
        }
        PathChainElement temp = previousElement;
        setPreviousElement(nextElement);
        setNextElement(temp);
    }

    public boolean isInChainBackwards(PathChainElement element) {
        if(element == this) {
            return true;
        }
        if(previousElement == null) {
            return false;
        }
        return previousElement.isInChainBackwards(element);
    }
}

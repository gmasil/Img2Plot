package de.gmasil.edgedetection.svg;

import de.gmasil.edgedetection.svg.element.SvgElement;

import java.util.Set;

public class PathChainElement {
    private final SvgElement element;
    private PathChainElement previousElement;
    private PathChainElement nextElement;

    public PathChainElement(SvgElement element) {
        this.element = element;
    }

    public SvgElement getElement() {
        return element;
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
        element.invert();
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

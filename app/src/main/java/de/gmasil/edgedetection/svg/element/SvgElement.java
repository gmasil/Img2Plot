package de.gmasil.edgedetection.svg.element;

import de.gmasil.edgedetection.svg.Point;

public abstract class SvgElement {

    protected float strokeWidth = 1;
    protected String strokeColor = "#000000";

    public abstract String toSvgString();

    public abstract void invert();

    public abstract Point getFirstPoint();

    public abstract Point getLastPoint();

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }
}

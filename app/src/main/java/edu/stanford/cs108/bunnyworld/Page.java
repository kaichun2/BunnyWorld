package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;

public class Page {

    // TODO: should we create instance variables for commands?

    ArrayList<Shape> shapes;
    String name;

    // TODO: do we need to keep track (boolean) of what the first page is?
    public Page(String pageName) {
        this.name = pageName;
    }

    public void addShape(Shape shape) {
        this.shapes.add(shape);
    }

    public void deleteShape(Shape shape) {
        this.shapes.remove(shape);
    }

    public ArrayList<Shape> getShapes() {
        return this.shapes;
    }

    // TODO: what happens on a mouse event? moving the shape?
    public void onMouseEvent(int x, int y, String event) {
        Shape shape = getShape(x, y);

        if (shape == null) return; // no shape, no action

        // otherwise, what to do for each event?
        // onClick event
        // onDrop (release the mouse)
        // onDrag (dragging the shape should change x and y - call shape.setX and shape.setY

    }

    private Shape getShape(int clickX, int clickY) {
        // iterate through the shapes list
        // get the x and y of each shape
        // check if those are within the x and y coord of what's been clicked
        // if so, return that shape, else null
        return null;
    }
}

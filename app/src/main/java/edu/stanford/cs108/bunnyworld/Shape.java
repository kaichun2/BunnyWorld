package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;

public class Shape {
    private String name;

    // what should be displayed for that type of shape
    private String text;
    private String image;

    private ArrayList<String> commands;

    // position coordinates on the app screen
    private int x;
    private int y;
    private int width;
    private int height;

    private boolean hidden;
    private boolean moveable;

    public Shape(String name, int x, int y, String script) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.commands = parseScript(script);
        hidden = false;
        moveable = true;
        text = "";
        image = "";
    }

    // Get/Set methods
    public String getName() { return name; }

    public int getX() {return x;}
    public int getY() {return y;}

    // Cannot set position if not moveable
    public void setX(int x) {
        if (!moveable) return;
        this.x = x;
    }

    public void setY(int y) {
        if (!moveable) return;
        this.y = y;
    }

    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public void setText(String text) {this.text = text;}
    public void setImage(String image) {this.image = image;}

    public void setScript(String script) {
        this.commands = parseScript(script);
    }

    public void setHidden(Boolean hidden) {this.hidden = hidden;}

    // Knows how to draw itself, grey rectangle if no picture
    public void draw() {
        if (hidden) return;
        if (text.equals("")) {
            // use x and y to scale image to size
            // drawImage - TODO: make function
        } else {
            // drawText - TODO: make function
        }
    }

    // TODO: How to parse script?
    private ArrayList<String> parseScript(String script) {
        return new ArrayList<String>();
    }
}

package edu.stanford.cs108.bunnyworld;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class Shape {

    private String name;
    private String imgName;
    private int x, y;
    private int width, height;
    private int pageID;
    private ShapeText text;
    private boolean isVisible;
    private boolean isMovable;
    private HashMap<String, String> commands;
    private static final String[] validActionsArr = new String[] {"goto", "play", "hide", "show"};
    private static final Set<String> validActions = new HashSet<>(Arrays.asList(validActionsArr));

    public Shape(String name, int x, int y, String script) {
        this.name = name;
        this.imgName = imgName;
        this.x = x;
        this.y = y;
        this.pageID = pageID;
        this.text = text;
        this.isVisible = isVisible;
        this.isMovable = isMovable;
        this.commands = parseScript(script);
        this.isVisible = false;
        this.isMovable = false;
        this.text = null;
    }

    /*
     * draw function. Given a canvas, will draw itself on that canvas.
     */
    public void draw(Canvas canvas) { // TODO: verify that this is how we want this to work
        if (!isMovable || !isVisible) return; // if not movable


        /* Also have to redraw ShapeText when you draw/redraw/move Shape. */
        text.draw(canvas);
    }

    // This will map commands. (already tested)
    // key: trigger (on click, on enter, on drop shape-name)
    // value: set of commands, goto, play, hide, show (that was provided for trigger)
    // onclick/onEnter/onDrop shape-name will read from map and parse the set of commands, executing
    //                          them in sequential order at runtime
    // note: "on drop carrot" is different from "on drop dog" (can have multiple on drop commands)
    // example script: on enter play evil-laugh; on drop carrot hide carrot play;
    private HashMap<String, String> parseScript(String script) {
        HashMap<String, String> commands = new HashMap<>();

        String[] sepCommands = script.split("; ");
        for (String command : sepCommands) {
            if (command.isEmpty()) break; // edge case
            String commandName = "";
            if (command.startsWith("on click")) {
                commandName = "on click";
                // script is everything after on click, + 1 (space)
            } else if (command.startsWith("on enter")) {
                commandName = "on enter";
            } else if (command.startsWith("on drop")) {
                // command name is "on drop <shape-name>", so first three components
                String[] comp = command.split(" ");
                commandName = comp[0] + " " + comp[1]+ " " + comp[2];
            }

            String commandScript = command.substring(commandName.length() + 1); // rest of string
            if (commandScript.endsWith(";")) { // possible edge case based on how we split
                commandScript = commandScript.substring(0, commandScript.length()-1);
            }
            commands.put(commandName, commandScript);
        }

        return commands;
    }


    /* Any on click functionality, if defined. */
    public void onClick() {
        String commandScript = commands.get("on click");
        if (commandScript != null) { // if on click script defined
            executeCommandScript(commandScript);
        }
    }

    /* Any on enter functionality, if defined. */
    public void onEnter() {
        String commandScript = commands.get("on enter");
        if (commandScript != null) { // if on enter script defined
            executeCommandScript(commandScript);

        }

    }

    /* Functionality for when a specific shape is dropped on this shape. */
    public void onDrop(Shape shape) {
        String commandScript = commands.get("on drop " + shape.name);
        if (commandScript != null) { // if on drop shape.name defined
            executeCommandScript(commandScript);

        }
    }

    /* Giving a command script, executes whatever it says.
     *
     * Possible script actions:
     * 1) goto <page-name> : switch to show the page of the given name
     * 2) play <sound-name> : play the sound of the given name
     * 3) hide <shape-name> : make the given shape invisible and
     *                       un-clickable. shape may or may not
     *                       be on the currently displayed page
     * 4) show <shape-name> : make the given shape visible and active.
     *                       shape may/may not be on currently displayed
     *                       page.
     *
     * Assumption: All script actions are legal regardless of which
     *             trigger called them.
     */
    private void executeCommandScript(String commandScript) {
        String[] comp = commandScript.split(" ");

        // -go through each component and execute based on current action
        // -robust enough approach so that we can have scripts of form:
        //  action <thing1> <thing2> ... <thingN>
        String currAction = "";
        for (String curr : comp) {
            if (validActions.contains(curr)) {
                currAction = curr;
            } else if (currAction.equals("goto")) {


            } else if (currAction.equals("play")) {


            } else if (currAction.equals("hide")) {

            } else if (currAction.equals("show")) {

            }
        }
    }

    public String getName() { return name; }

    public String getImgName() { return imgName; }

    public int getPageID() { return pageID; }

    public int getX() { return x; }

    public int getY() { return y; }

    public boolean isVisible() { return isVisible; }

    public boolean isMovable() { return isMovable; }

    public String getText() { return text.getText(); }

    public void setScript(String script) {
        this.commands = parseScript(script);
    }

    public void setName(String name) { this.name = name; }

    public void setImgName(String name) { imgName = name; }

    public void setPageID(int id) { pageID = id; }

    public void setShapeText(ShapeText shapetext) { text = shapetext; }

    public void setMovable(boolean val) { isMovable = val; }

    public void setVisible(boolean val) { isVisible = val; }

    public void setXLoc(int x) {
        if (!isMovable) return;
        this.x = x;
    }

    public void setYLoc(int y) {
        if (!isMovable) return;
        this.y = y;
    }

    /* ShapeText Inner class. */
    public class ShapeText { // TODO: verify draw works, extra: maybe ivar for text color?
        private int xLoc, yLoc;
        private int fontSize; // sp
        private String text;

        public ShapeText(int x, int y, int fontSize, String text) {
            xLoc = x;
            yLoc = y;
            this.fontSize = fontSize;
            this.text = text;
        }

        /* Optional constructor for ShapeText that uses default customizations. */
        public ShapeText(String text) { // TODO: fidget with numbers to find ideal default values
            xLoc = x;
            yLoc = y + height;
            fontSize = 12;
            this.text = text;
        }

        public void draw(Canvas canvas) {
            Paint paint = new Paint();
            canvas.drawPaint(paint);
            paint.setTextSize(fontSize);
            paint.setColor(Color.BLACK);
            canvas.drawText(text, x, y, paint);
        }

        /* Getters and setters. */

        public int getX() { return xLoc; }

        public int getY() { return yLoc; }

        public int getFontSize() { return fontSize; }

        public String getText() { return text; }

        public void setX(int newX) { this.xLoc = newX; }

        public void setY(int newY) { this.yLoc = newY; }

        public void setFontSize(int fontSize) { this.fontSize = fontSize; }

        public void setText(String text) { this.text = text; }

    }

}
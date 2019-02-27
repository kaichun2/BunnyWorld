package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;


/**
 * Whenever you need to supply a context object:
 * If you are in an activity, just pass in "this" keyword.
 * If you are in a View, pass in getContext().
 */
public class Shape {

    private String name;        /* Name of shape. Must be unique to all pages. */
    private String imgName;     /* Name of image (in drawable, no extension). */
    private float x, y;           /* x and y coordinate of Shape. */
    private float width, height;  /* width and height of Shape. */
    private int pageID;         /* The page this Shape is on. */
    private ShapeText textObj;     /* ShapeText object, requires xLoc, yLoc, fontSize, and text string. */
    private boolean isVisible;  /* Is this Shape visible? */
    private boolean isMovable;  /* Is this Shape movable? */

    /* The following are more for internal use, but the client must supply a script string. */
    private HashMap<String, String> commands;
    private static final String[] validActionsArr = new String[] {"goto", "play", "hide", "show"};
    private static final Set<String> validActions = new HashSet<>(Arrays.asList(validActionsArr));

    /* User can access all the shapes across all the pages using Shape.getAllShapes(). */
    private static final ArrayList<Shape> allShapes = new ArrayList<>();


    // TODO: how to efficiently load in bitmap drawables? need context but none here.
//    /* Load in BitmapDrawables. */
//    private BitmapDrawable carrotDrawable, carrot2Drawable, deathDrawable;
//    private BitmapDrawable duckDrawable, fireDrawable, mysticDrawable;
//
//    private void init() {
//        getResources().getDrawable();
//    }


    public Shape(String name, float x, float y, float width, float height, String script,
                 String imgName, int pageID, ShapeText textObj, boolean isVisible, boolean isMovable) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.commands = parseScript(script);
        this.imgName = imgName;
        this.pageID = pageID;
        this.textObj = textObj;
        this.isVisible = isVisible;
        this.isMovable = isMovable;
        if (!script.isEmpty()) commands = parseScript(script);
        allShapes.add(this);
    }

    public Shape() {
        /* Default values, all attributes should be set using setters. */
        this("", -1.0f, -1.0f, -1.0f, -1.0f, "", "",
                -1,null, false, false);
    }

    /*
     * draw function. Given a canvas, will draw itself on that canvas.
     */
    public void draw(Canvas canvas) { // TODO: verify that this is how we want this to work (isMovable?)
        // text obj takes precedence in basic design
        // in the basic functionality cases, the x and y of shapeText will match the associated shape
        if (textObj != null && !textObj.text.isEmpty()) {
            textObj.draw(canvas);
            return;
        }

        if (!isVisible) return; // if not visible, don't draw

        // choose bitmap image based on imgName
        if (imgName.isEmpty()) {
            // draw a grey rectangle

        } else {
            // draw  the bitmap image
//        canvas.drawBitmap(bitmap, left, top, paint);

        }


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


    /* Any on click functionality, if defined. shape1.onClick(this) */
    public void onClick(Context context, Canvas canvas) {
        String commandScript = commands.get("on click");
        if (commandScript != null) { // if on click script defined
            executeCommandScript(context, canvas, commandScript);
        }
    }

    /* Any on enter functionality, if defined. shape1.onEnter(this) */
    public void onEnter(Context context, Canvas canvas) {
        String commandScript = commands.get("on enter");
        if (commandScript != null) { // if on enter script defined
            executeCommandScript(context, canvas, commandScript);

        }

    }

    /*
    * Functionality for when a specific shape is dropped on this shape.
    * shape1.onDrop(this, shape2)
    */
    public void onDrop(Context context, Canvas canvas, Shape shape) {
        String commandScript = commands.get("on drop " + shape.name);
        if (commandScript != null) { // if on drop shape.name defined
            executeCommandScript(context, canvas, commandScript);

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
     *
     * Note: Context is required in order to access the mp3 files in res/raw.
     *       If you are in an activity, you can supply this with keyword
     *       "this" in most cases. Also need a canvas for "goto" option.
     */
    private void executeCommandScript(Context context, Canvas canvas, String commandScript) {
        String[] comp = commandScript.split(" ");

        // -go through each component and execute based on current action
        // -robust enough approach so that we can have scripts of form:
        //  action <thing1> <thing2> ... <thingN>
        String currAction = "";
        for (String curr : comp) {
            if (validActions.contains(curr)) {
                currAction = curr;
            } else if (currAction.equals("goto")) {
                ArrayList<Page> pages = Page.getPages();
                for (Page page : pages) {
                    if (page.getPageName().equals(curr)) {
                        Page.selectPage(context, canvas, page);
                        break;
                    }
                }
            } else if (currAction.equals("play")) { // play audio
                playAudio(context, curr);
            } else if (currAction.equals("hide")) {
                makeShapeVisible(curr, false); // hide shape curr
            } else if (currAction.equals("show")) {
                makeShapeVisible(curr, true); // show shape curr
            }
        }
    }

    /* Plays media file when given name of mp3 file in res/raw. */
    private void playAudio(Context context, String filename) {
        int audioID = context.getResources().getIdentifier(filename,
                "raw", context.getPackageName());
        MediaPlayer mp = MediaPlayer.create(context, audioID);
        mp.start();
    }

    /*
     * Makes the given shape either NOT VISIBLE or VISIBLE depending
     * on supplied boolean. Note that the given shape may not be on
     * the CURRENT page (as seen on specs). As a result, all shape names
     * should be unique. This will have to be error checked later
     * in extension TODO
     */
    private void makeShapeVisible(String shapeName, boolean visible) {
        // go through static list of all shapes until we find one that matches given shape name
        // then (if found) update the shape's visible property to be false
        for (Shape shape : allShapes) {
            if (shape.name.equals(shapeName)) {
                shape.setVisible(visible);
            }
        }

    }

    public String getName() { return name; }

    public String getImgName() { return imgName; }

    public int getPageID() { return pageID; }

    public float getX() { return x; }

    public float getY() { return y; }

    public float getWidth() { return width; }

    public float getHeight() { return height; }

    public boolean isVisible() { return isVisible; }

    public boolean isMovable() { return isMovable; }

    public String getText() { return textObj.getText(); }

    public static ArrayList<Shape> getAllShapes() { return allShapes; }

    public void setScript(String script) {
        this.commands = parseScript(script);
    }

    public void setName(String name) { this.name = name; }

    public void setImgName(String name) { imgName = name; }

    public void setPageID(int id) { pageID = id; }

    public void setShapeText(ShapeText shapetext) { textObj = shapetext; }

    public void setMovable(boolean val) { isMovable = val; }

    public void setVisible(boolean val) { isVisible = val; }

    public void setX(float x) {
        if (!isMovable) return;
        this.x = x;
    }

    public void setY(float y) {
        if (!isMovable) return;
        this.y = y;
    }

    /* ShapeText Inner class.
     * If this design feels over the top, it's because this will be useful later
     * if we decide to (as an extension) allow the user to do more interesting things
     * with the text associated with a shape. For example, we could have the shape text
     * coordinates be locked to directly above the image, similar to how RPG games lock
     * a name text over a character.
     */
    public class ShapeText { // TODO: verify draw works, extra: maybe ivar for text color?
        private float xLoc, yLoc;
        private int fontSize; // sp
        private String text;

        public ShapeText(float x, float y, int fontSize, String text) {
            xLoc = x;
            yLoc = y;
            this.fontSize = fontSize;
            this.text = text;
        }

        /* Optional constructor for ShapeText that uses default customizations. */
        /* Use this for basic functionality design. textObj.setFontSize(num) as well. */
        public ShapeText(String text) {
            xLoc = x;
            yLoc = y;
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

        public float getX() { return xLoc; }

        public float getY() { return yLoc; }

        public int getFontSize() { return fontSize; }

        public String getText() { return text; }

        public void setX(float newX) { this.xLoc = newX; }

        public void setY(float newY) { this.yLoc = newY; }

        public void setFontSize(int fontSize) { this.fontSize = fontSize; }

        public void setText(String text) { this.text = text; }

    }

}
package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.simple.JSONObject;


/**
 * Whenever you need to supply a context object:
 * If you are in an activity, just pass in "this" keyword.
 * If you are in a View, pass in getContext().
 */
public class Shape {

    private String name;         /* Name of shape. Must be unique to all pages. */
    private String imgName;      /* Name of image (in drawable, no extension). */
    private float x, y;          /* x and y coordinate of Shape. */
    private float width, height; /* width and height of Shape. */
    private int pageID;          /* The page this Shape is on. */
    private ShapeText textObj;   /* ShapeText object, requires xLoc, yLoc, fontSize, and text string. */
    private boolean isVisible;   /* Is this Shape visible? */
    private boolean isMovable;   /* Is this Shape movable? */
    private String script;       /* Script given to shape. */

    /* Grey paint object (for when there's no image). */
    private Paint grayPaint = new Paint();
    private Paint textPaint = new Paint();

    /* The following are more for internal use, but the client must supply a script string. */
    private HashMap<String, String> commands;
    private static final String[] validActionsArr = new String[] {"goto", "play", "hide", "show"};
    private static final Set<String> validActions = new HashSet<>(Arrays.asList(validActionsArr));

    /* User can access all the shapes across all the pages using Shape.getAllShapes(). */
    private static final ArrayList<Shape> allShapes = new ArrayList<>();

    /* Load in BitmapDrawables. */
    private static HashMap<String, BitmapDrawable> drawables = new HashMap<>();
    private static BitmapDrawable carrotDrawable, carrot2Drawable, deathDrawable;
    private static BitmapDrawable duckDrawable, fireDrawable, mysticDrawable;
    private static BitmapDrawable texticonDrawable, greyboxDrawable;

    /* Array with the names of valid sounds. */
    private static String[] sounds = new String[] {"evillaugh", "carrotcarrotcarrot", "fire",
                                                   "hooray", "munch", "munching", "woof"};


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
        grayPaint.setColor(Color.GRAY);
    }

    public Shape() {
        /* Default values, all attributes should be set using setters. */
        this("", -1.0f, -1.0f, -1.0f, -1.0f, "", "",
                -1,null, true, false);
    }

    // init drawables and map the imgNames to the respective drawables
    public static void initDrawables(Context context) {
        // load in drawables
        carrotDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.carrot);
        carrot2Drawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.carrot2);
        deathDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.death);
        duckDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.duck);
        fireDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.fire);
        mysticDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.mystic);
        texticonDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.texticon);
        greyboxDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.greybox);

        // initialize drawables hashmap so we can access them by imgName
        drawables.put("carrot", carrotDrawable);
        drawables.put("carrot2", carrot2Drawable);
        drawables.put("death", deathDrawable);
        drawables.put("duck", duckDrawable);
        drawables.put("fire", fireDrawable);
        drawables.put("mystic", mysticDrawable);
        drawables.put("texticon", texticonDrawable);
        drawables.put("greybox", greyboxDrawable);
    }

    public static HashMap<String, BitmapDrawable> getDrawables(Context context) {
        initDrawables(context);
        return drawables;
    }

    /*
     * draw function. Given a canvas, will draw itself on that canvas.
     */
    public void draw(Canvas canvas) {
        if (!isVisible) return; // if not visible, don't draw


        // text obj takes precedence in basic design
        // in the basic functionality cases, the x and y of shapeText will match the associated shape
        if (textObj != null && !textObj.text.isEmpty()) {
            textObj.draw(canvas);
            return;
        }

        // choose bitmap image based on imgName
        if (imgName.isEmpty()) {
            // draw a grey rectangle
            canvas.drawRect(x, y, x + width, y + height, grayPaint);

        } else {
            // draw  the bitmap image
            Bitmap image = drawables.get(imgName).getBitmap();

            // scale image by width and height
            Bitmap scaled = Bitmap.createScaledBitmap(image, (int) width, (int) height, true);

            // draw scaled image
            canvas.drawBitmap(scaled, x, y, null); // may need paint later for extensions
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
    public Page onClick(Context context) {
        Page newPage = null;
        String commandScript = commands.get("on click");
        if (commandScript != null) { // if on click script defined
            newPage = executeCommandScript(context, commandScript);
        }
        return newPage;
    }

    /* Any on enter functionality, if defined. shape1.onEnter(this) */
    public Page onEnter(Context context) {
        Page newPage = null;
        String commandScript = commands.get("on enter");
        if (commandScript != null) { // if on enter script defined
            newPage = executeCommandScript(context, commandScript);
        }
        return newPage;
    }

    /*
    * Functionality for when a specific shape is dropped on this shape.
    * shape1.onDrop(this, shape2)
    */
    public Page onDrop(Context context, Shape shape) {
        Page newPage = null;
        String commandScript = commands.get("on drop " + shape.name);
        if (commandScript != null) { // if on drop shape.name defined
            newPage = executeCommandScript(context, commandScript);
        }
        return newPage;
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
    private Page executeCommandScript(Context context, String commandScript) {
        String[] comp = commandScript.split(" ");
        Page newPage = null;

        // -go through each component and execute based on current action
        // -robust enough approach so that we can have scripts of form:
        //  action <thing1> <thing2> ... <thingN>
        String currAction = "";
        for (String curr : comp) {
            if (validActions.contains(curr)) {
                currAction = curr;
            } else if (currAction.equals("goto")) {
                Log.d("wtf", curr);
                ArrayList<Page> pages = Page.getPages();
                for (Page page : pages) {
                    if (page.getPageName().equals(curr)) {
                        newPage = page;
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
        return newPage;
    }

    /* Plays media file when given name of mp3 file in res/raw. */
    public static void playAudio(Context context, String filename) {
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
                break;
            }
        }

    }

    // returns the json string for this shape
    public String getShapeJSON() {
        JSONObject jsonObj = new JSONObject();

        /* Create a hashmap representing a shape. */
        HashMap<String, String> shapeJSON = new HashMap<>();
        shapeJSON.put("name", name);
        shapeJSON.put("imgName", imgName);
        shapeJSON.put("isVisible", isVisible ? "true" : "false");
        shapeJSON.put("isMovable", isMovable ? "true" : "false");
        shapeJSON.put("x", String.valueOf(x));
        shapeJSON.put("y", String.valueOf(y));
        shapeJSON.put("width", String.valueOf(width));
        shapeJSON.put("height", String.valueOf(height));
        shapeJSON.put("textObj", textObj == null ? "null" : textObj.getShapetextJSON());
        shapeJSON.put("script", script == null ? "" : script);

        /* Have JSONObject parse that dictionary into a JSON format. */
        jsonObj.putAll(shapeJSON);

        return Page.prettyPrintJSON(jsonObj);
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

    public String getScript() { return script; }

    public HashMap<String, String> getCommands() { return commands; }

    public ShapeText getShapeText() {
        return textObj;
    }

    public static ArrayList<Shape> getAllShapes() { return allShapes; }

    public static String[] getSounds() { return sounds; }

    // sets script ivar but also parses script
    public void setScript(String script) {
        commands.clear();
        this.script = script;

        this.commands = parseScript(script);
    }

    public void setName(String name) { this.name = name; }

    public void setImgName(String name) { imgName = name; }

    public void setPageID(int id) { pageID = id; }

    // mostly for setting to null
    public void setShapeText(ShapeText shapetext) { textObj = shapetext; }

    // alternate set shape text (use this one when doing extensions, more robust)
    public void setShapeText(float x, float y, int fontSize, String text) {
        textObj = new ShapeText(x, y, fontSize, text);
    }

    // use this for the basic functionality, will default x, yto shape ivar's values and fontSize=12
    public void setShapeText(String text) {
        textObj = new ShapeText(text);
    }

    public void setMovable(boolean val) { isMovable = val; }

    public void setVisible(boolean val) { isVisible = val; }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) { this.width = width; }

    public void setHeight(float height) { this.height = height; }

    @Override
    public String toString() {
        StringBuilder shapeContents = new StringBuilder();
        shapeContents.append("Shape name: " + name + "\n");
        shapeContents.append("imgName: " + imgName + "\n");
        shapeContents.append("pageID: " + pageID + "\n");
        shapeContents.append("isVisible: " + isVisible + "\n");
        shapeContents.append("isMovable: " + isMovable + "\n");
        shapeContents.append("x: " + x + "\n");
        shapeContents.append("y: " + y + "\n");
        shapeContents.append("width: " + width + "\n");
        shapeContents.append("height: " + height + "\n");
        shapeContents.append("script commands: " + commands.toString() + "\n");
        if (textObj != null) {
            shapeContents.append("textObj: " + textObj.toString() + "\n");
        } else {
            shapeContents.append("textObj is null. \n");
        }

        return shapeContents.toString();
    }

    /* ShapeText Inner class.
     * If this design feels over the top, it's because this will be useful later
     * if we decide to (as an extension) allow the user to do more interesting things
     * with the text associated with a shape. For example, we could have the shape text
     * coordinates be locked to directly above the image, similar to how RPG games lock
     * a name text over a character.
     */
    public class ShapeText {
        private float xLoc, yLoc;
        private int fontSize; // sp
        private String text;

        private boolean bold;
        private boolean italic;
        private boolean underline;
        private int color;



        public ShapeText(float x, float y, int fontSize, String text) {
            xLoc = x;
            yLoc = y;
            this.fontSize = fontSize;
            this.text = text;

            bold = false;
            italic = false;
            underline = false;
            color = Color.BLACK; //  TODO use this in draw (i think black was default before)
            Log.d("dog12", String.valueOf(Color.BLACK));

        }

        /* Optional constructor for ShapeText that uses default customizations. */
        /* Use this for basic functionality design. textObj.setFontSize(num) as well. */
        public ShapeText(String text) { // xLoc = x, yLoc = y, always now
            xLoc = x;
            yLoc = y;
            fontSize = 36;
            this.text = text;

            bold = false;
            italic = false;
            underline = false;
            color = Color.BLACK; //  TODO use this in draw
        }

        // text also only draws itself if outer object is visible
        public void draw(Canvas canvas) {
            if (isVisible) {
                textPaint.setColor(Color.WHITE);
                textPaint.setStyle(Paint.Style.FILL);
                textPaint.setColor(color);
                textPaint.setTextSize(fontSize);
                if (bold && italic) textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                else if (bold) textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                else if (italic) textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                if (underline) textPaint.setUnderlineText(true);

                canvas.drawText(text, x, y + fontSize, textPaint);
            }
        }

        @Override
        public String toString() {
            StringBuilder textContents = new StringBuilder();
            textContents.append("(xLoc: " + xLoc + ", yLoc: " + yLoc + ") with ");
            textContents.append(fontSize + " font and text: " + text + "\n");
            return textContents.toString();
        }

        /* Getters and setters. */

        public boolean getBold() { return bold; }

        public boolean getItalic() { return italic; }

        public boolean getUnderline() { return underline; }

        public int getTColor() { return color; }

        public float getX() { return xLoc; }

        public float getY() { return yLoc; }

        public int getFontSize() { return fontSize; }

        public String getText() { return text; }

        // JSON format for shapetext
        public String getShapetextJSON() {
            JSONObject jsonObj = new JSONObject();

            /* Create a hashmap representing a shapetext. */
            HashMap<String, String> shapeTextJSON = new HashMap<>();
            shapeTextJSON.put("xLoc", String.valueOf(xLoc));
            shapeTextJSON.put("yLoc", String.valueOf(yLoc));
            shapeTextJSON.put("text", text);
            shapeTextJSON.put("fontSize", String.valueOf(fontSize));
            shapeTextJSON.put("bold", bold ? "true" : "false");
            shapeTextJSON.put("italic", italic ? "true" : "false");
            shapeTextJSON.put("underline", underline ? "true" : "false");
            shapeTextJSON.put("color", String.valueOf(color));


            /* Have JSONObject parse that dictionary into a JSON format. */
            jsonObj.putAll(shapeTextJSON);

            return Page.prettyPrintJSON(jsonObj);
        }

        public void setBold(boolean b) { bold = b; }

        public void setItalic(boolean i) { italic = i; }

        public void setUnderline(boolean u) { underline = u; }

        public void setTColor(int col) {
            color = col;
        }

        public void setX(float newX) { this.xLoc = newX; }

        public void setY(float newY) { this.yLoc = newY; }

        public void setFontSize(int fontSize) { this.fontSize = fontSize; }

        public void setText(String text) { this.text = text; }

    }

}
package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * First: Before using the page class, you must load the data in from Database.
 *  Use Page.loadDatabase(this, "name_of_game")         (from activity) OR
 *      Page.loadDatabase(getContext(), "name_of_game") (from View)
 *
 * Then: Now you can use  Page.getPages() to get all of the
 * pages (in order) from the database for the  game loaded earlier. Each page will
 * contain a pageName ivar (use getPageName()) and an ArrayList of shapes ivar.
 *
 * The first argument is the "context." If you are calling from an activity,
 * then it should be fine to just pass in the keyword "this." If you're in
 * a view, then use getContext().
 */

public class Page {
    private String pageName;          /* Unique page names only. */
    private int pageID;               /* Sequential Page ID. (May be useful later for extensions. */
    private ArrayList<Shape> shapes;  /* List of all shapes associated with this page. */

    /* User can access all the pages across all the pages using Page.getAllPages(). */
    /* Only accessible after the user loads the database with Page.loadDatabase(this, "gamename").*/
    /* In sequential order of Page IDs due to how database is structured. */
    private static final ArrayList<Page> allPages = new ArrayList<>();

    public Page(String pageName, int pageID, ArrayList<Shape> shapes) {
        this.pageName = pageName;
        this.pageID = pageID;
        this.shapes = shapes;
        allPages.add(this);
    }

    /*
     * Selecting the current page will call all of its shapes' on Enter functions
     * and then call onDraw on each shape. Each shape should have a visible/invisible
     * property already initially defined.
     *
     * Requires context, so pass "this" or "getContext()" (activity vs view)
     *
     * Requires canvas since we will need to call onDraws
     */
    public static void selectPage(Context context, Canvas canvas, Page page) {
        page.onEnter(context, canvas); // shape on enter functions
        page.drawAllShapes(canvas);
    } // note: up to client implementing draw area view to call invalidate

    @SuppressWarnings("unchecked")
    public static void loadDatabase(Context context, String nameOfGame) {
        Log.d("dog", "launching parse for pages");
        JSONParser parser = new JSONParser();
        try {
            String json = getJSONDataFromFile(context, nameOfGame);
            JSONObject data = (JSONObject) parser.parse(json);
            JSONArray pages = (JSONArray) data.get("pages");
            Iterator<JSONObject> it = pages.iterator();
            while (it.hasNext()) {
                JSONObject page = it.next();
                String page_name = (String) page.get("page_name");
                int page_id = (int) (long) page.get("page_id"); // this won't overflow.
                ArrayList<Shape> shapes = parseShapes((JSONArray) page.get("shapes"));
                // create new page, added automatically to static arr of pages
                new Page(page_name, page_id, shapes);
            }

        } catch(Exception ex) {
            System.out.println("Error parsing database file for " + nameOfGame + ".");
            Log.d("dog", "file reading failed");
            ex.printStackTrace();
        }
    }

    private static String getJSONDataFromFile(Context context, String filename) {
        StringBuilder ret = new StringBuilder();
        try {
            InputStream in = context.getResources().openRawResource(
                    context.getResources().getIdentifier(filename,
                            "raw", context.getPackageName()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line);
            }
        } catch (IOException ex) {
            Log.d("dog", "Error getting json data from file " + filename + " in getJSONDataFromFile.");
        }
        return ret.toString();
    }

    private static ArrayList<Shape> parseShapes(JSONArray shapes) {
        ArrayList<Shape> pageShapes = new ArrayList<>();
        /* TODO: design shape class and parse shapes!! */

        return pageShapes;
    }

    @Override
    public String toString() {
        return "Page " + getPageID() + ": " + getPageName();
    }

    public static void test(Context context) {
        Page.loadDatabase(context,"sampledatafile");
        ArrayList<Page> pages = Page.getPages();
        for (Page page : pages) {
            Log.d("dog", page.toString());
        }
    }

    /* Returns list of all pages. loadDatabase() first. */
    public static ArrayList<Page> getPages() {
        return allPages;
    }

    public String getPageName() {
        return pageName;
    }

    public int getPageID() {
        return pageID;
    }

    // Execute onEnter triggers for every shape on this page.
    // page.onEnter(this, canvas)
    public void onEnter(Context context, Canvas canvas) {
        for (Shape shape : shapes) {
            shape.onEnter(context, canvas);
        }
    }

    // call all shapes on draw functions
    public void drawAllShapes(Canvas canvas) {
        for (Shape shape : shapes) {
            shape.draw(canvas);
        }
    }

    /* Use this on a page to get the associated shapes, which you can then have draw themselves. */
    public ArrayList<Shape> getShapes() {
        return shapes;
    }

    /* TODO: don't think add/delete shape necessary if we can just make the shapes invisible/visible
     * will prob start each page off with all the shapes it'll use already in shapes, and then
     * update the canvas not by adding/removing shapes but by setting an invisible/visible property.*/
//    public void addShape(Shape shape) {
//        this.shapes.add(shape);
//    }
//
//    public void deleteShape(Shape shape) {
//        this.shapes.remove(shape);
//    }


    /*
    TODO: what happens when user clicks on canvas (which represents page) during game?
    1) we pass the x and y coordinates + event to following function
    2) we see if a shape is at that location (do nothing if not),
        if there's a shape:
        2a) if event is onClick, call that shape's onClick
        2b) if event is onDrop (is that a thing?) compare the shapes at that location (what if there's more than 2? top 2?)
        2c) onEnter is handled separately. (see onEnter function in Page)

        (will prob call a handleMouseEvent function for the respective shape since this is more relevant there)

        Client must supply context (this or getContext()), the x and y location of click, and a string
        called event that details the action. Client is responsible for making String event to be either
        1) "on click" or "on drop" --> this function won't do anything otherwise.

        If "on drop" is passed, client must be sure that there are at least two shapes at the specified
        x and y coordinates, and the two shapes on top are the ones being dealt with.

        TODO: on drag functionality?
     */
    public void onMousePageEvent(Context context, Canvas canvas, int x, int y, String event) {
        Shape topShape = getShape(x, y, 1);
        if (topShape == null) return; // no shape, no action

        if (event.equals("on click")) { // clicked top shape
            topShape.onClick(context, canvas);
        } else if (event.equals("on drop")) { // dropped top shape onto secondTopShape
            Shape secondTopShape = getShape(x, y, 2);
            secondTopShape.onDrop(context, canvas, topShape); // execute "on drop topShape" script for secondTopShape
        }


        // otherwise, what to do for each event?
        // onClick event
        // onDrop (release the mouse)
        // onDrag (dragging the shape should change x and y - call shape.setX and shape.setY

    }

    /* Gets the shape at (clickX, clickY). If lim=1, topmost. If lim=2, one below topmost, etc. */
    private Shape getShape(int clickX, int clickY, int lim) { // TODO: (likely reuse code from prev assignment)
        // iterate through the shapes list
        // get the x and y of each shape
        // check if those are within the x and y coord of what's been clicked
        // if so, return that shape, else null

        Shape ret = null;
        int seen = 0;
        for (int i = shapes.size() -1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if ((shape.getX() <= clickX && clickX <= shape.getX() + shape.getWidth()) && // x is contained in shape
                    (shape.getY() <= clickY && clickY <= shape.getY() + shape.getHeight())) { // y is also contained in shape
                ret = shape;
                seen++;
                if (seen == lim) break;
            }
        }

        return ret;

    }
}

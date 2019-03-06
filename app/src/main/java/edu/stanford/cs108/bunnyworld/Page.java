package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static android.content.Context.MODE_PRIVATE;

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

    /* Possessions will be loaded into a static array of shapes after loadDatabase(). */
    private static ArrayList<Shape> possessions = new ArrayList<>();

    /* Feel free to use this when testing your code. */
    public static final String SAMPLE_DATA_FILE = "sampledatafile";

    /* Bunny world file. */
    public static final String BUNNY_WORLD_FILE = "bunnyworld";

    /* Text file where we will store the names of the games we have files for. */
    public static final String GAME_NAMES_FILE = "gamesnamesfile";

    public Page(String pageName, int pageID, ArrayList<Shape> shapes) {
        this.pageName = pageName;
        this.pageID = pageID;
        this.shapes = shapes;
        allPages.add(this);
    }

    // a default constructor -> don't add to allPages
    // I will be using this as a "special" page/marker that lets
    // me know when an invalid onDrop has been detected (see onMousePageEvent)
    public Page() {
        this.pageID = -1;
        pageName = "";
        shapes = null;
    }

    @Override
    public String toString() {
        StringBuilder pageContents = new StringBuilder();
        pageContents.append("Page " + getPageID() + ": " + getPageName() + "\n");
        for (Shape shape : shapes) {
            pageContents.append(shape.toString() + "\n");
        }
        return pageContents.toString();
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

    public static ArrayList<Shape> getPossessions() { return possessions; }

    public void setPageName(String name) {
        this.pageName = name;
    }

    public static void setPossessions(ArrayList<Shape> poss) { possessions = poss; }

    /* Use this on a page to get the associated shapes, which you can then have draw themselves. */
    public ArrayList<Shape> getShapes() {
        return shapes;
    }

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

     */
    public Page onMousePageEvent(Context context, float x, float y, String event) {
        Page newPage = null;
        Shape topShape = getShape(x, y, 1);
        if (topShape == null) return null; // no shape, no action

        if (event.equals("on click")) { // clicked top shape
            newPage = topShape.onClick(context);
        } else if (event.equals("on drop")) { // dropped top shape onto secondTopShape
            Shape secondTopShape = getShape(x, y, 2);
            if (secondTopShape == null) return null; // don't do anything if there wasn't a second top shape!!!

            // if there isn't an "on drop topShape" for secondTopShape, then we'll return
            // a new, empty page with pageID = -1 to denote the "snap back to place" case in gameeditor
            if (!secondTopShape.getCommands().containsKey("on drop " + topShape.getName())) {
                return new Page();
            }

            newPage = secondTopShape.onDrop(context, topShape); // execute "on drop topShape" script for secondTopShape
        }
        return newPage;
    }

    /* Gets the shape at (clickX, clickY). If lim=1, topmost. If lim=2, one below topmost, etc. */
    // note that shapes near the end were added last so they're on top.
    public Shape getShape(float clickX, float clickY, int lim) {
        Shape ret = null;
        int seen = 0;
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.isVisible()) {
                if ((shape.getX() <= clickX && clickX <= shape.getX() + shape.getWidth()) && // x is contained in shape
                        (shape.getY() <= clickY && clickY <= shape.getY() + shape.getHeight())) { // y is also contained in shape
                    ret = shape;
                    seen++;
                    if (seen == lim) break;
                }
            }
        }
        if (seen != lim) return null; // exited for loop without finding expected shape (lim)
        return ret;

    }

    /* All the database parsing and page/shape/possessions instantiation/initialization. */

    @SuppressWarnings("unchecked")
    public static void loadDatabase(Context context, String nameOfGame) {
        Log.d("dog", "launching parse for pages");

        // what we load from database should be the only thing in allShapes /
        // possessions / allPages. so reset it before loading
        allPages.clear();
        possessions.clear();
        Shape.getAllShapes().clear();

        String json = getDataFromFile(context, nameOfGame + ".json");
        loadDatabaseFromJSONString(context, json);
    }

    public static void loadDatabaseFromJSONString(Context context, String json) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject data = (JSONObject) parser.parse(json);
            JSONArray pages = (JSONArray) data.get("pages");
            Iterator<JSONObject> it = pages.iterator();
            while (it.hasNext()) {
                JSONObject page = it.next();
                String page_name = (String) page.get("page_name");
                int page_id = Integer.parseInt((String) page.get("page_id")); // this won't overflow.
                ArrayList<Shape> shapes = parseShapes((JSONArray) page.get("shapes"));
                // create new page, added automatically to static arr of pages
                new Page(page_name, page_id, shapes);
            }

            /* Load possessions inventory into array of shapes. */
            JSONArray possArr = (JSONArray) data.get("possessions");
            loadPossessionsArr(possArr);

            /* Finally, initialize the drawables in the shapes array. */
            Shape.initDrawables(context); // need context in order to read res/raw

        } catch(Exception ex) {
            Log.d("dog", "file reading failed");
            ex.printStackTrace();
        }
    }

    private static void loadPossessionsArr(JSONArray possArr) {
        possessions = parseShapes(possArr);
    }

    // file is in internal storage
    private static String getDataFromFile(Context context, String filename) {
        StringBuilder ret = new StringBuilder();
        try {

            if (!fileExists(context, filename)) { // create new file
                new File(context.getFilesDir(), filename);
            }

            FileInputStream in = context.openFileInput(filename);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line);
            }

            in.close();

        } catch (IOException ex) {
            Log.d("dog", "Error getting json data from file " + filename + " in getDataFromFile.");
        }
        return ret.toString();
    }

    private static boolean fileExists(Context context, String filename) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + filename;
        File test = new File(path);
        return test.exists();
    }

    /* Parse the shapes in the given array of shapes. */
    private static ArrayList<Shape> parseShapes(JSONArray shapes) {
        ArrayList<Shape> pageShapes = new ArrayList<>();
        Iterator<JSONObject> it = shapes.iterator();
        while (it.hasNext()) {
            // choose shape
            JSONObject shapeObj = it.next();

            // collect shape attributes
            String name = (String) shapeObj.get("name");
            String imgName = (String) shapeObj.get("imgName");
            int pageID = -1; // pageID is null if shape is in possessions!
            if (shapeObj.get("pageID") != null) {
                pageID = Integer.parseInt((String) shapeObj.get("pageID"));
            }
            boolean isVisible = (boolean) shapeObj.get("isVisible");
            boolean isMovable = (boolean) shapeObj.get("isMovable");
            float x = Float.parseFloat((String) shapeObj.get("x"));
            float y = Float.parseFloat((String) shapeObj.get("y"));
            float width = Float.parseFloat((String) shapeObj.get("width"));
            float height = Float.parseFloat((String) shapeObj.get("height"));
            String script = (String) shapeObj.get("script");

            // create shape
            Shape shape = new Shape(); // note: has shapeText as null by default
            shape.setName(name);
            shape.setImgName(imgName);
            shape.setPageID(pageID);
            shape.setVisible(isVisible);
            shape.setMovable(isMovable);
            shape.setX(x);
            shape.setY(y);
            shape.setWidth(width);
            shape.setHeight(height);
            shape.setScript(script);

            // do textObj portion separately
            JSONObject textObj = (JSONObject) shapeObj.get("textObj");
            if (textObj != null) {
                float xLoc = Float.parseFloat((String) textObj.get("xLoc"));
                float yLoc = Float.parseFloat((String) textObj.get("yLoc"));
                int fontSize = Integer.parseInt((String) textObj.get("fontSize"));
                String text = (String) textObj.get("text");
                shape.setShapeText(xLoc, yLoc, fontSize, text);
            }
            pageShapes.add(shape);
        }

        return pageShapes;
    }

    /* Get the Page as a JSON string. */
    public String getPageJSON() {
        JSONObject jsonObj = new JSONObject();

        /* Create a hashmap representing a page (shape and shapetext also need this) */
        HashMap<String, String> pageJSON = new HashMap<>();
        pageJSON.put("page_id", Integer.toString(pageID));
        pageJSON.put("page_name", pageName);
        pageJSON.put("num_shapes", String.valueOf(shapes.size()));
        pageJSON.put("shapes", getShapesJSON());

        /* Have JSONObject parse that dictionary into a JSON format. */
        jsonObj.putAll(pageJSON);

        return prettyPrintJSON(jsonObj);
    }

    /* Parse the shapes associated with this Page into a JSON Array format (but as a string). */
    public String getShapesJSON() {
        String[] jsonArr = new String[shapes.size()];

        for (int i = 0; i < shapes.size(); i++) {
            jsonArr[i] = shapes.get(i).getShapeJSON();
        }

        return prettyPrintJSON(jsonArr);
    }

    /* Returns a json string for all the pages in allPages. */
    public static String getAllPagesJSON() {
        String[] jsonArr = new String[allPages.size()];

        for (int i = 0; i < allPages.size(); i++) {
            jsonArr[i] = allPages.get(i).getPageJSON();
        }

        Log.d("2cats", prettyPrintJSON(jsonArr));
        return prettyPrintJSON(jsonArr);

    }

    /* Load possessions array into json formatted string. */
    public static String getPossessionsJSON() {
        String[] possJSON = new String[possessions.size()];

        for (int i = 0; i < possessions.size(); i++) {
            possJSON[i] = possessions.get(i).getShapeJSON();
        }

        return prettyPrintJSON(possJSON);
    }

    /* Load the current state of allPages and possessions inventory into a file. */
    /* The file it's loaded into is called game.json where game is the string passed in. */
    public static void loadIntoDatabaseFile(Context context, String game) { // game is name of game (no spaces), alphanumeric
        JSONObject gameJSON = new JSONObject();

        /* Create a hashmap representing a game.  */
        HashMap<String, String> gameObj = new HashMap<>();
        gameObj.put("game_name", game);
        gameObj.put("num_pages", String.valueOf(allPages.size()));
        gameObj.put("pages", getAllPagesJSON());
        gameObj.put("num_possessions", String.valueOf(possessions.size()));
        gameObj.put("possessions", getPossessionsJSON());


        /* Have JSONObject parse that dictionary into a JSON format. */
        gameJSON.putAll(gameObj);


        String jsonString = prettyPrintJSON(gameObj);

        /* Write to database file in internal storage. */
        loadJSONStringIntoDatabase(context, game, jsonString);
    }

    private static void loadJSONStringIntoDatabase(Context context, String game, String jsonString) {
        try {
            String filename = game + ".json";
            if (!fileExists(context, filename)) { // create file
                new File(context.getFilesDir(), filename);
            }

            FileOutputStream out = context.openFileOutput(filename, MODE_PRIVATE);
            out.write(jsonString.getBytes());
            out.close();

            updateListOfGames(context, game);

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("error", "failed to write to internal storage database");
        }
    }

    /*
    We will be keeping a .txt file on the internal storage that contains
    the names of the games, separated by new line characters.
     */
    private static void updateListOfGames(Context context, String game) {
        String filename = GAME_NAMES_FILE + ".txt";
        try {
            if (!fileExists(context, filename)) {
                new File(context.getFilesDir(), filename);
            }

            String fileText = getDataFromFile(context, filename);
            String[] games = fileText.split(" ");

            boolean isInFile = false;   // is game already in file?
            for (String gameInFile : games) {
                if (gameInFile.equals(game)) {
                    isInFile = true;
                    break;
                }
            }

            if (!isInFile) { // if game isn't already in file then we should add it
                if (!fileText.isEmpty()) {
                    fileText += " " + game; // if not empty, new line comes before
                } else {
                    fileText += game; // if empty, just add game itself
                }
            }

            FileOutputStream out = context.openFileOutput(filename, MODE_PRIVATE);
            out.write(fileText.getBytes());
            out.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("error", "error when writing to internal storage - update list of games");
        }
    }

    /* Returns an arraylist of all the games we have data files for. */
    public static ArrayList<String> getGames(Context context) {
        ArrayList<String> games = new ArrayList<>();
        try {
            String fileText = getDataFromFile(context, GAME_NAMES_FILE + ".txt");

            String[] fileGames = fileText.split(" ");
            for (String game : fileGames) {
                games.add(game);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("error", "error when getting games from gamesnamefile.txt");
        }
        return games;
    }

    /* Reset/delete the entire database. Empties game_names file. */
    public static void deleteAllGames(Context context) {
        ArrayList<String> gamesToDelete = Page.getGames(context);

        for (String game : gamesToDelete) {
            deleteGameFromDatabase(context, game);
        }

        /* Empty the game_names file. */
        try {
            FileOutputStream out = context.openFileOutput(GAME_NAMES_FILE + ".txt", MODE_PRIVATE);
            out.write("".getBytes());
            out.close();
        } catch (Exception ex) {
            Log.d("error" , "error when trying to delete database.");
        }

    }

    /* Deletes specified game's file in database and update game_names file. */
    /* Files should be json (as they are normally stored).*/
    public static void deleteGame(Context context, String game) {
        deleteGameFromDatabase(context, game);

        // update game names file
        ArrayList<String> games = Page.getGames(context);
        StringBuilder updatedGames = new StringBuilder();

        for (int i = 0; i < games.size(); i++) {
            String chosenGame = games.get(i);
            if (!chosenGame.equals(game)) {
                if (i > 0) updatedGames.append(" "); // first element won't have a space
                updatedGames.append(chosenGame);
            }
        }

        try {
            FileOutputStream out = context.openFileOutput(GAME_NAMES_FILE + ".txt", MODE_PRIVATE);
            out.write(updatedGames.toString().getBytes());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("error", "error when attempting to delete game from game names file.");
        }
    }

    /*
     * This particular function will only delete the game from
     * the database as opposed to also deleting it from the game_names
     * file. Used internally. It's just fod decomposition.
     */
    private static void deleteGameFromDatabase(Context context, String game) {
        File fileToDelete = new File(context.getFilesDir(), game + ".json");
        fileToDelete.delete();
    }

    /*
    Delete the specified game in the database.
    (Does nothing if game doesn't exist)
    Removes that particular game from game_names file.
    */

    public static String prettyPrintJSON(Object jsonObj) {
        /* return pretty print version of json str. */
        /* useful for debugging */
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj); // return string format
        } catch (Exception ex) {
            Log.d("pupper", "error with pretty printing json in getShapesJSON() page.");
            ex.printStackTrace();
        }

        // the horror...
        json = json.replace("\\\"", "\"").replace("\\n", "\n");
        json = json.replace("\"{", "{").replace( "}\"", "}");
        json = json.replace("\"[", "[").replace("]\"", "]");
        json = json.replace("\"null\"", "null");
        json = json.replace("\"true\"", "true").replace("\"false\"", "false");
        json = json.replace(", {", ", \n{");

        Log.d("cattty", json);

        return json;
    }


    /* Loading res/raw files into internal storage.
    *
    * Purpose: We can't write to files in res/raw, so we can't maintain
    *       our database files in that location. So
    *       we will be using Android's internal storage, which stores
    *       files privately on the user's phone. Since we are using an
    *       emulator and we don't all share the same emulator (i.e. each
    *       emulator will have its own local files), in order to get a sample
    *       database file into everyone's emulator, I am placing the sample file
    *       into res/raw and loading it into your emulator's internal storage
    *       if you call loadDatabase(). Likewise, we must do the same thing for
    *       the canonical bunny world example.
    *
    *       This doesn't affect the functionality defined in specs. The sample
    *       file is just so y'all can test things! When you save to the database,
    *       it will be stored in your emulator phone's internal storage, so the
    *       data is persistent.
    * */
    public static void loadRawFileIntoInternalStorage(Context context, String rawfilename) {
        String jsonString = getRawFile(context, rawfilename);
        loadJSONStringIntoDatabase(context, rawfilename, jsonString);
    }

    /* sampledatabase.json in res/raw folder. */
    private static String getRawFile(Context context, String rawfilename) {
        StringBuilder ret = new StringBuilder();
        try {
            InputStream in = context.getResources().openRawResource(
                    context.getResources().getIdentifier(rawfilename,
                            "raw", context.getPackageName()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line);
            }

            in.close();
        } catch (IOException ex) {
            Log.d("dog", "Error getting json data from file " + rawfilename + " in getSampleDatabaseFile.");
        }
        return ret.toString();
    }

    public static void test(Context context) {
//        Page.loadDatabase(context, SAMPLE_DATA_FILE);
//        ArrayList<Page> pages = Page.getPages();
//        String string1 = "";
//        for (Page page : pages) {
//            string1 += page.toString();
//            string1 += page.getPageJSON();
//        }
//
//        loadIntoDatabaseFile(context, SAMPLE_DATA_FILE);
//        loadIntoDatabaseFile(context, SAMPLE_DATA_FILE + "1");
//        loadIntoDatabaseFile(context, SAMPLE_DATA_FILE + "2");
//        loadIntoDatabaseFile(context, SAMPLE_DATA_FILE + "3");
//        loadIntoDatabaseFile(context, SAMPLE_DATA_FILE + "4");
//
//
//        String string2 = "";
//        for (Page page : pages) {
//            string2 += page.toString();
//            string2 += page.getPageJSON();
//        }
//
//        Log.d("dog2", string1.equals(string2) ? "OKAY" : "NOPE");
//
//
//        ArrayList<String> games = Page.getGames(context);
//        for (String game: games) {
//            Log.d("waddup", game);
//        }
//
//        for (String game : games) {
//            deleteGame(context, game);
//        }
//
//        deleteAllGames(context);
//
//        deleteGame(context, "nonexistent");
//
//        ArrayList<String> games2 = Page.getGames(context);
//        Log.d("waddup", "starting games2");
//        for (String game1: games2) {
//            Log.d("waddup", game1);
//        }
    }
}

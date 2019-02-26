package edu.stanford.cs108.bunnyworld;

import android.content.Context;
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
 * Client should use Page.getPages(this, "name of game") to get all of the
 * pages (in order) from the database for a particular game. Each page will
 * contain a pageName ivar (use getPageName()) and an
 * ArrayList of shapes ivar (use getShapes()).
 *
 * The first argument is the "context." If you are calling from an activity,
 * then it should be fine to just pass in the keyword "this."
 */
public class Page {
    private String pageName;
    private int pageID;
    private ArrayList<Shape> shapes;

    public Page(String pageName, int pageID, ArrayList<Shape> shapes) {
        this.pageName = pageName;
        this.pageID = pageID;
        this.shapes = shapes;
    }

    public String getPageName() {
        return pageName;
    }

    public int getPageID() {
        return pageID;
    }

    /* Use this on a page to get the associated shapes, which you can then have draw themselves. */
    public ArrayList<Shape> getShapes() {
        return shapes;
    }

    public static ArrayList<Page> getPages(Context context, String nameOfGame) {
//        String filename = nameOfGame + ".json";
        ArrayList<Page> pages = parseDatabaseForPages(context, nameOfGame);
        return pages;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<Page> parseDatabaseForPages(Context context, String filename) {
        Log.d("dog", "launching parse for pages");
        ArrayList<Page> allPages = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            String json = getJSONDataFromFile(context, filename);
            JSONObject data = (JSONObject) parser.parse(json);
//            String num_pages = (String) data.get("num_pages");
            JSONArray pages = (JSONArray) data.get("pages");
            Iterator<JSONObject> it = pages.iterator();
            while (it.hasNext()) {
                JSONObject page = it.next();
                String page_name = (String) page.get("page_name");
                int page_id = (int) (long) page.get("page_id"); // this won't overflow.
                ArrayList<Shape> shapes = parseShapes((JSONArray) page.get("shapes"));
                Page newP = new Page(page_name, page_id, shapes);
                allPages.add(newP);
            }

        } catch(Exception ex) {
            System.out.println("Error parsing database file for " + filename + ".");
            Log.d("dog", "file reading failed");
            ex.printStackTrace();
        }
        return allPages;
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
//        Log.d("dog", ret.toString());
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
        ArrayList<Page> pages = Page.getPages(context,"sampledatafile");
        for (Page page : pages) {
            Log.d("dog", page.toString());
        }
    }
}

package edu.stanford.cs108.bunnyworld;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.GridView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


/**
 * Page Directory contains all of the pages for the specified game.
 * The user can edit a particular page by clicking on it in the layout,
 * or they can create a new page. Before the user saves the game to the
 * database, they must check for errors and resolve all of those errors
 * before saving. The user can also change the game name from this screen.
 * The user can also undo any pages they have deleted since opening
 * the game.
 */
public class PageDirectory extends AppCompatActivity {

    ArrayList<Page> pages;
    public static final String PAGE_ID = "page";
    public static final String GAME = "game";
    public static final String PATH = "/data/user/0/edu.stanford.cs108.bunnyworld/app_resourceDir";
    String gameName;
    String isError = "unchecked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_directory);

        Bundle extras = getIntent().getExtras();
        gameName = extras.getString(MainActivity.GAME_EXTRA);
        boolean isCreate = extras.getBoolean(MainActivity.IS_CREATE);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.game_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(gameName);

        if (!isCreate) { // edit, load it
            Page.loadDatabase(this, gameName);
            drawPages();

            System.out.println("loading resources");

            for (int i = 0; i < Shape.importedResources.size(); i ++) {
                Shape.loadResourceFromStorage(PATH, Shape.importedResources.get(i), this);
            }
        } else {
            // reset data from any previous things we've done, create game should be fresh
            Page.getPages().clear();
            Page.getPossessions().clear();
            Shape.getAllShapes().clear();

            // by specs, we need a page1 already loaded in
            Page page1 = new Page("page1", 1, new ArrayList<Shape>());
            drawPages();
        }

        pages = Page.getPages();

        final FloatingActionButton addPage = findViewById(R.id.add_page);
        addPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPage(view);
            }
        });

        final GridView pageGrid = (GridView) findViewById(R.id.page_grid);
        pageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pageID = pages.get(position).getPageID();

                Intent gameEditorIntent = new Intent(getApplicationContext(), GameEditor.class);
                gameEditorIntent.putExtra(PAGE_ID, pageID);
                gameEditorIntent.putExtra(GAME, gameName);
                startActivity(gameEditorIntent);


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isError = "unchecked";
        invalidateOptionsMenu();
        drawPages();
    }

    public void addPage(View view) {
        showPopUp(PageDirectory.this);
    }

    private void showPopUp(final Context context) {
        final AlertDialog.Builder pageNameDialog = new AlertDialog.Builder(PageDirectory.this);
        pageNameDialog.setTitle("Enter new page name:");
        pageNameDialog.setView(R.layout.name_editor);

        pageNameDialog.setPositiveButton("Ok", null);
        pageNameDialog.setNegativeButton("Cancel", null);

        final AlertDialog pageName = pageNameDialog.create();
        pageName.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                EditText newPageName = ((AlertDialog) pageName).findViewById(R.id.editable_page_name);
                newPageName.setText("page" + (pages.size() + 1));
                Button ok = pageName.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EditText newPageName = ((AlertDialog) pageName).findViewById(R.id.editable_page_name);
                        String name = newPageName.getText().toString();

                        if (!pageExists(name) && !name.contains(" ") && !name.equals("")) {
                            Page newPage = new Page(name, pages.size() + 1, new ArrayList<Shape>());

                            drawPages();

                            Intent gameEditorIntent = new Intent(getApplicationContext(), GameEditor.class);
                            gameEditorIntent.putExtra(PAGE_ID, newPage.getPageID());
                            gameEditorIntent.putExtra(GAME, gameName);
                            startActivity(gameEditorIntent);

                            pageName.dismiss();
                        } else {
                            TextView errorMessage = ((AlertDialog) pageName).findViewById(R.id.error_message);
                            errorMessage.setVisibility(v.VISIBLE);

                            if (pageExists(name)) {
                                errorMessage.setText("Page with that name already exists.");
                            } else if (name.contains(" ")) {
                                errorMessage.setText("Page names cannot have spaces.");
                            } else if (name.equals("")) {
                                errorMessage.setText("Page names cannot be empty.");
                            }
                        }
                    }
                });

                Button cancel = pageName.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        pageName.dismiss();
                    }
                });
            }
        });

        pageName.show();
    }

    private boolean pageExists(String page) {
        ArrayList<Page> allPages = Page.getPages();
        for (Page p : allPages) {
            if (page.equals(p.getPageName())) {
                return true;
            }
        }

        return false;
    }

    private void drawPages() {
        GridView pageGrid = (GridView) findViewById(R.id.page_grid);
        List<String> pageNames = new ArrayList<String>();
        ArrayList<Page> pages = Page.getPages();

        for (Page p : pages) {
            pageNames.add(p.getPageName());
            System.out.println(p.getPageName());
        }

        ArrayAdapter<String> gridAdapter = new ArrayAdapter<String>
                (this, R.layout.page_grid_layout, pageNames);

        pageGrid.setAdapter(gridAdapter);
    }


    public void editGameName(MenuItem item) {
        final AlertDialog.Builder gameNameDialog = new AlertDialog.Builder(PageDirectory.this);
        gameNameDialog.setTitle("Enter new game name:");
        gameNameDialog.setView(R.layout.name_editor);

        gameNameDialog.setPositiveButton("Ok", null);
        gameNameDialog.setNegativeButton("Cancel", null);


        final AlertDialog gameNameD = gameNameDialog.create();
        gameNameD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = gameNameD.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EditText newGameName = ((AlertDialog) gameNameD).findViewById(R.id.editable_page_name);
                        String newGameNameStr = newGameName.getText().toString();

                        if (!gameExists(newGameNameStr) && !newGameNameStr.contains(" ") && !newGameNameStr.equals("")) {
                            Page.deleteGame(getApplicationContext(), gameName);
                            gameName = newGameNameStr;
                            getSupportActionBar().setTitle(gameName);
                            Page.loadIntoDatabaseFile(getApplicationContext(), gameName);
                            gameNameD.dismiss();
                        } else {
                            TextView errorMsg = ((AlertDialog) gameNameD).findViewById(R.id.error_message);
                            errorMsg.setVisibility(View.VISIBLE);

                            if (gameExists(newGameNameStr)) {
                                errorMsg.setText("Game with that name already exists.");
                            } else if (newGameNameStr.contains(" ")) {
                                errorMsg.setText("Game name cannot have spaces.");
                            } else if (newGameNameStr.equals("")) {
                                errorMsg.setText("Game name cannot be empty.");
                            }
                        }
                    }
                });

                Button cancel = gameNameD.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        gameNameD.dismiss();
                    }
                });
            }
        });

        gameNameD.show();

    }

    private boolean gameExists(String obj) {
        ArrayList<String> gameNames = Page.getGames(this);
        for (String game : gameNames) {
            if (obj.equals(game) && !gameName.equals(game)) {
                return true;
            }
        }

        return false;
    }

    public void saveGame(MenuItem item) {
        if (isError.equals("no errors")) {
            Toast saveToast = Toast.makeText(getApplicationContext(), "Saved " + gameName, Toast.LENGTH_SHORT);
            saveToast.show();
            Page.loadIntoDatabaseFile(this, gameName);

        } else if (isError.equals("errors")) {
            Toast errorToast = Toast.makeText(getApplicationContext(), "Unable to save due to errors", Toast.LENGTH_SHORT);
            errorToast.show();

        } else if (isError.equals("unchecked")) {
            Toast testToast = Toast.makeText(getApplicationContext(), "Please run the test before saving", Toast.LENGTH_SHORT);
            testToast.show();
        }
    }

    public void deleteGame(MenuItem item) {
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(PageDirectory.this);
        deleteDialog.setTitle("Delete");

        deleteDialog.setMessage("Are you sure you want to delete?");

        deleteDialog.setPositiveButton("Delete", null);
        deleteDialog.setNegativeButton("Cancel", null);

        final AlertDialog delete = deleteDialog.create();
        delete.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button yes = delete.getButton(AlertDialog.BUTTON_POSITIVE);
                yes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Page.deleteGame(getApplicationContext(), gameName);
                        Page.getPages().clear();
                        Page.getPossessions().clear();
                        Shape.getAllShapes().clear();
                        onBackPressed();
                        delete.dismiss();
                    }
                });

                Button no = delete.getButton(AlertDialog.BUTTON_NEGATIVE);
                no.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // do nothing
                        delete.dismiss();
                    }
                });
            }
        });

        delete.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_dir_editor, menu);

        MenuItem errorButton = menu.findItem(R.id.error_button_page);
        Button error = (Button) errorButton.getActionView();
        error.setText(R.string.check_errors);
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> invalidStates = runErrorTestPage();
                showStatus(invalidStates);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // for undo page delete support
    public void undoPageDelete(MenuItem menuItem) {
        Stack<Page> deletedPages = GameEditor.deletedPages;
        if (!deletedPages.empty()) {
            Page returnedPage = deletedPages.pop();
            Page.getPages().add(returnedPage);

            for (Shape shape : returnedPage.getShapes()) {
                Shape.getAllShapes().add(shape);
            }
        }

        // restart activity
        onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem errorIcon = menu.findItem(R.id.error_icon_page);
        if (isError.equals("unchecked")) {
            errorIcon.setIcon(R.drawable.ic_refresh_black_24dp);
        } else if (isError.equals("errors")) {
            errorIcon.setIcon(R.drawable.ic_error_black_24dp);
        } else if (isError.equals("no errors")) {
            errorIcon.setIcon(R.drawable.ic_check_circle_black_24dp);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public ArrayList<String> runErrorTestPage() {
        ArrayList<Page> allPages = Page.getPages();
        ArrayList<Shape> allShapes = Shape.getAllShapes();
        Set<String> pageNames = new HashSet<>(Arrays.asList(GameEditor.getPageNames()));
        Set<String> shapeNames = new HashSet<>(Arrays.asList(GameEditor.getShapeNames()));
        Set<String> validActions = new HashSet<>(Arrays.asList(GameEditor.scriptActions));

        ArrayList<String> invalidStates = new ArrayList<>(); // reset old states and run test from scratch
        for (Page page : allPages) {
            for (Shape shape : page.getShapes()) {
                HashMap<String, String> commands = shape.getCommands();

                // validate on drop <shape-name> s.t. shape-name exists for current shape
                for (String trigger : commands.keySet()) {
                    Log.d("dog", "verifying... " + trigger);
                    if (trigger.startsWith("on drop")) {
                        String shapeName = trigger.substring("on drop".length() + 1);
                        Log.d("dog", shapeName + " is the shape name");
                        if (!shapeNames.contains(shapeName)) {
                            String invalidState = shape.getName() + " contains invalid script trigger <"
                                    + trigger + "> since shape " + shapeName + " does not exist.";
                            if (!invalidStates.contains(invalidState)) {
                                invalidStates.add(invalidState);
                            }
                        }
                    }
                }

                // validate each command in rest of script is valid (note that only on drop trigger
                // references other objects, and since we enforce correct triggers in UI design,
                // we know the other triggers are valid (onclick, onenter)
                for (String command : commands.values()) {
                    // iterate through diff pieces of command, note that this assumes
                    // page and shape names have no spaces
                    String prevCommand = ""; // will be used to differentiate b/w pages and shapes
                    for (String portion : command.split(" ")) {
                        // note that the script triggers and primitives should also be
                        // restricted as shape/page names!
                        if (validActions.contains(portion)) {
                            prevCommand = portion;
                        } else {
                            // note that prevCommand is guaranteed to be one of the following
                            // cases by design (actions come before page/shape)
                            if (prevCommand.equals("goto")) { // currently a page
                                if (!pageNames.contains(portion)) {
                                    String invalidState = shape.getName() + " contains invalid script action "
                                            + "goto <" + portion + "> since page " + portion + " does not exist.";
                                    if (!invalidStates.contains(invalidState)) {
                                        invalidStates.add(invalidState);
                                    }
                                }
                            } else if (prevCommand.equals("hide")) { // currently a shape
                                if (!shapeNames.contains(portion)) {
                                    String invalidState = shape.getName() + " contains invalid script action " +
                                            "hide <" + portion + "> since shape " + portion + " does not exist.";
                                    if (!invalidStates.contains(invalidState)) {
                                        invalidStates.add(invalidState);
                                    }
                                }
                            }
                            // Note: play corresponds to a sound, and those are guaranteed
                            //       to be valid given the way the UI is designed :)
                        }

                    }
                }
            }
        }

        // validate that shapes have unique names
        for (Shape currShape : allShapes) {
            for (Shape otherShape : allShapes) {
                if (!currShape.equals(otherShape) && currShape.getName().equals(otherShape.getName())) {
                    String invalidState = "The following shape name is not unique: " + currShape.getName();
                    if (!invalidStates.contains(invalidState)) {
                        invalidStates.add(invalidState);
                    }
                    break; // if we found a duplicate, no reason to look for more
                }
            }
        }

        // validate that pages have unique names
        for (Page currPage : allPages) {
            for (Page otherPage : allPages) {
                if (!currPage.equals(otherPage) && currPage.getPageName().equals(otherPage.getPageName())) {
                    String invalidState = "The following page name is not unique: " + currPage.getPageName();
                    if (!invalidStates.contains(invalidState)) {
                        invalidStates.add(invalidState);
                    }
                    break; // if we found a duplicate, no reason to look for more
                }
            }
        }

        // validate page and shape names are unique with respect to each other
        for (Page currPage: allPages) {
            for (Shape shape : allShapes) {
                if (currPage.getPageName().equals(shape.getName())) {
                    String invalidState = "The following page name is not unique (conflict with a shape): " + currPage.getPageName();
                    if (!invalidStates.contains(invalidState)) {
                        invalidStates.add(invalidState);
                    }
                    break; // if we found a duplicate, no reason to look for more
                }
            }
        }

        // "unchecked" is initial default value and is set periodically
        // as the user makes changes to the pages and shapes
        if (invalidStates.isEmpty()) {
            isError = "no errors";
        } else {
            isError = "errors";
        }

        invalidateOptionsMenu();
        return invalidStates;
    }

    public void showStatus(ArrayList<String> invalidStates) {
        // isError corresponds to currently displayed item
        final AlertDialog.Builder errorIconDialog = new AlertDialog.Builder(PageDirectory.this);
        errorIconDialog.setTitle("Current error status:");

        String message = "";
        switch (isError) {
            case "unchecked": // currently unreachable since they have to validate game to see dialog
                message = "Check Errors to validate current state of game.";
                break;
            case "errors":
                message = "The following issues were detected: ";
                for (String invalidState : invalidStates) {
                    message += "\n\n" + invalidState;
                }
                break;
            case "no errors":
                message = "No errors were detected. Safe to save.";
                break;
        }
        errorIconDialog.setMessage(message);

        errorIconDialog.setPositiveButton("OK", null);
        final AlertDialog errorIconD = errorIconDialog.create();
        errorIconD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = errorIconD.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        errorIconD.dismiss();
                    }
                });
            }
        });

        errorIconD.show();
    }

}

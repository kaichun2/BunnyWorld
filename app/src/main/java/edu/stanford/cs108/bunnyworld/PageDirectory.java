package edu.stanford.cs108.bunnyworld;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

public class PageDirectory extends AppCompatActivity {

    ArrayList<Page> pages;
    public static final String PAGE_ID = "page";
    public static final String GAME = "game";
    String gameName;

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
        Toast saveToast = Toast.makeText(getApplicationContext(), "Saved " + gameName, Toast.LENGTH_SHORT);

        saveToast.show();

        // TO DO: verify save
        // loadIntoDatabaseFile
        Page.loadIntoDatabaseFile(this, gameName);
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
}

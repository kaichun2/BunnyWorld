package edu.stanford.cs108.bunnyworld;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import android.content.Intent;

public class PageDirectory extends AppCompatActivity {

    ArrayList<Page> pages;
    public static final String PAGE_ID = "page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_directory);

        Bundle extras = getIntent().getExtras();
        String game = extras.getString(MainActivity.GAME_EXTRA);
        boolean isCreate = extras.getBoolean(MainActivity.IS_CREATE);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.game_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(game);

        if (!isCreate) { // edit, load it
            Page.loadDatabase(this, game);
            drawPages();
        } else {
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
                startActivity(gameEditorIntent);


            }
        });
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
                Button ok = pageName.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TO DO: create a page
                        // validate that it is a unique page name
                        // verify that there are no spaces
                        EditText newPageName = ((AlertDialog) pageName).findViewById(R.id.editable_page_name);

                        Page newPage = new Page(newPageName.getText().toString(), pages.size() + 1, new ArrayList<Shape>());

                        drawPages();

                        pageName.dismiss();
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

    private void drawPages() {
        GridView pageGrid = (GridView) findViewById(R.id.page_grid);
        List<String> pageNames = new ArrayList<String>();
        ArrayList<Page> pages = Page.getPages();

        for (Page p : pages) {
            pageNames.add(p.getPageName());
        }

        ArrayAdapter<String> gridAdapter = new ArrayAdapter<String>
                (this, R.layout.page_grid_layout, pageNames);

        pageGrid.setAdapter(gridAdapter);
    }


}

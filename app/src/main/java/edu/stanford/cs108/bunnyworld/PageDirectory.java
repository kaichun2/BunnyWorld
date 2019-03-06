package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.GridLayout;
import java.util.ArrayList;
import android.content.Intent;

public class PageDirectory extends AppCompatActivity {

    int numPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_directory);

        Page.loadDatabase(this, "sampledatafile");
        ArrayList<Page> pages = Page.getPages();
        numPages = pages.size();
        for (Page p : pages) {
            drawPage(p.getPageName());
        }
    }

    public void addPage(View view) {
        showPopUp(PageDirectory.this);
    }

    private void showPopUp(final Context context) {
        final EditText pageName = new EditText(context);
        String num = Integer.toString(numPages);//get num from db
        String defaultStr = "Page " + num;
        pageName.setText(defaultStr);

        AlertDialog prompt = new AlertDialog.Builder(context)
                .setTitle("Page Name")
                .setView(pageName)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = String.valueOf(pageName.getText());
                        createPage(name);
                        numPages++;
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        prompt.show();
    }

    private void createPage(String name) {
        ArrayList<Shape> empty = new ArrayList<Shape>();
        Page newPage = new Page(name, numPages - 1, empty);


        drawPage(name);
    }

    private void drawPage(String name) {
        LinearLayout ll = findViewById(R.id.buttonlayout);
        Button nextPg = new Button(PageDirectory.this);
        nextPg.setText(name);
        nextPg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PageDirectory.this, GameEditor.class);
                startActivity(intent);
            }
        });
        ll.addView(nextPg);
    }

}

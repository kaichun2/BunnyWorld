package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    public static ArrayList<Page> pages;
    public static ArrayList<Shape> possessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle extras = getIntent().getExtras();
        String game = extras.getString(MainActivity.GAME_EXTRA);
        Page.loadDatabase(this, game);

        pages = Page.getPages();
        possessions = Page.getPossessions();
    }

}

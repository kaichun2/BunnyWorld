package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    public static ArrayList<Page> pages;
    public static ArrayList<Shape> possessions;
    public static String game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle extras = getIntent().getExtras();
        game = extras.getString(MainActivity.GAME_EXTRA);
        Page.loadDatabase(this, game);

        pages = Page.getPages();
        possessions = Page.getPossessions();
    }

    public void onRestartGameClick(View view) {
        // resets properties
        Page.loadDatabase(this, game);
        pages = Page.getPages();
        possessions = Page.getPossessions();

        GameView.reset = true;
        GameView gameview = findViewById(R.id.game_view);
        gameview.invalidate();
    }

}

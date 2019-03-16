package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


/**
 * Outer shell for the game canvas GameView. Contains a toolbar
 * above the canvas that is the same size as the toolbar in GameEditor
 * and PageDirectory, so what you create in GameEditor is exactly
 * what you get when playing the game. The toolbar displays the name
 * of the current page in addition to a restart icon that can be clicked
 * to restart the current game.
 */
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

        Log.d("game", game);
        Log.d("game", Page.getPages().toString());

        pages = Page.getPages();
        possessions = Page.getPossessions();

        GameView.updateActivity(this); // update current reference
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onRestartGameClick(MenuItem menuItem) {
        // resets properties
        Page.loadDatabase(this, game);
        pages = Page.getPages();
        possessions = Page.getPossessions();

        GameView.reset = true;
        GameView gameview = findViewById(R.id.game_view);
        gameview.invalidate();

    }
}

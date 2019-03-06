package edu.stanford.cs108.bunnyworld;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.*;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private String[] games; // all the games

    public static final String GAME_EXTRA = "game";
    public static final String IS_CREATE = "is_create";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        putRawFilesIntoInternalAndroidStorage();
        ArrayList<String> gamesList = Page.getGames(this);
        String[] gamesArr = new String[gamesList.size()];
        games = gamesList.toArray(gamesArr);
    }

    // you only have to run this once on your emulator
    // feel free to comment it out after you've ran it once
    // (this is necessary to get the two res/raw json files
    // into internal storage, Patrick Young approved)
    // only loading bunny world file
    private void putRawFilesIntoInternalAndroidStorage() {
        Page.loadRawFileIntoInternalStorage(this, Page.SAMPLE_DATA_FILE);
        Page.loadRawFileIntoInternalStorage(this, Page.BUNNY_WORLD_FILE);

        // reset page, shape, and possessions arrays so it doesn't mess with anything
        Page.getPages().clear();
        Shape.getAllShapes().clear();
        Page.getPossessions().clear();
    }


    public void selectPlayGame(View view) {
        final AlertDialog.Builder playGameDialog = new AlertDialog.Builder(MainActivity.this);
        playGameDialog.setTitle("Select game to play:");

        playGameDialog.setSingleChoiceItems( games, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        playGameDialog.setPositiveButton("Play", null);
        playGameDialog.setNegativeButton("Cancel", null);

        final AlertDialog playGame = playGameDialog.create();
        playGame.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = playGame.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ListView lw = ((AlertDialog) playGame).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                        // pass intent to go to new activity
                        Intent gameActivityIntent = new Intent(getApplicationContext(), GameActivity.class);
                        gameActivityIntent.putExtra(GAME_EXTRA, checkedItem.toString());
                        startActivity(gameActivityIntent);
                        playGame.dismiss();
                    }
                });

                Button cancel = playGame.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        playGame.dismiss();
                    }
                });
            }
        });

        playGame.show();
    }

    public void createGame(View view) { // goes to page 2
        final AlertDialog.Builder createGameDialog = new AlertDialog.Builder(MainActivity.this);
        createGameDialog.setTitle("Enter game name:");
        createGameDialog.setView(R.layout.page_name_editor);

        createGameDialog.setPositiveButton("Ok", null);
        createGameDialog.setNegativeButton("Cancel", null);


        final AlertDialog createGame = createGameDialog.create();
        createGame.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = createGame.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EditText newGameName = ((AlertDialog) createGame).findViewById(R.id.editable_page_name);
                        String newGame = newGameName.getText().toString();

                        // pass intent if valid
                        if (!newGame.equals(Page.GAME_NAMES_FILE) && !hasElem(newGame)) {
                            Intent pageDirectoryIntent = new Intent(getApplicationContext(), PageDirectory.class);
                            pageDirectoryIntent.putExtra(GAME_EXTRA, newGame);
                            pageDirectoryIntent.putExtra(IS_CREATE, true);
                            startActivity(pageDirectoryIntent);
                            createGame.dismiss();
                        } else {

                            // TODO - TASSICA - make this show up, edit stuff in xml file

                            // error handling
                            if (newGame.equals(Page.GAME_NAMES_FILE)) {
                                createGame.setMessage("Please choose a different game name.");
                            } else {
                                createGame.setMessage("Game with that name already exists.");
                            }
                        }

                    }
                });

                Button cancel = createGame.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        createGame.dismiss();
                    }
                });
            }
        });

        createGame.show();
    }

    private boolean hasElem(String game) {
        for (String curr : games) {
            if (curr.equals(game)) return true;
        }
        return false;
    }

    public void selectEditGame(View view) { // goes to page 2
        final AlertDialog.Builder editGameDialog = new AlertDialog.Builder(MainActivity.this);
        editGameDialog.setTitle("Select game to edit:");


        editGameDialog.setSingleChoiceItems( games, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        editGameDialog.setPositiveButton("Edit", null);
        editGameDialog.setNegativeButton("Cancel", null);


        final AlertDialog editGame = editGameDialog.create();
        editGame.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = editGame.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ListView lw = ((AlertDialog) editGame).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                        // pass intent to go to new activity
                        Intent gameActivityIntent = new Intent(getApplicationContext(), PageDirectory.class);
                        gameActivityIntent.putExtra(GAME_EXTRA, checkedItem.toString());
                        gameActivityIntent.putExtra(IS_CREATE, false);
                        startActivity(gameActivityIntent);

                        editGame.dismiss();
                    }
                });

                Button cancel = editGame.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        editGame.dismiss();
                    }
                });
            }
        });

        editGame.show();
    }
}

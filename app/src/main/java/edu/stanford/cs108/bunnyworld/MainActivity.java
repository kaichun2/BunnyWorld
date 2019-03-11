package edu.stanford.cs108.bunnyworld;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.*;
import android.view.View;
import android.text.style.*;
import java.util.ArrayList;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;


public class MainActivity extends AppCompatActivity {

    private String[] games; // all the games

    public static final String GAME_EXTRA = "game";
    public static final String IS_CREATE = "is_create";
    private int backgrdImgIndex = 1;
    private int[] color = new int[]{Color.RED,Color.GREEN,Color.BLUE,Color.CYAN,Color.GRAY,Color.MAGENTA,Color.YELLOW,Color.BLACK};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.title);
        SpannableString title = new SpannableString("Bunny World");
        title.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, title.length(), 0);
        for(int i = 0; i < title.length(); i++) {
            title.setSpan(new ForegroundColorSpan(color[i % color.length]), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(title);
        putRawFilesIntoInternalAndroidStorage();
        updateGames();
    }

    private void updateGames() {
        ArrayList<String> gamesList = Page.getGames(this);
        games = new String[gamesList.size()];
        games = gamesList.toArray(games);
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

    public void change_background(View view) {
        LinearLayout background = (LinearLayout) findViewById(R.id.background);
        int img = 0;
        if (backgrdImgIndex == 0) {
            img = R.drawable.bunny;
        } else if (backgrdImgIndex == 1) {
            img = R.drawable.bunny1;
        } else if (backgrdImgIndex == 2) {
            img = R.drawable.bunny2;
        } else if (backgrdImgIndex == 3) {
            img = R.drawable.bunny3;
        } else {
            img = R.drawable.bunny4;
        }
        backgrdImgIndex = backgrdImgIndex != 4 ? backgrdImgIndex + 1 : 0;
        background.setBackgroundResource(img);
    }


    public void selectPlayGame(View view) {
        final AlertDialog.Builder playGameDialog = new AlertDialog.Builder(MainActivity.this);
        playGameDialog.setTitle("Select game to play:");

        Log.d("dog", " " + games.length);
        if (games.length >= 1) {
            playGameDialog.setSingleChoiceItems(games, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        } else {
            playGameDialog.setMessage("No games available--create one!");
        }

        playGameDialog.setPositiveButton("Play", null);
        playGameDialog.setNegativeButton("Cancel", null);

        final AlertDialog playGame = playGameDialog.create();
        playGame.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = playGame.getButton(BUTTON_POSITIVE);
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

                Button cancel = playGame.getButton(BUTTON_NEGATIVE);
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
        createGameDialog.setView(R.layout.name_editor);

        createGameDialog.setPositiveButton("Ok", null);
        createGameDialog.setNegativeButton("Cancel", null);


        final AlertDialog createGame = createGameDialog.create();
        createGame.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = createGame.getButton(BUTTON_POSITIVE);
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

                Button cancel = createGame.getButton(BUTTON_NEGATIVE);
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


        if (games.length >= 1) {
            editGameDialog.setSingleChoiceItems(games, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        } else {
            editGameDialog.setMessage("No games available--create one!");
        }

        editGameDialog.setPositiveButton("Edit", null);
        editGameDialog.setNegativeButton("Cancel", null);


        final AlertDialog editGame = editGameDialog.create();
        editGame.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = editGame.getButton(BUTTON_POSITIVE);
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

                Button cancel = editGame.getButton(BUTTON_NEGATIVE);
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

    // button click for deleting all games
    public void deleteAllGames(View view) {
        final AlertDialog deleteGameDialog = new AlertDialog.Builder(MainActivity.this).create();
        deleteGameDialog.setTitle(getResources().getString(R.string.areyousure));
        deleteGameDialog.setMessage(getResources().getString(R.string.deletegameswarning));

        deleteGameDialog.setButton(BUTTON_POSITIVE, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE) {
                            dialog.dismiss();
                            Page.deleteAllGames(getApplicationContext());
                            updateGames();
                            Toast.makeText(getApplicationContext(), "All games deleted.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        deleteGameDialog.setButton(BUTTON_NEGATIVE, getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_NEGATIVE) {
                            dialog.dismiss();
                        }
                    }
                });

        deleteGameDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGames(); // if deleted a game, update games is necessary
    }
}

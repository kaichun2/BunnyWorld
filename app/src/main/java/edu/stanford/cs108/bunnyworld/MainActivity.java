package edu.stanford.cs108.bunnyworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.*;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToDir(View view) {
        Intent intent = new Intent (this, PageDirectory.class);
        startActivity(intent);
    }

    public void selectEditGame(View view) {
        final AlertDialog.Builder editGameDialog = new AlertDialog.Builder(MainActivity.this);
        editGameDialog.setTitle("Select game to edit:");

        // TO DO: get game names
        editGameDialog.setSingleChoiceItems( new String[] {"game 1", "game 2"}, 0 , new DialogInterface.OnClickListener() {
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

    public void createGame(View view) {
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
                        // TO DO:
                        // validate that it is a unique game name
                        // verify that there are no spaces
                        // restrict so that it can't equal gamenamesfile
                        EditText newPageName = ((AlertDialog) createGame).findViewById(R.id.editable_page_name);

                        // pass intent


                        createGame.dismiss();
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

    public void selectPlayGame(View view) {
        final AlertDialog.Builder playGameDialog = new AlertDialog.Builder(MainActivity.this);
        playGameDialog.setTitle("Select game to play:");

        // TO DO: get game names
        playGameDialog.setSingleChoiceItems( new String[] {"game 1", "game 2"}, 0 , new DialogInterface.OnClickListener() {
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
}

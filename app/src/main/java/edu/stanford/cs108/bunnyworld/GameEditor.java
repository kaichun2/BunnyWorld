package edu.stanford.cs108.bunnyworld;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEditor extends AppCompatActivity {

    ExpandableListView expScriptTriggers;
    SimpleExpandableListAdapter expListAdapter;
    private String triggers[] = {"On Click", "On Enter", "On Drop", "Property" };
    private String scriptActions[] = {"GoTo", "PlaySound", "Visibility"};
    private String[][] actions = { scriptActions, scriptActions, scriptActions, {"Set Property"} };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_editor);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TO DO: Set to page name
        getSupportActionBar().setTitle("Page name");

        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

        for (int i = 0; i < triggers.length; i++) {
            Map<String, String> currTrigger = new HashMap<String, String>();
            currTrigger.put("Trigger", triggers[i]);
            groupData.add(currTrigger);

            List<Map<String, String>> childActions = new ArrayList<Map<String, String>>();
            for (int j = 0; j < actions[i].length; j++) {
                Map<String, String> currAction = new HashMap<String, String>();
                currAction.put("Action", actions[i][j]);
                childActions.add(currAction);
            }
            childData.add(childActions);
        }

        int groupLayout = R.layout.exp_list_group;
        String[] groupFrom = { "Trigger" };
        int[] groupTo = { R.id.exp_list_group };

        int childLayout = R.layout.exp_list_item;
        String[] childFrom = { "Action" };
        int[] childTo = { R.id.exp_list_item };

        expListAdapter = new SimpleExpandableListAdapter(this, groupData, groupLayout, groupFrom, groupTo,
                childData, childLayout, childFrom, childTo );

        expScriptTriggers = (ExpandableListView) findViewById(R.id.expScriptTriggers);
        expScriptTriggers.setAdapter(expListAdapter);


        expScriptTriggers.setOnChildClickListener(
                new ExpandableListView.OnChildClickListener() {

                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        TextView action_view = v.findViewById(R.id.exp_list_item);
                        String action = action_view.getText().toString();

                        if (groupPosition == 3) {
                            showPropertyDialogs(action);
                        } else {
                            showTriggerDialogs(action, groupPosition);
                        }

                        return false;
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_game_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showTriggerDialogs(String action, int groupPosition) {
        if (action.equals(scriptActions[0])) {

            showGoToDialog(groupPosition);

        } else if (action.equals(scriptActions[1])) {

            showPlaySoundDialog(groupPosition);

        } else if (action.equals(scriptActions[2])) {

            showVisiblityDialog(groupPosition);

        }

    }

    private void showPropertyDialogs(String action) {
        if (action.equals(actions[3][0])) {
            final AlertDialog.Builder propertyDialog = new AlertDialog.Builder(GameEditor.this);
            propertyDialog.setTitle("Properties:");
            propertyDialog.setView(R.layout.properties_dialog);

            propertyDialog.setPositiveButton("Ok", null);
            propertyDialog.setNegativeButton("Cancel", null);

            final AlertDialog property = propertyDialog.create();
            property.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button ok = property.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // set script
                            property.dismiss();
                        }
                    });

                    Button cancel = property.getButton(AlertDialog.BUTTON_NEGATIVE);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // do nothing
                            property.dismiss();
                        }
                    });
                }
            });

            property.show();
        }
    }

    private void showGoToDialog(int groupPosition) {
        final AlertDialog.Builder gotoDialog = new AlertDialog.Builder(GameEditor.this);
        gotoDialog.setTitle("Choose the page to go to:");

        gotoDialog.setPositiveButton("Ok", null);
        gotoDialog.setNegativeButton("Cancel", null);

        // TO DO: set the checkedItem to the current page that it goes to
        // TO DO: replace with the array of pages
        gotoDialog.setSingleChoiceItems( new String[] {"Page 1", "Page 2"}, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        final AlertDialog goTo = gotoDialog.create();
        goTo.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = goTo.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // set script
                        goTo.dismiss();
                    }
                });

                Button cancel = goTo.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        goTo.dismiss();
                    }
                });
            }
        });

        goTo.show();
    }

    private void showPlaySoundDialog(int groupPosition) {
        final AlertDialog.Builder playSoundDialog = new AlertDialog.Builder(GameEditor.this);
        playSoundDialog.setTitle("Choose the sound to play:");

        playSoundDialog.setPositiveButton("Ok", null);
        playSoundDialog.setNegativeButton("Cancel", null);
        playSoundDialog.setNeutralButton("Play", null);


        // TO DO: set the checkedItem to the current sounds that plays
        // TO DO: replace with the array of sound resources
        playSoundDialog.setSingleChoiceItems( new String[] {"cat sound", "dog sound"}, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });


        final AlertDialog playSound = playSoundDialog.create();
        playSound.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button play = playSound.getButton(AlertDialog.BUTTON_NEUTRAL);
                play.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TO DO: play sound
                    }
                });

                Button ok = playSound.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TO DO: set script
                        playSound.dismiss();
                    }
                });

                Button cancel = playSound.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        playSound.dismiss();
                    }
                });
            }
        });

        playSound.show();

    }

    private void showVisiblityDialog(int groupPosition) {
        final AlertDialog.Builder visibilityDialog = new AlertDialog.Builder(GameEditor.this);
        visibilityDialog.setTitle("Choose the visibility:");

        visibilityDialog.setPositiveButton("Ok", null);
        visibilityDialog.setNegativeButton("Cancel", null);

        // TO DO: replace with the current visibility
        visibilityDialog.setSingleChoiceItems( new String[] {"Hide", "Show"}, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        final AlertDialog visible = visibilityDialog.create();
        visible.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = visible.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // set script
                        visible.dismiss();
                    }
                });

                Button cancel = visible.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        visible.dismiss();
                    }
                });
            }
        });

        visible.show();

    }

    public void showScript(View view) {
        final AlertDialog.Builder scriptDialog = new AlertDialog.Builder(GameEditor.this);
        scriptDialog.setTitle("Current script:");

        // TO DO: get actual script
        scriptDialog.setMessage("<Input current script>");

        scriptDialog.setPositiveButton("Ok", null);

        final AlertDialog script = scriptDialog.create();
        script.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = script.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // set script
                        script.dismiss();
                    }
                });
            }
        });

        script.show();
    }

    public void editObjectName(View view) {
        RelativeLayout objNameHeader = findViewById(R.id.obj_name_header);

        objNameHeader.setVisibility(view.GONE);

        LinearLayout editableObjName = findViewById(R.id.editable_obj_name);

        editableObjName.setVisibility(view.VISIBLE);
    }

    public void changeObjName(View view) {

        // TO DO: set object name

        RelativeLayout objNameHeader = findViewById(R.id.obj_name_header);

        objNameHeader.setVisibility(view.VISIBLE);

        LinearLayout editableObjName = findViewById(R.id.editable_obj_name);

        editableObjName.setVisibility(view.GONE);
    }

    public void deleteObject(View view) {
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(GameEditor.this);
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
                        // TO DO: delete object
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

    public void deletePage(MenuItem item) {
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(GameEditor.this);
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
                        // TO DO: delete object
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

    public void saveObject(View view) {

        // TO DO: pass in object name
        Toast saveToast = Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT);

        saveToast.show();

        // TO DO: verify save
    }

    public void savePage(MenuItem item) {

        // TO DO: pass in page name
        Toast saveToast = Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT);

        saveToast.show();

        // TO DO: verify save
    }

    public void editPageName(MenuItem item) {
        final AlertDialog.Builder pageNameDialog = new AlertDialog.Builder(GameEditor.this);
        pageNameDialog.setTitle("Enter new page name:");
        pageNameDialog.setView(R.layout.page_name_editor);

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
                        // TO DO: change page name
                        // verify that there are no spaces
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
}

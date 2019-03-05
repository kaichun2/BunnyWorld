package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEditor extends AppCompatActivity {

    ExpandableListView expScriptTriggers;
    SimpleExpandableListAdapter expListAdapter;
    static Page currPage;
    int selectedResource;
    static int selectedShape;
    private String triggers[] = {"on click", "on enter", "on drop", "property" };
    private String scriptActions[] = {"goto", "play", "visibility"};
    private String[][] actions = { scriptActions, scriptActions, scriptActions, {"Set Property"} };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_editor);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        Intent intent = getIntent();

        // pass page ID
        int pageID = intent.getIntExtra("PAGE ID", 0);
        Page.loadDatabase(this, "sampledatafile");
        ArrayList<Page> allPages = Page.getPages();
//         currPage = allPages.get(pageID - 1);
        currPage = allPages.get(0);

        // TO DO: Set to page name
        getSupportActionBar().setTitle(currPage.getPageName());


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

        Map<String, BitmapDrawable> resources = Shape.getDrawables(this);
        drawResources(resources);

        selectedResource = -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_game_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void drawResources(Map<String, BitmapDrawable> resources) {
        LinearLayout resourceView = findViewById(R.id.resource_scroll);

        for (Map.Entry<String, BitmapDrawable> resource : resources.entrySet()) {
            final ImageView resourceV = new ImageView(this);
            Bitmap resourceBitmap = resource.getValue().getBitmap();
            resourceV.setImageBitmap(resourceBitmap);
            resourceV.setAdjustViewBounds(true);
            resourceV.setPadding(5,5,5,5);
            resourceV.setId(View.generateViewId());
            resourceV.setTag(resource.getKey());

            resourceV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (selectedResource != -1) {
                        ImageView currResource = findViewById(selectedResource);
                        currResource.setBackgroundColor(getResources().getColor(R.color.light_grey));
                    }

                    selectedResource = resourceV.getId();
                    resourceV.setBackgroundColor(Color.BLACK);
                    CanvasView.setSelectedResource(resourceV.getId());
                }
            });

            resourceView.addView(resourceV);
        }
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

            final Shape curr = currPage.getShapes().get(selectedShape);

            final AlertDialog property = propertyDialog.create();
            property.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button ok = property.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            System.out.println("triggered property dialog");
                            EditText x_val = (EditText) ((AlertDialog) property).findViewById(R.id.editable_X);
                            EditText y_val = (EditText) ((AlertDialog) property).findViewById(R.id.editable_Y);
                            EditText width = (EditText) ((AlertDialog) property).findViewById(R.id.editable_width);
                            EditText height = (EditText) ((AlertDialog) property).findViewById(R.id.editable_height);
                            Switch isMovable = (Switch) ((AlertDialog) property).findViewById(R.id.is_movable);
                            Switch isHidden = (Switch) ((AlertDialog) property).findViewById(R.id.is_hidden);

                            curr.setX(Float.parseFloat(x_val.getText().toString()));
                            System.out.println(Float.parseFloat(x_val.getText().toString()));
                            System.out.println(curr.getX());
                            curr.setY(Float.parseFloat(y_val.getText().toString()));
                            curr.setWidth(Float.parseFloat(width.getText().toString()));
                            curr.setHeight(Float.parseFloat(height.getText().toString()));
                            curr.setMovable(isMovable.isChecked());
                            curr.setVisible(!isHidden.isChecked());

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

            EditText x_val = (EditText) ((AlertDialog) property).findViewById(R.id.editable_X);
            EditText y_val = (EditText) ((AlertDialog) property).findViewById(R.id.editable_Y);
            EditText width = (EditText) ((AlertDialog) property).findViewById(R.id.editable_width);
            EditText height = (EditText) ((AlertDialog) property).findViewById(R.id.editable_height);
            Switch isMovable = (Switch) ((AlertDialog) property).findViewById(R.id.is_movable);
            Switch isHidden = (Switch) ((AlertDialog) property).findViewById(R.id.is_hidden);

            isMovable.setChecked(curr.isMovable());
            isHidden.setChecked(!curr.isVisible());
            x_val.setText(String.valueOf(curr.getX()));
            y_val.setText(String.valueOf(curr.getY()));
            width.setText(String.valueOf(curr.getWidth()));
            height.setText(String.valueOf(curr.getHeight()));

        }
    }

    private void showGoToDialog(final int groupPosition) {
        final AlertDialog.Builder gotoDialog = new AlertDialog.Builder(GameEditor.this);
        gotoDialog.setTitle("Choose the page to go to:");

        gotoDialog.setPositiveButton("Ok", null);
        gotoDialog.setNegativeButton("Cancel", null);

        // TO DO: set the checkedItem to the current page that it goes to
        ArrayList<Page> allPages = Page.getPages();

        String[] pageNames = new String[allPages.size()];

        for(int i = 0; i < allPages.size(); i++) {
            pageNames[i] = allPages.get(i).getPageName();
        }
        gotoDialog.setSingleChoiceItems( pageNames, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        final Shape curr = currPage.getShapes().get(selectedShape);

        final AlertDialog goTo = gotoDialog.create();
        goTo.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = goTo.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ListView lw = ((AlertDialog) goTo).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                        String script = curr.getScript();
                        String [] oldScript;
                        if (script != null) {
                            oldScript = script.split(" ");
                        } else {
                            oldScript = null;
                        }

                        String [] trigger = triggers[groupPosition].split(" ");
                        String newScript = "";
                        boolean didUpdate = false;

                        if (oldScript != null) {
                            for (int i = 0; i < oldScript.length - 3; i++) {
                                if (oldScript[i].equals(trigger[0]) && oldScript[i + 1].equals(trigger[1]) && oldScript[i + 2].equals("goto")) {
                                    oldScript[i + 3] = checkedItem.toString() + ";";
                                    didUpdate = true;
                                }
                            }
                        }


                        if (!didUpdate) {
                            if (script != null) {
                                newScript = script + " " + triggers[groupPosition] + " goto " + checkedItem.toString() + ";";
                            } else {
                                newScript = " " + triggers[groupPosition] + " goto " + checkedItem.toString() + ";";
                            }

                        } else {
                            for (int i = 0; i < oldScript.length; i++) {
                                newScript += oldScript[i] + " ";
                            }
                        }

                        curr.setScript(newScript);

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

    private void showPlaySoundDialog(final int groupPosition) {
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

                final Shape curr = currPage.getShapes().get(selectedShape);

                Button ok = playSound.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ListView lw = ((AlertDialog) playSound).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                        String script = curr.getScript();
                        String [] oldScript;
                        if (script != null) {
                            oldScript = script.split(" ");
                            System.out.println(Arrays.toString(oldScript));
                        } else {
                            oldScript = null;
                        }

                        String [] trigger = triggers[groupPosition].split(" ");
                        String newScript = "";
                        boolean didUpdate = false;

                        if (oldScript != null) {
                            for (int i = 0; i < oldScript.length - 3; i++) {
                                if (oldScript[i].equals(trigger[0]) && oldScript[i + 1].equals(trigger[1]) && oldScript[i + 2].equals("play")) {
                                    oldScript[i + 3] = checkedItem.toString();
                                    didUpdate = true;
                                }
                            }
                        }


                        if (!didUpdate) {
                            if (script != null) {
                                newScript = script + " " + triggers[groupPosition] + " play " + checkedItem.toString() + ";";
                            } else {
                                newScript = " " + triggers[groupPosition] + " play " + checkedItem.toString() + ";";
                            }

                        } else {
                            for (int i = 0; i < oldScript.length; i++) {
                                newScript += oldScript[i] + " ";
                            }
                        }

                        curr.setScript(newScript);
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

    private void showVisiblityDialog(final int groupPosition) {
        final AlertDialog.Builder visibilityDialog = new AlertDialog.Builder(GameEditor.this);
        visibilityDialog.setTitle("Choose the visibility:");

        visibilityDialog.setPositiveButton("Ok", null);
        visibilityDialog.setNegativeButton("Cancel", null);

        // TO DO: replace with the current visibility
        visibilityDialog.setSingleChoiceItems( new String[] {"hide", "show"}, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        final Shape curr = currPage.getShapes().get(selectedShape);

        final AlertDialog visible = visibilityDialog.create();
        visible.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = visible.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ListView lw = ((AlertDialog) visible).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                        String script = curr.getScript();
                        String [] oldScript;
                        if (script != null) {
                            oldScript = script.split(" ");
                            System.out.println(Arrays.toString(oldScript));
                        } else {
                            oldScript = null;
                        }

                        String [] trigger = triggers[groupPosition].split(" ");
                        String newScript = "";
                        boolean didUpdate = false;

                        if (oldScript != null) {
                            for (int i = 0; i < oldScript.length - 3; i++) {
                                if (oldScript[i].equals(trigger[0]) && oldScript[i + 1].equals(trigger[1]) && oldScript[i + 2].equals(checkedItem.toString())) {
                                    oldScript[i + 3] = curr.getName();
                                    didUpdate = true;
                                }
                            }
                        }


                        if (!didUpdate) {
                            if (script != null) {
                                newScript = script + " " + triggers[groupPosition] + " " + checkedItem.toString() + " " + curr.getName() + ";";
                            } else {
                                newScript = triggers[groupPosition] + " " + checkedItem.toString() + " " + curr.getName() + ";";
                            }

                        } else {
                            for (int i = 0; i < oldScript.length; i++) {
                                newScript += oldScript[i] + " ";
                            }
                        }

                        curr.setScript(newScript);
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
        final Shape curr = currPage.getShapes().get(selectedShape);
        scriptDialog.setMessage(curr.getScript());

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

        RelativeLayout objNameHeader = findViewById(R.id.obj_name_header);

        objNameHeader.setVisibility(view.VISIBLE);

        LinearLayout editableObjName = findViewById(R.id.editable_obj_name);

        editableObjName.setVisibility(view.GONE);

        EditText newObjName = findViewById(R.id.change_obj_name);

        final Shape curr = currPage.getShapes().get(selectedShape);
        curr.setName(newObjName.getText().toString());

        TextView objName = findViewById(R.id.obj_name);
        objName.setText(curr.getName());
    }

    public void deleteObject(View view) {
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(GameEditor.this);
        deleteDialog.setTitle("Delete");

        deleteDialog.setMessage("Are you sure you want to delete?");

        deleteDialog.setPositiveButton("Delete", null);
        deleteDialog.setNegativeButton("Cancel", null);

        final Shape curr = currPage.getShapes().get(selectedShape);

        final AlertDialog delete = deleteDialog.create();
        delete.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button yes = delete.getButton(AlertDialog.BUTTON_POSITIVE);
                yes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TO DO: delete object
                        // delete from array list
                        currPage.getShapes().remove(curr);
                        selectedShape = -1;

                        LinearLayout objProperties = findViewById(R.id.obj_properties);
                        objProperties.setVisibility(v.GONE);

                        TextView clickObj = findViewById(R.id.click_obj);
                        clickObj.setVisibility(v.VISIBLE);

                        CanvasView canvasView = findViewById(R.id.canvas);
                        canvasView.invalidate();
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
                        // remove from the arraylist
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
        final Shape curr = currPage.getShapes().get(selectedShape);

        Toast saveToast = Toast.makeText(getApplicationContext(), "Saved " + curr.getName(), Toast.LENGTH_SHORT);

        saveToast.show();

        // TO DO: verify save
        // loadIntoDatabaseFile
    }

    public void savePage(MenuItem item) {

        Toast saveToast = Toast.makeText(getApplicationContext(), "Saved " + currPage.getPageName(), Toast.LENGTH_SHORT);

        saveToast.show();

        // TO DO: verify save
        // loadIntoDatabaseFile
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
                        // validate that it is a unique page name
                        // verify that there are no spaces
                        EditText newPageName = ((AlertDialog) pageName).findViewById(R.id.editable_page_name);

                        currPage.setPageName(newPageName.getText().toString());


                        getSupportActionBar().setTitle(currPage.getPageName());


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

    static public Page getCurrPage() {
        return currPage;
    }

    static public void setSelectedShaped(int i) {
        selectedShape = i;
    }
}

package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
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
    String gameName;
    private String triggers[] = {"on click", "on enter", "on drop", "property" };
    private String scriptActions[] = {"goto", "play", "hide", "show"};
    private String[][] actions = { scriptActions, scriptActions, scriptActions, {"Set Property"} };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_editor);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        int pageID = extras.getInt(PageDirectory.PAGE_ID);
        gameName = extras.getString(PageDirectory.GAME);
        ArrayList<Page> allPages = Page.getPages();
        currPage = allPages.get(pageID - 1);

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
                        } else if (groupPosition == 2) {
                            showDropDialogs(action, groupPosition, childPosition);
                        } else {
                            showTriggerDialogs(action, groupPosition, childPosition);
                        }

                        return false;
                    }
                }
        );

        Map<String, BitmapDrawable> resources = Shape.getDrawables(this);
        drawResources(resources);

        selectedResource = -1;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowHeight = displayMetrics.heightPixels;
        int windowWidth = displayMetrics.widthPixels;

        LinearLayout.LayoutParams resourceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, windowHeight / 4);
        HorizontalScrollView resourcePanel = findViewById(R.id.resource_panel);
        resourcePanel.setLayoutParams(resourceParams);

        FrameLayout.LayoutParams rightpanelParams = new FrameLayout.LayoutParams(windowWidth / 5, FrameLayout.LayoutParams.MATCH_PARENT);
        LinearLayout rightPanel = findViewById(R.id.right_panel);
        rightPanel.setLayoutParams(rightpanelParams);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_game_editor, menu);

        MenuItem propertyItem = menu.findItem(R.id.right_panel_visibility);
        Spinner propertiesSpinner = (Spinner) propertyItem.getActionView();

        ArrayAdapter<CharSequence> propertiesAdapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, android.R.layout.simple_spinner_item);
        propertiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        propertiesSpinner.setAdapter(propertiesAdapter);

        propertiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override

            public void onItemSelected(AdapterView adapterView, View view, int i, long l) {
                String selected = adapterView.getSelectedItem().toString();
                LinearLayout propertiesView = findViewById(R.id.right_panel);

                if (selected.equals("Show Left")) {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(225, FrameLayout.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.LEFT;
                    propertiesView.setLayoutParams(params);
                    propertiesView.setVisibility(view.VISIBLE);
                } else if (selected.equals("Show Right")) {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(225, FrameLayout.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.RIGHT;
                    propertiesView.setLayoutParams(params);
                    propertiesView.setVisibility(view.VISIBLE);
                } else if (selected.equals("Hide")) {
                    propertiesView.setVisibility(view.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });

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
    
    private void showDropDialogs(String action, int groupPosition, int childPosition) {
        if (action.equals(scriptActions[0])) {

            showOnDropDialog(groupPosition, childPosition, "Choose the page to go to:", "Choose page:", getShapeNames(), getPageNames());

        } else if (action.equals(scriptActions[1])) {

            showOnDropDialog(groupPosition, childPosition, "Choose the sound to play:", "Choose sound:", getShapeNames(), Shape.getSounds());

        } else if (action.equals(scriptActions[2]) || action.equals(scriptActions[3])) {

            showOnDropDialog(groupPosition, childPosition, "Choose the shape to " + scriptActions[childPosition] + ":", "Choose shape:", getShapeNames(), getShapeNames());

        }
    }

    private void showOnDropDialog(final int groupPosition, final int childPosition, String title, String message, String[] shapeNames, String[] arrayNames) {
        final AlertDialog.Builder triggerDialog = new AlertDialog.Builder(GameEditor.this);
        triggerDialog.setTitle(title);
        triggerDialog.setView(R.layout.ondrop_dialog);

        View holder = View.inflate(this, R.layout.ondrop_dialog, null);
        triggerDialog.setView(holder);

        final Spinner shapeSpinner = (Spinner) holder.findViewById(R.id.shape_options);

        ArrayAdapter<String> shapedataAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, shapeNames);
        shapedataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shapeSpinner.setAdapter(shapedataAdapter);

        final Spinner otherSpinner = (Spinner) holder.findViewById(R.id.other_options);

        ArrayAdapter<String> otherdataAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, arrayNames);
        otherdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        otherSpinner.setAdapter(otherdataAdapter);

        TextView otherText = (TextView) holder.findViewById(R.id.other_name);
        otherText.setText(message);


        triggerDialog.setPositiveButton("Ok", null);
        triggerDialog.setNegativeButton("Cancel", null);

        if (scriptActions[childPosition].equals("play")) {
            triggerDialog.setNeutralButton("Play", null);
        }

        final Shape curr = currPage.getShapes().get(selectedShape);

        final AlertDialog triggerD = triggerDialog.create();
        triggerD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = triggerD.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String shape = shapeSpinner.getSelectedItem().toString();
                        String other = otherSpinner.getSelectedItem().toString();


                        String script = curr.getScript();
                        String newScript;

                        if (script != null) {
                            newScript = script + " " + triggers[groupPosition] + " " + shape + " " + scriptActions[childPosition] + " " + other + ";";
                        } else {
                            newScript = " " + triggers[groupPosition] + " " + shape + " " + scriptActions[childPosition] + " " + other + ";";
                        }

                        curr.setScript(newScript);

                        triggerD.dismiss();
                    }
                });

                Button cancel = triggerD.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        triggerD.dismiss();
                    }
                });

                if (scriptActions[childPosition].equals("play")) {
                    Button play = triggerD.getButton(AlertDialog.BUTTON_NEUTRAL);
                    play.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            String other = otherSpinner.getSelectedItem().toString();
                            Shape.playAudio(getApplicationContext(), other);
                        }
                    });
                }
            }
        });

        triggerD.show();

    }

    private String[] getPageNames() {
        ArrayList<Page> allPages = Page.getPages();

        String[] pageNames = new String[allPages.size()];

        for(int i = 0; i < allPages.size(); i++) {
            pageNames[i] = allPages.get(i).getPageName();
        }

        return pageNames;
    }

    private String[] getShapeNames() {
        ArrayList<Shape> allShapes = Shape.getAllShapes();

        String[] shapeNames = new String[allShapes.size()];

        for (int i = 0; i < allShapes.size(); i++) {
            shapeNames[i] = allShapes.get(i).getName();
        }

        return shapeNames;
    }

    private void showTriggerDialogs(String action, int groupPosition, int childPosition) {

        if (action.equals(scriptActions[0])) {

            showDialog(groupPosition, childPosition, "Choose the page to go to:", getPageNames() );

        } else if (action.equals(scriptActions[1])) {

            showDialog(groupPosition, childPosition, "Choose the sound to play:", Shape.getSounds() );

        } else if (action.equals(scriptActions[2]) || action.equals(scriptActions[3])) {

            showDialog(groupPosition, childPosition, "Choose the shape to " + scriptActions[childPosition] + ":", getShapeNames() );

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
                            EditText x_val = (EditText) ((AlertDialog) property).findViewById(R.id.editable_X);
                            EditText y_val = (EditText) ((AlertDialog) property).findViewById(R.id.editable_Y);
                            EditText width = (EditText) ((AlertDialog) property).findViewById(R.id.editable_width);
                            EditText height = (EditText) ((AlertDialog) property).findViewById(R.id.editable_height);
                            EditText newText = (EditText) ((AlertDialog) property).findViewById(R.id.editable_text_string);
                            Switch isMovable = (Switch) ((AlertDialog) property).findViewById(R.id.is_movable);
                            Switch isHidden = (Switch) ((AlertDialog) property).findViewById(R.id.is_hidden);


                            curr.setX(Float.parseFloat(x_val.getText().toString()));
                            curr.setY(Float.parseFloat(y_val.getText().toString()));

                            curr.setMovable(isMovable.isChecked());
                            curr.setVisible(!isHidden.isChecked());
                            curr.setShapeText(newText.getText().toString());

                            String imgName = curr.getImgName();
                            if (!imgName.equals("texticon")) {
                                curr.setWidth(Float.parseFloat(width.getText().toString()));
                                curr.setHeight(Float.parseFloat(height.getText().toString()));
                            }

                            // text object
                            if (!curr.getText().isEmpty()) {
                                // setting font size
                                EditText fontSize = (EditText) ((AlertDialog) property).findViewById(R.id.font_size);
                                curr.getShapeText().setFontSize(Integer.parseInt(fontSize.getText().toString()));
                                curr.setWidth(curr.getText().length() * curr.getShapeText().getFontSize() / 2);
                                curr.setHeight(curr.getShapeText().getFontSize());
                            }


                            CanvasView canvasView = findViewById(R.id.canvas);
                            canvasView.invalidate();

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
            EditText newText = (EditText) ((AlertDialog) property).findViewById(R.id.editable_text_string);

            LinearLayout textLayout = ((AlertDialog) property).findViewById(R.id.textLayout);
            LinearLayout hwLayout = ((AlertDialog) property).findViewById(R.id.height_width_layout);
            LinearLayout fontLayout = ((AlertDialog) property).findViewById(R.id.font_layout);
            EditText fontSize = (EditText) ((AlertDialog) property).findViewById(R.id.font_size);

            String imgName = curr.getImgName();
            if (imgName.equals("texticon")) {
                textLayout.setVisibility(View.VISIBLE);
                fontLayout.setVisibility(View.VISIBLE);
                hwLayout.setVisibility(View.GONE);
                fontSize.setText(String.valueOf((curr.getShapeText().getFontSize())));
            } else {
                textLayout.setVisibility(View.GONE);
                fontLayout.setVisibility(View.GONE);
                hwLayout.setVisibility(View.VISIBLE);
            }

            isMovable.setChecked(curr.isMovable());
            isHidden.setChecked(!curr.isVisible());
            x_val.setText(String.valueOf(curr.getX()));
            y_val.setText(String.valueOf(curr.getY()));
            width.setText(String.valueOf(curr.getWidth()));
            height.setText(String.valueOf(curr.getHeight()));
            if (imgName.equals("texticon")) newText.setText(curr.getText());

        }
    }

    private void showDialog(final int groupPosition, final int childPosition, String message, String[] arrayNames) {
        final AlertDialog.Builder triggerDialog = new AlertDialog.Builder(GameEditor.this);
        triggerDialog.setTitle(message);

        triggerDialog.setPositiveButton("Ok", null);
        triggerDialog.setNegativeButton("Cancel", null);

        // TO DO: set the checkedItem to the current item that it goes to

        if (scriptActions[childPosition].equals("play")) {
            triggerDialog.setNeutralButton("Play", null);
        }

        triggerDialog.setSingleChoiceItems( arrayNames, 0 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        final Shape curr = currPage.getShapes().get(selectedShape);

        final AlertDialog triggerD = triggerDialog.create();
        triggerD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = triggerD.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ListView lw = ((AlertDialog) triggerD).getListView();
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

                        System.out.println(childPosition);

                        if (oldScript != null) {
                            for (int i = 0; i < oldScript.length - 3; i++) {
                                if (oldScript[i].equals(trigger[0]) && oldScript[i + 1].equals(trigger[1]) && oldScript[i + 2].equals(scriptActions[childPosition])) {
                                    oldScript[i + 3] = checkedItem.toString() + ";";
                                    didUpdate = true;
                                }
                            }
                        }


                        if (!didUpdate) {
                            if (script != null) {
                                newScript = script + " " + triggers[groupPosition] + " " + scriptActions[childPosition] + " " + checkedItem.toString() + ";";
                            } else {
                                newScript = " " + triggers[groupPosition] + " " + scriptActions[childPosition] + " " + checkedItem.toString() + ";";
                            }

                        } else {
                            for (int i = 0; i < oldScript.length; i++) {
                                newScript += oldScript[i] + " ";
                            }
                        }

                        curr.setScript(newScript);

                        triggerD.dismiss();
                    }
                });

                Button cancel = triggerD.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                        triggerD.dismiss();
                    }
                });

                if (scriptActions[childPosition].equals("play")) {
                    Button play = triggerD.getButton(AlertDialog.BUTTON_NEUTRAL);
                    play.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            ListView lw = ((AlertDialog) triggerD).getListView();
                            Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            Shape.playAudio(getApplicationContext(), checkedItem.toString());
                        }
                    });
                }
            }
        });

        triggerD.show();
    }

    public void showScript(View view) {
        final AlertDialog.Builder scriptDialog = new AlertDialog.Builder(GameEditor.this);
        scriptDialog.setTitle("Current script:");

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
                        currPage.getShapes().remove(curr);
                        selectedShape = -1;

                        CanvasView.setSelectedShape(selectedShape);

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
                        // TODO: CHECK IF THERES ONLY ONE PAGE?
                        ArrayList<Page> allPages = Page.getPages();
                        int removed = allPages.indexOf(currPage);
                        int removedID = currPage.getPageID();
                        allPages.remove(currPage);

                        for (int i = removed; i < allPages.size(); i++) {
                            Page curr = allPages.get(i);
                            curr.setPageID(removedID);

                            removedID++;
                        }

                        onBackPressed();
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

    public void savePage(MenuItem item) {

        Toast saveToast = Toast.makeText(getApplicationContext(), "Saved " + gameName, Toast.LENGTH_SHORT);

        saveToast.show();

        Page.loadIntoDatabaseFile(this, gameName);
    }

    public void editPageName(MenuItem item) {
        final AlertDialog.Builder pageNameDialog = new AlertDialog.Builder(GameEditor.this);
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
                        // TODO: validate that it is a unique page name, verify that there are no spaces
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

    public void resetScript(View view) {
        final Shape curr = currPage.getShapes().get(selectedShape);

        Toast resetToast = Toast.makeText(getApplicationContext(), "Reset script of " + curr.getName(), Toast.LENGTH_SHORT);

        resetToast.show();

        curr.setScript("");
    }
}

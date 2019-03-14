package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    String isError = "unchecked";
    static float MINIMUM_SIZE = 30;
    int windowWidth, windowHeight;

    static float RESOURCE_BOUNDARY = 0;
    static float RESOURCE_OFFSET = 30;
    static int actionBarHeight;


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
        windowHeight = displayMetrics.heightPixels;
        windowWidth = displayMetrics.widthPixels;

        CanvasView.setWindowHeight(windowHeight);
        CanvasView.setWindowWidth(windowWidth);

        LinearLayout.LayoutParams resourceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, windowHeight / 4);
        HorizontalScrollView resourcePanel = findViewById(R.id.resource_panel);
        resourcePanel.setLayoutParams(resourceParams);

        FrameLayout.LayoutParams rightpanelParams = new FrameLayout.LayoutParams(windowWidth / 5, FrameLayout.LayoutParams.MATCH_PARENT);
        LinearLayout rightPanel = findViewById(R.id.right_panel);
        rightPanel.setLayoutParams(rightpanelParams);

        TypedValue tv = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);

        RESOURCE_BOUNDARY = windowHeight * 3.0f / 4.0f - actionBarHeight - RESOURCE_OFFSET;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_game_editor, menu);

        MenuItem errorButton = menu.findItem(R.id.error_button);
        Button error = (Button) errorButton.getActionView();
        error.setText(R.string.check_errors);
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> invalidStates = runErrorTest();
                showStatus(invalidStates);
            }
        });

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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem errorIcon = menu.findItem(R.id.error_icon);
        if (isError.equals("unchecked")) {
            errorIcon.setIcon(R.drawable.ic_refresh_black_24dp);
        } else if (isError.equals("errors")) {
            errorIcon.setIcon(R.drawable.ic_error_black_24dp);
        } else if (isError.equals("no errors")) {
            errorIcon.setIcon(R.drawable.ic_check_circle_black_24dp);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void showStatus(ArrayList<String> invalidStates) {
        // isError corresponds to currently displayed item
        final AlertDialog.Builder errorIconDialog = new AlertDialog.Builder(GameEditor.this);
        errorIconDialog.setTitle("Current error status:");

        String message = "";
        switch (isError) {
            case "unchecked": // currently unreachable since they have to validate game to see dialog
                message = "Check Errors to validate current state of game.";
                break;
            case "errors":
                message = "The following issues were detected: ";
                for (String invalidState : invalidStates) {
                    message += "\n\n" + invalidState;
                }
                break;
            case "no errors":
                message = "No errors were detected. Safe to save.";
                break;
        }
        errorIconDialog.setMessage(message);

        errorIconDialog.setPositiveButton("OK", null);
        final AlertDialog errorIconD = errorIconDialog.create();
        errorIconD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = errorIconD.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        errorIconD.dismiss();
                    }
                });
            }
        });

        errorIconD.show();
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

                        String triggerS = triggers[groupPosition];
                        String actionS = scriptActions[childPosition];

                        String script = curr.getScript();

                        String newScript;

                        if (script != null) {
                            newScript = script + " " + triggerS + " " + shape + " " + actionS + " " + other + ";";
                        } else {
                            newScript = " " + triggerS + " " + shape + " " + actionS + " " + other + ";";
                        }

                        String onClick = "on click ";
                        String onEnter = "on enter ";
                        String onDrop = "";
                        Map<String, String> onDropDict = new HashMap<String, String>();

                        String[] splitNewScript = newScript.split(";");

                        for (int i = 0; i < splitNewScript.length; i++) {
                            String currScript = splitNewScript[i];
                            if (currScript.contains(triggers[0])) {
                                int index = currScript.indexOf(triggers[0]);
                                String portion = currScript.substring(index + triggers[0].length() + 1);
                                onClick += portion + " ";
                            } else if (currScript.contains(triggers[1])) {
                                int index = currScript.indexOf(triggers[1]);
                                String portion = currScript.substring(index + triggers[1].length() + 1);
                                onEnter += portion + " ";
                            } else if (currScript.contains(triggers[2])) {
                                int indexTrigger = currScript.indexOf(triggers[2]);
                                String portion = currScript.substring(indexTrigger + triggers[2].length() + 1);
                                System.out.println("portion " + portion );

                                String[] portionSplit = portion.split(" ");
                                String currShape = portionSplit[0];
                                int indexShape = portion.indexOf(currShape);
                                System.out.println("currShape " + currShape);
                                System.out.println(onDropDict.get(currShape));

                                String correctPortion = (onDropDict.get(currShape) != null ? onDropDict.get(currShape) : "") + " " + portion.substring(indexShape + currShape.length() + 1);
                                System.out.println("correctPortion " + correctPortion);

                                onDropDict.put(currShape, correctPortion);
                                for (String curr: onDropDict.keySet()) {
                                    System.out.println(curr);
                                    System.out.println(onDropDict.get(curr));
                                }
                            }
                        }

                        String finalScript = "";

                        for (String currShape: onDropDict.keySet()) {
                            onDrop += (onDrop.equals("") ? "" : " ") + "on drop " + currShape + onDropDict.get(currShape) + ";";
                            System.out.println("onDrop " + onDrop);
                        }
                        finalScript += !onDrop.equals("") ? " " + onDrop.trim(): "";

                        finalScript += !onClick.equals("on click ") ? " " + onClick.trim() + ";" : "";
                        finalScript += !onEnter.equals("on enter ") ? " " + onEnter.trim() + ";" : "";


                        curr.setScript(finalScript.trim());

                        // since we updated a script, let user know that they should check for errors
                        isError = "unchecked";
                        invalidateOptionsMenu();

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

                            //EditText color = (EditText) ((AlertDialog) property).findViewById(R.id.textColor);
                            CheckBox bold = (CheckBox) ((AlertDialog) property).findViewById(R.id.boldOption);
                            CheckBox italic = (CheckBox) ((AlertDialog) property).findViewById(R.id.italicOption);
                            CheckBox ul = (CheckBox) ((AlertDialog) property).findViewById(R.id.underlineOption);
                            RadioGroup rg = (RadioGroup) ((AlertDialog) property).findViewById(R.id.color_options);

                            curr.setMovable(isMovable.isChecked());
                            curr.setVisible(!isHidden.isChecked());
                            curr.setShapeText(newText.getText().toString());


                            String imgName = curr.getImgName();
                            if (!imgName.equals("texticon")) {

                                float possWidth = Float.parseFloat(width.getText().toString());
                                float possHeight = Float.parseFloat(height.getText().toString());

                                if (possWidth > 0) {
                                    curr.setWidth(possWidth);
                                } else {
                                    Toast widthToast = Toast.makeText(getApplicationContext(), "Width cannot be 0", Toast.LENGTH_SHORT);
                                    widthToast.show();
                                    curr.setWidth(MINIMUM_SIZE);
                                }

                                if (possHeight > 0) {
                                    curr.setHeight(possHeight);
                                } else {
                                    Toast heightToast = Toast.makeText(getApplicationContext(), "Height cannot be 0", Toast.LENGTH_SHORT);
                                    heightToast.show();
                                    curr.setHeight(MINIMUM_SIZE);
                                }


                            }

                            float actualWidth = curr.getWidth();
                            float actualHeight = curr.getHeight();
                            float possX = Float.parseFloat(x_val.getText().toString());
                            float possY = Float.parseFloat(y_val.getText().toString());

                            if (possX >= 0 && possX + actualWidth <= windowWidth) {
                                curr.setX(possX);
                            } else {
                                Toast xToast = Toast.makeText(getApplicationContext(), "x value is out of bounds", Toast.LENGTH_SHORT);
                                xToast.show();
                            }

                            if (possY >= 0 && possY + actualHeight <= RESOURCE_BOUNDARY) {
                                curr.setY(possY);
                            } else {
                                Toast yToast = Toast.makeText(getApplicationContext(), "y value is out of bounds", Toast.LENGTH_SHORT);
                                yToast.show();
                            }


                            // text object
                            if (!curr.getText().isEmpty()) {
                                // setting font size
                                EditText fontSize = (EditText) ((AlertDialog) property).findViewById(R.id.font_size);
                                curr.getShapeText().setFontSize(Integer.parseInt(fontSize.getText().toString()));
                                curr.setWidth(curr.getText().length() * curr.getShapeText().getFontSize() / 2);
                                curr.setHeight(curr.getShapeText().getFontSize());

                                //int col = Integer.parseInt(color.getText().toString());
                                int col = Color.BLACK;
                                int id = rg.getCheckedRadioButtonId();
                                if (id  == R.id.red) {col = Color.RED; }
                                else if (id  == R.id.yellow) {col = Color.YELLOW; }
                                else if (id  == R.id.green) {col = Color.GREEN; }
                                else if (id  == R.id.blue) {col = Color.BLUE; }
                                else if (id  == R.id.magenta) {col = Color.MAGENTA; }
                                else if (id  == R.id.gray) {col = Color.GRAY; }

                                curr.getShapeText().setTColor(col);
                                curr.getShapeText().setBold(bold.isChecked());
                                curr.getShapeText().setItalic(italic.isChecked());
                                curr.getShapeText().setUnderline(ul.isChecked());
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
            LinearLayout colorLayout = ((AlertDialog) property).findViewById(R.id.colorLayout);
            EditText fontSize = (EditText) ((AlertDialog) property).findViewById(R.id.font_size);
            RadioGroup rg = (RadioGroup) ((AlertDialog) property).findViewById(R.id.color_options);
            CheckBox bold = (CheckBox) ((AlertDialog) property).findViewById(R.id.boldOption);
            CheckBox italic = (CheckBox) ((AlertDialog) property).findViewById(R.id.italicOption);
            CheckBox ul = (CheckBox) ((AlertDialog) property).findViewById(R.id.underlineOption);

            String imgName = curr.getImgName();
            if (imgName.equals("texticon")) {
                textLayout.setVisibility(View.VISIBLE);
                fontLayout.setVisibility(View.VISIBLE);
                colorLayout.setVisibility(View.VISIBLE);
                hwLayout.setVisibility(View.GONE);
                fontSize.setText(String.valueOf((curr.getShapeText().getFontSize())));

                if(curr.getShapeText().getTColor() == Color.RED) rg.check(R.id.red);
                else if(curr.getShapeText().getTColor() == Color.YELLOW) rg.check(R.id.yellow);
                else if(curr.getShapeText().getTColor() == Color.GREEN) rg.check(R.id.green);
                else if(curr.getShapeText().getTColor() == Color.BLUE) rg.check(R.id.blue);
                else if(curr.getShapeText().getTColor() == Color.MAGENTA) rg.check(R.id.magenta);
                else if(curr.getShapeText().getTColor() == Color.GRAY) rg.check(R.id.gray);
                else rg.check(R.id.black);

                bold.setChecked(curr.getShapeText().getBold());
                italic.setChecked(curr.getShapeText().getItalic());
                ul.setChecked(curr.getShapeText().getUnderline());



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

                        String triggerS = triggers[groupPosition];
                        String actionS = scriptActions[childPosition];

                        String script = curr.getScript();
                        String [] oldScript;
                        if (script != null) {
                            oldScript = script.split(" ");
                        } else {
                            oldScript = null;
                        }

                        String [] trigger = triggerS.split(" ");
                        String newScript = "";
                        boolean didUpdate = false;
                        boolean sawTrigger = false;


                        if (oldScript != null) {
                            for (int i = 0; i < oldScript.length - 1; i++) {
                                if (oldScript[i].equals(trigger[0]) && oldScript[i + 1].equals(trigger[1])) {
                                    sawTrigger = true;
                                }

                                if (oldScript[i].contains(";")) {
                                    sawTrigger = false;
                                }



                                if ( sawTrigger && oldScript[i].equals(actionS)) {
                                    if (oldScript[i + 1].contains(";")) {
                                        oldScript[i + 1] = checkedItem.toString() + ";";
                                    } else {
                                        oldScript[i + 1] = checkedItem.toString();
                                    }
                                    didUpdate = true;
                                }
                            }
                        }


                        if (!didUpdate) {
                            if (script != null) {
                                newScript = script + " " + triggerS + " " + actionS + " " + checkedItem.toString() + ";";
                            } else {
                                newScript = " " + triggerS + " " + actionS + " " + checkedItem.toString() + ";";
                            }

                        } else {
                            for (int i = 0; i < oldScript.length; i++) {
                                newScript += oldScript[i] + " ";
                            }
                        }

                        String onClick = "on click ";
                        String onEnter = "on enter ";
                        String onDrop = "on drop ";

                        String[] splitNewScript = newScript.split(";");

                        for (int i = 0; i < splitNewScript.length; i++) {
                            String currScript = splitNewScript[i];
                            if (currScript.contains(triggers[0])) {
                                int index = currScript.indexOf(triggers[0]);
                                String portion = currScript.substring(index + triggers[0].length() + 1);
                                onClick += portion + " ";
                            } else if (currScript.contains(triggers[1])) {
                                int index = currScript.indexOf(triggers[1]);
                                String portion = currScript.substring(index + triggers[1].length() + 1);
                                onEnter += portion + " ";
                            } else if (currScript.contains(triggers[2])) {
                                int index = currScript.indexOf(triggers[2]);
                                String portion = currScript.substring(index + triggers[2].length() + 1);
                                onDrop += portion + " ";
                            }
                        }

                        String finalScript = "";

                        finalScript += !onDrop.equals("on drop ") ? " " + onDrop.trim() + ";" : "";
                        finalScript += !onClick.equals("on click ") ? " " + onClick.trim() + ";" : "";
                        finalScript += !onEnter.equals("on enter ") ? " " + onEnter.trim() + ";" : "";



                        curr.setScript(finalScript.trim());

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

    private boolean objExists(String obj) {
        ArrayList<Shape> allShapes = currPage.getShapes();
        for (Shape s : allShapes) {
            if (obj.equals(s.getName())) {
                return true;
            }
        }

        return false;
    }

    public void changeObjName(View view) {

        EditText newObjName = findViewById(R.id.change_obj_name);
        String name = newObjName.getText().toString();

        if (!objExists(name) && !name.contains(" ") && !name.equals("")) {
            final Shape curr = currPage.getShapes().get(selectedShape);
            curr.setName(name);

            TextView objName = findViewById(R.id.obj_name);
            objName.setText(curr.getName());

            RelativeLayout objNameHeader = findViewById(R.id.obj_name_header);

            objNameHeader.setVisibility(view.VISIBLE);

            LinearLayout editableObjName = findViewById(R.id.editable_obj_name);

            editableObjName.setVisibility(view.GONE);

            TextView errorMsg = findViewById(R.id.error_shape);
            errorMsg.setVisibility(view.GONE);

        } else {

            TextView errorMsg = findViewById(R.id.error_shape);
            errorMsg.setVisibility(view.VISIBLE);

            if (objExists(name)) {
                errorMsg.setText("Shape with that name already exists.");
            } else if (name.contains(" ")) {
                errorMsg.setText("Shape names cannot have spaces.");
            } else if (name.equals("")) {
                errorMsg.setText("Shape names cannot be empty.");
            }
        }


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
                        // make sure to remove from all shapes arraylist as well
                        Shape.getAllShapes().remove(curr);
                        selectedShape = -1;

                        // since we deleted a shape, let user know that they should check for errors
                        isError = "unchecked";
                        invalidateOptionsMenu();

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
                        ArrayList<Page> allPages = Page.getPages();
                        int removed = allPages.indexOf(currPage);
                        int removedID = currPage.getPageID();

                        if (allPages.size() > 1) {
                            allPages.remove(currPage);

                            for (int i = removed; i < allPages.size(); i++) {
                                Page curr = allPages.get(i);
                                curr.setPageID(removedID);

                                removedID++;
                            }

                            onBackPressed();
                            delete.dismiss();
                        } else {
                            delete.setMessage("Unable to delete the only remaining page.");
                        }

                        // since we deleted a page, let user know that they should check for errors
                        isError = "unchecked";
                        invalidateOptionsMenu();

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

        if (isError.equals("no errors")) {
            Toast saveToast = Toast.makeText(getApplicationContext(), "Saved " + gameName, Toast.LENGTH_SHORT);

            saveToast.show();

            Page.loadIntoDatabaseFile(this, gameName);
        } else if (isError.equals("errors")) {
            Toast errorToast = Toast.makeText(getApplicationContext(), "Unable to save due to errors", Toast.LENGTH_SHORT);

            errorToast.show();
        } else if (isError.equals("unchecked")) {
            Toast testToast = Toast.makeText(getApplicationContext(), "Please run the test before saving", Toast.LENGTH_SHORT);

            testToast.show();
        }



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
                        EditText newPageName = ((AlertDialog) pageName).findViewById(R.id.editable_page_name);
                        String name = newPageName.getText().toString();

                        if (!pageExists(name) && !name.contains(" ") && !name.equals("")) {
                            currPage.setPageName(name);
                            getSupportActionBar().setTitle(currPage.getPageName());
                            pageName.dismiss();
                        } else {
                            TextView errorMessage = ((AlertDialog) pageName).findViewById(R.id.error_message);
                            errorMessage.setVisibility(v.VISIBLE);

                            if (pageExists(name)) {
                                errorMessage.setText("Page with that name already exists.");
                            } else if (name.contains(" ")) {
                                errorMessage.setText("Page names cannot have spaces.");
                            } else if (name.equals("")) {
                                errorMessage.setText("Page names cannot be empty.");
                            }
                        }
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

    private boolean pageExists(String page) {
        ArrayList<Page> allPages = Page.getPages();
        for (Page p : allPages) {
            if (page.equals(p.getPageName())) {
                return true;
            }
        }

        return false;
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

    /*
    Runs an error test and updates the displayed icon.
    isError args (corresponds to different displayed icons):
        "unchecked" means the user has not yet ran an error check, or has made
        changes since last check.
        "no errors" means the user ran the error check and no errors were found
        "errors" means that errors were found
     */
    public ArrayList<String> runErrorTest() {
        ArrayList<Page> allPages = Page.getPages();
        Set<String> pageNames = new HashSet<>(Arrays.asList(getPageNames()));
        Set<String> shapeNames = new HashSet<>(Arrays.asList(getShapeNames()));
        Set<String> validActions = new HashSet<>(Arrays.asList(scriptActions));

        ArrayList<String> invalidStates = new ArrayList<>(); // reset old states and run test from scratch
        for (Page page : allPages) {
            for (Shape shape : page.getShapes()) {
                HashMap<String, String> commands = shape.getCommands();

                // validate on drop <shape-name> s.t. shape-name exists for current shape
                for (String trigger : commands.keySet()) {
                    Log.d("dog", "verifying... " + trigger);
                    if (trigger.startsWith("on drop")) {
                        String shapeName = trigger.substring("on drop".length() + 1);
                        Log.d("dog", shapeName + " is the shape name");
                        if (!shapeNames.contains(shapeName)) {
                            String invalidState = shape.getName() + " contains invalid script trigger <"
                                    + trigger + "> since shape " + shapeName + " does not exist.";
                            invalidStates.add(invalidState);
                        }
                    }
                }

                // validate each command in rest of script is valid (note that only on drop trigger
                // references other objects, and since we enforce correct triggers in UI design,
                // we know the other triggers are valid (onclick, onenter)
                for (String command : commands.values()) {
                    // iterate through diff pieces of command, note that this assumes
                    // page and shape names have no spaces
                    String prevCommand = ""; // will be used to differentiate b/w pages and shapes
                    for (String portion : command.split(" ")) {
                        // note that the script triggers and primitives should also be
                        // restricted as shape/page names!
                        if (validActions.contains(portion)) {
                            prevCommand = portion;
                        } else {
                            // note that prevCommand is guaranteed to be one of the following
                            // cases by design (actions come before page/shape)
                            if (prevCommand.equals("goto")) { // currently a page
                                if (!pageNames.contains(portion)) {
                                    String invalidState = shape.getName() + " contains invalid script action "
                                            + "goto <" + portion + "> since page " + portion + " does not exist.";
                                    invalidStates.add(invalidState);
                                }
                            } else if (prevCommand.equals("hide")) { // currently a shape
                                if (!shapeNames.contains(portion)) {
                                    String invalidState = shape.getName() + " contains invalid script action " +
                                            "hide <" + portion + "> since shape " + portion + " does not exist.";
                                    invalidStates.add(invalidState);
                                }
                            }
                            // Note: play corresponds to a sound, and those are guaranteed
                            //       to be valid given the way the UI is designed :)
                        }

                    }
                }
            }
        }

        // "unchecked" is initial default value and is set periodically
        // as the user makes changes to the pages and shapes
        if (invalidStates.isEmpty()) {
            isError = "no errors";
        } else {
            isError = "errors";
        }

        invalidateOptionsMenu();
        return invalidStates;
    }

    // Ability to change the color of the background [extension].
    public void changeBackground(MenuItem item) {
        switch(item.getItemId()) {
            case (R.id.redbg):
                currPage.setBackgroundImage("redsquare.png");
                break;
            case (R.id.orangebg):
                currPage.setBackgroundImage("orangesquare.png");
                break;
            case (R.id.yellowbg):
                currPage.setBackgroundImage("yellowsquare.jpg");
                break;
            case (R.id.greenbg):
                currPage.setBackgroundImage("greensquare.png");
                break;
            case (R.id.bluebg):
                currPage.setBackgroundImage("bluesquare.png");
                break;
            case (R.id.purplebg):
                currPage.setBackgroundImage("purplesquare.png");
                break;
            case (R.id.nobg):
                currPage.setBackgroundImage("");
            default:
                currPage.setBackgroundImage("");
        }

        CanvasView canvasView = findViewById(R.id.canvas); // make it redraw after they set it
        canvasView.invalidate();
        Toast toast = Toast.makeText(getApplicationContext(), "The background has been set!", Toast.LENGTH_SHORT);
        toast.show();
    }

    // TODO rena (note that you need the menuItem argument for
    // this to be registered as an onClick but you don't actually
    // have to do anything with it!)
    public void copyShape(MenuItem menuItem) {

    }

    public void cutShape(MenuItem menuItem) {

    }

    public void pasteShape(MenuItem menuItem) {

    }

    public void undoShapeDelete(MenuItem item) {

    }
}

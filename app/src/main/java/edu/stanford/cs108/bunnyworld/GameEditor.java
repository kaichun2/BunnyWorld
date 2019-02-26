package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameEditor extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_editor);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListGroups = ExpandableListData.getData();
        expandableListTitle = new ArrayList<String>(expandableListGroups.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitle, expandableListGroups);
        expandableListView.setAdapter(expandableListAdapter);
    }

}

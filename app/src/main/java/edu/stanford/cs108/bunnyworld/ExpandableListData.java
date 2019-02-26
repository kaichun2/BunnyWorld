package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListData {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListGroups = new HashMap<String, List<String>>();

        List<String> script_actions = new ArrayList<String>();
        script_actions.add("Go To");
        script_actions.add("Play Sound");
        script_actions.add("Show");
        script_actions.add("Hide");

        expandableListGroups.put("Set property", script_actions);
        expandableListGroups.put("On click", script_actions);
        expandableListGroups.put("On enter", script_actions);
        expandableListGroups.put("On drop", script_actions);

        return expandableListGroups;
    }
}

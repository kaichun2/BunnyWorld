package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] pageArray = {
                "Page 2",
                "Page 3"
        };

        Spinner gamePage = (Spinner) findViewById(R.id.gamePage);
        SpinnerAdapter gamePage_adapter = new ArrayAdapter<String>( this,R.layout.spinner_item,pageArray);
        gamePage.setAdapter(gamePage_adapter);
    }

    protected void startGame(View view) {
        Spinner gamePage = (Spinner) findViewById(R.id.gamePage);
        String pageNum = gamePage.getSelectedItem().toString();
        // To do: change to pageNum

    }

    protected void editGame(View view) {
        Spinner gamePage = (Spinner) findViewById(R.id.gamePage);
        String pageNum = gamePage.getSelectedItem().toString();
        // To do: change to pageNum
    }
}

package com.example.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    Button testButton;
    TextView textView;
    String path = ">>test_path<<";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        testButton = (Button) findViewById(R.id.testButton);
        textView = (TextView) findViewById(R.id.textView);

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(getString(R.string.chosen_path_extra), path);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        Set<String> paths = getSharedPreferences(MainActivity.PATHS_PREFS, MODE_PRIVATE).getStringSet("path_prefs", new HashSet<String>());
        for (String path : paths) {
            textView.append(path + "\n");
        }
    }

}

package com.example.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class GalleryActivity extends AppCompatActivity {

    public static final String RESULT_PATH_EXTRA = "result_path";
    Button testButton;
    String path = ">>test_path<<";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        testButton = (Button) findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_PATH_EXTRA, path);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}

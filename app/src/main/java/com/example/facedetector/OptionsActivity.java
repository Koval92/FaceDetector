package com.example.facedetector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class OptionsActivity extends AppCompatActivity {

    private SeekBar maxFacesSeekBaar;
    private EditText resultPathEditText;
    private Button applyPathButton;
    private Button resetPathButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        maxFacesSeekBaar = (SeekBar) findViewById(R.id.maxFacesSeekBar);
        resultPathEditText = (EditText) findViewById(R.id.resultPathEditText);
        applyPathButton = (Button) findViewById(R.id.applyPathButton);
        resetPathButton = (Button) findViewById(R.id.resetPathButton);

        resultPathEditText.setText(Utils.getResultPath());

        applyPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = resultPathEditText.getText().toString();
                Utils.setResultPath(path);
            }
        });

        resetPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.resetResultPath();
                resultPathEditText.setText(Utils.getResultPath());
            }
        });

        maxFacesSeekBaar.setProgress(Utils.getMaxFaces() - 1);
        maxFacesSeekBaar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Utils.setMaxFaces(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), "Max faces: " + Utils.getMaxFaces(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

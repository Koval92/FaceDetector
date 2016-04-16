package com.example.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    Button selectButton;
    Button deleteButton;
    Button previewButton;
    ListView pathsListView;
    TextView pathTextView;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        selectButton = (Button) findViewById(R.id.selectButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        previewButton = (Button) findViewById(R.id.previewButton);
        pathsListView = (ListView) findViewById(R.id.pathsListView);
        pathTextView = (TextView) findViewById(R.id.pathTextView);

        final Set<String> pathsSet = Utils.loadImagePaths(getApplicationContext());
        final List<String> paths = new ArrayList<String>(pathsSet);
        path = getIntent().getStringExtra(getString(R.string.chosen_path_extra));
        pathTextView.setText(getString(R.string.selected_path, path));

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, paths);
        pathsListView.setAdapter(adapter);

        pathsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                path = paths.get(position);
                pathTextView.setText(getString(R.string.selected_path, path));
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(getString(R.string.chosen_path_extra), path);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths.remove(path);
                pathsSet.remove(path);
                Utils.saveImagePaths(getApplicationContext(), pathsSet);
                adapter.notifyDataSetChanged();
                int pos = 0;
                pathsListView.performItemClick(pathsListView.getChildAt(pos), pos, pathsListView.getItemIdAtPosition(pos));
            }
        });

        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

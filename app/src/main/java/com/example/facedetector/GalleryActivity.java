package com.example.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    Button selectButton;
    Button deleteButton;
    ListView pathsListView;
    TextView pathTextView;
    ImageView previewImageView;
    String path;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals(getString(R.string.options))) {
            Intent intent = new Intent(getApplicationContext(), OptionsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        selectButton = (Button) findViewById(R.id.selectButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        pathsListView = (ListView) findViewById(R.id.pathsListView);
        pathTextView = (TextView) findViewById(R.id.pathTextView);
        previewImageView = (ImageView) findViewById(R.id.previewImageView);

        final Set<String> pathsSet = Utils.loadImagePaths(getApplicationContext());
        final List<String> paths = new ArrayList<String>(pathsSet);
        path = getIntent().getStringExtra(getString(R.string.chosen_path_extra));
        updatePreview();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, paths);
        pathsListView.setAdapter(adapter);

        pathsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                path = paths.get(position);
                updatePreview();
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
                if (paths.size() <= 1) {
                    Toast.makeText(getApplicationContext(), "You can't delete only path", Toast.LENGTH_SHORT).show();
                    return;
                }
                paths.remove(path);
                pathsSet.remove(path);
                Utils.saveImagePaths(getApplicationContext(), pathsSet);
                adapter.notifyDataSetChanged();
                int pos = 0;
                pathsListView.performItemClick(pathsListView.getChildAt(pos), pos, pathsListView.getItemIdAtPosition(pos));
            }
        });
    }

    private void updatePreview() {
        pathTextView.setText(getString(R.string.selected_path, path));

        Bitmap image = BitmapFactory.decodeFile(path);
        int dstWidth = 400;
        int dstHeight = image.getHeight() * dstWidth / image.getWidth();
        Bitmap thumbnail = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);

        previewImageView.setImageBitmap(thumbnail);
    }

}

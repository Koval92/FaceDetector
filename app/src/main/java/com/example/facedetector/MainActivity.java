package com.example.facedetector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 4564;
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "bhemmp4tnge1z2v";
    private final static String ACCESS_SECRET = "1lyrkonah3k2irv";
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private ImageView imageView;
    private Button detectFacesButton;
    private Button sendButton;
    private Button makePhotoButton;
    private Button selectPhotoButton;
    private Button loginButton;
    private SeekBar scaleSeekBar;
    private TextView scaleTextView;
    private TextView pathTextView;
    private String imagePath;
    private String resultPath;
    private Bitmap thumbnail;
    private int originalWidth;
    private int originalHeight;
    private int scaledWidth;
    private int scaledHeight;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeLayoutObjects();
        setOnClickListeners();
        resultPath = Environment.getExternalStorageDirectory() + "/result.jpg";
        replaceImage(Environment.getExternalStorageDirectory() + "/" + "faces.jpg");
        configureDropbox();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, item + " clicked", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void configureDropbox() {
        loggedIn(false);

        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, Session.AccessType.APP_FOLDER, token);
        } else {
            session = new AndroidAuthSession(pair, Session.AccessType.APP_FOLDER);
        }

        dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropboxAPI.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.apply();

                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeLayoutObjects() {
        imageView = (ImageView) this.findViewById(R.id.imageView);
        detectFacesButton = (Button) this.findViewById(R.id.detectFacesButton);
        sendButton = (Button) this.findViewById(R.id.sendButton);
        makePhotoButton = (Button) this.findViewById(R.id.makePhotoButton);
        selectPhotoButton = (Button) this.findViewById(R.id.selectPhotoButton);
        scaleSeekBar = (SeekBar) this.findViewById(R.id.scaleSeekBar);
        scaleTextView = (TextView) this.findViewById(R.id.sizeTextView);
        pathTextView = (TextView) this.findViewById(R.id.pathTextView);
        loginButton = (Button) this.findViewById(R.id.loginButton);
    }

    private void setOnClickListeners() {
        makePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivityForResult(intent, GALLERY_REQUEST);
            }
        });

        detectFacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("detectFacesButton", "clicked");
                detectFaces();
            }
        });

        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scaledWidth = originalWidth * progress / 100;
                scaledHeight = originalHeight * progress / 100;
                scaleTextView.setText(getString(R.string.image_size, scaledWidth, scaledHeight));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i("scaleSeekBar", "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("scaleSeekBar", "onStopTrackingTouch");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoggedIn) {
                    dropboxAPI.getSession().unlink();
                    loggedIn(false);
                } else {
                    dropboxAPI.getSession().startAuthentication(MainActivity.this);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DropboxUploader upload = new DropboxUploader(sendButton.getContext(), dropboxAPI, resultPath);
                upload.execute();
            }
        });
    }

    public void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        sendButton.setEnabled(isLogged);
        loginButton.setText(isLogged ? "Logout" : "Login");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            replaceImage(getLastImagePath());
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            String path = data.getStringExtra(GalleryActivity.RESULT_PATH_EXTRA);
            Toast.makeText(this, "Chosen photo: " + path, Toast.LENGTH_SHORT).show();
            replaceImage(path);
        } else {
            Toast.makeText(this, "Unknown activity result", Toast.LENGTH_SHORT).show();
        }
    }

    public String getLastImagePath() {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToLast()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
            cursor.close();
        } else {
            Log.e("lastImagePath", "null cursor or no files");
        }

        Log.i("Last Image Path", path);
        return path;
    }

    private void replaceImage(String path) {
        imagePath = path;
        thumbnail = null;
        Bitmap image = BitmapFactory.decodeFile(imagePath);

        if (image == null) {
            Log.e("replaceImage", "image is null");
            Toast.makeText(this, "Couldn't load image", Toast.LENGTH_SHORT).show();
            Bitmap.Config config = Bitmap.Config.RGB_565;
            image = Bitmap.createBitmap(THUMBNAIL_WIDTH, THUMBNAIL_WIDTH, config);
        }

        originalWidth = image.getWidth();
        originalHeight = image.getHeight();

        int dstWidth = THUMBNAIL_WIDTH;
        int dstHeight = image.getHeight() * dstWidth / image.getWidth();

        thumbnail = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);
        imageView.setImageBitmap(thumbnail);
        scaleSeekBar.setProgress(100);
        pathTextView.setText(getString(R.string.path, path));

        Log.i("imagePath", path);
        Log.i("image", originalWidth + " x " + originalHeight);
        Log.i("thumb", thumbnail.getWidth() + " x " + thumbnail.getHeight());
    }

    public void detectFaces() {
        long startTime = System.nanoTime();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inMutable = true;

        Bitmap image = BitmapFactory.decodeFile(imagePath, options);

        if (image == null) {
            Log.e("detectFaces", "image is null");
            Toast.makeText(this, "Couldn't load image", Toast.LENGTH_SHORT).show();
            return;
        }

        image = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false);
        int MAX_FACES = 1;

        FaceDetector face_detector = new FaceDetector(image.getWidth(), image.getHeight(), MAX_FACES);

        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];

        long midTime = System.nanoTime();

        int faceCount = face_detector.findFaces(image, faces);

        long endTime = System.nanoTime();
        Log.d("Face_Detection", "Face Count: " + String.valueOf(faceCount));
        Log.d("Face_Detection", "Image size: " + image.getWidth() + " x " + image.getHeight());
        Toast.makeText(MainActivity.this, "Detecting finished in " + (endTime - startTime) / 1000000 + " ms", Toast.LENGTH_SHORT).show();

        drawFaces(image, faces, faceCount);
        saveBitmapWithFaces(image);
    }

    private void saveBitmapWithFaces(Bitmap image) {
        resultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.jpg";
        File resultFile = new File(resultPath);
        if (resultFile.exists()) {
            boolean deleted = resultFile.delete();
            Log.d("saveBitmap", "deleted: " + deleted);
        }
        try {
            FileOutputStream out = new FileOutputStream(resultFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Log.d("saveBitmap", "saved to " + resultFile.getAbsolutePath() + ", size: " + resultFile.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawFaces(Bitmap image, FaceDetector.Face[] faces, int facesCount) {
        Canvas canvas = new Canvas(image);

        canvas.drawBitmap(image, 0, 0, null);
        Paint tmp_paint = new Paint();
        PointF tmp_point = new PointF();

        for (int i = 0; i < facesCount; i++) {
            FaceDetector.Face face = faces[i];
            tmp_paint.setColor(Color.RED);
            tmp_paint.setAlpha(100);

            face.getMidPoint(tmp_point);
            canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(), tmp_paint);
        }

        int dstWidth = THUMBNAIL_WIDTH;
        int dstHeight = image.getHeight() * dstWidth / image.getWidth();

        thumbnail = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);
        imageView.setImageBitmap(thumbnail);
    }
}

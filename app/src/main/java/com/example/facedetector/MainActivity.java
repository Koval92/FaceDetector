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
    public static final int THUMBNAIL_WIDTH = 400;
    private static final int CAMERA_REQUEST = 1888;
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "bhemmp4tnge1z2v";
    private final static String ACCESS_SECRET = "1lyrkonah3k2irv";
    private ImageView imageView;
    private Button detectFacesButton;
    private Button sendButton;
    private Button photoButton;
    private SeekBar scaleSeekBar;
    private TextView scaleTextView;
    private String imagePath;
    private String resultPath;
    private int originalWidth;
    private int originalHeight;
    private int scaledWidth;
    private int scaledHeight;
    private Bitmap thumbnail;
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private boolean isLoggedIn;
    private Button loginButton;

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
                editor.commit();

                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeLayoutObjects() {
        imageView = (ImageView) this.findViewById(R.id.imageView);
        detectFacesButton = (Button) this.findViewById(R.id.detectFacesButton);
        sendButton = (Button) this.findViewById(R.id.sendButton);
        photoButton = (Button) this.findViewById(R.id.photoButton);
        scaleSeekBar = (SeekBar) this.findViewById(R.id.scaleSeekBar);
        scaleTextView = (TextView) this.findViewById(R.id.sizeTextView);
        loginButton = (Button) this.findViewById(R.id.loginButton);
    }

    private void setOnClickListeners() {
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        detectFacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("detectFacesButton", "clicked");
                detectFaces();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("sendButton", "clicked");
            }
        });

        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scaledWidth = originalWidth * progress / 100;
                scaledHeight = originalHeight * progress / 100;
                scaleTextView.setText("Image size: " + scaledWidth + " x " + scaledHeight);
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
            Log.wtf("lastImagePath", "null cursor or no files");
        }

        Log.i("Last Image Path", path);
        return path;
    }

    private void replaceImage(String path) {
        imagePath = path;
        thumbnail = null;
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        originalWidth = image.getWidth();
        originalHeight = image.getHeight();

        int dstWidth = THUMBNAIL_WIDTH;
        int dstHeight = image.getHeight() * dstWidth / image.getWidth();

        thumbnail = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);
        imageView.setImageBitmap(thumbnail);
        scaleSeekBar.setProgress(100);

        Log.i("imagePath", path);
        Log.i("image", originalWidth + " x " + originalHeight);
        Log.i("thumb", thumbnail.getWidth() + " x " + thumbnail.getHeight());
    }

    public void detectFaces() {
        // Set internal configuration to RGB_565
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inMutable = true;

        Bitmap image = BitmapFactory.decodeFile(imagePath, options);
        image = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false);
        int MAX_FACES = 1;

        FaceDetector face_detector = new FaceDetector(image.getWidth(), image.getHeight(), MAX_FACES);

        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        int faceCount = face_detector.findFaces(image, faces);
        Log.d("Face_Detection", "Face Count: " + String.valueOf(faceCount));
        Log.d("Face_Detection", "Image size: " + image.getWidth() + " x " + image.getHeight());

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

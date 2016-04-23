package com.example.facedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class FaceDetectorTask extends AsyncTask<Void, Void, Bitmap> {
    private final Context context;
    private final String imagePath;
    private int scaledWidth;
    private int scaledHeight;
    private long midTime;
    private long endTime;
    private long startTime;
    private int faceCount;

    public FaceDetectorTask(Context context, String imagePath, int scaledWidth, int scaledHeight) {
        this.context = context;
        this.imagePath = imagePath;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
    }

    private static void saveBitmapWithFaces(Bitmap image) {
        File resultFile = new File(Utils.getResultPath());
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

    @Override
    protected Bitmap doInBackground(Void... params) {
        startTime = System.nanoTime();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inMutable = true;

        Bitmap image = BitmapFactory.decodeFile(imagePath, options);

        if (image == null) {
            this.cancel(false);
        }

        image = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false);

        FaceDetector face_detector = new FaceDetector(image.getWidth(), image.getHeight(), Utils.getMaxFaces());

        FaceDetector.Face[] faces = new FaceDetector.Face[Utils.getMaxFaces()];

        midTime = System.nanoTime();

        faceCount = face_detector.findFaces(image, faces);

        endTime = System.nanoTime();

        drawFaces(image, faces, faceCount);
        saveBitmapWithFaces(image);

        return image;
    }

    @Override
    protected void onPostExecute(Bitmap image) {
        super.onPostExecute(image);

        if (image == null) {
            Log.e("detectFaces", "image is null");
            Toast.makeText(context, "Couldn't load image", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Face_Detection", "Face Count: " + String.valueOf(faceCount));
        Log.d("Face_Detection", "Image size: " + image.getWidth() + " x " + image.getHeight());
        Toast.makeText(context, "Detecting finished in " + (endTime - startTime) / 1000000 + " ms", Toast.LENGTH_SHORT).show();
    }

    private void drawFaces(Bitmap image, FaceDetector.Face[] faces, int facesCount) {
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
    }
}

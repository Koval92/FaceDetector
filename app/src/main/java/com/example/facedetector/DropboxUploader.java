package com.example.facedetector;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DropboxUploader extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropboxAPI;
    private String filePath;
    private Context context;

    public DropboxUploader(Context context, DropboxAPI<?> dropboxAPI,
                           String filePath) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.filePath = filePath;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final File tempDir = context.getCacheDir();
        File file;
        FileWriter fr;
        try {
            file = new File(filePath);

            FileInputStream fileInputStream = new FileInputStream(file);
            String dbPath = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".jpg";
            dropboxAPI.putFile(dbPath, fileInputStream, file.length(), null, null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File Uploaded Successfully!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_LONG).show();
        }
    }
}
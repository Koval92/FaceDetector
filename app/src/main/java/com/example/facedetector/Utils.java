package com.example.facedetector;

import android.content.Context;
import android.os.Environment;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    public final static String PATHS_PREFS = "path_prefs";
    private static String resultPath = Environment.getExternalStorageDirectory() + "/result.jpg";
    private static int maxFaces = 1;

    public static void saveImagePaths(Context context, Set<String> imagePaths) {
        context.getSharedPreferences(PATHS_PREFS, Context.MODE_PRIVATE).edit().remove(PATHS_PREFS).apply();
        context.getSharedPreferences(PATHS_PREFS, Context.MODE_PRIVATE).edit().putStringSet(PATHS_PREFS, imagePaths).apply();
    }

    public static Set<String> loadImagePaths(Context context) {
        return context.getSharedPreferences(PATHS_PREFS, Context.MODE_PRIVATE).getStringSet(PATHS_PREFS, new HashSet<String>());
    }

    public static String getResultPath() {
        return resultPath;
    }

    public static void setResultPath(String resultPath) {
        Utils.resultPath = resultPath;
    }

    public static int getMaxFaces() {
        return maxFaces;
    }

    public static void setMaxFaces(int maxFaces) {
        Utils.maxFaces = maxFaces;
    }
}

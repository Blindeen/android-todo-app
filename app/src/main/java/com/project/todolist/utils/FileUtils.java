package com.project.todolist.utils;

import static com.project.todolist.utils.Utils.displayToast;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    private static String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

        return "." + extension;
    }

    public static String getFilenameWithExtension(Context context, Uri uri) {
        String filename = uri.getLastPathSegment();
        String fileExtension = getFileExtension(context, uri);
        return filename + fileExtension;
    }

    public static void copyFile(Context context, Uri sourceUri) {
        String filenameWithExtension = getFilenameWithExtension(context, sourceUri);
        String pathname = context.getFilesDir() + "/" + filenameWithExtension;
        File destinationFile = new File(pathname);

        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             FileOutputStream out = new FileOutputStream(destinationFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            displayToast(context, "Failed to copy file");
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}

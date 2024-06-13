package com.project.todolist.utils;

import static com.project.todolist.utils.Utils.displayToast;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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

    public static String getFilenameWithExtension(Context context, Uri uri) throws IOException {
        String filename;
        if (context.getContentResolver().getType(uri) != null) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        filename = cursor.getString(columnIndex);
                    } else {
                        throw new IOException("Column " + OpenableColumns.DISPLAY_NAME + " does not exist in the cursor");
                    }
                } else {
                    throw new IOException("Cursor is null or could not move to first row");
                }
            }
        } else {
            filename = uri.getLastPathSegment() + getFileExtension(context, uri);
        }

        return filename;
    }

    public static void copyFile(Context context, Uri sourceUri) throws IOException {
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

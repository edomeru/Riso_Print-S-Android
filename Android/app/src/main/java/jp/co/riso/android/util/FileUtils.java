/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * FileUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @class FileUtils
 * 
 * @brief Utility class for file operations
 */
public class FileUtils {
    
    /**
     * @brief Copy a file.
     * 
     * @param src Source file
     * @param dst Destination file
     */
    public static void copy(File src, File dst) throws IOException {
        if(src == null || dst == null) {
            return;
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        
        in.close();
        out.close();
    }
    
    /**
     * @brief Copy a file from inputstream.
     * 
     * @param in Source inputstream
     * @param dst Destination file
     */
    public static void copy(InputStream in, File dst) throws IOException {
        if(in == null || dst == null) {
            return;
        }
        OutputStream out = new FileOutputStream(dst);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        
        in.close();
        out.close();
    }
    
    
    public static void delete(File src) throws IOException {
        
        src.delete();
        
    }

    /**
     * @brief Gets the filename from uri with content scheme
     *
     * @param context Application context
     * @param uri File uri
     * @return Filename
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}

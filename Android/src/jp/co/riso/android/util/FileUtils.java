/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * FileUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}

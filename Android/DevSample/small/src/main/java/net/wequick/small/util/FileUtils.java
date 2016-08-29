/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.wequick.small.util;

import android.content.Context;

import net.wequick.small.Small;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class consists exclusively of static methods that operate on file.
 */
public final class FileUtils {
    private static final String DOWNLOAD_PATH = "small_patch";

    public interface OnProgressListener {
        void onProgress(int length);
    }

    public static void unZipFolder(File zipFile, String outPath) throws Exception {
        unZipFolder(new FileInputStream(zipFile), outPath, null);
    }

    /**
     * 解压zip
     *
     * @param inStream
     * @param outPath  输出路径
     * @param listener
     * @throws Exception
     */
    public static void unZipFolder(InputStream inStream,
                                   String outPath,
                                   OnProgressListener listener) throws Exception {

        //ZipInputStream Java自带压缩、解压流
        ZipInputStream inZip = new ZipInputStream(inStream);
        ZipEntry zipEntry;
        while ((zipEntry = inZip.getNextEntry()) != null) { //循环
            String szName = zipEntry.getName();
            if (szName.startsWith("META-INF")) continue;

            //如果路径是directory，则只创建文件夹
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                // 获取文件夹全路径
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPath + File.separator + szName);
                folder.mkdirs(); //创建文件夹
            } else {
                //创建文件并写入

                //创建文件
                File file = new File(outPath + File.separator + szName);
                if (!file.createNewFile()) {
                    System.err.println("Failed to create file: " + file);
                    return;
                }
                // get the output stream of the file
                // 写入流至文件
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                    if (listener != null) {
                        listener.onProgress(len);
                    }
                }
                out.close();
            }
        }
        inZip.close();
    }

    public static File getInternalFilesPath(String dir) {
        File file = Small.getContext().getDir(dir, Context.MODE_PRIVATE);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }


    /**
     * 获取patch下载路径
     *
     * @return
     */
    public static File getDownloadBundlePath() {
        return getInternalFilesPath(DOWNLOAD_PATH);
    }
}

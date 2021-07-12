package com.example.filemanager.helpers;

import android.util.Log;

import com.example.filemanager.models.CommonFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class FileUtil {
    public static void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            }
            copyFile(src, dst);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFile(child);
            }
        }
        file.delete();
    }

    public static void moveFile(String srcDir, String dstDir){
        copyFileOrDirectory(srcDir,dstDir);
        File src = new File(srcDir);
        deleteFile(src);
    }

    public static List<CommonFile> getListFile(File root, List<CommonFile> fileList) {
        File[] files = root.listFiles();
        fileList.clear();
        for (File file : files) {
            CommonFile f = new CommonFile(file);
            Log.d("File", file.getAbsolutePath());
            fileList.add(f);
        }
        return fileList;
    }
}

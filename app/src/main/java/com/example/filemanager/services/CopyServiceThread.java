package com.example.filemanager.services;

import com.example.filemanager.helpers.FileUtil;

public class CopyServiceThread implements Runnable{
    private String src;
    private String dst;
    public static final int COPY = 1;
    public static final int MOVE = 2;
    private int type;
    public CopyServiceThread(String src, String dst, int type) {
        this.src = src;
        this.dst = dst;
        this.type = type;
    }

    @Override
    public void run() {
        if(type == COPY) {
            FileUtil.copyFileOrDirectory(src, dst);
        }
        else{
            FileUtil.moveFile(src, dst);
        }
    }

}

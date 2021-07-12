package com.example.filemanager.services;

import com.example.filemanager.helpers.FileUtil;

public class CopyServiceThread implements Runnable{
    private String src;
    private String dst;

    public CopyServiceThread(String src, String dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void run() {
        FileUtil.copyFileOrDirectory(src,dst);
    }
}

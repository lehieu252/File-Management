package com.example.filemanager.models;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class CommonFile implements Serializable {
    private File file;
    private String name;
    private String modifiedDate;
    private int itemCount;
    private double size;
    private boolean isSelected;



    public CommonFile(File file) {
        this.file = file;
        initFileInfo();
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    void initFileInfo(){
        this.name = file.getName();
        Date date = new Date(file.lastModified());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd HH:mm");
        this.modifiedDate = sdfDate.format(date);
        if(file.isDirectory()){
            if(file.listFiles() != null){
                this.itemCount = Objects.requireNonNull(file.listFiles()).length;
            }
            else{
                this.itemCount = 0;
            }
        }
        else{
            this.size = file.length();
        }
    }
}

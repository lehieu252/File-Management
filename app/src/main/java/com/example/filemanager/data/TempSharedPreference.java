package com.example.filemanager.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.filemanager.models.CommonFile;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TempSharedPreference {
    private Context context;
    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor editor;

    public TempSharedPreference(Context context) {
        this.context = context;
        this.sharedPreference = context.getSharedPreferences("TempData", Context.MODE_PRIVATE);
        this.editor = sharedPreference.edit();
    }

    public void savePathList(List<CommonFile> selectedFiles) {
        Gson gson = new Gson();
        List<String> pathList = new ArrayList<>();
        for (CommonFile file : selectedFiles) {
            pathList.add(file.getFile().getPath());
        }
        String jsonText = gson.toJson(pathList);
        editor.putString("path_list", jsonText);
        editor.apply();
    }

    public List<String> getPathList() {
        Gson gson = new Gson();
        String jsonText = sharedPreference.getString("path_list", null);
        if(jsonText == null) return null;
        String[] listPath = gson.fromJson(jsonText, String[].class);
        return Arrays.asList(listPath);
    }

    public void clearPathList() {
        editor.clear().apply();
    }

    public void saveMode(int mode){
        editor.putInt("ACTION_MODE",mode);
        editor.apply();
    }

    public int getMode(){
        return sharedPreference.getInt("ACTION_MODE",1);
    }
}

package com.example.filemanager.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.example.filemanager.R;

public class LoadingDialog {
    private Activity context;
    private AlertDialog dialog;
    public boolean sLoading = false;
    public LoadingDialog(Activity context) {
        this.context = context;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog,null));
        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog(){
        dialog.dismiss();
    }
}

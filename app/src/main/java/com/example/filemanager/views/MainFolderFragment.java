package com.example.filemanager.views;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.filemanager.R;
import com.example.filemanager.adapters.FileAdapter;
import com.example.filemanager.data.TempSharedPreference;
import com.example.filemanager.databinding.FragmentMainFolderBinding;
import com.example.filemanager.helpers.FileUtil;
import com.example.filemanager.models.CommonFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class MainFolderFragment extends Fragment {

    FragmentMainFolderBinding binding;
    private List<CommonFile> fileList = new ArrayList<CommonFile>();
    FileAdapter fileAdapter;
    TempSharedPreference tempSharedPreference;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_folder, container, false);
        tempSharedPreference = new TempSharedPreference(getContext());
        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (root.listFiles() != null) {
            if (root.listFiles().length > 0) {
                binding.folderRecyclerview.setVisibility(View.VISIBLE);
                binding.txtNoFiles.setVisibility(View.GONE);
                FileUtil.getListFile(root,fileList);
            }
        }
        fileAdapter = new FileAdapter(fileList, 1);
        binding.folderRecyclerview.setAdapter(fileAdapter);
        fileAdapter.setItemClick(new FileAdapter.OnItemClick() {
            @Override
            public void onItemClick(View view, CommonFile file, int pos) {
                if (fileAdapter.selectedItemCount() > 0) {
                    toggleSelection(pos);
                }
            }

            @Override
            public void onItemLongClick(View view, CommonFile file, int pos) {
                toggleSelection(pos);
                binding.bottomOptionMenu.setVisibility(View.VISIBLE);
            }
        });
        binding.bottomOptionMenu.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.move: {
                    Toast.makeText(getContext(), "Move", Toast.LENGTH_SHORT).show();
                    return true;
                }
                case R.id.copy: {
                    Toast.makeText(getContext(), "Copy", Toast.LENGTH_SHORT).show();
                    Log.d("SelectedFile",fileAdapter.getSelectedItems().toString());
                    List<CommonFile> copyList = new ArrayList<>();
                    for(int i=0;i<fileAdapter.getSelectedItems().size();i++){
                        copyList.add(fileList.get(fileAdapter.getSelectedItems().get(i)));
                    }
                    tempSharedPreference.savePathList(copyList);
                    fileAdapter.clearAllSelection();
                    binding.bottomOptionMenu.setVisibility(View.GONE);
                    Log.d("copied",tempSharedPreference.getPathList().toString());
                    return true;
                }
                case R.id.delete: {
                    Toast.makeText(getContext(), "Delete", Toast.LENGTH_SHORT).show();
                    return true;
                }
                case R.id.cancel: {
                    Toast.makeText(getContext(), "Cancel", Toast.LENGTH_SHORT).show();
                    fileAdapter.clearAllSelection();
                    binding.bottomOptionMenu.setVisibility(View.GONE);
                    return true;
                }
                default:
                    return false;
            }
        });

        return binding.getRoot();
    }

    private void toggleSelection(int position) {
        fileAdapter.toggleSelection(position);
    }

    private void deleteSelection() {
        List<Integer> selectedItemPos = fileAdapter.getSelectedItems();
        for (int i = selectedItemPos.size() - 1; i >= 0; i--) {
            fileAdapter.removeItems(selectedItemPos.get(i));
        }
    }

}
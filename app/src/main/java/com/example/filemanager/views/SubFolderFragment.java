package com.example.filemanager.views;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.filemanager.R;
import com.example.filemanager.adapters.FileAdapter;
import com.example.filemanager.databinding.FragmentSubFolderBinding;
import com.example.filemanager.helpers.FileUtil;
import com.example.filemanager.models.CommonFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SubFolderFragment extends Fragment {

    FragmentSubFolderBinding binding;
    private List<CommonFile> fileList = new ArrayList<CommonFile>();
    FileAdapter fileAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sub_folder, container, false);
        Bundle bundle = getArguments();
        CommonFile folder = (CommonFile) bundle.getSerializable("Folder");
        File root = new File(folder.getFile().getPath());
        Log.d("File or Folder", String.valueOf(root.isDirectory()));

        if (root.listFiles() != null) {
            if (root.listFiles().length > 0) {
                binding.folderRecyclerview.setVisibility(View.VISIBLE);
                binding.txtNoFiles.setVisibility(View.GONE);
                FileUtil.getListFile(root,fileList);
            }
        }
        fileAdapter = new FileAdapter(fileList, 2);
        binding.folderRecyclerview.setAdapter(fileAdapter);
        fileAdapter.setItemClick(new FileAdapter.OnItemClick() {
            @Override
            public void onItemClick(View view, CommonFile file, int pos) {
                if (fileAdapter.selectedItemCount() > 0){
                    toggleSelection(pos);
                }
            }

            @Override
            public void onItemLongClick(View view, CommonFile file, int pos) {
                toggleSelection(pos);
                binding.bottomOptionMenu.setVisibility(View.VISIBLE);

            }
        });

        binding.bottomOptionMenu.setOnNavigationItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.move:{
                    Toast.makeText(getContext(),"Move",Toast.LENGTH_SHORT).show();
                    return true;
                }
                case R.id.copy:{
                    Toast.makeText(getContext(),"Copy",Toast.LENGTH_SHORT).show();

                    fileAdapter.clearAllSelection();
                    binding.bottomOptionMenu.setVisibility(View.GONE);

                    return true;
                }
                case R.id.delete:{
                    Toast.makeText(getContext(),"Delete",Toast.LENGTH_SHORT).show();
                    return true;
                }
                case R.id.cancel:{
                    Toast.makeText(getContext(),"Cancel",Toast.LENGTH_SHORT).show();
                    fileAdapter.clearAllSelection();
                    binding.bottomOptionMenu.setVisibility(View.GONE);
                    return true;
                }
                default: return false;
            }
        });


        return binding.getRoot();
    }

    private void toggleSelection(int position){
        fileAdapter.toggleSelection(position);
    }

    private void deleteSelection(){
        List<Integer> selectedItemPos = fileAdapter.getSelectedItems();
        for(int i = selectedItemPos.size() - 1; i >=0;i--){
            fileAdapter.removeItems(selectedItemPos.get(i));
        }
        fileAdapter.notifyDataSetChanged();
    }
}
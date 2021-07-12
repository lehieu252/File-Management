package com.example.filemanager.views;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.filemanager.services.CopyServiceThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainFolderFragment extends Fragment {

    private FragmentMainFolderBinding binding;
    private List<CommonFile> fileList = new ArrayList<CommonFile>();
    private FileAdapter fileAdapter;
    TempSharedPreference tempSharedPreference;
    private int mode;       // 1 : Copy, 2: Move
    @SuppressLint("SetTextI18n")
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
                FileUtil.getListFile(root, fileList);
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
                binding.bottomActionMenu.setVisibility(View.GONE);
            }
        });
        binding.bottomOptionMenu.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.move: {
                    Toast.makeText(getContext(), "Move", Toast.LENGTH_SHORT).show();
                    Log.d("SelectedFile", fileAdapter.getSelectedItems().toString());
                    List<CommonFile> copyList = new ArrayList<>();
                    for (int i = 0; i < fileAdapter.getSelectedItems().size(); i++) {
                        copyList.add(fileList.get(fileAdapter.getSelectedItems().get(i)));
                    }
                    tempSharedPreference.savePathList(copyList);
                    fileAdapter.backToOrigin();
                    if (tempSharedPreference.getPathList() != null) {
                        Log.d("copied", tempSharedPreference.getPathList().toString());
                        binding.chosenItemCount.setText(tempSharedPreference.getPathList().size() + " items");
                        binding.bottomOptionMenu.setVisibility(View.GONE);
                        binding.bottomActionMenu.setVisibility(View.VISIBLE);
                        binding.bottomActionMove.setVisibility(View.VISIBLE);
                        binding.bottomActionCopy.setVisibility(View.INVISIBLE);
                    }
                    mode = 2;
                    return true;
                }
                case R.id.copy: {
                    Toast.makeText(getContext(), "Copy", Toast.LENGTH_SHORT).show();
                    Log.d("SelectedFile", fileAdapter.getSelectedItems().toString());
                    List<CommonFile> copyList = new ArrayList<>();
                    for (int i = 0; i < fileAdapter.getSelectedItems().size(); i++) {
                        copyList.add(fileList.get(fileAdapter.getSelectedItems().get(i)));
                    }
                    tempSharedPreference.savePathList(copyList);
                    fileAdapter.backToOrigin();
                    if (tempSharedPreference.getPathList() != null) {
                        Log.d("copied", tempSharedPreference.getPathList().toString());
                        binding.chosenItemCount.setText(tempSharedPreference.getPathList().size() + " items");
                        binding.bottomOptionMenu.setVisibility(View.GONE);
                        binding.bottomActionMenu.setVisibility(View.VISIBLE);
                    }
                    mode = 1;
                    return true;
                }
                case R.id.delete: {
                    Toast.makeText(getContext(), "Delete", Toast.LENGTH_SHORT).show();
                    Log.d("Selected", fileAdapter.getSelectedItems().toString());
                    deleteSelection();
                    fileAdapter.deleteSelection(FileUtil.getListFile(root, fileList));
                    binding.bottomOptionMenu.setVisibility(View.GONE);
                    binding.bottomActionMenu.setVisibility(View.GONE);
                    return true;
                }
                case R.id.cancel: {
                    Toast.makeText(getContext(), "Cancel", Toast.LENGTH_SHORT).show();
                    fileAdapter.backToOrigin();
                    binding.bottomOptionMenu.setVisibility(View.GONE);
                    binding.bottomActionMenu.setVisibility(View.GONE);
                    return true;
                }
                default:
                    return false;
            }
        });

        if (tempSharedPreference.getPathList() != null) {
            Log.d("copied", tempSharedPreference.getPathList().toString());
            binding.chosenItemCount.setText(tempSharedPreference.getPathList().size() + " items");
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.VISIBLE);
            if (mode == 1) {
                binding.bottomActionMove.setVisibility(View.INVISIBLE);
                binding.bottomActionCopy.setVisibility(View.VISIBLE);
            } else {
                binding.bottomActionMove.setVisibility(View.VISIBLE);
                binding.bottomActionCopy.setVisibility(View.INVISIBLE);
            }
        }

        binding.bottomActionCancel.setOnClickListener(v -> {
            tempSharedPreference.clearPathList();
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.GONE);
        });

        binding.bottomActionCopy.setOnClickListener(v -> {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<String> listCopy = tempSharedPreference.getPathList();
            for (String file : listCopy) {
                Runnable worker = new CopyServiceThread(file, root.getAbsolutePath(),CopyServiceThread.COPY);
                executor.execute(worker);
            }
            executor.shutdown();
            while(!executor.isTerminated()){
                Log.d("Thread", "Running");
            }
            fileAdapter.updateNew(FileUtil.getListFile(root, fileList));
            binding.folderRecyclerview.setVisibility(View.VISIBLE);
            binding.txtNoFiles.setVisibility(View.GONE);
            tempSharedPreference.clearPathList();
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.GONE);
        });

        binding.bottomActionMove.setOnClickListener(v -> {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<String> listCopy = tempSharedPreference.getPathList();
            for (String file : listCopy) {
                Runnable worker = new CopyServiceThread(file, root.getAbsolutePath(),CopyServiceThread.MOVE);
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                Log.d("Thread", "Running");
            }
            fileAdapter.updateNew(FileUtil.getListFile(root, fileList));
            binding.folderRecyclerview.setVisibility(View.VISIBLE);
            binding.txtNoFiles.setVisibility(View.GONE);
            tempSharedPreference.clearPathList();
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.GONE);
        });

        return binding.getRoot();
    }

    private void toggleSelection(int position) {
        fileAdapter.toggleSelection(position);
    }

    private void deleteSelection() {
        for (int i = 0; i < fileAdapter.getSelectedItems().size(); i++) {
            FileUtil.deleteFile(new File(fileList.get(fileAdapter.getSelectedItems().get(i)).getFile().getPath()));
        }
    }

}
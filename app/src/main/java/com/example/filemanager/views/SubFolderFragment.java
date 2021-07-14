package com.example.filemanager.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.filemanager.R;
import com.example.filemanager.adapters.FileAdapter;
import com.example.filemanager.data.TempSharedPreference;
import com.example.filemanager.databinding.FragmentSubFolderBinding;
import com.example.filemanager.helpers.FileUtil;
import com.example.filemanager.models.CommonFile;
import com.example.filemanager.services.CopyServiceThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubFolderFragment extends Fragment {

    private FragmentSubFolderBinding binding;
    private List<CommonFile> fileList = new ArrayList<CommonFile>();
    private FileAdapter fileAdapter;
    private LoadingDialog loadingDialog;
    File root;
    TempSharedPreference tempSharedPreference;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sub_folder, container, false);
        tempSharedPreference = new TempSharedPreference(getContext());
        loadingDialog = new LoadingDialog(getActivity());
        Bundle bundle = getArguments();
        CommonFile folder = (CommonFile) bundle.getSerializable("Folder");
        root = new File(folder.getFile().getPath());
        Log.d("File or Folder", String.valueOf(root.isDirectory()));

        if (root.listFiles() != null) {
            if (root.listFiles().length > 0) {
                binding.folderRecyclerview.setVisibility(View.VISIBLE);
                binding.txtNoFiles.setVisibility(View.GONE);
                FileUtil.getListFile(root, fileList);
            }
        }
        fileAdapter = new FileAdapter(fileList, 2);
        binding.folderRecyclerview.setAdapter(fileAdapter);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            fileAdapter.notifyDataSetChanged();
            Log.d("Root", String.valueOf(Objects.requireNonNull(root.listFiles()).length));
        });

        binding.bottomOptionMenu.setVisibility(View.GONE);
        binding.bottomActionMenu.setVisibility(View.GONE);

        fileAdapter.setItemClick(new FileAdapter.OnItemClick() {
            @Override
            public void onItemClick(View view, CommonFile file, int pos) {
                if (fileAdapter.isSelectionMode) {
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
                    tempSharedPreference.saveMode(2);
                    return true;
                }
                case R.id.copy: {
                    Toast.makeText(getContext(), "Copy", Toast.LENGTH_SHORT).show();
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
                    tempSharedPreference.saveMode(1);
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
            if (tempSharedPreference.getMode() == 2) {
                binding.bottomActionMove.setVisibility(View.VISIBLE);
                binding.bottomActionCopy.setVisibility(View.INVISIBLE);
            }
            if (tempSharedPreference.getMode() == 1) {
                binding.bottomActionMove.setVisibility(View.INVISIBLE);
                binding.bottomActionCopy.setVisibility(View.VISIBLE);
            }
        }

        binding.bottomActionCancel.setOnClickListener(v -> {
            tempSharedPreference.clearPathList();
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.GONE);
        });

        binding.bottomActionCopy.setOnClickListener(v -> {
            binding.folderRecyclerview.setVisibility(View.VISIBLE);
            binding.txtNoFiles.setVisibility(View.GONE);
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.GONE);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<String> listCopy = tempSharedPreference.getPathList();
            for (String file : listCopy) {
                Runnable worker = new CopyServiceThread(file, root.getAbsolutePath(), CopyServiceThread.COPY);
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                Log.d("Thread", "Running");
            }
            fileAdapter.updateNew(FileUtil.getListFile(root, fileList));
            tempSharedPreference.clearPathList();
        });

        binding.bottomActionMove.setOnClickListener(v -> {
            binding.folderRecyclerview.setVisibility(View.VISIBLE);
            binding.txtNoFiles.setVisibility(View.GONE);
            binding.bottomOptionMenu.setVisibility(View.GONE);
            binding.bottomActionMenu.setVisibility(View.GONE);
            ExecutorService executor = Executors.newFixedThreadPool(20);
            List<String> listCopy = tempSharedPreference.getPathList();
            for (String file : listCopy) {
                Runnable worker = new CopyServiceThread(file, root.getAbsolutePath(), CopyServiceThread.MOVE);
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                Log.d("Thread", "Running");
            }
            fileAdapter.updateNew(FileUtil.getListFile(root, fileList));
            tempSharedPreference.clearPathList();
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fileAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fileAdapter.filter(newText);
                return true;
            }
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
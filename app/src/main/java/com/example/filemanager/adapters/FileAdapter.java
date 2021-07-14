package com.example.filemanager.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;
import com.example.filemanager.models.CommonFile;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private int screen;
    private List<CommonFile> files;
    private List<CommonFile> tmpFiles;
    public boolean isSelectionMode = false;
    private OnItemClick itemClick;
    private SparseBooleanArray selectedItems;
    private int selectedIndex = -1;

    public FileAdapter(List<CommonFile> files, int screen) {
        this.screen = screen;
        this.files = files;
        tmpFiles = new ArrayList<>();
        tmpFiles.addAll(files);

        this.selectedItems = new SparseBooleanArray();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.file_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommonFile data = files.get(position);
        if (data != null) {
            if(isSelectionMode) {
                selectedMode(holder, position);
            }
            else{
                holder.uncheckBtn.setVisibility(View.GONE);
                holder.checkBtn.setVisibility(View.GONE);
            }
            holder.fCard.setActivated(selectedItems.get(position, false));
            holder.fName.setText(data.getName());
            holder.fModifiedDate.setText(data.getModifiedDate());
            if (data.getFile().isDirectory()) {
                holder.folderIcon.setVisibility(View.VISIBLE);
                holder.fileIcon.setVisibility(View.GONE);
                holder.fItemCount.setVisibility(View.VISIBLE);
                holder.fSize.setVisibility(View.GONE);
                holder.fItemCount.setText(data.getItemCount() + " items");
            } else if(data.getFile().isFile()) {
                holder.folderIcon.setVisibility(View.GONE);
                holder.fileIcon.setVisibility(View.VISIBLE);
                holder.fItemCount.setVisibility(View.GONE);
                holder.fSize.setVisibility(View.VISIBLE);
                if (data.getSize() >= 1024 && data.getSize() < 1024 * 1024) {
                    holder.fSize.setText(String.format("%.2f", data.getSize() / 1024) + " kB");
                } else if (data.getSize() >= 1024 * 1024 && data.getSize() < 1024 * 1024 * 1024) {
                    holder.fSize.setText(String.format("%.2f", data.getSize() / (1024 * 1024)) + " MB");
                } else if (data.getSize() >= 1024 * 1024 * 1024) {
                    holder.fSize.setText(String.format("%.2f", data.getSize() / (1024 * 1024 * 1024)) + " GB");
                } else {
                    holder.fSize.setText(String.format("%.2f", data.getSize()) + " B");
                }
            }

            holder.fCard.setOnClickListener(v -> {
                if (itemClick == null) return;
                if(isSelectionMode) {
                    itemClick.onItemClick(v, data, position);
                    Log.d("Check", String.valueOf(selectedItems.get(position)));
                    Log.d("Check", String.valueOf(selectedItems.size()));
                }
                else{
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Folder", data);
                    if(data.getFile().isDirectory()) {
                        if (!this.isSelectionMode && screen == 1) {
                            Navigation.findNavController(v).navigate(R.id.action_mainFolderFragment_to_subFolderFragment, bundle);
                            Log.d("test", String.valueOf(isSelectionMode));
                        }
                        if (!this.isSelectionMode && screen == 2) {
                            Navigation.findNavController(v).navigate(R.id.action_subFolderFragment_self, bundle);
                        }
                    }
                    else{
                        Log.d("Path", getFileExt(data.getName()));
                        String fileExt = getFileExt(data.getName());
                        if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png") || fileExt.equals("gif")) {
                            File file = new File(data.getFile().getPath());
                            Intent intent = new Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                                                    FileProvider.getUriForFile(v.getContext(), v.getContext().getPackageName() + ".provider", file) : Uri.fromFile(file),
                                            "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            v.getContext().startActivity(intent);
                        } else if (fileExt.equals("mp4") || fileExt.equals("3gp")) {
                            File file = new File(data.getFile().getPath());
                            Intent intent = new Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                                                    FileProvider.getUriForFile(v.getContext(), v.getContext().getPackageName() + ".provider", file) : Uri.fromFile(file),
                                            "video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            v.getContext().startActivity(intent);
                        } else if (fileExt.equals("txt") || fileExt.equals("html") || fileExt.equals("pdf")) {
                            File file = new File(data.getFile().getPath());
                            Intent intent = new Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                                                    FileProvider.getUriForFile(v.getContext(), v.getContext().getPackageName() + ".provider", file) : Uri.fromFile(file),
                                            "text/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            v.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(), "Cannot open this  file", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            holder.fCard.setOnLongClickListener(v -> {
                if (itemClick == null) {
                    return false;
                } else {
                    itemClick.onItemLongClick(v, data, position);
                    isSelectionMode = true;
                    this.notifyDataSetChanged();
                    return true;
                }
            });


        }
    }

    public String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }


    private void selectedMode(FileAdapter.ViewHolder viewHolder, int position) {
        if (selectedItems.get(position, false)) {
            viewHolder.uncheckBtn.setVisibility(View.GONE);
            viewHolder.checkBtn.setVisibility(View.VISIBLE);
            if (selectedIndex == position) selectedIndex = -1;
        } else {
            viewHolder.checkBtn.setVisibility(View.GONE);
            viewHolder.uncheckBtn.setVisibility(View.VISIBLE);
            if (selectedIndex == position) selectedIndex = -1;
        }
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public void backToOrigin() {
        selectedItems.clear();
        files.clear();
        files.addAll(tmpFiles);
        isSelectionMode = false;
        selectedIndex = -1;
        Log.d("List File", files.toString());
        Log.d("List File", tmpFiles.toString());
        this.notifyDataSetChanged();
    }

    public void deleteSelection(List<CommonFile> files) {
        selectedItems.clear();
        this.files = files;
        tmpFiles.clear();
        tmpFiles.addAll(files);
        isSelectionMode = false;
        selectedIndex = -1;
        this.notifyDataSetChanged();
    }

    public void updateNew(List<CommonFile> files) {
        selectedItems.clear();
        this.files = files;
        tmpFiles.clear();
        tmpFiles.addAll(files);
        isSelectionMode = false;
        selectedIndex = -1;
        this.notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectedIndex = position;
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public int selectedItemCount() {
        return selectedItems.size();
    }

    public void filter(String text){
        files.clear();
        if(text.isEmpty()){
            files.addAll(tmpFiles);
        }
        else {
            text = text.toLowerCase();
            for(CommonFile file : tmpFiles ){
                if(file.getName().toLowerCase().contains(text)){
                    files.add(file);
                }
            }
        }
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fName;
        public TextView fModifiedDate;
        public TextView fSize;
        public TextView fItemCount;
        public ImageView checkBtn;
        public ImageView uncheckBtn;
        public RelativeLayout folderIcon;
        public RelativeLayout fileIcon;
        public RelativeLayout fCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.fName = itemView.findViewById(R.id.file_name);
            this.fModifiedDate = itemView.findViewById(R.id.file_modified_date);
            this.fSize = itemView.findViewById(R.id.file_size);
            this.fItemCount = itemView.findViewById(R.id.file_item_count);
            this.checkBtn = itemView.findViewById(R.id.checked_btn);
            this.uncheckBtn = itemView.findViewById(R.id.unchecked_btn);
            this.folderIcon = itemView.findViewById(R.id.folder_icon);
            this.fileIcon = itemView.findViewById(R.id.file_icon);
            this.fCard = itemView.findViewById(R.id.file_card);
        }
    }

    public interface OnItemClick {
        void onItemClick(View view, CommonFile file, int pos);

        void onItemLongClick(View view, CommonFile file, int pos);
    }

}

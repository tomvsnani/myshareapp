package com.example.mysharecare;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class PathModel {
    List<ModelClass> list;
    String path="";

    public PathModel(List<ModelClass> list, String path) {
        this.list = list;
        this.path = path;
    }

    public List<ModelClass> getList() {
        return list;
    }

    public String getPath() {
        return path;
    }
   static DiffUtil.ItemCallback<PathModel> pathDiffUtil=new DiffUtil.ItemCallback<PathModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull PathModel oldItem, @NonNull PathModel newItem) {
            return oldItem.getPath().equals(newItem.getPath());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PathModel oldItem, @NonNull PathModel newItem) {

            return oldItem.getList().equals(newItem.getList());
        }
    };
}

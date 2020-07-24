package com.example.mysharecare;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class TransferDataModel {
    private String name;
    private int totalFileSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(int totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    public int getSizeSent() {
        return sizeSent;
    }

    public void setSizeSent(int sizeSent) {
        this.sizeSent = sizeSent;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    private int sizeSent;
    private transient Drawable drawable;

    static DiffUtil.ItemCallback<TransferDataModel> transferDataModelItemCallback=new DiffUtil.ItemCallback<TransferDataModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransferDataModel oldItem, @NonNull TransferDataModel newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getSizeSent()==(newItem.getSizeSent()) &&
            oldItem.getTotalFileSize()==(newItem.getTotalFileSize()) ;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransferDataModel oldItem, @NonNull TransferDataModel newItem) {
            return false;
        }
    };
}

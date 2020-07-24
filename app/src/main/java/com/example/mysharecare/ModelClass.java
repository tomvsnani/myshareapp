package com.example.mysharecare;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.io.Serializable;

public class ModelClass implements Serializable {
    private String name="";
    private String type="";
    private Long size=0L;
    private int eachFileSize=0;

    public int getEachFileSize() {
        return eachFileSize;
    }

    public void setEachFileSize(int eachFileSize) {
        this.eachFileSize = eachFileSize;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private transient Drawable label;
    private String uri="";

    public  String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public  Drawable getLabel() {
        return label;
    }

    public void setLabel(Drawable label) {
        this.label = label;
    }

}

package com.example.mysharecare;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MyViewModel extends ViewModel {
    Context context;
    String extra;
    private List<ModelClass> modelClassListApps ;
    private List<ModelClass> modelClassListVideos ;
    private List<ModelClass> modelClassListAudios ;
    private List<ModelClass> modelClassListImages ;
    private List<ModelClass> modelClassListFiles;

    public List<ModelClass> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<ModelClass> selectedItems) {
        this.selectedItems = selectedItems;
    }

    private List<ModelClass> selectedItems=new ArrayList<>();
    private Repository repository;

    public MyViewModel(Context context, String extra) {
        repository = new Repository(context, extra);

    }

    List<ModelClass> getAppList() {
        if(modelClassListApps!=null)
        return modelClassListApps;
        else return modelClassListApps = repository.getAppList();
    }

    List<ModelClass> getVideoList() {
        if(modelClassListVideos!=null)
        return modelClassListVideos;
        else  return modelClassListVideos = repository.getVideoList();
    }

    List<ModelClass> getAudioList() {
        if ((modelClassListAudios!=null))
        return modelClassListAudios;
        else return   modelClassListAudios = repository.getAudioList();
    }

    List<ModelClass> getImageList()
    {
        if(modelClassListImages!=null)
        return modelClassListImages;
        else  return modelClassListImages = repository.getImageList();

    }

    List<ModelClass> getFileList() {
        if(modelClassListFiles!=null)
        return modelClassListFiles;
        else return modelClassListFiles = repository.getFileList();
    }


}

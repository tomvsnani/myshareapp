package com.example.mysharecare;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MyViewModelFactory extends ViewModelProvider.NewInstanceFactory{
    Context context;
    String extra;
public MyViewModelFactory(Context context,String extra){
    this.context=context;
    this.extra=extra;
}
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MyViewModel(context,extra);
    }
}

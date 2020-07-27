package com.example.mysharecare;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPgaerAdapter extends FragmentStateAdapter {

SelectItemsToSendFragment fragment;
    public ViewPgaerAdapter(@NonNull Fragment fragmentActivity) {

      super(fragmentActivity);
        this.fragment= (SelectItemsToSendFragment) fragmentActivity;
    }

    @Override
    public Fragment createFragment(int position) {
        String extra="";
        Log.d("positionada", String.valueOf(position));
        switch (position){
            case 0:extra="app";
                break;
            case 1:extra="video";
                break;
            case 2:extra="audio";
                break;
            case 3:extra="images";
                break;
            case 4:extra="others";
            break;
        }
        SelectionCategoriesFragment selectionCategoriesFragment=new SelectionCategoriesFragment(extra,fragment);
        return selectionCategoriesFragment;

    }

    @Override
    public int getItemCount() {
        return 5;
    }
}

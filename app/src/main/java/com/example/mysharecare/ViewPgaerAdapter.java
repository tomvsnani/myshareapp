package com.example.mysharecare;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPgaerAdapter extends FragmentStateAdapter {

    SelectItemsToSendFragment fragment;

    public ViewPgaerAdapter(@NonNull Fragment fragmentActivity) {

        super(fragmentActivity);
        this.fragment = (SelectItemsToSendFragment) fragmentActivity;
    }

    @Override
    public Fragment createFragment(int position) {
        String extra = "";

        switch (position) {
            case 0:
                extra = AppConstants.Apps;
                break;
            case 1:
                extra = AppConstants.Videos;
                break;
            case 2:
                extra = AppConstants.Audios;
                break;
            case 3:
                extra = AppConstants.Images;
                break;
            case 4:
                extra = AppConstants.Files;
                break;
        }
        return new SelectionCategoriesFragment(extra, fragment);

    }

    @Override
    public int getItemCount() {
        return 5;
    }
}

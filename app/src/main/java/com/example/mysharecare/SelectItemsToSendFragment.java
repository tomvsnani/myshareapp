package com.example.mysharecare;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SelectItemsToSendFragment extends Fragment implements SelectionAdapter.SelectedItemsInterface {
    List<ModelClass> modelClassList = new ArrayList<>();
    Button sendButton;
    ViewPager2 viewPager2;
    TabLayout tableLayout;
    Button selectedItemsCountButton;
    Toolbar toolbar;


    public SelectItemsToSendFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_items_to_send, container, false);
        sendButton = v.findViewById(R.id.sendtoActivitybutton);
        viewPager2 = v.findViewById(R.id.viewpager);
        tableLayout = v.findViewById(R.id.tablayout);
        toolbar = v.findViewById(R.id.fargmenttosendfragmenttoolbar);
        ((AppCompatActivity) (getActivity())).setSupportActionBar(toolbar);
        selectedItemsCountButton = v.findViewById(R.id.selecteditemsbutton);
        viewPager2.setAdapter(new ViewPgaerAdapter(this));
//
        new TabLayoutMediator(tableLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                switch (position) {

                    case 0:
                        tab.setText(AppConstants.Apps);
                        break;
                    case 1:
                        tab.setText(AppConstants.Videos);
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;
                    case 2:
                        tab.setText(AppConstants.Audios);
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;
                    case 3:
                        tab.setText(AppConstants.Images);
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;
                    case 4:
                        tab.setText(AppConstants.Files);
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;

                }

            }
        }).attach();

        return v;
    }

    @Override
    public void SelectedItemsCallback(ModelClass modelClass, boolean addOrRemoveExtra) {

        if (addOrRemoveExtra)
            modelClassList.add(modelClass);
        else
            modelClassList.remove(modelClass);

        selectedItemsCountButton.setText(String.valueOf("Items ( " + modelClassList.size() + " )"));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modelClassList.size() > 0) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);

                    intent.putExtra(AppConstants.SEND_MODEL_LIST_EXTRA, (Serializable) modelClassList);
                    startActivity(intent);
                } else
                    Toast.makeText(getContext(), "Please select items to send .", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.mysharecare;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SelectItemsToSendFragment extends Fragment implements SelectionAdapter.SelectedItemsInterface {
    List<ModelClass> modelClassList = new ArrayList<>();
    Button sendButton;
    ViewPager2 viewPager2;
    TabLayout tableLayout;
    Button selectedItemsCountButton;


    public SelectItemsToSendFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_items_to_send, container, false);
        sendButton = v.findViewById(R.id.sendtoActivitybutton);
        viewPager2 = v.findViewById(R.id.viewpager);
        tableLayout = v.findViewById(R.id.tablayout);
        selectedItemsCountButton = v.findViewById(R.id.selecteditemsbutton);
        viewPager2.setAdapter(new ViewPgaerAdapter(this));
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 3 || position == 4) {
                    SelectionCategoriesFragment.i = position;
                }
                super.onPageSelected(position);
            }
        });
        new TabLayoutMediator(tableLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                Log.d("position", String.valueOf(position));
                switch (position) {

                    case 0:
                        tab.setText("Apps");
                        break;
                    case 1:
                        tab.setText("Videos");
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;
                    case 2:
                        tab.setText("Audio");
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;
                    case 3:
                        tab.setText("Images");
                        tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        break;
                    case 4:
                        tab.setText("Files");
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

                    intent.putExtra("sendItems", (Serializable) modelClassList);
                    startActivity(intent);
                } else
                    Toast.makeText(getContext(), "Please select items to send .", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
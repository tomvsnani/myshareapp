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

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SelectItemsToSendFragment extends Fragment implements SelectionAdapter.SelectedItemsInterface {
    List<ModelClass> modelClassList = new ArrayList<>();
    List<InputStream> inputStreams = new ArrayList<>();
    MutableLiveData<List<InputStream>> listMutableLiveData = new MutableLiveData<>();

    Button sendButton;
    ViewPager2 viewPager2;
    TabLayout tableLayout;


    public SelectItemsToSendFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_items_to_send, container, false);
        sendButton = v.findViewById(R.id.sendtoActivitybutton);
        viewPager2 = v.findViewById(R.id.viewpager);
        tableLayout = v.findViewById(R.id.tablayout);
        viewPager2.setAdapter(new ViewPgaerAdapter(this));
        new TabLayoutMediator(tableLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                Log.d("position", String.valueOf(position));
                switch (position) {

                    case 0:
                        tab.setText("Apps");
                        break;
                    case 1:
                        tab.setText("videos");
                        break;
                    case 2:
                        tab.setText("Audio");
                        break;
                    case 3:
                        tab.setText("others");
                        break;
                }

            }
        }).attach();

        return v;
    }


    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public void SelectedItemsCallback(ModelClass modelClass, boolean addOrRemoveExtra) {

       if(addOrRemoveExtra)
            modelClassList.add(modelClass);
       else
           modelClassList.remove(modelClass);

     // Log.d("typee", modelClass.getType()+modelClass.getLabel());
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), MainActivity.class);

                intent.putExtra("sendItems", (Serializable) modelClassList);
                startActivity(intent);
            }
        });
    }
}
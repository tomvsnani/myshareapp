package com.example.mysharecare;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectionCategoriesFragment extends Fragment {
    String extra;
    List<ModelClass> modelClassList = new ArrayList<>();
    SelectionAdapter selectionAdapter;
SelectItemsToSendFragment selectItemsToSendFragment;

    public SelectionCategoriesFragment(String extra,SelectItemsToSendFragment fragment) {
        Log.d("positionextra", String.valueOf(extra));
        this.extra = extra;
        this.selectItemsToSendFragment=fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private void retrieveFiles() {
        switch (extra) {
            case "app": {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                     //   List<ResolveInfo> resolveInfos = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                        List<ApplicationInfo> applicationInfoList = getActivity().getPackageManager().getInstalledApplications(0);
                        for (ApplicationInfo resolveInfo :applicationInfoList) {
                          if((resolveInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                          {
                              continue;
                          }
                            ModelClass modelClass = new ModelClass();
                            Log.d("position", resolveInfo.packageName);
                            String s = resolveInfo.publicSourceDir;
                            modelClass.setName(resolveInfo.loadLabel(getActivity().getPackageManager()).toString());
                            modelClass.setLabel(resolveInfo.loadIcon(getActivity().getPackageManager()));
                            modelClass.setUri(s);
                            modelClass.setType(extra);
                            modelClass.setSize(new File(resolveInfo.publicSourceDir).length());
                            modelClassList.add(modelClass);
                        }
                        sendDataToAdapter();
                    }
                });
                thread.start();

            }
            break;


            case "audio": {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] projection = new String[]{MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.DISPLAY_NAME,
                                MediaStore.Audio.Media.SIZE};
                        Log.d("positiona", "inaudio");
                        Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                projection, null, null, null
                        );
                        Log.d("positiona", String.valueOf(cursor.getCount()));
                        int id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                        int name = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                        int size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                        cursor.moveToFirst();
                        while (cursor.moveToNext()) {
                            Log.d("positiona", "audio");
                            ModelClass modelClass = new ModelClass();
                            Long idd = cursor.getLong(id);
                            String displayname = cursor.getString(name);
                           Long sizee = (long) cursor.getInt(size);
                            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idd);
                            modelClass.setName(displayname);
                            modelClass.setUri(uri.toString());
                            modelClass.setType(extra);
                            modelClass.setSize(sizee);
                            modelClassList.add(modelClass);
                        }
                        sendDataToAdapter();
                    }
                });
                thread.start();

            }
            break;


            case "video": {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] projection = new String[]{MediaStore.Video.Media._ID,
                                MediaStore.Video.Media.DISPLAY_NAME,
                                MediaStore.Video.Media.SIZE};
                        Log.d("positionv", "invideo");
                        Cursor cursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                projection, null, null, null
                        );
                        Log.d("positionv", String.valueOf(cursor.getCount()));
                        int id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                        int name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                        int size = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                        cursor.moveToFirst();
                        while (cursor.moveToNext()) {
                            Log.d("positionv", "video");
                            ModelClass modelClass = new ModelClass();
                            Long idd = cursor.getLong(id);
                            String displayname = cursor.getString(name);
                            Long sizee = (long) cursor.getInt(size);
                            Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, idd);
                            modelClass.setName(displayname);
                            modelClass.setUri(uri.toString());
                            modelClassList.add(modelClass);
                            modelClass.setSize(sizee);
                            modelClass.setType(extra);
                        }
                        sendDataToAdapter();
                    }
                });
                thread.start();

            }
            break;


            case "others": {

            }
            break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selection_categories, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerforcategoryselection);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        selectionAdapter = new SelectionAdapter(getContext(),selectItemsToSendFragment,extra);
        recyclerView.setAdapter(selectionAdapter);
        return v;
    }

    @Override
    public void onStart() {

        super.onStart();
        retrieveFiles();

    }

    private void sendDataToAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (modelClassList.size() > 0) {
                    Log.d("positionsubmitted", String.valueOf(modelClassList.size()));
                    Collections.sort(modelClassList, new Comparator<ModelClass>() {
                        @Override
                        public int compare(ModelClass modelClass, ModelClass t1) {
                            return modelClass.getName().compareTo(t1.getName());
                        }
                    });
                    selectionAdapter.submitList(modelClassList);
                }
            }
        });
    }
}

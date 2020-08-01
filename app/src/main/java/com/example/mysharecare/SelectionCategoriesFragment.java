package com.example.mysharecare;

import android.content.ContentUris;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import static com.example.mysharecare.Utils.getBitmapFromDrawable;
import static com.example.mysharecare.Utils.getBytesFromBitmap;

public class SelectionCategoriesFragment extends Fragment {
    String extra;
    SelectionAdapter selectionAdapter;
    SelectItemsToSendFragment selectItemsToSendFragment;
    RecyclerView recyclerview;
    Stack<PathModel> listStackFiles = new Stack<>();
    static int i = -1;
    ProgressBar progressBar;
    Toolbar toolbar;
    DisplayPathAdapter pathAdapter;
    List<PathModel> modelList = new ArrayList<>();

    public SelectionCategoriesFragment(String extra, SelectItemsToSendFragment fragment) {
        this.extra = extra;
        this.selectItemsToSendFragment = fragment;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listStackFiles.empty();

    }

    private void retrieveFiles() {
        final Repository repository = new Repository(getContext(), extra);
        switch (extra) {

                case AppConstants.Apps: {
                progressBar.setVisibility(View.VISIBLE);
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        sendDataToAdapter(repository.getAppList());
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                    }
                });
                thread.start();


            }
            break;


            case AppConstants.Audios: {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            sendDataToAdapter(repository.getAudioList());

                            progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                    thread.start();
                }

            break;


            case AppConstants.Videos: {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            sendDataToAdapter(repository.getVideoList());

                            progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                    thread.start();

                }

            break;


            case AppConstants.Files: {


                    sendDataToAdapter(repository.getFileList());

                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            }
            break;
            case AppConstants.Images: {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendDataToAdapter(repository.getImageList());

                            progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                    thread.start();


            }
            break;
        }


        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                handleBackstack(listStackFiles);

            }

            private void handleBackstack(Stack<PathModel> listStackFiles) {
                Log.d("backpressed", String.valueOf(listStackFiles.size()));

                if (listStackFiles.size() != 0) {
                    modelList.remove(listStackFiles.pop());

                    if (!listStackFiles.isEmpty()) {
                        if (listStackFiles.size() == 1) {
                            Toast.makeText(getContext(), "press back again to exit", Toast.LENGTH_SHORT).show();

                        }
                        PathModel pathModel = listStackFiles.peek();
                        selectionAdapter.submitList(pathModel.getList());
                        pathAdapter.submitList(modelList);
                    } else {
                        setEnabled(false);
                        if (getActivity() != null)
                            getActivity().onBackPressed();
                    }
                }
                if (listStackFiles.size() == 0) {
                    setEnabled(false);
                    if (getActivity() != null)
                        getActivity().onBackPressed();
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selection_categories, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerforcategoryselection);
        recyclerview = v.findViewById(R.id.showpathrecyclerview);
        LinearLayoutManager staggeredGridLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        selectionAdapter = new SelectionAdapter(this, selectItemsToSendFragment, extra);
        recyclerView.setAdapter(selectionAdapter);
        progressBar = v.findViewById(R.id.itemsloadingprogressbar);
        RecyclerView recyclerView1 = v.findViewById(R.id.showpathrecyclerview);
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        pathAdapter = new DisplayPathAdapter();
        recyclerView1.setAdapter(pathAdapter);


        if (extra.equals(AppConstants.Images) || extra.equals(AppConstants.Files)) {
            recyclerview.setVisibility(View.VISIBLE);
        } else recyclerview.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onStart() {

        super.onStart();
        if (getActivity() != null)
            retrieveFiles();

    }

    private void sendDataToAdapter(final List<ModelClass> modelClassList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (modelClassList.size() > 0) {
                    if (extra.equals(AppConstants.Files)) {
                        sortList(modelClassList);
                        Collections.sort(modelClassList, new Comparator<ModelClass>() {
                            @Override
                            public int compare(ModelClass modelClass, ModelClass t1) {
                                return t1.getType().compareTo(modelClass.getType());
                            }
                        });

                    } else if (!extra.equals(AppConstants.Images))
                        sortList(modelClassList);

                    if (extra.equals(AppConstants.Files) || extra.equals(AppConstants.Images)) {
                        PathModel pathModel = new PathModel(modelClassList, "Home");
                        modelList.add(pathModel);
                        selectionAdapter.submit(pathModel, modelList, modelClassList);
                    }
                    if (extra.equals(AppConstants.Videos) || extra.equals(AppConstants.Audios) || extra.equals(AppConstants.Apps))
                        selectionAdapter.submitList(modelClassList);
                }

            }
        });

    }


    private void sortList(List<ModelClass> modelClassList) {
        Collections.sort(modelClassList, new Comparator<ModelClass>() {
            @Override
            public int compare(ModelClass modelClass, ModelClass t1) {

                return modelClass.getName().compareTo(t1.getName());
            }
        });
    }


}


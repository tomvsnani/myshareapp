package com.example.mysharecare;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class SelectionCategoriesFragment extends Fragment {
    String extra;
    List<ModelClass> modelClassList = new ArrayList<>();
    SelectionAdapter selectionAdapter;
    SelectItemsToSendFragment selectItemsToSendFragment;
    static Stack<List<ModelClass>> listStack = new Stack<>();
    ProgressBar progressBar;

    public SelectionCategoriesFragment(String extra, SelectItemsToSendFragment fragment) {
        Log.d("positionextra", String.valueOf(extra));
        this.extra = extra;
        this.selectItemsToSendFragment = fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private void retrieveFiles() {
        switch (extra) {
            case "app": {
                if (modelClassList.size() == 0) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            //   List<ResolveInfo> resolveInfos = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                            List<ApplicationInfo> applicationInfoList = getActivity().getPackageManager().getInstalledApplications(0);
                            //   Collections.sort(applicationInfoList,new ApplicationInfo.DisplayNameComparator(getActivity().getPackageManager()));
                            for (final ApplicationInfo resolveInfo : applicationInfoList) {
                                if ((resolveInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                    continue;
                                }
                                final ModelClass modelClass = new ModelClass();
                                Log.d("position", resolveInfo.packageName);
                                String s = resolveInfo.publicSourceDir;
                                modelClass.setName(resolveInfo.loadLabel(getActivity().getPackageManager()).toString());
                                Thread thread1=new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(resolveInfo.loadIcon(getActivity().getPackageManager()))));

                                    }
                                });
                                thread1.start();
                                 modelClass.setUri(s);
                                modelClass.setType(extra);
                                modelClass.setSize(new File(resolveInfo.publicSourceDir).length());
                                modelClassList.add(modelClass);
                            }
                            sendDataToAdapter();
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
            }
            break;


            case "audio": {
                if (modelClassList.size() == 0) {
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
            }
            break;


            case "video": {
                if (modelClassList.size() == 0) {
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
                                modelClass.setSize(sizee);
                                modelClass.setType(extra);
                                modelClassList.add(modelClass);
                            }
                            sendDataToAdapter();
                            sendDataToAdapter();
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
            }
            break;


            case "others": {
                if (modelClassList.size() == 0) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    Log.d("filecontents", "s");
                    File[] s = file.listFiles();
                    for (File g : s) {
                        ModelClass modelClass = new ModelClass();
                        modelClass.setName(g.getName());
                        modelClass.setUri(g.getAbsolutePath());
                        modelClass.setSize(g.length());
                        if (!g.isFile())
                            modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(getActivity().getResources()
                                    .getDrawable(R.drawable.ic_baseline_movie_filter_24))));
                        else {
                            modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(getActivity().getResources()
                                    .getDrawable(R.drawable.ic_baseline_filter_vintage_24))));
                            modelClass.setType("others");

                           // Log.d("filesize", String.valueOf(g.length()));
                        }
                        modelClassList.add(modelClass);

                    }
                    sendDataToAdapter();

                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
            break;
        }
        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("backpressed", "yes");

                if (!listStack.empty()) {
                    listStack.pop();
                    if (!listStack.empty())
                        if (listStack.size() == 1)
                            Toast.makeText(getContext(), "press back again to exit", Toast.LENGTH_SHORT).show();
                    selectionAdapter.submitList(listStack.peek());
                } else {
                    setEnabled(false);
                    getActivity().onBackPressed();
                }

            }
        });
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        drawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        bitmap.recycle();
        byte[] bytes= byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selection_categories, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerforcategoryselection);
        LinearLayoutManager staggeredGridLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        selectionAdapter = new SelectionAdapter(getContext(), selectItemsToSendFragment, extra);
        recyclerView.setAdapter(selectionAdapter);
        progressBar = v.findViewById(R.id.itemsloadingprogressbar);
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

//                    Collections.sort(modelClassList, new Comparator<ModelClass>() {
//                        @Override
//                        public int compare(ModelClass modelClass, ModelClass t1) {
//                            return modelClass.getName().compareTo(t1.getName());
//                        }
//                    });

                    selectionAdapter.submit(modelClassList);
                }
            }
        });
    }
}

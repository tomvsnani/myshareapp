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

public class SelectionCategoriesFragment extends Fragment {
    String extra;
    List<ModelClass> modelClassList = new ArrayList<>();
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
        Log.d("positionextra", String.valueOf(extra));
        this.extra = extra;
        this.selectItemsToSendFragment = fragment;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listStackFiles.empty();
        Log.d("positionextrac", String.valueOf(extra));
    }

    private void retrieveFiles() {

        switch (extra) {
            case "app": {
                if (modelClassList.size() == 0) {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);

                                  List<ApplicationInfo> applicationInfoList = getActivity().getPackageManager().getInstalledApplications(0);
                               for (final ApplicationInfo resolveInfo : applicationInfoList) {
                                if ((resolveInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                    continue;
                                }
                                final ModelClass modelClass = new ModelClass();
                                Log.d("position", resolveInfo.packageName);
                                String s = resolveInfo.publicSourceDir;
                                if (getActivity() != null)
                                    modelClass.setName(resolveInfo.loadLabel(getActivity().getPackageManager()).toString());
                                Thread thread1 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getActivity() != null)
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
                                    MediaStore.Audio.Media.SIZE,
                                    MediaStore.Audio.Media.DATA
                            };
                            Log.d("positiona", "inaudio");
                            Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    projection, null, null, null
                            );
                            Log.d("positiona", String.valueOf(cursor.getCount()));
                            int id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                            int name = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                            int size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                            int data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                            cursor.moveToFirst();
                            while (cursor.moveToNext()) {
                                Log.d("positiona", "audio");
                                ModelClass modelClass = new ModelClass();
                                Long idd = cursor.getLong(id);
                                String displayname = cursor.getString(name);
                                Long sizee = (long) cursor.getInt(size);
                                String dataa = cursor.getString(data);
                                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idd);
                                modelClass.setName(displayname);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    modelClass.setUri(uri.toString());
                                } else modelClass.setUri(dataa);
                                modelClass.setType(extra);
                                modelClass.setId(idd);
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
                                    MediaStore.Video.Media.SIZE,
                                    MediaStore.Video.Media.DATA
                            };
                            Log.d("positionv", "invideo");
                            Cursor cursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    projection, null, null, null
                            );
                            Log.d("positionv", String.valueOf(cursor.getCount()));
                            int id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                            int name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                            int size = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                            int data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                            cursor.moveToFirst();
                            while (cursor.moveToNext()) {
                                Log.d("positionv", "video");
                                ModelClass modelClass = new ModelClass();
                                Long idd = cursor.getLong(id);
                                String displayname = cursor.getString(name);
                                Long sizee = (long) cursor.getInt(size);
                                String dataa = cursor.getString(data);
                                Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, idd);
                                modelClass.setName(displayname);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    modelClass.setUri(uri.toString());
                                } else modelClass.setUri(dataa);
                                modelClass.setSize(sizee);
                                modelClass.setType(extra);
                                modelClass.setId(idd);
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
                        if (!g.isFile()) {
                            modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(getActivity().getResources()
                                    .getDrawable(R.drawable.ic_baseline_movie_filter_24))));
                            modelClass.setType("dir");
                        } else {
                            modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(getActivity().getResources()
                                    .getDrawable(R.drawable.ic_baseline_filter_vintage_24))));
                            modelClass.setType("others");

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
            case "images": {
                if (modelClassList.size() == 0) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String[] projection = new String[]{MediaStore.Images.Media._ID,
                                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                                    MediaStore.Images.Media._ID,
                                    MediaStore.Images.Media.BUCKET_ID
                                    , MediaStore.Images.Media.DATA

                            };

                            Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
                            );


                            int id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                            int name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                            int bucketId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                            int data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                            List<String> bucketNameList = new ArrayList<>();


                            cursor.moveToFirst();
                            while (cursor.moveToNext()) {


                                final ModelClass modelClass = new ModelClass();
                                Long idd = cursor.getLong(id);
                                String displayname = cursor.getString(name);
                                String bucketIdd = cursor.getString(bucketId);
                                String dataa = cursor.getString(data);

                                if (!bucketNameList.contains(bucketIdd)) {
                                    bucketNameList.add(bucketIdd);

                                    final Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idd);

                                    modelClass.setName(displayname);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        modelClass.setUri(uri.toString());
                                    } else modelClass.setUri(dataa);

                                    modelClass.setType("album");

                                    modelClass.setId(idd);
                                    modelClass.setBucketId(bucketIdd);
                                    modelClassList.add(modelClass);


                                }
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
        }


        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
              //  Log.d("positionextraa", String.valueOf(i));


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
                        PathModel pathModel=listStackFiles.peek();
                        selectionAdapter.submitList(pathModel.getList());
                        //modelList.remove(pathModel);
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

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        bitmap.recycle();
        byte[] bytes = byteArrayOutputStream.toByteArray();
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


        if (extra.equals("images") || extra.equals("others")) {
            recyclerview.setVisibility(View.VISIBLE);
        } else recyclerview.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onStart() {

        super.onStart();
        if(getActivity()!=null)
        retrieveFiles();

    }

    private void sendDataToAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (modelClassList.size() > 0) {
                    if (extra.equals("others")) {
                        sortList();
                        Collections.sort(modelClassList, new Comparator<ModelClass>() {
                            @Override
                            public int compare(ModelClass modelClass, ModelClass t1) {
                                return modelClass.getType().compareTo(t1.getType());
                            }
                        });

                    } else if (!extra.equals("images"))
                        sortList();

                    if (extra.equals("others") || extra.equals("images")) {
                        PathModel pathModel = new PathModel(modelClassList, "Home");
                        modelList.add(pathModel);
                        selectionAdapter.submit(pathModel, modelList, modelClassList);
                    }
                    if (extra.equals("video") || extra.equals("audio") || extra.equals("app"))
                        selectionAdapter.submitList(modelClassList);
                }

            }
        });

    }


    private void sortList() {
        Collections.sort(modelClassList, new Comparator<ModelClass>() {
            @Override
            public int compare(ModelClass modelClass, ModelClass t1) {

                return modelClass.getName().compareTo(t1.getName());
            }
        });
    }


}


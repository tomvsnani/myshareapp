package com.example.mysharecare;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.mysharecare.Utils.getBitmapFromDrawable;
import static com.example.mysharecare.Utils.getBytesFromBitmap;

public class Repository {
    Context context;
    String extra;
    List<ModelClass> modelClassListApps = new ArrayList<>();
    List<ModelClass> modelClassListVideos = new ArrayList<>();
    List<ModelClass> modelClassListAudios = new ArrayList<>();
    List<ModelClass> modelClassListImages = new ArrayList<>();
    List<ModelClass> modelClassListFiles = new ArrayList<>();

    public Repository(Context context, String extra) {
        this.context = context;
        this.extra = extra;
    }

    public List<ModelClass> getImageList() {
        String[] projection = new String[]{MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID
                , MediaStore.Images.Media.DATA

        };

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
        assert cursor != null;
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

                modelClass.setType(AppConstants.Albums);

                modelClass.setId(idd);
                modelClass.setBucketId(bucketIdd);
                modelClassListImages.add(modelClass);


            }
        }
        return modelClassListImages;
    }

    public List<ModelClass> getVideoList() {
        String[] projection = new String[]{MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA
        };

        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
        );

        int id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        int name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
        int size = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
        int data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {

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
            modelClass.setType(AppConstants.Videos);
            modelClass.setId(idd);
            modelClassListVideos.add(modelClass);
        }
        return modelClassListVideos;
    }

    public List<ModelClass> getAudioList() {
        String[] projection = new String[]{MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = context.getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
        );

        int id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        int name = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
        int size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
        int data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {

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
            modelClass.setType(AppConstants.Audios);
            modelClass.setId(idd);
            modelClass.setSize(sizee);
            modelClassListAudios.add(modelClass);
        }
        return modelClassListAudios;
    }

    public List<ModelClass> getAppList() {

        List<ApplicationInfo> applicationInfoList = context.getPackageManager().getInstalledApplications(0);
        for (final ApplicationInfo resolveInfo : applicationInfoList) {
            if ((resolveInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue;
            }
            final ModelClass modelClass = new ModelClass();

            String s = resolveInfo.publicSourceDir;
            if (context != null)
                modelClass.setName(resolveInfo.loadLabel(context.getPackageManager()).toString());
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (context != null)
                        modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(resolveInfo.loadIcon(context.getPackageManager()))));

                }
            });
            thread1.start();
            modelClass.setUri(s);
            modelClass.setType(AppConstants.Apps);
            modelClass.setSize(new File(resolveInfo.publicSourceDir).length());
            modelClassListApps.add(modelClass);
        }
        return modelClassListApps;

    }


    public List<ModelClass> getFileList() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

        File[] s = file.listFiles();
        for (File g : s) {
            ModelClass modelClass = new ModelClass();
            modelClass.setName(g.getName());
            modelClass.setUri(g.getAbsolutePath());
            modelClass.setSize(g.length());
            if (!g.isFile()) {
                modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_baseline_movie_filter_24))));
                modelClass.setType(AppConstants.Directory);
            } else {
                modelClass.setBytes(getBytesFromBitmap(getBitmapFromDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_baseline_filter_vintage_24))));
                modelClass.setType(AppConstants.Files);

            }
            modelClassListFiles.add(modelClass);

        }
        return modelClassListFiles;
    }

}

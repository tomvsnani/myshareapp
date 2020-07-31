package com.example.mysharecare;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectionAdapter extends ListAdapter<ModelClass, SelectionAdapter.SelectionViewHolder> {
    Context context;
    SelectedItemsInterface selectedItemsInterface;
    String type;
    SelectionCategoriesFragment selectionCategoriesFragment;
    MutableLiveData<Integer> selectedPosition = new MutableLiveData<>();
    static List<Integer> selectedList = new ArrayList<>();
    String path = "";

    protected SelectionAdapter(SelectionCategoriesFragment context, final SelectedItemsInterface selectedItemsInterface, String type) {
        super(modelClassItemCallback);
        this.selectionCategoriesFragment = context;
        this.context = context.getContext();
        this.selectedItemsInterface = selectedItemsInterface;
        this.type = type;


        selectedPosition.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d("integer",""+integer);
                if (!selectedList.contains(integer)) {
                    selectedList.add(integer);
                    selectedItemsInterface.SelectedItemsCallback(getCurrentList().get(integer), true);

                } else {
                    selectedList.remove(integer);
                    selectedItemsInterface.SelectedItemsCallback(getCurrentList().get(integer), false);
                }

                notifyItemChanged(integer);
            }
        });
    }


    @NonNull
    @Override
    public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new SelectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.selectionadapterrow
                , parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SelectionViewHolder holder, int position) {

        final ModelClass modelClass = getCurrentList().get(position);

        holder.nametextview.setText(modelClass.getName());

        if (selectedList.contains(position) && !(modelClass.getType().equals("dir") || modelClass.getType().equals("album"))) {

            holder.linearLayout.setBackground(context.getResources().getDrawable(R.drawable.buttonshape));

        } else {
            holder.linearLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.sizeTextView.setText(String.format("%.1f Mb", (double) (modelClass.getSize()) / 1000000));
        if (!(modelClass.getType().equals("dir") || modelClass.getType().equals("album"))) {
            holder.sizeTextView.setVisibility(View.VISIBLE);
        } else holder.sizeTextView.setVisibility(View.GONE);


        if (modelClass.getBytes() != null && modelClass.getBytes().length > 0)
            Glide.with(context).load(getImageFromBytes(modelClass)).into(holder.iconimageview);

        if (modelClass.getType().equals("audio") || modelClass.getType().equals("video") || modelClass.getType().equals("image") ||
                modelClass.getType().equals("album")) {

            getBitmap(modelClass, holder);
        }

    }

    private void getBitmap(ModelClass modelClass, SelectionViewHolder holder) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!modelClass.getType().equals("audio")) {
                try {


                    Glide.with(context)
                            .load(context.getContentResolver()
                                    .loadThumbnail(Uri.parse(modelClass.getUri()), new Size(100, 100),
                                            new CancellationSignal()))
                            .into(holder.iconimageview);

                } catch (IOException e) {


                    Glide.with(context).load(R.drawable.ic_baseline_movie_filter_24).into(holder.iconimageview);
                    e.printStackTrace();
                }
            }
        }
        //////// versions below android 10 ////
        else {

            if (modelClass.getType().equals("image") || modelClass.getType().equals("album")) {
                Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver()
                        , modelClass.getId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
                Glide.with(context)
                        .load(bitmap)
                        .into(holder.iconimageview);

            }
            if (modelClass.getType().equals("video")) {

                Glide.with(context)
                        .load(MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), modelClass.getId()
                                , MediaStore.Images.Thumbnails.MICRO_KIND, null))
                        .into(holder.iconimageview);

            }
        }
        if (modelClass.getType().equals("audio"))
            Glide.with(context).load(R.drawable.ic_baseline_music_note_24).into(holder.iconimageview);

    }

    private Bitmap getImageFromBytes(ModelClass modelClass) {
        return BitmapFactory.decodeByteArray(modelClass.getBytes(), 0, modelClass.getBytes().length);
    }

    class SelectionViewHolder extends RecyclerView.ViewHolder {
        TextView nametextview;
        ImageView iconimageview;
        LinearLayout linearLayout;
        TextView sizeTextView;


        public SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            nametextview = itemView.findViewById(R.id.nametextview);
            iconimageview = itemView.findViewById(R.id.icontextview);
            linearLayout = itemView.findViewById(R.id.idlinear);
            sizeTextView = itemView.findViewById(R.id.sizetextview);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ModelClass modelClass = getCurrentList().get(getAdapterPosition());
                    Log.d("enteredd", modelClass.getUri());
                    if (type.equals("others")) {

                        File file = new File(getCurrentList().get(getAdapterPosition()).getUri());
                        if (file.isDirectory()) {
                            List<ModelClass> modelClassList = new ArrayList<>();
                            File[] files = file.listFiles();
                            for (File g : files) {
                                ModelClass modelClassInner = new ModelClass();
                                modelClassInner.setName(g.getName());
                                modelClassInner.setUri(g.getAbsolutePath());
                                modelClassInner.setSize(g.length());
                                Log.d("filesize", String.valueOf(g.length()));
                                if (!g.isFile()) {
                                    modelClassInner.setBytes(getBytesFromBitmap(getBitmapFromDrawable(context.getResources()
                                            .getDrawable(R.drawable.ic_baseline_movie_filter_24))));
                                    modelClassInner.setType("dir");
                                } else {
                                    modelClassInner.setBytes(getBytesFromBitmap(getBitmapFromDrawable(context.getResources()
                                            .getDrawable(R.drawable.ic_baseline_filter_vintage_24))));
                                    modelClassInner.setType("others");

                                    Log.d("filesize", String.valueOf(g.length()));
                                }

                                modelClassList.add(modelClassInner);

                            }
                            PathModel pathModel = new PathModel(modelClassList, modelClass.getName());
                            selectionCategoriesFragment.modelList.add(pathModel);
                            submit(pathModel, selectionCategoriesFragment.modelList, modelClassList);
                        } else {
                            Log.d("filesize", String.valueOf(file.length()));

                            selectedPosition.setValue(getAdapterPosition());

                        }

                    } else if (modelClass.getType().equals("album")) {

                        final ArrayList<ModelClass> modelClassList = new ArrayList<>();
                        //  final List<PathModel> pathModelList=selectionCategoriesFragment.modelList;


                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String[] projection = new String[]{MediaStore.Images.Media._ID,
                                        MediaStore.Images.Media.DISPLAY_NAME,
                                        MediaStore.Images.Media.SIZE,
                                        MediaStore.Images.Media.DATA
                                };

                                Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        projection, MediaStore.Images.ImageColumns.BUCKET_ID + " =? ", new String[]{modelClass.getBucketId()},
                                        MediaStore.Images.Media.DATE_TAKEN + " DESC"
                                );

                                int id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                                int name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                                int size = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);


                                while (cursor.moveToNext()) {
                                    ModelClass modelClass = new ModelClass();
                                    Long idd = cursor.getLong(id);
                                    String displayname = cursor.getString(name);
                                    Long sizee = (long) cursor.getInt(size);
                                    String urii = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idd);
                                    modelClass.setName(displayname);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        modelClass.setUri(uri.toString());
                                    } else
                                        modelClass.setUri(urii);
                                    modelClass.setSize(sizee);
                                    modelClass.setType("image");
                                    modelClass.setId(idd);
                                    modelClassList.add(modelClass);
                                }
                                PathModel pathModel = new PathModel(modelClassList, modelClass.getName());
                                selectionCategoriesFragment.modelList.add(pathModel);
                                submit(pathModel, selectionCategoriesFragment.modelList, modelClassList);

                            }
                        });
                        thread.start();

                    } else {

                        selectedPosition.setValue(getAdapterPosition());

                    }

                }

            });

        }
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
    public void submitList(@Nullable List<ModelClass> list) {

        super.submitList(new ArrayList<ModelClass>(list));

    }

    public void submit(PathModel pathModel, List<PathModel> modelList, List<ModelClass> modelClassList) {

        selectionCategoriesFragment.pathAdapter.submitList(modelList);

        selectionCategoriesFragment.listStackFiles.push(pathModel);
        submitList(modelClassList);

    }

    @Override
    public int getItemCount() {
        Log.d("positionsize", String.valueOf(getCurrentList().size()));
        return Math.max(0, getCurrentList().size());
    }

    interface SelectedItemsInterface {
        public void SelectedItemsCallback(ModelClass modelClass, boolean isAdd);
    }


    public static DiffUtil.ItemCallback<ModelClass> modelClassItemCallback = new DiffUtil.ItemCallback<ModelClass>() {
        @Override
        public boolean areItemsTheSame(@NonNull ModelClass oldItem, @NonNull ModelClass newItem) {
            return checkIfSame(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ModelClass oldItem, @NonNull ModelClass newItem) {
            return checkIfSame(oldItem, newItem);
        }
    };

    public static boolean checkIfSame(ModelClass old, ModelClass newItem) {
        if (old != null && newItem != null)
            return old.getUri().equals(newItem.getUri()) && old.getName().equals(newItem.getName());
        else return true;
    }
}

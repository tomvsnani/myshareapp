package com.example.mysharecare;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SendingFilesAdapter extends ListAdapter<ModelClass, SendingFilesAdapter.SendingFilesViewHolder> {
    Context context;
    int progressForFileBeingSent = -1;
    PackageManager packageManager;
    PackageInfo packageInfo;
    HashMap<Integer, Integer> progressValue = new HashMap<Integer, Integer>();
    MutableLiveData<String> connectionCloseLiveData=new MutableLiveData<>();


    protected SendingFilesAdapter(Context context) {

        super(modelClassItemCallback);
        this.context = context;
        packageManager=context.getPackageManager();
    }

    @NonNull
    @Override
    public SendingFilesAdapter.SendingFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new SendingFilesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sendingfilesrowlayout
                , parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull SendingFilesAdapter.SendingFilesViewHolder holder, int position) {

        ModelClass modelClass = getCurrentList().get(position);

        int maxSize = (int) (modelClass.getSize() / (1024 * 1024));
        if (maxSize == 0) {
            holder.progressBar.setMax(1);
        } else holder.progressBar.setMax(maxSize);

        holder.nametextview.setText(modelClass.getName());

        if(modelClass.getBytes()!=null && modelClass.getBytes().length>0)

            holder.iconimageview.setImageBitmap(getImageFromBytes(modelClass));

        if(modelClass.getType().equals(AppConstants.Audios))
            Glide.with(context).load(R.drawable.ic_baseline_music_note_24).into(holder.iconimageview);

        if(modelClass.getType().equals(AppConstants.Videos))
            Glide.with(context).load(R.drawable.ic_baseline_movie_filter_24).into(holder.iconimageview);

        if(modelClass.getType().equals(AppConstants.Images))
            Glide.with(context).load(R.drawable.ic_baseline_image_24).into(holder.iconimageview);

        if (progressValue.size() > 0)
            if (progressValue.containsKey(position)) {
                int progressStatus = progressValue.get(position) / (1024 * 1024);

                if (progressStatus == 0) {

                    holder.progressBar.setProgress(1);
                } else {

                    holder.progressBar.setProgress(progressStatus);
                }

                double i = ((double) progressValue.get(position) / ((double) (1024 * 1024)));

                holder.sizeCountTextview.setText(String.format(Locale.getDefault(), "%.2fmb /%.2fmb",
                        i,
                        (double) modelClass.getSize() / (1024 * 1024)));
            } else {
                holder.progressBar.setProgress(0);
                holder.sizeCountTextview.setText(String.format("0/%d", modelClass.getSize() / (1024 * 1024)));
            }

    }
    private Bitmap getImageFromBytes(ModelClass modelClass) {
        return BitmapFactory.decodeByteArray(modelClass.getBytes(),0,modelClass.getBytes().length);
    }
//    private void loadAppIcon(ModelClass modelClass) {
//        packageInfo=packageManager.getPackageArchiveInfo(modelClass.getUri(),0);
//        packageInfo.applicationInfo.publicSourceDir=modelClass.getUri();
//        packageInfo.applicationInfo.sourceDir=modelClass.getUri();
//        modelClass.setLabel(packageInfo.applicationInfo.loadIcon(packageManager));
//    }

    class SendingFilesViewHolder extends RecyclerView.ViewHolder {
        TextView nametextview;
        TextView sizeCountTextview;
        ImageView iconimageview;
        ProgressBar progressBar;



        public SendingFilesViewHolder(@NonNull View itemView) {
            super(itemView);
            nametextview = itemView.findViewById(R.id.filetransfernametextview);
            iconimageview = itemView.findViewById(R.id.filetransferimageview);
            sizeCountTextview = itemView.findViewById(R.id.showfileprogressscounttextview);
            progressBar = itemView.findViewById(R.id.eachfileprogressbar);


        }
    }

    @Override
    public void submitList(@Nullable List<ModelClass> list) {

        super.submitList(list == null ? null : new ArrayList<ModelClass>(list));


    }

    public void setProgress(int progressForFileBeingSent, int progress) {
        this.progressForFileBeingSent = progressForFileBeingSent;
        progressValue.put(progressForFileBeingSent, progress);
        notifyItemChanged(progressForFileBeingSent);


    }


    @Override
    public int getItemCount() {

        return Math.max(0, getCurrentList().size());
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
        return old.getUri().equals(newItem.getUri()) && old.getName().equals(newItem.getName()) && old.getSize().compareTo(newItem.getSize()) == 0
                ;
    }
}

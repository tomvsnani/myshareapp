package com.example.mysharecare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectionAdapter extends ListAdapter<ModelClass, SelectionAdapter.SelectionViewHolder> {
    Context context;
    SelectedItemsInterface selectedItemsInterface;
    String type;
    MutableLiveData<Integer> selectedPosition = new MutableLiveData<>();
    List<Integer> selectedList = new ArrayList<>();

    protected SelectionAdapter(Context context, final SelectedItemsInterface selectedItemsInterface, String type) {
        super(modelClassItemCallback);

        this.context = context;
        this.selectedItemsInterface = selectedItemsInterface;
        this.type = type;

        selectedPosition.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (!selectedList.contains(integer)) {
                    selectedList.add(integer);
                    selectedItemsInterface.SelectedItemsCallback(getCurrentList().get(integer),true);

                }
                else{
                    selectedList.remove(integer);
                    selectedItemsInterface.SelectedItemsCallback(getCurrentList().get(integer),false);
                }

                notifyItemChanged(integer);
            }
        });
    }
    public SelectionAdapter(String type){

        super(modelClassItemCallback);
        this.type=type;

    }



    @NonNull
    @Override
    public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(type.equals("fileprogress"))
            return new SelectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sendingfilesrowlayout
                    , parent, false));
        else
        return new SelectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.selectionadapterrow
                , parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {

        ModelClass modelClass =  getCurrentList().get(position);

        if (selectedList.contains(position)) {

           holder.linearLayout.setBackgroundColor(Color.RED);

        } else {
            holder.linearLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.nametextview.setText(modelClass.getName());
        if (modelClass.getLabel() != null)
            holder.iconimageview.setImageDrawable(modelClass.getLabel());

    }

    class SelectionViewHolder extends RecyclerView.ViewHolder {
        TextView nametextview;
        ImageView iconimageview;
        LinearLayout linearLayout;


        public SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            nametextview = itemView.findViewById(R.id.nametextview);
            iconimageview = itemView.findViewById(R.id.icontextview);
            linearLayout = itemView.findViewById(R.id.idlinear);
            // linearLayout.setBackgroundColor(Color.RED);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!type.equals("fileprogress")) {
                        if (type.equals("others")) {
                            File file = new File(getCurrentList().get(getAdapterPosition()).getUri());
                            if (file.isDirectory()) {
                                List<ModelClass> modelClassList = new ArrayList<>();
                                File[] files = file.listFiles();
                                for (File g : files) {
                                    ModelClass modelClass = new ModelClass();
                                    modelClass.setName(g.getName());
                                    modelClass.setUri(g.getAbsolutePath());
                                    modelClass.setSize(g.length());
                                    Log.d("filesize", String.valueOf(g.length()));
                                    if (!g.isFile())
                                        modelClass.setLabel(context.getResources()
                                                .getDrawable(R.drawable.ic_baseline_movie_filter_24));
                                    else {
                                        modelClass.setLabel(context.getResources()
                                                .getDrawable(R.drawable.ic_baseline_filter_vintage_24));
                                        modelClass.setType("others");

                                        Log.d("filesize", String.valueOf(g.length()));
                                    }

                                    modelClassList.add(modelClass);

                                }
                                submit(modelClassList);
                            } else {
                                Log.d("filesize", String.valueOf(file.length()));

                                selectedPosition.setValue(getAdapterPosition());

                            }

                        } else {

                            ModelClass modelClass = getCurrentList().get(getAdapterPosition());
                            Log.d("positionn", modelClass.getName()+ modelClass.getType()+"  "+modelClass.getSize()+"  "+modelClass.getUri()+modelClass.getLabel());
                            selectedPosition.setValue(getAdapterPosition());
                            Toast.makeText(context, "selected", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });

        }
    }

    @Override
    public void submitList(@Nullable List<ModelClass> list) {

        super.submitList(new ArrayList<ModelClass>(list));

    }

    public void submit(List<ModelClass> list) {
        if (type.equals("others"))
            SelectionCategoriesFragment.listStack.push(list);
        submitList(list);

    }

    @Override
    public int getItemCount() {
        Log.d("positionsize", String.valueOf(getCurrentList().size()));
        return Math.max(0, getCurrentList().size());
    }

    interface SelectedItemsInterface {
        public void SelectedItemsCallback(ModelClass modelClass,boolean isAdd);
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
        return old.getUri().equals(newItem.getUri()) && old.getName().equals(newItem.getName())
                ;
    }
}

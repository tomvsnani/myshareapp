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

import java.util.ArrayList;
import java.util.List;

public class SelectionAdapter extends ListAdapter<ModelClass, SelectionAdapter.SelectionViewHolder> {
    Context context;
    SelectedItemsInterface selectedItemsInterface;
    String type;
    MutableLiveData<Integer> selectedPosition = new MutableLiveData<>();
    List<Integer> selectedList = new ArrayList<>();
    int[] array = new int[5];

    protected SelectionAdapter(Context context, SelectedItemsInterface selectedItemsInterface, String type) {
        super(modelClassItemCallback);

        this.context = context;
        this.selectedItemsInterface = selectedItemsInterface;
        this.type = type;

        selectedPosition.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (!selectedList.contains(integer)) {
                    selectedList.add(integer);

                }
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
    public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {

        ModelClass modelClass = (ModelClass) getCurrentList().get(position);
        if (selectedList.contains(position)) {
            ((SelectionViewHolder) (holder)).nametextview.setBackgroundColor(Color.RED);
        }
        ((SelectionViewHolder) (holder)).nametextview.setText(modelClass.getName());
        if (modelClass.getLabel() != null)
            ((SelectionViewHolder) (holder)).iconimageview.setImageDrawable(modelClass.getLabel());

    }

    class SelectionViewHolder extends RecyclerView.ViewHolder {
        TextView nametextview;
        ImageView iconimageview;
        LinearLayout linearLayout;


        public SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            nametextview = itemView.findViewById(R.id.nametextview);
            iconimageview = itemView.findViewById(R.id.icontextview);
            linearLayout=itemView.findViewById(R.id.idlinear);
            linearLayout.setBackgroundColor(Color.RED);
          linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ModelClass modelClass = getCurrentList().get(getAdapterPosition());
                    selectedItemsInterface.SelectedItemsCallback(modelClass);
                    // selectedPosition.setValue(getAdapterPosition());
                    Toast.makeText(context, "selected", Toast.LENGTH_SHORT).show();

                }
            });

        }
    }

    @Override
    public void submitList(@Nullable List<ModelClass> list) {
        Log.d("positionsizee", String.valueOf(list.size()));
        super.submitList(new ArrayList<ModelClass>(list));

    }

    @Override
    public int getItemCount() {
        Log.d("positionsize", String.valueOf(getCurrentList().size()));
        return Math.max(0, getCurrentList().size());
    }

    interface SelectedItemsInterface {
        public void SelectedItemsCallback(ModelClass modelClass);
    }


    public static   DiffUtil.ItemCallback<ModelClass> modelClassItemCallback=new DiffUtil.ItemCallback<ModelClass>() {
        @Override
        public boolean areItemsTheSame(@NonNull ModelClass oldItem, @NonNull ModelClass newItem) {
            return checkIfSame(oldItem,newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ModelClass oldItem, @NonNull ModelClass newItem) {
            return checkIfSame(oldItem, newItem);
        }
    };
    public static boolean checkIfSame(ModelClass old,ModelClass newItem){
        return old.getUri().equals(newItem.getUri()) && old.getLabel().equals(newItem.getLabel()) && old.getName().equals(newItem.getName());
    }
}

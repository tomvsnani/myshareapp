package com.example.mysharecare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DisplayPathAdapter extends ListAdapter<PathModel, DisplayPathAdapter.PathViewholder> {
    protected DisplayPathAdapter() {
        super(PathModel.pathDiffUtil);
    }

    @NonNull
    @Override
    public PathViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.pathrowlayout,parent,false);

        return new PathViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PathViewholder holder, int position) {
        PathModel pathModel=getCurrentList().get(position);
        holder.pathTextView.setText(pathModel.getPath());

    }

    @Override
    public void submitList(@Nullable List<PathModel> list) {
        super.submitList(list==null?null:new ArrayList<PathModel>(list));
    }

    class PathViewholder extends RecyclerView.ViewHolder{
        TextView pathTextView;
        ImageView pathImageView;
        public PathViewholder(@NonNull View itemView) {
            super(itemView);
            pathTextView=itemView.findViewById(R.id.pathtextview);
            pathImageView=itemView.findViewById(R.id.pathimageview);

        }
    }
}

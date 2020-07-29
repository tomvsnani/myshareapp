package com.example.mysharecare;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FileProgressFragment extends Fragment  {


    List<ModelClass> modelClassList;
    Socket socket;
    Boolean isReceiver;
    ProgressBar progressBar;
    TextView filesSendReceivedTextview;
    TextView filesRemainingTextview;
    TextView timeRemainingTextview;
    TextView sendingHeadingTextView;
    SendingFilesAdapter sendingFilesAdapter;

    int totalFilesSize;

    TrafficStats trafficStats;

    FileTransferService fileTransferService;

    public FileProgressFragment(List<ModelClass> modelClassList, Socket socket, Boolean isReceiver) {
        this.modelClassList = modelClassList;
        this.socket = socket;
        this.isReceiver = isReceiver;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (modelClassList != null)
            for (ModelClass modelClass : modelClassList) {
                totalFilesSize += modelClass.getSize();
            }
        trafficStats = new TrafficStats();


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                FileTransferService.LocalBinder localBinder = (FileTransferService.LocalBinder) iBinder;
                fileTransferService = localBinder.getService();
                Log.d("connected","yess");
                fileTransferService.setServiceArguments(modelClassList,socket,isReceiver,FileProgressFragment.this);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        Intent intent = new Intent(getContext(), FileTransferService.class);
        intent.setAction("start");
        if (getActivity() != null) {
            getActivity().startService(intent);
            getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_file_progress, container, false);

        initializeViews(v);



        return v;
    }


    private void initializeViews(View v) {
        RecyclerView recyclerView = v.findViewById(R.id.sendingfilesrecyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        sendingFilesAdapter = new SendingFilesAdapter(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(sendingFilesAdapter);
        recyclerView.setItemAnimator(null);
        progressBar = v.findViewById(R.id.totalfilessendingprogressbar);
        filesRemainingTextview = v.findViewById(R.id.remainingfilesextview);
        timeRemainingTextview = v.findViewById(R.id.timeremailingsextview);
        filesSendReceivedTextview = v.findViewById(R.id.sentfilestextview);
        sendingHeadingTextView = v.findViewById(R.id.sendingextview);

        progressBar.setMax(totalFilesSize);


    }













}
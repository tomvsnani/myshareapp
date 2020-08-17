package com.example.mysharecare;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


public class FileProgressFragment extends Fragment {


    List<ModelClass> modelClassList;
    Socket socket;
    Boolean isReceiver;
    ProgressBar progressBar;
    TextView filesSendReceivedTextview;
    TextView filesRemainingTextview;
    TextView timeRemainingTextview;
    TextView sendingHeadingTextView;
    SendingFilesAdapter sendingFilesAdapter;
    Toolbar toolbar;
    Button cancelTransferButton;
    ServiceConnection serviceConnection;

    int totalFilesSize;

    TrafficStats trafficStats;

    FileTransferService fileTransferService;
    WifiP2pManager.Channel channel;
    WifiP2pManager wifiP2pManager;
    Intent intent;

    public FileProgressFragment(List<ModelClass> modelClassList, Socket socket, Boolean isReceiver, WifiP2pManager.Channel channel
            , WifiP2pManager wifiP2pManager) {
        this.modelClassList = modelClassList;
        this.socket = socket;
        this.isReceiver = isReceiver;
        this.channel = channel;
        this.wifiP2pManager = wifiP2pManager;
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            try {
                fileTransferService.closeConnections();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) (getActivity())).setSupportActionBar(toolbar);
        ((AppCompatActivity) (getActivity())).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) (getActivity())).getSupportActionBar().setDisplayShowTitleEnabled(false);


        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                FileTransferService.LocalBinder localBinder = (FileTransferService.LocalBinder) iBinder;
                fileTransferService = localBinder.getService();

                fileTransferService.setServiceArguments(modelClassList, socket, isReceiver, FileProgressFragment.this);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        intent = new Intent(getContext(), FileTransferService.class);
        intent.setAction("start");
        if (getActivity() != null) {
            getActivity().startService(intent);
            getActivity().getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_file_progress, container, false);
        setHasOptionsMenu(true);
        initializeViews(v);

        if (isReceiver) {
            sendingHeadingTextView.setText("Files Received :");
        } else sendingHeadingTextView.setText("Files sent :");

        cancelTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileTransferService != null) {
                    try {
                        fileTransferService.closeConnections();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

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
        cancelTransferButton = v.findViewById(R.id.canceltransferbutton);
        toolbar = v.findViewById(R.id.fileprogressfragmenttoolbar);
        progressBar.setMax(totalFilesSize);


    }


}
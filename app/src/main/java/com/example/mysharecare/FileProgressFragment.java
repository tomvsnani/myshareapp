package com.example.mysharecare;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
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
import java.net.Socket;
import java.util.List;


public class FileProgressFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    List<ModelClass> modelClassList;
    Socket socket;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Boolean isReceiver;
    ProgressBar progressBar;
    TextView filesSendReceivedTextview;
    TextView filesRemainingTextview;
    TextView timeRemainingTextview;
    SendingFilesAdapter sendingFilesAdapter;
    int filesSentCounter;
    int remainingFilesCounter;
    int totalFilesSize;
    int fileSizeSent;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_file_progress, container, false);
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

        progressBar.setMax(totalFilesSize);

        return v;
    }

    @Override
    public void onStart() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startFileTransfer();
            }
        });
        thread.start();


        super.onStart();
    }

    private void startFileTransfer() {
        if (!isReceiver) {
            try {

                WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeInt(modelClassList.size());
                dataOutputStream.writeInt(totalFilesSize);
                Uri uri = null;
                DataInputStream e;
                final Long startTime = System.nanoTime();
                remainingFilesCounter = modelClassList.size();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendingFilesAdapter.submitList(modelClassList);
                    }
                });


                for (int i1 = 0; i1 < modelClassList.size(); i1++) {

                    ModelClass modelClass = modelClassList.get(i1);
                    Log.d("filesend", modelClass.getName() + " " + modelClass.getSize() + " " + modelClass.getLabel());
                    if (modelClass.getType().equals("app") || modelClass.getType().equals("others")) {

                        e = new DataInputStream(new FileInputStream(new File(modelClass.getUri())));
                        if (modelClass.getType().equals("app"))
                            dataOutputStream.writeUTF(modelClass.getName() + ".apk");
                        else
                            dataOutputStream.writeUTF(modelClass.getName());



                    } else {

                        uri = Uri.parse(modelClass.getUri());
                        e = new DataInputStream(getActivity().getContentResolver().openInputStream(uri));
                        dataOutputStream.writeUTF(modelClass.getName());
                    }
                    dataOutputStream.writeUTF(modelClass.getType());
                    int filesize = modelClass.getSize().intValue();
                    dataOutputStream.writeInt(filesize);


                    byte[] bytes = new byte[8000];

                    int i;
                    Log.d("buffersize", String.valueOf("working"));
                    int eachFileSizeSent = 0;
                    while ((i = e.read(bytes, 0, Math.min(bytes.length, filesize))) > 0) {

                        Log.d("filesend", "insenderloopsending");
                        filesize = filesize - i;
                        fileSizeSent += i;
                        eachFileSizeSent += i;
                        dataOutputStream.write(bytes, 0, i);

                        final int finalI = i1;
                        final int finalEachFileSizeSent = eachFileSizeSent;
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(fileSizeSent);

                            }
                        });

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendingFilesAdapter.setProgress(finalI, finalEachFileSizeSent);
                            }
                        });


                        Log.d("buffersize", String.valueOf(i));
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            filesSentCounter++;
                            filesSendReceivedTextview.setText("Files Sent : " + filesSentCounter);
                            remainingFilesCounter--;
                            filesRemainingTextview.setText("Remaining Files " + remainingFilesCounter);
                        }
                    });

                }
                final Long stoptime = System.nanoTime();
                final float time = (float) (((stoptime - startTime) * 0.1) / (1000 * 60));


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {

                Log.d("filesend", "inreceiver");
                final DataInputStream e = new DataInputStream(socket.getInputStream());

                int numOfFiles = e.readInt();
                totalFilesSize = e.readInt();
                progressBar.setMax(totalFilesSize);

                final Long startTime = System.nanoTime();
                remainingFilesCounter = numOfFiles;
                for (int i = 0; i < numOfFiles; i++) {
                    ModelClass modelClass = new ModelClass();

                    Log.d("filesend", "inreceiverloop");
                    String name = e.readUTF();
                    String type=e.readUTF();
                    modelClass.setName(name);
                    Log.d("sizeapp", name);
                    File file = new File(getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    int j = 0;
                    int size = e.readInt();
                    modelClass.setSize((long) size);
                    modelClass.setUri(file.getAbsolutePath());
                    modelClass.setType(type);

                    byte[] b = new byte[8000];
                    Log.d("sizeofapp", String.valueOf(size));
                    while ((j = e.read(b, 0, Math.min(b.length, size))) > 0) {
                        size = size - j;
                        fileSizeSent += j;
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(fileSizeSent);
                            }
                        });
                        fileOutputStream.write(b, 0, j);
                        Log.d("buffersize", String.valueOf(j));
                        Log.d("filesend", "inreceiverloopsending");
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            filesSentCounter++;
                            filesSendReceivedTextview.setText("Files Received : " + filesSentCounter);
                            remainingFilesCounter--;
                            filesRemainingTextview.setText("Remaining Files " + remainingFilesCounter);
                        }
                    });


                }
                final Long stoptime = System.nanoTime();
                final float time = (float) (((stoptime - startTime) * 0.1) / (1000 * 60));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
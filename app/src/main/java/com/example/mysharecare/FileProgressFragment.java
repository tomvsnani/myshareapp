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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

                final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(modelClassList);
                dataOutputStream.writeInt(totalFilesSize);
                Uri uri = null;
                DataInputStream dataInputStream;
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
                   // Log.d("filesend", modelClass.getName() + " " + modelClass.getSize() + " " + modelClass.getLabel());
                    if (modelClass.getType().equals("app") || modelClass.getType().equals("others")) {

                        dataInputStream = new DataInputStream(new FileInputStream(new File(modelClass.getUri())));


                    } else {

                        uri = Uri.parse(modelClass.getUri());
                        dataInputStream = new DataInputStream(getActivity().getContentResolver().openInputStream(uri));

                    }

                    int filesize = modelClass.getSize().intValue();

                    byte[] bytes = new byte[8000];

                    int i;
                    Log.d("buffersize", String.valueOf("working"));
                    int eachFileSizeSent = 0;
                    while ((i = dataInputStream.read(bytes, 0, Math.min(bytes.length, filesize))) > 0) {

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
                final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                final List<ModelClass> modelClassList = (List<ModelClass>) objectInputStream.readObject();
                if (modelClassList != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendingFilesAdapter.submitList(modelClassList);
                        }
                    });
                    int numOfFiles = modelClassList.size();
                    totalFilesSize = dataInputStream.readInt();
                    progressBar.setMax(totalFilesSize);

                    final Long startTime = System.nanoTime();
                    remainingFilesCounter = numOfFiles;
                    for (int i = 0; i < numOfFiles; i++) {
                       ModelClass modelClass=modelClassList.get(i);
                        File file=null;
                        Log.d("filesend", "inreceiverloop");
                        String name = modelClass.getName();
                        String type = modelClass.getType();
                        Log.d("sizeapp", name);
                        if(modelClass.getType().equals("app")) {
                            file = new File(getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name+".apk");
                        }
                        else
                            file = new File(getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);

                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        int j = 0;
                        int size = modelClass.getSize().intValue();


                        byte[] b = new byte[8000];
                        Log.d("sizeofapp", String.valueOf(size));
                        while ((j = dataInputStream.read(b, 0, Math.min(b.length, size))) > 0) {
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
                }
                } catch(IOException e){
                    e.printStackTrace();
                } catch(ClassNotFoundException e){
                    e.printStackTrace();
                }

        }
    }
}
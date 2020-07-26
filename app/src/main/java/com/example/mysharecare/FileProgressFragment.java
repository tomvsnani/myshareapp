package com.example.mysharecare;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.net.TrafficStatsCompat;
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
import java.util.Timer;
import java.util.TimerTask;


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
    TextView sendingHeadingTextView;
    SendingFilesAdapter sendingFilesAdapter;
    int filesSentCounter;
    int remainingFilesCounter;
    int totalFilesSize;
    int fileSizeSent;
    TrafficStats trafficStats;
    long totalRtBytes = 0;
    Timer timer;
    int uid;
    boolean isTransferFinished = false;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimerTask timerTask = new TimerTask() {


            @Override
            public void run() {

                double remainingSizeToBeSentInMb = (totalFilesSize - fileSizeSent) / (double) 1000000;
                final double timeInSeconds = remainingSizeToBeSentInMb / getSpeed();
                final double timeInMinutes = timeInSeconds / 60;
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if ((int) timeInMinutes <= 0)
                                timeRemainingTextview.setText("Time Remaining   " + (int) timeInSeconds + "seconds");
                            else {
                                if ((int) timeInSeconds > 1)
                                    timeRemainingTextview.setText("Time Remaining " + String.format("   %.2f", timeInMinutes) + " minutes");
                                else
                                    timeRemainingTextview.setText("Time Remaining " + String.format("  %.2f", timeInMinutes) + " minute");
                            }
                        }
                    });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
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
        sendingHeadingTextView = v.findViewById(R.id.sendingextview);

        progressBar.setMax(totalFilesSize);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startFileTransfer();
            }
        });
        thread.start();

        return v;
    }


    private void startFileTransfer() {
        if (!isReceiver) {
            try {
                final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(modelClassList);
                dataOutputStream.writeInt(totalFilesSize);
                Uri uri = null;
                DataInputStream dataInputStream = null;

                remainingFilesCounter = modelClassList.size();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendingFilesAdapter.submitList(modelClassList);
                    }
                });

                final Long startTime = System.currentTimeMillis();
                for (int i1 = 0; i1 < modelClassList.size(); i1++) {

                    ModelClass modelClass = modelClassList.get(i1);
                    if (modelClass.getType().equals("app") || modelClass.getType().equals("others")) {

                        dataInputStream = new DataInputStream(new FileInputStream(new File(modelClass.getUri())));


                    } else {

                        uri = Uri.parse(modelClass.getUri());
                        dataInputStream = new DataInputStream(getActivity().getContentResolver().openInputStream(uri));

                    }

                    int filesize = modelClass.getSize().intValue();

                    byte[] bytes = new byte[61440];

                    int i;

                    int eachFileSizeSent = 0;
                    Log.d("sendingpackets", "yes");

                    while ((i = dataInputStream.read(bytes, 0, Math.min(bytes.length, filesize))) > 0) {
                        Log.d("sendingpackets", "yesinwhile");
                        String anim=". ";
                        anim=anim+".";
                        if(anim.length()==6)
                            anim=". ";
                        sendingHeadingTextView.setText(anim);
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

                Log.d("okay", "time");
                final Long stoptime = System.currentTimeMillis();
                final double time = (((double) (stoptime - startTime) / ((double) (1000 * 60))));
                Log.d("okay", String.format("%.2f", time));
                if (dataInputStream != null)
                    dataInputStream.close();
                objectOutputStream.close();
                dataOutputStream.close();
                if (socket != null && socket.isConnected())
                    socket.close();
                isTransferFinished = true;
                timer.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        // // / / / / // / / / / / / // / / / / / /// / / / / / // / / / / // / / / / / // / / // / / / //  / / / / /


        else {
            try {

                final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                FileOutputStream fileOutputStream = null;
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
                    remainingFilesCounter = numOfFiles;
                    final Long startTime = System.currentTimeMillis();
                    for (int i = 0; i < numOfFiles; i++) {
                        Log.d("receivingfiles", "yes");
                        ModelClass modelClass = modelClassList.get(i);
                        File file = null;
                        String name = modelClass.getName();
                        if (modelClass.getType().equals("app")) {
                            file = new File(getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name + ".apk");
                        } else
                            file = new File(getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);

                        file.createNewFile();
                        fileOutputStream = new FileOutputStream(file);
                        int j = 0;
                        int size = modelClass.getSize().intValue();

                        int eachFileSize = 0;
                        byte[] b = new byte[61440];

                        while ((j = dataInputStream.read(b, 0, Math.min(b.length, size))) > 0) {
                            Log.d("receivingpackets", "yesinwhile");
                            size = size - j;
                            fileSizeSent += j;
                            eachFileSize += j;
                            progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(fileSizeSent);
                                }
                            });
                            final int finalEachFileSize = eachFileSize;
                            final int finalI = i;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sendingFilesAdapter.setProgress(finalI, finalEachFileSize);
                                }
                            });
                            fileOutputStream.write(b, 0, j);
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

                    final Long stoptime = System.currentTimeMillis();
                    final double time = (((double) (stoptime - startTime) / ((double) (1000 * 60))));
                    Log.d("okay", String.format("%.2f", time));
                    dataInputStream.close();
                    objectInputStream.close();
                    if (fileOutputStream != null)
                        fileOutputStream.close();
                    if (socket != null && socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {

            }

        }
    }

    public double getSpeed() {
        long wifibytes = 0;
        if (!isReceiver) {
            wifibytes = (TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes());
        } else wifibytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
        long differenceInBytes = wifibytes - totalRtBytes;
        totalRtBytes = wifibytes;

        return differenceInBytes / (double) (1000000);

    }
}
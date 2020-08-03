package com.example.mysharecare;

import android.os.Environment;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

public class Receiver {
    FileTransferService fileTransferService;
    DataInputStream dataInputStreamReceiver;
    ObjectInputStream objectInputStreamReceiver;
    FileOutputStream fileOutputStreamReceiver;
    Socket socket;
    FileProgressFragment fileProgressFragment;

    public Receiver(FileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
        dataInputStreamReceiver=fileTransferService.dataInputStreamReceiver;
        fileOutputStreamReceiver=fileTransferService.fileOutputStreamReceiver;
        socket=fileTransferService.socket;
        fileProgressFragment=fileTransferService.fileProgressFragment;
        objectInputStreamReceiver=fileTransferService.objectInputStreamReceiver;
    }

    public void initializeReceiving() {
        try {


            fileOutputStreamReceiver = null;
            fileTransferService.modelClassList = (List<ModelClass>) objectInputStreamReceiver.readObject();
            if (fileTransferService.modelClassList != null) {
                if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
                    fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileProgressFragment.sendingFilesAdapter.submitList(fileTransferService.modelClassList);
                        }
                    });
                int numOfFiles = fileTransferService.modelClassList.size();
                fileTransferService.totalFilesSize = dataInputStreamReceiver.readInt();
                fileProgressFragment.progressBar.setMax(fileTransferService.totalFilesSize);
                fileTransferService.remainingFilesCounter = numOfFiles;
                final Long startTime = System.currentTimeMillis();


                for (int i = 0; i < numOfFiles; i++) {
                    Log.d("receivingfiles", String.valueOf(i));
                    ModelClass modelClass = fileTransferService.modelClassList.get(i);
                    File file = null;
                    String name = modelClass.getName();
                    if (modelClass.getType().equals(AppConstants.Apps)) {
                        file = new File(fileTransferService.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name + ".apk");
                    } else
                        file = new File(fileTransferService.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);

                    file.createNewFile();
                    fileOutputStreamReceiver = new FileOutputStream(file);
                    int j = 0;
                    int size = modelClass.getSize().intValue();

                    int eachFileSize = 0;
                    byte[] b = new byte[61440];
                    fileTransferService.updateFilesSentReceivedViews();
                    while ((j = dataInputStreamReceiver.read(b, 0, Math.min(b.length, size))) > 0) {


                        Log.d("utff", String.valueOf(j));
                        size = size - j;
                        fileTransferService.fileSizeSent += j;
                        eachFileSize += j;
                        if (fileProgressFragment != null)
                            fileProgressFragment.progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    fileProgressFragment.progressBar.setProgress(fileTransferService.fileSizeSent);
                                }
                            });
                        final int finalEachFileSize = eachFileSize;
                        final int finalI = i;
                        if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
                            fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fileProgressFragment.sendingFilesAdapter.setProgress(finalI, finalEachFileSize);
                                }
                            });

                        fileOutputStreamReceiver.write(b, 0, j);

                    }
                    Log.d("receiving", "cameout");
                    fileTransferService.filesSentCounter++;
                    fileTransferService.updateFilesSentReceivedViews();
                }

                fileTransferService.calculateTimeElapsed(startTime);
                fileTransferService.timer.cancel();


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

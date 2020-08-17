package com.example.mysharecare;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender {
    FileTransferService fileTransferService;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    ObjectOutputStream objectOutputStream;
    Socket socket;
    int totalFilesSize;
    FileProgressFragment fileProgressFragment;


    public Sender(FileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
        dataOutputStream = fileTransferService.dataOutputStream;
        dataInputStream = fileTransferService.dataInputStream;
        objectOutputStream = fileTransferService.objectOutputStream;
        socket = fileTransferService.socket;
        totalFilesSize = fileTransferService.totalFilesSize;
        fileProgressFragment = fileTransferService.fileProgressFragment;
    }

    public void initializeSending() {
        try {

            objectOutputStream.writeObject(fileTransferService.modelClassList);
            dataOutputStream.writeInt(totalFilesSize);
            Uri uri = null;
            dataInputStream = null;

            fileTransferService.remainingFilesCounter = fileTransferService.modelClassList.size();
            if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
                fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileProgressFragment.sendingFilesAdapter.submitList(fileTransferService.modelClassList);
                    }
                });

            final Long startTime = System.currentTimeMillis();
            for (int i1 = 0; i1 < fileTransferService.modelClassList.size(); i1++) {

                ModelClass modelClass = fileTransferService.modelClassList.get(i1);


                if (modelClass.getType().equals(AppConstants.Apps) || modelClass.getType().equals(AppConstants.Files)) {
                    uri = Uri.parse(modelClass.getUri());
                    dataInputStream = new DataInputStream(new FileInputStream(new File(uri.toString())));


                } else {
                    uri = Uri.parse(modelClass.getUri());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        dataInputStream = new DataInputStream(fileTransferService.getContentResolver().openInputStream(uri));
                    } else
                        dataInputStream = new DataInputStream(new FileInputStream(new File(uri.toString())));


                }

                int filesize = modelClass.getSize().intValue();

                byte[] bytes = new byte[61440];

                int i;

                int eachFileSizeSent = 0;

                fileTransferService.updateFilesSentReceivedViews();

                while ((i = dataInputStream.read(bytes, 0, Math.min(bytes.length, filesize))) > 0) {



                    filesize = filesize - i;
                    fileTransferService.fileSizeSent += i;
                    eachFileSizeSent += i;
                    dataOutputStream.write(bytes, 0, i);


                    final int finalI = i1;
                    final int finalEachFileSizeSent = eachFileSizeSent;
                    if (fileProgressFragment != null)
                        fileProgressFragment.progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                fileProgressFragment.progressBar.setProgress(fileTransferService.fileSizeSent);

                            }
                        });
                    if (fileProgressFragment != null && fileProgressFragment.getActivity() != null) {
                        fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fileProgressFragment.sendingFilesAdapter.setProgress(finalI, finalEachFileSizeSent);
                            }
                        });

                    }
                }
                fileTransferService.filesSentCounter++;

                fileTransferService.updateFilesSentReceivedViews();


            }


            fileTransferService.calculateTimeElapsed(startTime);
            fileTransferService.timer.cancel();
            //closeAllConnectionsSender(dataOutputStream, objectOutputStream, dataInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

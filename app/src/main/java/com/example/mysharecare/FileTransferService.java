package com.example.mysharecare;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FileTransferService extends Service {
    List<ModelClass> modelClassList;
    Socket socket;
    boolean isReceiver;
    int filesSentCounter;
    int remainingFilesCounter;
    int totalFilesSize = 0;
    int fileSizeSent = 0;
    TrafficStats trafficStats;
    long totalRtBytes = 0;
    Timer timer;
    boolean isTransferFinished = false;
    FileProgressFragment fileProgressFragment;
    IBinder iBinder = new LocalBinder();
    DataInputStream dataInputStreamReceiver;
    ObjectInputStream objectInputStreamReceiver;
    FileOutputStream fileOutputStreamReceiver;
    DataOutputStream dataOutputStream;
    ObjectOutputStream objectOutputStream;
    DataInputStream dataInputStream = null;
    int fileNumberToClose = -5;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null && intent.getAction().equals("stop")) {

            getApplicationContext().unbindService(fileProgressFragment.serviceConnection);
            stopForeground(true);
            stopSelf();

        }

        return START_NOT_STICKY;
    }


    public void closeConnections() throws IOException {
        AlertDialog alertDialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(fileProgressFragment.getContext());
        builder.setMessage("Do you really want to close the connection ? ");
        builder.setPositiveButton("Yes , close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {

                if (isReceiver) {
                    try {
                        closeConnectionsReceiver(dataInputStreamReceiver, objectInputStreamReceiver, fileOutputStreamReceiver);
                        closewifidirectandSerice();
                        Intent intent = new Intent(fileProgressFragment.getContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        fileProgressFragment.getActivity().startActivity(intent);
                        dialogInterface.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                    try {
                        closeAllConnectionsSender(dataOutputStream, objectOutputStream, dataInputStream);
                        closewifidirectandSerice();
                        Intent intent = new Intent(fileProgressFragment.getContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                        dialogInterface.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }


            }

            private void closewifidirectandSerice() {
                fileProgressFragment.wifiP2pManager.clearServiceRequests(fileProgressFragment.channel, null);
                fileProgressFragment.wifiP2pManager.clearLocalServices(fileProgressFragment.channel, null);
                fileProgressFragment.wifiP2pManager.removeGroup(fileProgressFragment.channel, null);
                if (fileProgressFragment.getActivity() != null)
                    fileProgressFragment.getActivity().getApplicationContext().unbindService(fileProgressFragment.serviceConnection);
                fileProgressFragment.getActivity().stopService(fileProgressFragment.intent);
            }
        });

        builder.setNegativeButton("No ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void startForegroundd() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
            Intent intent = new Intent(this, FileTransferService.class);
            intent.setAction("stop");
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
            builder.addAction(R.drawable.ic_launcher_background, "Exit", pendingIntent);
            builder.setContentTitle("Transferring files");
            builder.setContentText("shareit");
            builder.setSmallIcon(R.drawable.ic_launcher_background);
            startForeground(1, builder.build());

        }
    }

    class LocalBinder extends Binder {
        FileTransferService getService() {
            return FileTransferService.this;
        }
    }


    public void setServiceArguments(List<ModelClass> modelClassList, Socket socket
            , boolean isReceiver, FileProgressFragment serviceCallback) {

        this.modelClassList = modelClassList;
        this.socket = socket;
        this.isReceiver = isReceiver;
        this.fileProgressFragment = serviceCallback;


        if (modelClassList != null)
            for (ModelClass modelClass : modelClassList) {
                totalFilesSize += modelClass.getSize();
            }

        trafficStats = new TrafficStats();
        startForegroundd();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startFileTransfer();
            }
        });
        thread.start();
    }


    private void startFileTransfer() {
        calculateRemainingTime();
        if (!isReceiver) {

            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Sender sender = new Sender(this);
            sender.initializeSending();
            dataInputStream=sender.dataInputStream;


        } else {
            try {
                dataInputStreamReceiver = new DataInputStream(socket.getInputStream());
                objectInputStreamReceiver = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Receiver receiver = new Receiver(this);
            receiver.initializeReceiving();
            fileOutputStreamReceiver=receiver.fileOutputStreamReceiver;

        }
    }


    void calculateTimeElapsed(Long startTime) {

        final Long stoptime = System.currentTimeMillis();
        final double time = (((double) (stoptime - startTime) / ((double) (1000 * 60))));

    }


    void updateFilesSentReceivedViews() {
        if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
            fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    if (fileProgressFragment != null)
                        fileProgressFragment.filesSendReceivedTextview.setText(String.valueOf(" " + filesSentCounter + "/"));

                    if (fileProgressFragment != null)
                        fileProgressFragment.filesRemainingTextview.setText(String.valueOf(remainingFilesCounter));
                }
            });
    }


    void closeAllConnectionsSender(DataOutputStream dataOutputStream, ObjectOutputStream objectOutputStream, DataInputStream dataInputStream) throws IOException {
        if (dataInputStream != null)
            dataInputStream.close();
        objectOutputStream.close();
        dataOutputStream.close();
        if (socket != null && socket.isConnected())
            socket.close();
        isTransferFinished = true;
        timer.cancel();
    }


    void closeConnectionsReceiver(DataInputStream dataInputStream, ObjectInputStream objectInputStream, FileOutputStream fileOutputStream) throws IOException {
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
            timer.cancel();
        }
    }


    private void calculateRemainingTime() {

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {

                double remainingSizeToBeSentInMb = (totalFilesSize - fileSizeSent) / (double) 1000000;
                final double timeInSeconds = remainingSizeToBeSentInMb / getConnectionSpeed();
                final double timeInMinutes = timeInSeconds / 60;
                if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
                    fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if ((int) timeInMinutes <= 0)
                                fileProgressFragment.timeRemainingTextview.setText("Time Remaining   " + (int) timeInSeconds + " seconds");
                            else {
                                if ((int) timeInSeconds > 1)
                                    fileProgressFragment.timeRemainingTextview.setText("Time Remaining " + String.format("   %.2f", timeInMinutes) + " minutes");
                                else
                                    fileProgressFragment.timeRemainingTextview.setText("Time Remaining " + String.format("  %.2f", timeInMinutes) + " minute");
                            }
                        }
                    });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }


    public double getConnectionSpeed() {
        long wifibytes;
        if (!isReceiver) {
            wifibytes = (TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes());
        } else wifibytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
        long differenceInBytes = wifibytes - totalRtBytes;
        totalRtBytes = wifibytes;

        return differenceInBytes / (double) (1000000);

    }
}

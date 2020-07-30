package com.example.mysharecare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    private void startForegroundd() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
            Intent intent = new Intent(this, FileTransferService.class);
            intent.putExtra("start", "stop");
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
            builder.addAction(R.drawable.ic_launcher_background, "stop", pendingIntent);
            builder.setContentTitle("noti");
            builder.setContentText("yess");
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
            initializeSending();

        }
        // // / / / / // / / / / / / // / / / / / /// / / / / / // / / / / // / / / / / // / / // / / / //  / / / / /


        else {
            initializeReceiving();

        }
    }

    private void initializeReceiving() {
        try {

            final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            FileOutputStream fileOutputStream = null;
            modelClassList = (List<ModelClass>) objectInputStream.readObject();
            if (modelClassList != null) {
                if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
                    fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileProgressFragment.sendingFilesAdapter.submitList(modelClassList);
                        }
                    });
                int numOfFiles = modelClassList.size();
                totalFilesSize = dataInputStream.readInt();
                fileProgressFragment.progressBar.setMax(totalFilesSize);
                remainingFilesCounter = numOfFiles;
                final Long startTime = System.currentTimeMillis();


                for (int i = 0; i < numOfFiles; i++) {
                    Log.d("receivingfiles", "yes");
                    ModelClass modelClass = modelClassList.get(i);
                    File file = null;
                    String name = modelClass.getName();
                    if (modelClass.getType().equals("app")) {
                        file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name + ".apk");
                    } else
                        file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);

                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    int j = 0;
                    int size = modelClass.getSize().intValue();

                    int eachFileSize = 0;
                    byte[] b = new byte[61440];
                    updateFilesSentReceivedViews();
                    while ((j = dataInputStream.read(b, 0, Math.min(b.length, size))) > 0) {
                        Log.d("receivingpackets", "yesinwhile");
                        size = size - j;
                        fileSizeSent += j;
                        eachFileSize += j;
                        if (fileProgressFragment != null)
                            fileProgressFragment.progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    fileProgressFragment.progressBar.setProgress(fileSizeSent);
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
                        fileOutputStream.write(b, 0, j);
                    }

                    filesSentCounter++;
                    updateFilesSentReceivedViews();
                }

                calculateTimeElapsed(startTime);
                closeConnectionsReceiver(dataInputStream, objectInputStream, fileOutputStream);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void initializeSending() {
        try {
            final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(modelClassList);
            dataOutputStream.writeInt(totalFilesSize);
            Uri uri = null;
            DataInputStream dataInputStream = null;

            remainingFilesCounter = modelClassList.size();
            if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
                fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileProgressFragment.sendingFilesAdapter.submitList(modelClassList);
                    }
                });

            final Long startTime = System.currentTimeMillis();
            for (int i1 = 0; i1 < modelClassList.size(); i1++) {

                ModelClass modelClass = modelClassList.get(i1);
                if (modelClass.getType().equals("app") || modelClass.getType().equals("others")) {
                    uri = Uri.parse(modelClass.getUri());
                    dataInputStream = new DataInputStream(new FileInputStream(new File(uri.toString())));


                } else {
                    uri = Uri.parse(modelClass.getUri());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        dataInputStream = new DataInputStream(this.getContentResolver().openInputStream(uri));
                    } else
                        dataInputStream = new DataInputStream(new FileInputStream(new File(uri.toString())));


                }

                int filesize = modelClass.getSize().intValue();

                byte[] bytes = new byte[61440];

                int i;

                int eachFileSizeSent = 0;
                Log.d("sendingpackets", "yes");
                updateFilesSentReceivedViews();

                while ((i = dataInputStream.read(bytes, 0, Math.min(bytes.length, filesize))) > 0) {
                    Log.d("sendingpackets", "yesinwhile");

                    if (fileProgressFragment != null)
                        fileProgressFragment.sendingHeadingTextView.post(new Runnable() {
                            @Override
                            public void run() {


                            }
                        });
                    filesize = filesize - i;
                    fileSizeSent += i;
                    eachFileSizeSent += i;
                    dataOutputStream.write(bytes, 0, i);
                    final int finalI = i1;
                    final int finalEachFileSizeSent = eachFileSizeSent;
                    if (fileProgressFragment != null)
                        fileProgressFragment.progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                fileProgressFragment.progressBar.setProgress(fileSizeSent);

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
                filesSentCounter++;

                updateFilesSentReceivedViews();

            }

            calculateTimeElapsed(startTime);
            closeAllConnectionsSender(dataOutputStream, objectOutputStream, dataInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void calculateTimeElapsed(Long startTime) {
        Log.d("okay", "time");
        final Long stoptime = System.currentTimeMillis();
        final double time = (((double) (stoptime - startTime) / ((double) (1000 * 60))));
        Log.d("okay", String.format("%.2f", time));
    }


    void updateFilesSentReceivedViews() {
        if (fileProgressFragment != null && fileProgressFragment.getActivity() != null)
            fileProgressFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    if (fileProgressFragment != null)
                        fileProgressFragment.filesSendReceivedTextview.setText(String.valueOf( " "+filesSentCounter+"/"));

                    if (fileProgressFragment != null)
                        fileProgressFragment.filesRemainingTextview.setText( String.valueOf( remainingFilesCounter));
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
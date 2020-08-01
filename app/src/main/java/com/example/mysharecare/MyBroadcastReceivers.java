package com.example.mysharecare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MyBroadcastReceivers extends BroadcastReceiver {
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    MainFragment mainFragment;

    public MyBroadcastReceivers(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainFragment mainFragment) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.mainFragment = mainFragment;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case LocationManager.PROVIDERS_CHANGED_ACTION: {
                    onLOcationStateChangedBroadcast();
                }
                break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION: {
                    onWifistateChangedBrodcast();
                }
                break;

                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:


                    onStateChangedBroadcast(intent);

                    break;


                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {

                    onConnectionChangedBroadcast(intent);

                }
                break;


                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {


                    onPeersChangedBroadcast(context);

                }
                break;

                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {

                }
                break;

                case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION: {

                    onDiscoveryChangedBroadcast(context, intent);

                }
                break;
            }
        }

    }

    private void onDiscoveryChangedBroadcast(Context context, Intent intent) {
        int extra = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
        if (extra == (WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)) {
            if (mainFragment.isReceiver)
                mainFragment.findDevicesTextview.setText("Waiting for connection requests...");
            else
                mainFragment.findDevicesTextview.setText("Searching for devices...");

            Toast.makeText(context, "Discovery Started", Toast.LENGTH_SHORT).show();
        }

        if (extra == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
            mainFragment.findDevicesTextview.setText("Search stopped.Trying to connect ...");
            if (!mainFragment.isConnected)
                mainFragment.startDiscoveryDevices();

            Toast.makeText(context, "Discovery Stopped ", Toast.LENGTH_SHORT).show();
        }
    }

    private void onWifistateChangedBrodcast() {
        if (mainFragment.wifiManager.isWifiEnabled())
            mainFragment.isWifiChanged.setValue(true);
        else mainFragment.isWifiChanged.setValue(false);
    }

    private void onLOcationStateChangedBroadcast() {
        if (mainFragment.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mainFragment.isLocationChanged.setValue(true);
        } else mainFragment.isLocationChanged.setValue(false);
    }

    private void onStateChangedBroadcast(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {


        } else {


        }
    }

    private void onConnectionChangedBroadcast(Intent intent) {
        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo != null && networkInfo.isConnected()) {
            mainFragment.findDevicesTextview.setText("Connected");
            mainFragment.isConnected = true;
            Toast.makeText(mainFragment.getContext(), "connected", Toast.LENGTH_SHORT).show();

            wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {

                    final Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                                Log.d("sizeserve", "server");

                                try {
                                    ServerSocket serverSocket = new ServerSocket(5000);
                                    final Socket socket = serverSocket.accept();
//                                    Log.d("socketconn", String.valueOf(socket.isConnected()));
//                                    ServerSocket serverSocket1 = new ServerSocket(5000);
//                                    Socket socket1 = serverSocket1.accept();
//                                    Log.d("socketconn1", String.valueOf(socket1.isConnected()));
//                                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                                  //  dataOutputStream.writeUTF("hello");
                                        mainFragment.getActivity().getSupportFragmentManager().beginTransaction().
                                                replace(R.id.filetransferprogressframelayout,
                                                        new FileProgressFragment(mainFragment.modelClassList, socket, mainFragment.isReceiver, channel, wifiP2pManager)).addToBackStack(null).commit();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }


                            if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                                try {
                                    Log.d("sizeserve", "client");

                                    Socket socket = new Socket();
                                    socket.connect(new InetSocketAddress(wifiP2pInfo.groupOwnerAddress.getHostAddress(), 5000), 5000);
//                                    Log.d("socketconn", String.valueOf(socket.isConnected()));
//                                    Socket socket1 = new Socket();
//                                    socket1.connect(new InetSocketAddress(wifiP2pInfo.groupOwnerAddress.getHostAddress(), 5000), 5000);
//                                    Log.d("socketconn", String.valueOf(socket1.isConnected()));
//                                    DataInputStream dataInputStream1 = new DataInputStream(socket1.getInputStream());
//                                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
//
//                                    Log.d("socket1", "fromsocket1 " + dataInputStream1.readUTF());
//                                    Log.d("socket", "fromsocket " + dataInputStream.readUTF());

                                        mainFragment.getActivity().getSupportFragmentManager().beginTransaction().
                                                replace(R.id.filetransferprogressframelayout,
                                                        new FileProgressFragment(mainFragment.modelClassList, socket, mainFragment.isReceiver, channel, wifiP2pManager)).addToBackStack(null).commit();


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    thread.start();

                }
            });
        }
        if (networkInfo != null && !networkInfo.isConnected()) {
            mainFragment.findDevicesTextview.setText("Reconnecting");
            mainFragment.deletePersistentGroups();
            mainFragment.startDiscoveryDevices();
        }
    }


    private void onPeersChangedBroadcast(final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mainFragment.getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
            return;

        }
        wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                Toast.makeText(context, "No device is found", Toast.LENGTH_SHORT).show();
                if (wifiP2pDeviceList.getDeviceList().size() == 0)
                    mainFragment.devicesAdapter.submitList(null);
            }
        });
    }


}

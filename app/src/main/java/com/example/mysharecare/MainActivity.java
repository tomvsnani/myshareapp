package com.example.mysharecare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    WifiBroadCast wifiBroadCast;
    Button findDevicesButton;
    Button wifiOnOffButton;
    ListView devicesListView;
    ArrayAdapter arrayAdapter;

    WifiP2pManager.ActionListener stopDiscovery;
    EditText messageTextView;
    WifiP2pManager.ActionListener connectDisconnectActionListener;
    Boolean isReceiver;
    FrameLayout frameLayout;
    MutableLiveData<List<InputStream>> inputStreamList = new MutableLiveData<>();
    List<ModelClass> modelClassList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        findDevicesButton = findViewById(R.id.finddevices);
        wifiOnOffButton = findViewById(R.id.wifionofftextview);
        devicesListView = findViewById(R.id.listview);
        frameLayout = findViewById(R.id.framelayout);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        String extra = getIntent().getStringExtra(AppConstants.SENDRECEIVEEXTRA);
        modelClassList = (List<ModelClass>) getIntent().getSerializableExtra("sendItems");
        // Log.d("sizemodel", String.valueOf(modelClassList.size()));

        if (extra != null && extra.equals(AppConstants.RECEIVE))
            isReceiver = true;
        else
            isReceiver = false;


        devicesListView.setAdapter(arrayAdapter);

        messageTextView = findViewById(R.id.message);

        startDiscoveryDevices();

        setupListeners();


    }


    class WifiBroadCast extends BroadcastReceiver {


        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:


                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                            wifiOnOffButton.setText("wifi is on. Turn off wifi ?");

                        } else {

                            wifiOnOffButton.setText("wifi is off. Turn on wifi ?");

                        }
                        break;


                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {

                        NetworkInfo networkInfo = (NetworkInfo) intent
                                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                        if (networkInfo != null && networkInfo.isConnected()) {

                            Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();

                            wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                                @Override
                                public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {

                                    final Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                                                Log.d("sizeserve", "server");
                                                wifiOnOffButton.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        wifiOnOffButton.setText("server");
                                                    }
                                                });
                                                try {
                                                    ServerSocket serverSocket = new ServerSocket(5000);
                                                    final Socket socket = serverSocket.accept();
                                                    Log.d("sizeconnected", String.valueOf(socket.isConnected()));
                                                    wifiOnOffButton.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            wifiOnOffButton.setText("connected");
                                                        }
                                                    });


                                                    try {
                                                        Log.d("size1", "f");

                                                        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                                        dataOutputStream.writeInt(modelClassList.size());
                                                        Uri uri;
                                                        for (ModelClass modelClass : modelClassList) {
                                                            if (modelClass.getType().equals("app")) {
                                                                uri = Uri.fromFile(new File(modelClass.getUri()));
                                                            } else
                                                                uri = Uri.parse(modelClass.getUri());

                                                            Log.d("sizelength", String.valueOf(modelClass.getSize()));
                                                            final DataInputStream e = new DataInputStream(getContentResolver().openInputStream(uri));
                                                            Log.d("sizeuri", String.valueOf(uri));
                                                            dataOutputStream.writeUTF(modelClass.getName());
                                                            dataOutputStream.writeInt(modelClass.getSize().intValue());


                                                            byte[] bytes = new byte[modelClass.getSize().intValue()];

//                                                            dataOutputStream.writeUTF(modelClass.getName());
                                                            e.readFully(bytes);
                                                            dataOutputStream.write(bytes);
                                                            dataOutputStream.flush();
//
//                                                            int i = 0;
//                                                            while ((i = e.read(bytes,0,bytes.length)) != -1) {
//                                                                Log.d("size1writing", String.valueOf(i));
//                                                                dataOutputStream.write(bytes, 0, i);
//
//                                                            }

                                                        }

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            }


                                            if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                                                try {
                                                    Log.d("sizeserve", "client");


                                                    wifiOnOffButton.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            wifiOnOffButton.setText("client");

                                                        }
                                                    });
                                                    List<String> strings = new ArrayList<>();
                                                    Socket socket = new Socket();
                                                    socket.connect(new InetSocketAddress(wifiP2pInfo.groupOwnerAddress.getHostAddress(), 5000), 5000);

                                                    final DataInputStream e = new DataInputStream(socket.getInputStream());

                                                    int numOfFiles = e.readInt();


                                                    for (int i = 0; i < numOfFiles; i++) {

                                                        String name = e.readUTF();
                                                        Log.d("sizeapp", name);
                                                        File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
                                                        file.createNewFile();
                                                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                        int j = 0;
                                                        int size = e.readInt();
                                                        byte[] b = new byte[size];
                                                        Log.d("sizeofapp", String.valueOf(size));


                                                        findDevicesButton.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                findDevicesButton.setText("receiving");
                                                            }
                                                        });
                                                        e.readFully(b);
                                                        //      Log.d("size1receiving", String.valueOf(j/(1024.0*1024)));
                                                        fileOutputStream.write(b);
                                                        fileOutputStream.flush();

                                                    }
//
//

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

                    }
                    break;


                    case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {


                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                                if (!isReceiver) {
                                    final List<WifiP2pDevice> wifiP2pDevices = new ArrayList<>(wifiP2pDeviceList.getDeviceList());
                                    List<WifiP2pDevice> wifiP2pDeviceLists = new ArrayList<>();
                                    if (!wifiP2pDeviceLists.equals(wifiP2pDevices)) {
                                        wifiP2pDeviceLists = wifiP2pDevices;
                                        arrayAdapter.clear();
                                        arrayAdapter.add(wifiP2pDeviceLists);
                                        arrayAdapter.notifyDataSetChanged();

                                        devicesListView.setOnItemClickListener(
                                                new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                        Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                                                        WifiP2pDevice wifiP2pDevice = wifiP2pDevices.get(i);
                                                        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                                                        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
                                                        wifiP2pConfig.groupOwnerIntent = 15;
                                                        wifiP2pConfig.wps.setup = WpsInfo.PBC;

                                                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                                            return;
                                                        }
                                                        wifiP2pManager.connect(channel, wifiP2pConfig, connectDisconnectActionListener);
                                                    }
                                                });
                                    }


                                    if (wifiP2pDevices.size() == 0) {
                                        Toast.makeText(context, "No device is found", Toast.LENGTH_SHORT).show();
                                        arrayAdapter.clear();
                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                }
                                if (isReceiver)
                                    devicesListView.setVisibility(View.GONE);
                            }
                        });

                    }
                    break;

                    case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {

                    }
                    break;

                    case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION: {

                        int extra = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
                        if (extra == (WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)) {
                            findDevicesButton.setText("Discovery started . stop Discovery ?");
                            Toast.makeText(context, "Discovery Started", Toast.LENGTH_SHORT).show();
                        }
                        if (extra == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                            findDevicesButton.setText("Discovery stopped . start Discovery ?");
                            Toast.makeText(context, "Discovery Stopped", Toast.LENGTH_SHORT).show();
                        }

                    }
                    break;
                }
            }

        }


    }


    private void setupListeners() {
        stopDiscovery = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {


            }
        };

        connectDisconnectActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(MainActivity.this, "connection fai " + i, Toast.LENGTH_SHORT).show();
            }
        };

        wifiOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(Settings.Panel.ACTION_WIFI);
                    startActivity(intent);
                } else {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (wifiManager.isWifiEnabled())
                        wifiManager.setWifiEnabled(false);
                    else
                        wifiManager.setWifiEnabled(true);
                }

            }
        });

        findDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePersistentGroups();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deletePersistentGroups();
    }

    private void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        method.invoke(wifiP2pManager, channel, netid, null);
                    }
                }
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {

                    wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i) {

                        }
                    });
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void startDiscoveryDevices() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "No permission", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return;
        }
        wifiP2pManager.discoverPeers(channel, stopDiscovery);
//        if (findDevicesButton.getText().toString().contains("started") || findDevicesButton.getText().toString().contains("STARTED")) {
//            wifiP2pManager.stopPeerDiscovery(channel, stopDiscovery);
//            wifiP2pManager.cancelConnect(channel, connectDisconnectActionListener);
        //}
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        wifiBroadCast = new WifiBroadCast();
        registerReceiver(wifiBroadCast, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiBroadCast);
    }
}

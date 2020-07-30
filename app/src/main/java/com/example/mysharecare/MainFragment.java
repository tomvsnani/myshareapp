package com.example.mysharecare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.WIFI_P2P_SERVICE;


public class MainFragment extends Fragment {
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    WifiBroadCast wifiBroadCast;
    RecyclerView devicesListView;
    EditText messageTextView;
    WifiP2pManager.ActionListener connectDisconnectActionListener;
    Boolean isReceiver;
    ProgressBar progressBar;
    List<ModelClass> modelClassList;
    DevicesAdapter devicesAdapter;
    boolean isConnected = false;
    LocationManager locationManager;
    WifiManager wifiManager;
    AlertDialog alertDialog;
    MutableLiveData<Boolean> isLocationChanged = new MutableLiveData<>();
    MutableLiveData<Boolean> isWifiChanged = new MutableLiveData<>();
    Toolbar toolbar;
    TextView findDevicesTextview;
    Button searchForDevicesButton;

    public MainFragment(boolean isReceiver, List<ModelClass> modelClassList) {
        this.isReceiver = isReceiver;
        this.modelClassList = modelClassList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        devicesListView = v.findViewById(R.id.listview);
        progressBar = v.findViewById(R.id.datatransferprogressbar);
        findDevicesTextview = v.findViewById(R.id.findingdevicestextview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        devicesAdapter = new DevicesAdapter(getContext());
        devicesListView.setAdapter(devicesAdapter);
        devicesListView.setLayoutManager(linearLayoutManager);
        searchForDevicesButton = v.findViewById(R.id.searchfordevicesbutton);
        messageTextView = v.findViewById(R.id.message);
        toolbar = v.findViewById(R.id.mainactivitytoolbar);

        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) (getActivity())).setSupportActionBar(toolbar);
        wifiP2pManager = (WifiP2pManager) getActivity().getSystemService(WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(getContext(), getActivity().getMainLooper(), null);
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(getContext(), "Please allow Location Permissions", Toast.LENGTH_LONG).show();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }

        } else {
            if (!wifiManager.isWifiEnabled() || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showAlertDialog();
            } else {
                deletePersistentGroups();

                removeGroups();

                startDiscoveryDevices();

                setupListeners();
            }
        }

    }



    private void startLocalService() {
        wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                addlocalservice();
            }

            @Override
            public void onFailure(int i) {
                addlocalservice();
            }
        });
    }


    private void addlocalservice() {
        Map<String, String> map = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            map.put("name", Settings.Global.getString(getActivity().getContentResolver(), "device_name"));
        } else map.put("name", Build.BRAND + Build.MODEL);
        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp"
                , map);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
            return;

        }
        wifiP2pManager.addLocalService(channel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("discoverlocalservice", "success");

            }

            @Override
            public void onFailure(int i) {
                Log.d("discoverlocalservice", "fail  " + i);
            }
        });
    }


    private void startServiceDiscovery() {
        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                addServiceRequest();
            }

            @Override
            public void onFailure(int i) {

            }
        });
    }


    private void addServiceRequest() {
        final String[] deviceNames = {"Android device"};
        final List<WifiP2pDevice> wifiP2pDevices = new ArrayList<>();
        wifiP2pManager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String s, String s1, WifiP2pDevice wifiP2pDevice) {
                Log.d("discovertxtresponse", "success");
                Log.d("discovertxtresponse", wifiP2pDevice.deviceAddress);
                wifiP2pDevice.deviceName = deviceNames[0];
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!wifiP2pDevices.contains(wifiP2pDevice))
                    wifiP2pDevices.add(wifiP2pDevice);

                devicesAdapter.submit(wifiP2pManager, wifiP2pDevices, channel, connectDisconnectActionListener);


            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String s, Map<String, String> map, WifiP2pDevice wifiP2pDevice) {
                Log.d("discovertxtrecord", "success");
                Log.d("discovertexrecord", map.get("name"));
                deviceNames[0] = (map.get("name"));
            }
        });
        wifiP2pManager.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // startDiscoveryDevices();
                Log.d("discoverservicesrequest", "success");
            }

            @Override
            public void onFailure(int i) {
                Log.d("discoverservicesrequest", "fail " + i);
            }
        });
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
            return;

        }
        wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("discoverservices", "success");
            }

            @Override
            public void onFailure(int i) {
                Log.d("discoverservices", "fail " + i);
            }
        });
    }


    /////// Broadcast Receiver ////////////

    class WifiBroadCast extends BroadcastReceiver {
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


        private void onWifistateChangedBrodcast() {
            if (wifiManager.isWifiEnabled())
                isWifiChanged.setValue(true);
            else isWifiChanged.setValue(false);
        }

        private void onLOcationStateChangedBroadcast() {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                isLocationChanged.setValue(true);
            } else isLocationChanged.setValue(false);
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
                findDevicesTextview.setText("Connected");
                isConnected=true;
                Toast.makeText(getContext(), "connected", Toast.LENGTH_SHORT).show();

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
                                        Log.d("sizeconnected", String.valueOf(socket.isConnected()));
                                        getActivity().getSupportFragmentManager().beginTransaction().
                                                replace(R.id.filetransferprogressframelayout,
                                                        new FileProgressFragment(modelClassList, socket, isReceiver)).addToBackStack(null).commit();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }


                                if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                                    try {
                                        Log.d("sizeserve", "client");

                                        Socket socket = new Socket();
                                        socket.connect(new InetSocketAddress(wifiP2pInfo.groupOwnerAddress.getHostAddress(), 5000), 5000);

                                        getActivity().getSupportFragmentManager().beginTransaction().
                                                replace(R.id.filetransferprogressframelayout,
                                                        new FileProgressFragment(modelClassList, socket, isReceiver)).addToBackStack(null).commit();


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
                findDevicesTextview.setText("Reconnecting");
              deletePersistentGroups();
                startDiscoveryDevices();
            }
        }



        private void onPeersChangedBroadcast(final Context context) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
                return;

            }
            wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                @Override
                public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                    Toast.makeText(context, "No device is found", Toast.LENGTH_SHORT).show();
                    if (wifiP2pDeviceList.getDeviceList().size() == 0)
                        devicesAdapter.submitList(null);
                }
            });
        }


    }

    private void onDiscoveryChangedBroadcast(Context context, Intent intent) {
        int extra = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
        if (extra == (WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)) {
            if (isReceiver)
                findDevicesTextview.setText("Waiting for connection requests...");
            else
                findDevicesTextview.setText("Searching for devices...");

            Toast.makeText(context, "Discovery Started", Toast.LENGTH_SHORT).show();
        }

        if (extra == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
            findDevicesTextview.setText("Search stopped.Trying to connect ...");
            if (!isConnected)
                startDiscoveryDevices();

            Toast.makeText(context, "Discovery Stopped ", Toast.LENGTH_SHORT).show();
        }
    }




    private void setupListeners() {
        connectDisconnectActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getContext(), "connection fai " + i, Toast.LENGTH_SHORT).show();
            }
        };

    }





    private void deletePersistentGroups() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wifiP2pManager.cancelConnect(channel, null);
                    Method[] methods = WifiP2pManager.class.getMethods();
                    for (Method method : methods) {
                        if (method.getName().equals("deletePersistentGroup")) {
                            // Delete any persistent group
                            for (int netid = 0; netid < 32; netid++) {
                                method.invoke(wifiP2pManager, channel, netid, null);
                            }
                        }
                    }
                    removeGroups();
                    startDiscoveryDevices();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }







    private void removeGroups() {
        wifiP2pManager.cancelConnect(channel, null);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
            return;

        }
        wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {

                wifiP2pManager.removeGroup(channel, null);
            }
        });
    }









    private void startDiscoveryDevices() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
            return;

        }
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (isReceiver)
                    startLocalService();
                if (!isReceiver)
                    startServiceDiscovery();
            }

            @Override
            public void onFailure(int i) {

                //  stopPeerDiscovery();
            }
        });
    }













    private void stopPeerDiscovery() {
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                devicesAdapter.submitList(null);
                startDiscoveryDevices();
            }

            @Override
            public void onFailure(int i) {
                startDiscoveryDevices();
            }
        });
    }











    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);


        wifiBroadCast = new WifiBroadCast();
        getActivity().registerReceiver(wifiBroadCast, intentFilter);


    }

















    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View v = getLayoutInflater().inflate(R.layout.wifilocationtuenonlayout, null);
        builder.setView(v);
        final Button wifiTurnOnButton = v.findViewById(R.id.turnonwifibutton);
        final Button locationTurnOnButton = v.findViewById(R.id.turnonlocationbutton);
        final LinearLayout wifiLinearLayout = v.findViewById(R.id.wifilinearlayout);
        final LinearLayout locationLinearLayout = v.findViewById(R.id.locationlinearlayout);
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (wifiManager.isWifiEnabled() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    removeGroups();

                    startDiscoveryDevices();

                    setupListeners();
                } else {
                    locationLinearLayout.setVisibility(View.VISIBLE);
                    wifiLinearLayout.setVisibility(View.VISIBLE);
                    showAlertDialog();
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        isLocationChanged.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (alertDialog != null) {

                    if (aBoolean) {
                        locationTurnedOnsetting(locationTurnOnButton, locationLinearLayout);
                    } else {

                        locationLinearLayout.setVisibility(View.VISIBLE);
                        locationTunedOffSetting(locationTurnOnButton, locationLinearLayout);
                    }

                }
            }
        });

        isWifiChanged.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (alertDialog != null) {
                    if (aBoolean) {
                        wifiTurnedOnSetting(wifiTurnOnButton);
                    } else {
                        wifiTurnOffsetting(wifiTurnOnButton);
                        wifiLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (wifiManager.isWifiEnabled()) {
            wifiTurnedOnSetting(wifiTurnOnButton);
            wifiLinearLayout.setVisibility(View.GONE);
        } else {
            wifiManager.setWifiEnabled(true);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (wifiManager.isWifiEnabled()) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d("truee", "true");
//                    builder.setCancelable(true);
//                    alertDialog.setCancelable(true);
                    alertDialog.cancel();
                    alertDialog.dismiss();


                }
                wifiTurnedOnSetting(wifiTurnOnButton);
            } else wifiTurnOffsetting(wifiTurnOnButton);


            wifiTurnOnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (turnOnWifi(wifiManager)) {
                        wifiTurnedOnSetting(wifiTurnOnButton);
                    }

                }
            });

        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationTurnedOnsetting(locationTurnOnButton, locationLinearLayout);
        } else {
            locationTunedOffSetting(locationTurnOnButton, locationLinearLayout);
        }


        locationTurnOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (turnOnLOcation(locationManager))
                    locationTurnedOnsetting(locationTurnOnButton, locationLinearLayout);
            }
        });


    }

    private void wifiTurnOffsetting(Button wifiTurnOnButton) {
        wifiTurnOnButton.setText("Turn on");
        setButtonColor(wifiTurnOnButton, R.color.errorColor, R.drawable.buttondrawableerror);
        wifiTurnOnButton.setClickable(true);
    }

    private void wifiTurnedOnSetting(Button wifiTurnOnButton) {
        wifiTurnOnButton.setText("Turned On");
        setButtonColor(wifiTurnOnButton, R.color.colorPrimary, R.drawable.buttonshape);
        wifiTurnOnButton.setClickable(false);
    }

    private void locationTunedOffSetting(Button locationTurnOnButton, LinearLayout locationLinearLayout) {
        setButtonColor(locationTurnOnButton, R.color.errorColor, R.drawable.buttondrawableerror);
        locationTurnOnButton.setText("Turn On");
        //  locationLinearLayout.setVisibility(View.VISIBLE);
        locationTurnOnButton.setClickable(true);
    }

    private void locationTurnedOnsetting(Button locationTurnOnButton, LinearLayout locationLinearLayout) {
        setButtonColor(locationTurnOnButton, R.color.colorPrimary, R.drawable.buttonshape);
        locationTurnOnButton.setText("Turned On");
        // locationLinearLayout.setVisibility(View.GONE);
        locationTurnOnButton.setClickable(false);
    }

    private void setButtonColor(Button wifiTurnOnButton, int p, int p2) {
        wifiTurnOnButton.setTextColor(getResources().getColor(p));
        wifiTurnOnButton.setBackground(getResources().getDrawable(p2));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(wifiBroadCast);
    }

    @Override
    public void onDestroy() {
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                devicesAdapter.submitList(new ArrayList<WifiP2pDevice>());
                //          Toast.makeText(getContext(), "Discovery Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                //  Toast.makeText(getContext(), "Discovery Stop failed", Toast.LENGTH_SHORT).show();
            }
        });
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!wifiManager.isWifiEnabled() || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showAlertDialog();
                } else {
                    removeGroups();

                    startDiscoveryDevices();

                    setupListeners();
                }
            } else {
                Toast.makeText(getContext(), "Location permissions are needed . Please allow location permissions", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private boolean turnOnLOcation(LocationManager locationManager) {

        Toast.makeText(getContext(), "Please turn on location", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private boolean turnOnWifi(WifiManager wifiManager) {
        Toast.makeText(getContext(), "Please turn on wlan", Toast.LENGTH_SHORT).show();
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent = new Intent(Settings.Panel.ACTION_WIFI);

        } else {
            intent = new Intent(Settings.ACTION_WIFI_SETTINGS);

        }
        startActivity(intent);
        return wifiManager.isWifiEnabled();
    }

}
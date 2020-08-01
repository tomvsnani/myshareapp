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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
   MyBroadcastReceivers wifiBroadCast;
    RecyclerView devicesListView;
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
    TextView showConnectMessageTextView;
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
        showConnectMessageTextView = v.findViewById(R.id.showconnectmessageTextview);
        toolbar = v.findViewById(R.id.mainactivitytoolbar);

        if(isReceiver)
            showConnectMessageTextView.setText("Please wait until the connection Request is made .");
        else showConnectMessageTextView.setText("Please wait until the device is discovered .");

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

        } else map.put("name", Build.BRAND + " " + Build.MODEL + " " + Build.PRODUCT);

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp"
                , map);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getContext(), "Please allow location permission", Toast.LENGTH_SHORT).show();
            return;


        }
        wifiP2pManager.addLocalService(channel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {



            }

            @Override
            public void onFailure(int i){
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

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wifiP2pDevice.deviceName = deviceNames[0];
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!wifiP2pDevices.contains(wifiP2pDevice)) {
                    wifiP2pDevices.add(wifiP2pDevice);

                    showConnectMessageTextView.setText("Please select the device you want to connect to .");

                }

                devicesAdapter.submit(wifiP2pManager, wifiP2pDevices, channel, connectDisconnectActionListener);


            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String s, Map<String, String> map, WifiP2pDevice wifiP2pDevice) {

                deviceNames[0] = (map.get("name"));
            }
        });
        wifiP2pManager.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

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


    void deletePersistentGroups() {
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


    void startDiscoveryDevices() {
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


        wifiBroadCast = new MyBroadcastReceivers(wifiP2pManager,channel,MainFragment.this);

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
                    deletePersistentGroups();

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

            }

            @Override
            public void onFailure(int i) {

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

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);


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
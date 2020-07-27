package com.example.mysharecare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DevicesAdapter extends ListAdapter<WifiP2pDevice, DevicesAdapter.DeviceViewHolder> {
    Context context;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    WifiP2pManager.ActionListener actionListene;

    protected DevicesAdapter(Context context) {
        super(wifiP2pDeviceItemCallback);
        this.context = context;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.selectionadapterrow, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        WifiP2pDevice listModel = getCurrentList().get(position);
        holder.textView.setText(listModel.deviceName);
        holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_phone_android_24));
    }


    public void submit(WifiP2pManager wifiP2pManager,
                       List<WifiP2pDevice> listModels, WifiP2pManager.Channel channel, WifiP2pManager.ActionListener actionListene) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.actionListene = actionListene;
        submitList(listModels);
        Log.d("adaptersize", String.valueOf(getCurrentList().size()));
    }

    @Override
    public void submitList(@Nullable List<WifiP2pDevice> list) {
        super.submitList(list == null ? null : new ArrayList<WifiP2pDevice>(list));
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        LinearLayout linearLayout;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.nametextview);
            imageView = itemView.findViewById(R.id.icontextview);
            linearLayout = itemView.findViewById(R.id.idlinear);
            linearLayout.setClickable(true);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
                    final WifiP2pDevice wifiP2pDevice = getCurrentList().get(getAdapterPosition());
                    if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                        linearLayout.setClickable(false);
                    }

                    if (wifiP2pDevice.status == WifiP2pDevice.FAILED) {
                        wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                connectToDevice(wifiP2pDevice);
                            }

                            @Override
                            public void onFailure(int i) {
                                connectToDevice(wifiP2pDevice);
                            }
                        });
                    }
                    connectToDevice(wifiP2pDevice);

                }
            });
        }
    }

    private void connectToDevice(WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
        wifiP2pConfig.groupOwnerIntent = 15;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        wifiP2pManager.connect(channel, wifiP2pConfig, actionListene);
    }

    @Override
    public int getItemCount() {
        Log.d("adaptersizee", "one");
        Log.d("adaptersizee", String.valueOf(getCurrentList().size()));
        return Math.max(0, getCurrentList().size());
    }

    static DiffUtil.ItemCallback<WifiP2pDevice> wifiP2pDeviceItemCallback = new DiffUtil.ItemCallback<WifiP2pDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull WifiP2pDevice oldItem, @NonNull WifiP2pDevice newItem) {
            return areItemsSame(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull WifiP2pDevice oldItem, @NonNull WifiP2pDevice newItem) {
            return areItemsSame(oldItem, newItem);
        }
    };

    static public boolean areItemsSame(WifiP2pDevice oldItem, WifiP2pDevice newItem) {
        if (oldItem != null && newItem != null)
            return oldItem.deviceName.equals(newItem.deviceName) && oldItem.deviceAddress.equals(newItem.deviceName);
        return true;
    }

}




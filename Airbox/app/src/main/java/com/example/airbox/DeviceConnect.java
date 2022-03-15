package com.example.airbox;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeviceConnect extends AppCompatActivity  {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //返回上一頁
    private ImageButton btnBack;

    //藍芽連接
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private static final int REQUEST_ENABLE_BT = 2;

    //搜尋到的藍芽裝置
    private final Set<BluetoothDevice> discoveredDevices = new HashSet<>();

    //搜尋到的裝置列表
    private RecyclerViewAdapter recyclerViewAdapter;

    //搜尋藍芽按鈕
    private Button buttonDiscovery;

    //廣播
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) return;

                Log.d("onReceive", device.getName() + ":" + device.getAddress());
                if(device.getName()!=null) {
                    //過濾非airpocket的藍芽訊號
                    if (device.getName().contains("airpocket")) {
                        discoveredDevices.add(device);
                        updateList();
                    }
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                stopDiscovery();
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_connect);

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DeviceConnect.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到地圖頁面
        btnMap = findViewById(R.id.mapButton);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DeviceConnect.this, MapsActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到好友頁面
        btnFriend = findViewById(R.id.friend);
        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DeviceConnect.this, Friend.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到裝置頁面
        btnDevice = findViewById(R.id.device);
        btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(DeviceConnect.this, Device.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到推薦頁面
        btnTourism = findViewById(R.id.tourism);
        btnTourism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(DeviceConnect.this, Tourism.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到更多頁面
        btnSet = findViewById(R.id.set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(DeviceConnect.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/set_button_click", null, getPackageName()); //取得圖片Resource位子
        btnSet.setImageResource(imageResource);

        //版面設定
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());

        recyclerViewAdapter = new RecyclerViewAdapter();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(dividerItemDecoration);

        //檢查裝置是否有藍芽功能
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("本裝置不支援藍芽功能")
                    .setCancelable(false)
                    .setMessage("本裝置不支援藍芽功能。")
                    .setNeutralButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }

        //檢查藍芽是否開啟
        if(!bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_ENABLE_BT);
        }

        buttonDiscovery = findViewById(R.id.buttonDiscovery);
        buttonDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //檢查權限
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(DeviceConnect.this, "android.permission.ACCESS_COARSE_LOCATION")) {
                    ActivityCompat.requestPermissions(DeviceConnect.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 0);
                    return;
                }
                //判斷藍芽是否開啟
                if (!bluetoothAdapter.isEnabled()) {
                    Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intentBluetoothEnable);
                    return;
                }
                discoverDevices();
            }
        });

        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(broadcastReceiver, filter);

        updateList();
    }

    //將socket關閉
    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();

        SharedPreferences pref=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
        if(pref.contains("DeviceName")) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }

    //搜尋附近的藍芽裝置
    @SuppressLint("MissingPermission")
    private void startDiscovery() {
        bluetoothAdapter.startDiscovery();
        buttonDiscovery.setText("停止搜尋藍芽裝置");
    }

    //停止搜尋藍芽裝置
    @SuppressLint("MissingPermission")
    private void stopDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        buttonDiscovery.setText("搜尋附近的藍芽裝置");
    }

    //當搜尋到裝置時，停止搜尋並更新list，再繼續搜尋
    private void discoverDevices() {
        stopDiscovery();

        discoveredDevices.clear();
        updateList();

        startDiscovery();
    }

    private void updateList() {
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private ImageView connectImage;
        private TextView deviceName;
        private BluetoothDevice device;
        private boolean isPaired;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            connectImage = itemView.findViewById(R.id.connectImage);
            deviceName = itemView.findViewById(R.id.deviceName);

            //連接裝置
            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onClick(View view) {
                    stopDiscovery();

                    AlertDialog.Builder alertDialog =
                            new AlertDialog.Builder(DeviceConnect.this);
                    alertDialog.setTitle("請確認連接裝置名稱");
                    alertDialog.setMessage(device.getName());
                    alertDialog.setPositiveButton("配對", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final BluetoothDevice airdevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

                            try {
                                socket = airdevice.createRfcommSocketToServiceRecord(serialPortUUID);
                                socket.connect();

                                //將裝置address存進userDevice
                                SharedPreferences pref=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
                                SharedPreferences.Editor editor= pref.edit();
                                editor.putString("DeviceName",device.getName());
                                editor.putString("DeviceAddress",device.getAddress());
                                editor.commit();

                                //確認裝置address是否儲存進serDevice中的address
                                String name = getSharedPreferences("userDevice", MODE_MULTI_PROCESS).getString("DeviceName", "");
                                Log.v("name",name);
                                String address = getSharedPreferences("userDevice", MODE_MULTI_PROCESS).getString("DeviceAddress", "");
                                Log.v("address",address);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent();
                            intent.setClass(DeviceConnect.this, UserSet.class);
                            intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);

                        }
                    });
                    alertDialog.setCancelable(true);
                    alertDialog.show();
                }
            });

        }

        //顯示搜尋到的裝置以及設定圖示
        @SuppressLint("MissingPermission")
        void loadDevice(@NonNull BluetoothDevice device, boolean isPaired) {
            this.device = device;
            this.isPaired = isPaired;

            String name = this.device.getName();

            connectImage.setImageResource(this.isPaired ? R.drawable.connect : R.drawable.can_clickbtn);
            deviceName.setText(name);
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
            return new RecyclerViewHolder(view);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            /*
            if (position < pairedDevices.size()) {
                holder.loadDevice(pairedDevices.toArray(new BluetoothDevice[0])[position], true);
            } else {
                holder.loadDevice(discoveredDevices.toArray(new BluetoothDevice[0])[position - pairedDevices.size()], false);
            }
            */

            //只顯示airpocket的裝置
            int devic_show = bluetoothAdapter.getBondedDevices().size();

            holder.loadDevice(discoveredDevices.toArray(new BluetoothDevice[devic_show])[position], false);
        }

        @SuppressLint("MissingPermission")
        @Override
        public int getItemCount() {
            //return bluetoothAdapter.getBondedDevices().size() + discoveredDevices.size();
            return discoveredDevices.size();
        }
    }
}
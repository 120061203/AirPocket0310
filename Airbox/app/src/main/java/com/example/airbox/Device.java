package com.example.airbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class Device extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //刷新按鈕
    private ImageButton refresh_btn;

    //顯示溫溼度和PM25
    private TextView temp;
    private TextView humid;
    private TextView pm25_text;

    //定位
    private TextView sitename;
    private LocationManager locationManager;
    private String provider;
    List<String> list;
    private Double latitude;
    private Double longitude;
    private String lat_str="";
    private String lon_str="";
    private String city="";

    //顯示使用者當前位置的文字
    public TextView userLocation;

    //更新使用者當前位置的按鈕
    public ImageButton updateLocation;

    //顯示小樹
    private ImageView tree_show;

    //藍芽連接
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private boolean readerStop;

    //使用者最近測站資料的網址
    private String device_url = "http://airbox.servegame.com/weather/";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        //確認手機權限
        getPermission();

        //宣告手機裝置的位置管理
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        tree_show = findViewById(R.id.tree_show);
        userLocation = findViewById(R.id.userLocation_show);
        updateLocation = findViewById(R.id.updataLocation);
        temp = findViewById(R.id.temp);
        humid = findViewById(R.id.humid);
        pm25_text = findViewById(R.id.pm25);

        //刷新資料
        refresh_btn = findViewById(R.id.refresh);
        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

                /*
                getLocation();

                SharedPreferences pref_email=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);

                //未登入
                if(pref_email.contains("email")==false)
                {
                    Toast.makeText(Device.this, "更新資料", Toast.LENGTH_LONG).show();
                    sendGET();
                }
                //已登入
                else
                {
                    SharedPreferences pref=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
                    if(pref.contains("DeviceName")==false && inputStream==null)
                    {
                        Toast.makeText(Device.this, "更新資料", Toast.LENGTH_LONG).show();
                        sendGET();
                    }
                    else
                    {
                        Toast.makeText(Device.this, "接收裝置資料", Toast.LENGTH_LONG).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                socket = null;
                                inputStream = null;
                                readerStop = true;

                                connectDevice();
                            }
                        }).start();

                    }
                }
                */
            }
        });

        //切換到地圖頁面
        btnMap = findViewById(R.id.mapButton);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Device.this, MapsActivity.class);
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
                intent.setClass(Device.this, Friend.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到裝置頁面
        btnDevice = findViewById(R.id.device);
        btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/device_button_click", null, getPackageName()); //取得圖片Resource位子
        btnDevice.setImageResource(imageResource);

        //切換到推薦頁面
        btnTourism = findViewById(R.id.tourism);
        btnTourism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Device.this, Tourism.class);
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
                intent.setClass(Device.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //獲取使用者目前位置
        getLocation();

        SharedPreferences pref=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
        if(pref.contains("DeviceName")==false)
        {
            sendGET();
            Toast.makeText(this, "未連接裝置，顯示最近測站之資料", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "接收裝置資料", Toast.LENGTH_LONG).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectDevice();
                }
            }).start();
        }
    }

    //取得當前GPS info
    @SuppressLint("MissingPermission")
    public void getLocation()
    {
        Geocoder gc = new Geocoder(this, Locale.TRADITIONAL_CHINESE);

        list = locationManager.getProviders(true);
        if(list.contains(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
        }
        else if(list.contains(LocationManager.NETWORK_PROVIDER))
        {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else
        {
            provider = null;
        }

        locationManager.requestLocationUpdates(provider, 2000, 2, locationListener);

        try{
            //使用者沒有開啟位置資訊
            if(provider==null)
            {
                new AlertDialog.Builder(this)
                        .setTitle("提醒")
                        .setMessage("請開啟位置資訊才可以正常使用此應用程式")
                        .setCancelable(false)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                latitude = 22.7344107;
                                longitude = 120.2849883;
                                city="高雄市楠梓區";
                            }
                        })
                        .show();

                locationManager.removeUpdates(locationListener);
            }
            else
            {
                Location location = locationManager.getLastKnownLocation(provider);

                while (location == null) {
                    location = locationManager.getLastKnownLocation(provider);
                }
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                lat_str = Double.toString(latitude);
                lon_str = Double.toString(longitude);
                Log.v("latitude", lat_str);
                Log.v("longitude", lon_str);

                city = GPSCity(gc, latitude, longitude);
            }
            device_url = device_url + latitude + "," + longitude;
            userLocation.setText(city);
            locationManager.removeUpdates(locationListener);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //更新使用者當前位置
    @SuppressLint("MissingPermission")
    public void updateLocation(View view)
    {
        Geocoder gc = new Geocoder(this, Locale.TRADITIONAL_CHINESE);

        list = locationManager.getProviders(true);
        if(list.contains(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
        }
        else if(list.contains(LocationManager.NETWORK_PROVIDER))
        {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else
        {
            provider = null;
        }

        locationManager.requestLocationUpdates(provider, 2000, 2, locationListener);

        try{
            //使用者沒有開啟位置資訊
            if(provider==null)
            {
                new AlertDialog.Builder(this)
                        .setTitle("提醒")
                        .setMessage("未開啟定位資訊，更新位置失敗")
                        .setCancelable(false)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                latitude = 22.7344107;
                                longitude = 120.2849883;
                                city="高雄市楠梓區";
                            }
                        })
                        .show();

                locationManager.removeUpdates(locationListener);
            }
            else
            {
                Location location = locationManager.getLastKnownLocation(provider);

                while (location == null) {
                    location = locationManager.getLastKnownLocation(provider);
                }
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                lat_str = Double.toString(latitude);
                lon_str = Double.toString(longitude);
                Log.v("latitude", lat_str);
                Log.v("longitude", lon_str);

                city = GPSCity(gc, latitude, longitude);
            }
            device_url = device_url + latitude + "," + longitude;
            userLocation.setText(city);
            locationManager.removeUpdates(locationListener);

            Toast.makeText(this, "已更新位置", Toast.LENGTH_LONG).show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //將經緯度轉換成地址
    public static String GPSCity(Geocoder gc, double latitude, double longitude)
    {
        String city = "";
        try {

            List<Address> lstAddress = gc.getFromLocation(latitude, longitude, 1);
            //String returnAddress = lstAddress.get(0).getAddressLine(0);
            city = lstAddress.get(0).getAdminArea() + lstAddress.get(0).getLocality();
            System.out.println("GPSCity: " + lstAddress.get(0).getAdminArea());
            //lstAddress.get(0).getCountryName();  //省
            //lstAddress.get(0).getAdminArea();  //市
            //lstAddress.get(0).getLocality();  //區
            //lstAddress.get(0).getThoroughfare();  //街(包含路巷弄)
            //lstAddress.get(0).getFeatureName();  //號
            //lstAddress.get(0).getPostalCode();  //郵遞區號
        }catch(Exception e){
            e.printStackTrace();
        }
        return city;
    }

    //監聽位置變化
    LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.v("lat_update",Double.toString(latitude));
            Log.v("lon_update",Double.toString(longitude));
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            //GPS打開
        }

        @Override
        public void onProviderDisabled(String provider) {
            //GPS 關閉
        }
    };

    //獲取手機權限
    public void getPermission()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(Device.this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("提醒")
                        .setMessage("此應用程式，需要位置權限才能正常使用")
                        .setCancelable(false)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(Device.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                            }
                        })
                        .show();
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }

        }
    }

    //對於使用者按下權限請求的callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(Device.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "被永遠拒絕，只能使用者手動給予權限", Toast.LENGTH_LONG).show();
                    //開啟應用程式資訊，讓使用者手動給予權限
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(Device.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                    Toast.makeText(this, "按下拒絕", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "允許權限", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences pref=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
        if(pref.contains("DeviceName") && inputStream !=null)
        {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
            inputStream = null;
            readerStop = true;
        }

    }

    //抓取最近測站的資料
    private void sendGET() {
        ProgressDialog dialog = ProgressDialog.show(Device.this,"讀取中","請稍候",true);
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(device_url)
        //      .header("Cookie","")//有Cookie需求的話則可用此發送
        //      .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                Log.v("error", e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(Device.this, "加載失敗，請重新整理", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                //String text = responseBodyCopy.string();
                String text = response.body().string();
                String[] record = text.split("\\{'time': '|', 'SiteName': '|', 's_t0': |, 's_h0': |, 's_d0': |\\}");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Log.v("air_len", String.valueOf(record.length));
                        if (record.length == 6) {
                            Double air_dou = Double.parseDouble(record[5]);
                            Integer air_int = (int) Math.round(air_dou);
                            if (air_int <= 35) {
                                int imageResource = getResources().getIdentifier("@drawable/tree_one", null, getPackageName()); //取得圖片Resource位子
                                tree_show.setImageResource(imageResource);
                            } else if (air_int > 35 && air_int <= 41) {
                                int imageResource = getResources().getIdentifier("@drawable/tree_two", null, getPackageName()); //取得圖片Resource位子
                                tree_show.setImageResource(imageResource);
                            } else if (air_int > 41 && air_int <= 53) {
                                int imageResource = getResources().getIdentifier("@drawable/tree_three", null, getPackageName()); //取得圖片Resource位子
                                tree_show.setImageResource(imageResource);
                            } else if (air_int > 53 && air_int <= 70) {
                                int imageResource = getResources().getIdentifier("@drawable/tree_four", null, getPackageName()); //取得圖片Resource位子
                                tree_show.setImageResource(imageResource);
                            } else if (air_int > 70) {
                                int imageResource = getResources().getIdentifier("@drawable/tree_five", null, getPackageName()); //取得圖片Resource位子
                                tree_show.setImageResource(imageResource);
                            }

                            sitename = findViewById(R.id.sitename_show);

                            sitename.setText("測站：" + record[2]);
                            temp.setText(record[3] + "℃");
                            humid.setText(record[4] + "%");
                            pm25_text.setText(record[5]);

                            //空汙警報
                            SharedPreferences pref = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS);

                            if (pref.contains("Airpollution")) {
                                String data = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("Airpollution", "");
                                Log.v("Airpollution", data);

                                String pollution = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("PollutionDone", "");

                                if (air_int > Integer.parseInt(data) && pollution.equals("已提示") == false) {

                                    //將check_del儲存進mysqlcheck
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("PollutionDone", "已提示");
                                    editor.commit();

                                    new AlertDialog.Builder(Device.this)
                                            .setTitle("空汙警報")
                                            .setMessage("當前PM2.5濃度超過警報上限")
                                            .setCancelable(false)
                                            .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .show();
                                }
                            } else {
                                String pollution = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("PollutionDone", "");

                                int data = 35;
                                if (air_int > data && pollution.equals("已提示") == false) {

                                    //將check_del儲存進mysqlcheck
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("PollutionDone", "已提示");
                                    editor.commit();

                                    new AlertDialog.Builder(Device.this)
                                            .setTitle("提醒")
                                            .setMessage("當前PM2.5濃度超過警報上限")
                                            .setCancelable(false)
                                            .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .show();
                                }
                            }
                        } else {
                            Toast.makeText(Device.this, "加載失敗，請重新整理", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }

    //連接裝置
    @SuppressLint("MissingPermission")
    private void connectDevice(){
        String deviceName = getSharedPreferences("userDevice", MODE_MULTI_PROCESS).getString("DeviceName", "");
        Log.v("name",deviceName);
        String deviceAddress = getSharedPreferences("userDevice", MODE_MULTI_PROCESS).getString("DeviceAddress", "");
        Log.v("address",deviceAddress);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(serialPortUUID);
            socket.connect();
            inputStream = socket.getInputStream();
            reader();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Device.this, "已連接裝置，資料上傳中", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Device.this, "未成功連接裝置，顯示最近測站之資料", Toast.LENGTH_LONG).show();
                }
            });
            sendGET();
            e.printStackTrace();
        }
    }

    //讀取藍芽傳輸資料
    private void reader() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                readerStop = false;
                while (!readerStop) {
                    read();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //處理藍芽傳輸資料
    private void read() {
        if (inputStream == null) return;

        try {
            if (inputStream.available() <= 0) return;
            byte[] buffer = new byte[256];

            String data = new String(buffer, 0, inputStream.read(buffer));
            Log.v("devicedata",data);

            String boxID = data.substring(11,28);
            String data_split = data.substring(37,data.length()-4);
            Log.v("boxID",boxID);
            Log.v("data_split",data_split);

            String[] each_data_split = data_split.split("\\{\"year\":\"|\",\"month\":\"|\",\"day\":\"|\",\"hour\":\"|\",\"minute\":\"|\",\"second\":\"|\",\"temperature\":\"|\",\"humidity\":\"|\",\"pm100\":\"|\",\"pm25\":\"|\",\"pm1\":\"|\",\"longitude\":\"|\",\"latitude\":\"\"\\}");

            String temperature = each_data_split[7];
            String humidity = each_data_split[8];
            String pm100 = each_data_split[9];
            String pm25 = each_data_split[10];
            String pm1 = each_data_split[11];

            SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dff.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
            String dataID = dff.format(new Date());
            Log.v("time",dataID);
            String[] dataID_split = dataID.split("-| |:");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    temp.setText(temperature+"℃");
                    humid.setText(humidity+"%");
                    pm25_text.setText(pm25);

                    //小樹的顏色
                    int air = Integer.parseInt(pm25);
                    if(air<=35)
                    {
                        int imageResource = getResources().getIdentifier("@drawable/tree_one", null, getPackageName()); //取得圖片Resource位子
                        tree_show.setImageResource(imageResource);
                    }
                    else if(air>35 && air<=41)
                    {
                        int imageResource = getResources().getIdentifier("@drawable/tree_two", null, getPackageName()); //取得圖片Resource位子
                        tree_show.setImageResource(imageResource);
                    }

                    else if(air>41 && air<=53)
                    {
                        int imageResource = getResources().getIdentifier("@drawable/tree_three", null, getPackageName()); //取得圖片Resource位子
                        tree_show.setImageResource(imageResource);
                    }
                    else if(air>53 && air<=70)
                    {
                        int imageResource = getResources().getIdentifier("@drawable/tree_four", null, getPackageName()); //取得圖片Resource位子
                        tree_show.setImageResource(imageResource);
                    }
                    else if(air>70)
                    {
                        int imageResource = getResources().getIdentifier("@drawable/tree_five", null, getPackageName()); //取得圖片Resource位子
                        tree_show.setImageResource(imageResource);
                    }

                    //空汙警報
                    SharedPreferences pref = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS);

                    if (pref.contains("Airpollution")) {
                        String data = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("Airpollution", "");
                        Log.v("Airpollution", data);

                        String pollution = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("PollutionDone", "");

                        if (air > Integer.parseInt(data) && pollution.equals("已提示") == false) {

                            //將check_del儲存進mysqlcheck
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("PollutionDone", "已提示");
                            editor.commit();

                            new AlertDialog.Builder(Device.this)
                                    .setTitle("空汙警報")
                                    .setMessage("當前PM2.5濃度超過警報上限")
                                    .setCancelable(false)
                                    .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                    } else {
                        String pollution = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("PollutionDone", "");

                        int data = 35;
                        if (air > data && pollution.equals("已提示") == false) {

                            //將check_del儲存進mysqlcheck
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("PollutionDone", "已提示");
                            editor.commit();

                            new AlertDialog.Builder(Device.this)
                                    .setTitle("提醒")
                                    .setMessage("當前PM2.5濃度超過警報上限")
                                    .setCancelable(false)
                                    .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }

                }
            });

            //上傳裝置資料
            String year = dataID_split[0];
            String month = dataID_split[1];
            String day = dataID_split[2];
            String hour = dataID_split[3];
            String minute = dataID_split[4];
            String second = dataID_split[5];

            String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
            Log.v("ip",ip);

            MysqlCon con = new MysqlCon();
            con.connMql(ip);

            con.insertDeviceData(dataID,boxID,year,month,day,hour,minute,second,temperature,humidity,pm100,pm25,pm1,lat_str,lon_str);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
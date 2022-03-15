package com.example.airbox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Set;

public class UserSet extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //更多的功能
    private Button btnConn;
    private Button btnConn_done;
    private Button btnAir;
    private Button btnAccount;
    private Button btnLogout;

    //使用者頭像
    private TextView btnUserNameText;
    private Button btnUserName;

    //更多功能的文字
    private TextView btnConnText;
    private TextView btnAccountText;

    //藍芽連接
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_set);

        //藍芽適配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnUserNameText = (TextView) findViewById(R.id.user_name_background);
        btnConnText = (TextView) findViewById(R.id.connButton);
        btnLogout = findViewById(R.id.logoutButton);
        btnAccountText = (TextView) findViewById(R.id.accountButton);

        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);

        //未登入
        if(pref.contains("email")==false)
        {
            Log.v("login","未登入");

            //使用者頭像
            btnUserNameText.setText("未登入");
            btnUserNameText.setTextSize(13);

            //點擊頭像跳到登入頁面
            btnUserName = findViewById(R.id.user_name_background);
            btnUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(UserSet.this, Login.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });

            //添加裝置的文字設定
            btnConnText.setText("添加裝置");

            //未登入不可綁定裝置
            btnConn = findViewById(R.id.connButton);
            btnConn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder connalertDialog =
                            new AlertDialog.Builder(UserSet.this);
                    connalertDialog.setTitle("提示訊息");
                    connalertDialog.setMessage("請先進行登入再綁定裝置");
                    connalertDialog.setPositiveButton("離開", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    connalertDialog.setCancelable(true);
                    connalertDialog.show();
                }
            });

            //登出按鈕不可見
            btnLogout.setVisibility(View.INVISIBLE);

            //帳號的文字設定
            btnAccountText.setText("登入");
        }

        //已登入
        if(pref.contains("email"))
        {
            //登入後頭像不可點擊
            btnUserName = findViewById(R.id.user_name_background);
            btnUserName.setEnabled(false);

            //顯示使用者頭像
            String name_text = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("name", "");
            Log.v("name_text",name_text);

            String name_show = name_text.substring(name_text.length() - 2);
            Log.v("name_show",name_show);

            //頭像文字設定
            btnUserNameText.setText(name_show);
            btnUserNameText.setTextSize(22);

            //修改帳號基本資料的文字設定
            btnAccountText.setText("帳號基本資料");

            //抓取連接裝置的名稱
            String name = getSharedPreferences("userDevice", MODE_MULTI_PROCESS).getString("DeviceName", "");
            Log.v("DeviceName", name);

            String address = getSharedPreferences("userDevice", MODE_MULTI_PROCESS).getString("DeviceAddress", "");
            Log.v("address",address);

            //有連接裝置
            if (name.equals("") == false) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                        Log.v("ip",ip);

                        MysqlCon con = new MysqlCon();
                        con.connMql(ip);

                        String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                        Log.v("id_store",id_store);

                        //獲取裝置的mac碼
                        String[] mac = name.split("airpocket");

                        //檢查資料庫中是否有存入使用者的裝置資料
                        String check = con.checkUserDevice(id_store,mac[1]);
                        Log.v("check",check);

                        //沒有裝置資料
                        if (check.equals("true")==false)
                        {
                            //新增裝置資料到資料庫中
                            con.insertDevice(id_store,mac[1]);
                        }
                    }
                }).start();

                //連接裝置的文字設定
                btnConnText.setText(name);
                btnConnText.setTextSize(16);

                //有裝置時點擊連接裝置的按鈕
                btnConn_done = findViewById(R.id.connButton);
                btnConn_done.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder del_connalertDialog =
                                new AlertDialog.Builder(UserSet.this);
                        del_connalertDialog.setTitle("請確認解除配對");
                        del_connalertDialog.setMessage(name);
                        del_connalertDialog.setPositiveButton("解除配對", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                                        Log.v("ip",ip);

                                        MysqlCon con = new MysqlCon();
                                        con.connMql(ip);

                                        String userID = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                                        Log.v("userID",userID);

                                        //抓取裝置的mac碼
                                        String[] mac = name.split("airpocket");

                                        //使用者解除配對，將裝置id資料移除
                                        con.delDevice(userID,mac[1]);
                                    }
                                }).start();

                                //將藍芽裝置解除配對
                                try {
                                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                                    String s = String.valueOf(pairedDevices.size());
                                    Log.v("pair", s);

                                    if (pairedDevices.size() > 0) {
                                        for (BluetoothDevice device : pairedDevices) {
                                            if (device.getName().contains("airpocket")) {
                                                try {
                                                    Method m = device.getClass()
                                                            .getMethod("removeBond", (Class[]) null);
                                                    m.invoke(device, (Object[]) null);
                                                } catch (Exception e) {
                                                    Log.e("Removing failed.", e.getMessage());
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //清除userDevice的資料
                                SharedPreferences pref_device=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
                                SharedPreferences.Editor editor_device=pref_device.edit();
                                editor_device.clear();
                                editor_device.commit();

                                Intent intent = getIntent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                            }
                        });
                        del_connalertDialog.setCancelable(true);
                        del_connalertDialog.show();
                    }
                });

            }
            else
            {
                btnConnText.setText("添加裝置");

                //切換到藍芽連接裝置頁面
                btnConn = findViewById(R.id.connButton);
                btnConn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(UserSet.this, DeviceConnect.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                });
            }
        }

        //切換到地圖頁面
        btnMap = findViewById(R.id.mapButton);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(UserSet.this, MapsActivity.class);
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
                intent.setClass(UserSet.this, Friend.class);
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
                intent.setClass(UserSet.this, Device.class);
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
                intent.setClass(UserSet.this, Tourism.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到更多頁面
        btnSet = findViewById(R.id.set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/set_button_click", null, getPackageName()); //取得圖片Resource位子
        btnSet.setImageResource(imageResource);

        //切換到空汙警報設定的頁面
        btnAir = findViewById(R.id.airButton);
        btnAir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(UserSet.this, AirPollution.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到帳號設定頁面
        btnAccount = findViewById(R.id.accountButton);
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences pref = getSharedPreferences("userAccount", MODE_MULTI_PROCESS);

                //已登入跳到帳號設定頁面
                if (pref.contains("email"))
                {
                    Intent intent = new Intent();
                    intent.setClass(UserSet.this, AccountSet.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
                //未登入跳到登入頁面
                else
                {
                    Intent intent = new Intent();
                    intent.setClass(UserSet.this, Login.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }
        });

        //登出
        btnLogout = findViewById(R.id.logoutButton);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog =
                        new AlertDialog.Builder(UserSet.this);
                alertDialog.setTitle("您確定要登出嗎");
                alertDialog.setMessage("");
                alertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //清除所有紀錄
                        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor=pref.edit();
                        editor.clear();
                        editor.commit();

                        SharedPreferences pref_server=getSharedPreferences("server",MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor_server=pref_server.edit();
                        editor_server.clear();
                        editor_server.commit();

                        SharedPreferences pref_air=getSharedPreferences("userAirpollution",MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor_air= pref_air.edit();
                        editor_air.clear();
                        editor_air.commit();

                        SharedPreferences pref_attraction=getSharedPreferences("userAttraction",MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor_attraction= pref_attraction.edit();
                        editor_attraction.clear();
                        editor_attraction.commit();

                        SharedPreferences pref_device=getSharedPreferences("userDevice",MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor_device=pref_device.edit();
                        editor_device.clear();
                        editor_device.commit();

                        Intent intent = new Intent();
                        intent.setClass(UserSet.this, MapsActivity.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                });
                alertDialog.setNegativeButton("取消",(dialog, which) -> {
                    dialog.dismiss();
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });
    }
}
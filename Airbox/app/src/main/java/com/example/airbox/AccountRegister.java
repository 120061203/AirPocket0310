package com.example.airbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class AccountRegister extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //返回上一頁
    private ImageButton btnBack;

    //SharedPreferences變數
    private String server_ip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_register);

        String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
        Log.v("ip",ip);

        Button register_button = (Button) findViewById(R.id.register);
        register_button.setOnClickListener(register_btn);

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AccountRegister.this, Login.class);
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
                intent.setClass(AccountRegister.this, MapsActivity.class);
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
                intent.setClass(AccountRegister.this, Friend.class);
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
                intent.setClass(AccountRegister.this, Device.class);
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
                intent.setClass(AccountRegister.this, Tourism.class);
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
                intent.setClass(AccountRegister.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/set_button_click", null, getPackageName()); //取得圖片Resource位子
        btnSet.setImageResource(imageResource);
    }

    //使用者輸入基本資料註冊帳號
    private Button.OnClickListener register_btn = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AccountRegister.this,"註冊資料上傳中",Toast.LENGTH_SHORT).show();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //取得 EditText 資料
                    final EditText edit_name = (EditText) findViewById(R.id.name);
                    String stringname = edit_name.getText().toString();
                    final EditText edit_email = (EditText) findViewById(R.id.email);
                    String stringemail = edit_email.getText().toString();
                    final EditText edit_password = (EditText) findViewById(R.id.password);
                    String stringpassword = edit_password.getText().toString();

                    //名字長度
                    if(stringname.length()<3||stringname.length()>4)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountRegister.this,"名字長度應介於3到4個有效字元",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //郵件格式
                    else if(stringemail.contains("@")==false)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountRegister.this,"請輸入有效的郵件地址",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //欄位為空
                    else if(stringname.equals("")||stringemail.equals("")||stringpassword.equals(""))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountRegister.this,"欄位不可為空",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if(stringpassword.length()<6 || stringpassword.length()>12)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountRegister.this,"密碼之長度介於6到12個有效字元",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        //清空 EditText
                        edit_name.post(new Runnable() {
                            public void run() {
                                edit_name.setText("");
                            }
                        });
                        edit_email.post(new Runnable() {
                            public void run() {
                                edit_email.setText("");
                            }
                        });
                        edit_password.post(new Runnable() {
                            public void run() {
                                edit_password.setText("");
                            }
                        });

                        //將資料寫入資料庫
                        MysqlCon con = new MysqlCon();
                        con.connMql(server_ip);

                        //註冊帳號
                        con.insertData(stringname,stringemail,stringpassword);

                        //註冊成功則跳到登入頁面
                        Intent intent = new Intent();
                        intent.setClass(AccountRegister.this, Login.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountRegister.this,"註冊成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        }
    };
}
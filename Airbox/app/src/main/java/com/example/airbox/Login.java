package com.example.airbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //返回上一頁
    private ImageButton btnBack;

    //註冊按鈕
    private Button btnReg;

    //登入按鈕
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //設定按下登入按鈕的事件
        btnLogin = (Button) findViewById(R.id.login);
        btnLogin.setOnClickListener(login);

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Login.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到註冊頁面
        btnReg = (Button) findViewById(R.id.register);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Login.this, AccountRegister.class);
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
                intent.setClass(Login.this, MapsActivity.class);
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
                intent.setClass(Login.this, Friend.class);
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
                intent.setClass(Login.this, Device.class);
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
                intent.setClass(Login.this, Tourism.class);
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
                intent.setClass(Login.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/set_button_click", null, getPackageName()); //取得圖片Resource位子
        btnSet.setImageResource(imageResource);

    }

    //登入按鈕事件，確認帳號與密碼是否存在且正確
    private Button.OnClickListener login = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                    String ip = "http://192.168.58.187:8000/";
                    Log.v("ip",ip);

                    //取得 EditText 資料
                    final EditText edit_email = (EditText) findViewById(R.id.email);
                    String stringemail = edit_email.getText().toString();
                    final EditText edit_password = (EditText) findViewById(R.id.password);
                    String stringpassword = edit_password.getText().toString();

                    if(stringemail.equals("")||stringpassword.equals(""))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Login.this,"欄位不可為空",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        //清空 EditText
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

                        MysqlCon con = new MysqlCon();
                        con.connMql(ip);

                        //確認帳號密碼是否正確
                        String check = con.getUserData(stringemail,stringpassword);
                        Log.v("check",check);

                        //帳號正確
                        if (check.equals("true")==true)
                        {
                            con.connMql(ip);

                            //抓取使用者id
                            String UserID = con.getUserID(stringemail);

                            //抓取使用者名字
                            String Username = con.getUserName(stringemail);

                            //將使用者資料儲存進userAccount
                            SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);
                            SharedPreferences.Editor editor= pref.edit();
                            editor.putString("userID",UserID);
                            editor.putString("name",Username);
                            editor.putString("email",stringemail);
                            editor.putString("password",stringpassword);
                            editor.commit();

                            //確認帳號是否儲存進userAccount中的email
                            String useremail = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("email", "");
                            Log.v("email",useremail);

                            String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                            Log.v("id_store",id_store);

                            String name_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("name", "");
                            Log.v("name_store",name_store);

                            String password_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("password", "");
                            Log.v("password_store",password_store);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this,"登入成功",Toast.LENGTH_SHORT).show();
                                }
                            });

                            Intent intent = new Intent();
                            intent.setClass(Login.this, UserSet.class);
                            intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }
                        //帳號錯誤
                        else if(check.equals("false"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this,"帳號密碼錯誤，請重新輸入",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    };
}
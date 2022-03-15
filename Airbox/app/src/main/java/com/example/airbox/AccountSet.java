package com.example.airbox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AccountSet extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //返回上一頁
    private ImageButton btnBack;

    //個人資料設定
    private Button user_nameBtn;
    private Button user_emailBtn;
    private Button user_passwordBtn;
    private TextView user_name;
    private TextView edit_name;
    private TextView edit_email;

    //確認修改結果
    private String editname_check="";
    private String editemail_check="";
    private String editpassword_check="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_set);

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AccountSet.this, UserSet.class);
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
                intent.setClass(AccountSet.this, MapsActivity.class);
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
                intent.setClass(AccountSet.this, Friend.class);
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
                intent.setClass(AccountSet.this, Device.class);
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
                intent.setClass(AccountSet.this, Tourism.class);
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
                intent.setClass(AccountSet.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/set_button_click", null, getPackageName()); //取得圖片Resource位子
        btnSet.setImageResource(imageResource);

        //顯示使用者頭像
        String name_text = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("name", "");
        String name_show = name_text.substring(name_text.length() - 2);

        user_name = (TextView) findViewById(R.id.user_name_background);
        user_name.setText(name_show);

        //基本資料的名字顯示
        edit_name = (TextView) findViewById(R.id.edit_name);
        edit_name.setText(name_text);

        //修改名字
        user_nameBtn = findViewById(R.id.edit_name);
        user_nameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AccountSet.this);
                View view = getLayoutInflater().inflate(R.layout.acc_modify,null);
                alertDialog.setView(view);
                TextView title = view.findViewById(R.id.acc_modify_title);
                title.setText("修改暱稱");
                Button btnOK = view.findViewById(R.id.store);
                Button btnCan  = view.findViewById(R.id.cancel);
                EditText newNameText = view.findViewById(R.id.edit_acc);
                AlertDialog dialog = alertDialog.create();
                dialog.show();

                //將修改視窗的位置放在底部
                Window window = dialog.getWindow();
                window.setBackgroundDrawable(null);
                window.setGravity(Gravity.BOTTOM);

                WindowManager m = getWindowManager();
                Display d = m.getDefaultDisplay();
                WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
                p.width = d.getWidth();
                dialog.getWindow().setAttributes(p);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("newNameText",newNameText.getText().toString());
                                String new_name = newNameText.getText().toString();

                                //名字長度
                                if(new_name.length()<3||new_name.length()>4)
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AccountSet.this,"名字長度應介於3到4個有效字元",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                //欄位為空
                                else if(new_name.equals(""))
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AccountSet.this,"欄位不可為空",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else
                                {
                                    String email_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("email", "");
                                    Log.v("email_store",email_store);

                                    String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                                    Log.v("ip",ip);

                                    MysqlCon con = new MysqlCon();
                                    con.connMql(ip);

                                    //對資料庫修改名字
                                    editname_check = con.modifyName(email_store,new_name);

                                    Log.v("editname_check",editname_check);

                                    //修改成功
                                    if(editname_check.equals("true"))
                                    {
                                        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);
                                        SharedPreferences.Editor editor= pref.edit();
                                        editor.putString("name",new_name);
                                        editor.commit();

                                        dialog.cancel();

                                        Intent intent = getIntent();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(intent);
                                    }
                                    //修改失敗
                                    else
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(AccountSet.this,"修改失敗",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            }
                        }).start();
                    }
                });
                btnCan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newNameText.post(new Runnable() {
                            @Override
                            public void run() {
                                newNameText.setText("");
                            }
                        });
                        dialog.cancel();
                    }
                });
            }
        });

        //修改郵件
        edit_email = (TextView) findViewById(R.id.edit_email);
        String userid = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("email", "");
        edit_email.setText(userid);
        user_emailBtn = findViewById(R.id.edit_email);
        user_emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AccountSet.this);
                View view = getLayoutInflater().inflate(R.layout.acc_modify,null);
                alertDialog.setView(view);
                TextView title = view.findViewById(R.id.acc_modify_title);
                title.setText("修改郵箱");
                Button btnOK = view.findViewById(R.id.store);
                Button btnCan  = view.findViewById(R.id.cancel);
                EditText newEmailText = view.findViewById(R.id.edit_acc);
                AlertDialog dialog = alertDialog.create();
                dialog.show();

                //將修改視窗的位置放在底部
                Window window = dialog.getWindow();
                window.setBackgroundDrawable(null);
                window.setGravity(Gravity.BOTTOM);

                WindowManager m = getWindowManager();
                Display d = m.getDefaultDisplay();
                WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
                p.width = d.getWidth();
                dialog.getWindow().setAttributes(p);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("newEmailText",newEmailText.getText().toString());
                                String new_email = newEmailText.getText().toString();

                                //有效的郵件地址
                                if(new_email.contains("@")==false)
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AccountSet.this,"請輸入有效的郵件地址",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                //欄位為空
                                else if(new_email.equals(""))
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AccountSet.this,"欄位不可為空",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else
                                {
                                    String email_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("email", "");
                                    Log.v("email_store",email_store);

                                    String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                                    Log.v("ip",ip);

                                    MysqlCon con = new MysqlCon();
                                    con.connMql(ip);

                                    //對資料庫修改郵件
                                    editemail_check = con.modifyEmail(email_store,new_email);

                                    Log.v("editemail_check",editemail_check);

                                    //修改成功
                                    if(editemail_check.equals("true"))
                                    {
                                        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);
                                        SharedPreferences.Editor editor= pref.edit();
                                        editor.putString("email",new_email);
                                        editor.commit();

                                        dialog.cancel();

                                        Intent intent = getIntent();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(intent);
                                    }
                                    //修改失敗
                                    else
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(AccountSet.this,"修改失敗",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            }
                        }).start();
                    }
                });
                btnCan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newEmailText.post(new Runnable() {
                            @Override
                            public void run() {
                                newEmailText.setText("");
                            }
                        });
                        dialog.cancel();
                    }
                });

            }
        });

        //修改密碼
        user_passwordBtn = findViewById(R.id.edit_passowrd);
        user_passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AccountSet.this);
                View view = getLayoutInflater().inflate(R.layout.password_modify,null);
                alertDialog.setView(view);
                Button btnOK = view.findViewById(R.id.store);
                Button btnCan  = view.findViewById(R.id.cancel);
                EditText lastPasswordText = view.findViewById(R.id.edit_last_password);
                EditText newPasswordText = view.findViewById(R.id.edit_new_password);
                AlertDialog dialog = alertDialog.create();
                dialog.show();

                //將修改視窗的位置放在底部
                Window window = dialog.getWindow();
                window.setBackgroundDrawable(null);
                window.setGravity(Gravity.BOTTOM);

                WindowManager m = getWindowManager();
                Display d = m.getDefaultDisplay();
                WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
                p.width = d.getWidth();
                dialog.getWindow().setAttributes(p);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("lastPasswordText",lastPasswordText.getText().toString());
                                Log.v("newPasswordText",newPasswordText.getText().toString());
                                String lastPassword = lastPasswordText.getText().toString();
                                String newPassword = newPasswordText.getText().toString();

                                String password_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("password", "");
                                Log.v("password_store",password_store);

                                //檢查舊密碼是否輸入正確
                                if(lastPassword.equals(password_store))
                                {
                                    //新密碼與舊密碼一樣
                                    if(lastPassword.equals(newPassword))
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(AccountSet.this, "新密碼不可與舊密碼一樣，請重新輸入", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    //新密碼與舊密碼不一樣
                                    else
                                    {
                                        if(newPassword.length()<6 || newPassword.length()>12)
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AccountSet.this,"密碼之長度介於6到12個有效字元",Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        else
                                        {
                                            String email_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("email", "");
                                            Log.v("email_store",email_store);

                                            String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                                            Log.v("ip",ip);

                                            MysqlCon con = new MysqlCon();
                                            con.connMql(ip);

                                            //對資料庫修改密碼
                                            editpassword_check = con.modifyPassword(email_store,newPassword);

                                            Log.v("editpassword_check",editpassword_check);

                                            //修改成功
                                            if(editpassword_check.equals("true"))
                                            {
                                                SharedPreferences pref = getSharedPreferences("userAccount", MODE_MULTI_PROCESS);
                                                SharedPreferences.Editor editor = pref.edit();
                                                editor.putString("password", newPassword);
                                                editor.commit();

                                                dialog.cancel();

                                                Intent intent = getIntent();
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                startActivity(intent);
                                            }
                                            //修改失敗
                                            else
                                            {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(AccountSet.this,"修改失敗",Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AccountSet.this, "舊密碼輸入錯誤，請重新輸入", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
                btnCan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastPasswordText.post(new Runnable() {
                            @Override
                            public void run() {
                                lastPasswordText.setText("");
                            }
                        });
                        newPasswordText.post(new Runnable() {
                            @Override
                            public void run() {
                                newPasswordText.setText("");
                            }
                        });
                        dialog.cancel();
                    }
                });

            }
        });

    }
}
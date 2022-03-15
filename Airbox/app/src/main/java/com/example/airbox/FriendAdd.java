package com.example.airbox;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FriendAdd extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //搜尋好友
    private ImageButton btnSearch;
    private EditText edit_userid;
    private String stringuserid = "";
    private String searchFriendname = "";

    //顯示我的id
    private TextView myuserID;

    //返回按鈕
    private ImageButton btnBack;

    //顯示搜尋到的好友資訊
    private View user_name_background;
    private TextView user_name;
    private TextView user_allname;
    private Button addFriend_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);

        String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
        Log.v("id_store",id_store);

        edit_userid = (EditText) findViewById(R.id.friend_search);
        user_name_background = findViewById(R.id.user_name_background);
        user_name = findViewById(R.id.user_name);
        user_allname = findViewById(R.id.user_allname);
        addFriend_button = findViewById(R.id.addFriend_button);
        addFriend_button.setOnClickListener(addFriend);
        myuserID = findViewById(R.id.myuserID);

        //設定按下搜尋按鈕的事件
        btnSearch = findViewById(R.id.search_button);
        btnSearch.setOnClickListener(search);

        //未登入
        if(id_store.equals(""))
        {
            myuserID.setText("未登入\n登入後即可使用新增好友的功能");
            edit_userid.setVisibility(View.INVISIBLE);
            btnSearch.setVisibility(View.INVISIBLE);
        }
        //已登入
        else
        {
            myuserID.setText("我的ID："+ id_store);
        }

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(FriendAdd.this, Friend.class);
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
                intent.setClass(FriendAdd.this, MapsActivity.class);
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
                intent.setClass(FriendAdd.this, Friend.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/friend_button_click", null, getPackageName()); //取得圖片Resource位子
        btnFriend.setImageResource(imageResource);

        //切換到裝置頁面
        btnDevice = findViewById(R.id.device);
        btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(FriendAdd.this, Device.class);
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
                intent.setClass(FriendAdd.this, Tourism.class);
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
                intent.setClass(FriendAdd.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

    }

    //搜尋好友的事件
    private Button.OnClickListener search = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                    Log.v("ip",ip);

                    MysqlCon con = new MysqlCon();
                    con.connMql(ip);

                    //取得 EditText 資料
                    stringuserid = edit_userid.getText().toString();

                    //清空 EditText
                    edit_userid.post(new Runnable() {
                        public void run() {
                            edit_userid.setText("");
                        }
                    });

                    //搜尋好友名字
                    searchFriendname = con.searchFriend(stringuserid);

                    String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                    Log.v("id_store",id_store);

                    //輸入自己的id，跳出提示訊息
                    if(stringuserid.equals(id_store))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FriendAdd.this,"輸入之ID為當前使用者ID",Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    //確認是否已是好友
                    String checkfriend = con.checkFriend(id_store,stringuserid);

                    //已是好友
                    if(checkfriend.equals("true"))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FriendAdd.this,"該名使用者已為好友",Toast.LENGTH_LONG).show();
                                user_name_background.setVisibility(View.VISIBLE);
                                user_name.setVisibility(View.VISIBLE);
                                user_allname.setVisibility(View.VISIBLE);

                                String name_show = searchFriendname.substring(searchFriendname.length() - 2);
                                Log.v("name_show",name_show);

                                user_name.setText(name_show);
                                user_allname.setText(searchFriendname);
                            }
                        });

                    }
                    else
                    {
                        //查無使用者
                        if (searchFriendname.equals("")==true)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FriendAdd.this,"查無使用者",Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                        else
                        {
                            //有使用者且非好友
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    user_name_background.setVisibility(View.VISIBLE);
                                    user_name.setVisibility(View.VISIBLE);
                                    user_allname.setVisibility(View.VISIBLE);
                                    addFriend_button.setVisibility(View.VISIBLE);

                                    String name_show = searchFriendname.substring(searchFriendname.length() - 2);
                                    Log.v("name_show",name_show);

                                    user_name.setText(name_show);
                                    user_allname.setText(searchFriendname);
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    };

    //新增好友按鈕事件
    private Button.OnClickListener addFriend = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                    Log.v("ip",ip);

                    MysqlCon con = new MysqlCon();
                    con.connMql(ip);

                    String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                    Log.v("id_store",id_store);

                    //確認是否已送出好友申請
                    String check_sendbefore = con.checkSendFriend(id_store,stringuserid);

                    //已送出好友申請
                    if(check_sendbefore.equals("true"))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FriendAdd.this,"已送出好友申請",Toast.LENGTH_LONG).show();
                            }
                        });

                        Intent intent = getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                    else
                    {
                        //新增好友申請資料到資料庫
                        String check ="";
                        check = con.insertFriendData(id_store,stringuserid);
                        if(check.equals("true"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FriendAdd.this,"已送出好友申請",Toast.LENGTH_LONG).show();
                                }
                            });

                            Intent intent = getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }
                        else
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FriendAdd.this,"好友申請失敗，請重新傳送",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                }
            }).start();
        }
    };

}
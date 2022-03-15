package com.example.airbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendConfirm extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //新增好友頁面
    private ImageButton btnAddFriend;

    //好友頁面
    private Button btnMyFriend;

    //待確認好友頁面
    private Button btnconfirm;
    private TextView myfriendconfirm;

    //儲存待確認好友資料
    ArrayList<HashMap<String,String>> arrayList;

    //提示訊息
    private TextView friendconfirm_hint;

    //刷新頁面
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_confirm);

        arrayList = new ArrayList<>();

        //刷新頁面
        swipeRefreshLayout = findViewById(R.id.friend_confirm_recyclerview_refreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
        swipeRefreshLayout.setOnRefreshListener(()->{
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            /*
            arrayList.clear();
            getConfirmFriendData();
            */

            swipeRefreshLayout.setRefreshing(false);

        });

        //切換到新增好友的頁面
        btnAddFriend = findViewById(R.id.friend_add);
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(FriendConfirm.this, FriendAdd.class);
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
                intent.setClass(FriendConfirm.this, MapsActivity.class);
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
                intent.setClass(FriendConfirm.this, Friend.class);
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
                intent.setClass(FriendConfirm.this, Device.class);
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
                intent.setClass(FriendConfirm.this, Tourism.class);
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
                intent.setClass(FriendConfirm.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到好友頁面
        btnMyFriend = findViewById(R.id.myFriend);
        btnMyFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(FriendConfirm.this, Friend.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到待確認好友頁面(本頁面)
        btnconfirm = findViewById(R.id.confirm);
        btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(FriendConfirm.this, FriendConfirm.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //將待確認文字畫底線
        myfriendconfirm = (TextView) findViewById(R.id.confirm);
        myfriendconfirm.setText("待確認");
        myfriendconfirm.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);

        friendconfirm_hint = findViewById(R.id.friendconfirm_hint);
        if(pref.contains("email")==false) {
            friendconfirm_hint.setText("登入後即可查看好友確認列表");
        }

        else
        {
            getConfirmFriendData();
        }
    }

    //抓取待確認好友資料
    private void getConfirmFriendData()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendconfirm_hint = findViewById(R.id.friendconfirm_hint);
                        friendconfirm_hint.setText("加載中，請稍後...");
                    }
                });

                String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                Log.v("ip",ip);

                MysqlCon con = new MysqlCon();
                con.connMql(ip);

                String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                Log.v("id_store",id_store);

                //待確認好友
                String ConfirmFriendData = con.getConfirmFriendData(id_store);
                Log.v("ConfirmFriendData",ConfirmFriendData);

                String[] ConfirmFriendData_split = ConfirmFriendData.split(",");

                if(ConfirmFriendData.equals(""))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            friendconfirm_hint = findViewById(R.id.friendconfirm_hint);
                            friendconfirm_hint.setText("尚無好友發送請求");
                        }
                    });
                }

                else
                {
                    try {
                        JSONArray jsonArray = new JSONArray();

                        String userid = "";
                        String name = "";

                        for(int i=0;i<ConfirmFriendData_split.length;i++)
                        {
                            JSONObject object = new JSONObject();
                            if(i%2==0)
                            {
                                Log.v("record0",ConfirmFriendData_split[i]);
                                userid = ConfirmFriendData_split[i];
                            }
                            else
                            {
                                Log.v("record1",ConfirmFriendData_split[i]);
                                name = ConfirmFriendData_split[i];

                                object.put("userid",userid);
                                object.put("name",name);

                                jsonArray.put(object);
                            }
                        }
                        Log.v("object",jsonArray.toString());
                        Log.v("size",Integer.toString(jsonArray.length()));


                        for (int i =0;i<jsonArray.length();i++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            userid = jsonObject.getString("userid");
                            Log.v("userid",userid);
                            name = jsonObject.getString("name");
                            Log.v("name",name);

                            HashMap<String,String> hashMap = new HashMap<>();

                            hashMap.put("userid",userid);
                            hashMap.put("name",name);

                            arrayList.add(hashMap);
                        }

                        Log.v("data","catchData: "+arrayList);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendconfirm_hint = findViewById(R.id.friendconfirm_hint);
                                friendconfirm_hint.setText("");

                                RecyclerView recyclerView;
                                MyAdapter myAdapter;
                                recyclerView = findViewById(R.id.friend_confirm_recyclerview);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
                                myAdapter = new MyAdapter();
                                recyclerView.setAdapter(myAdapter);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        public void removeItem(int position){
            arrayList.remove(position);
            notifyItemRemoved(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView friend_name,friend_allname;
            Button addFriend_cancel,addFriend_accept;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                friend_name = itemView.findViewById(R.id.user_name_background);
                friend_allname = itemView.findViewById(R.id.friend_allname);
                addFriend_cancel = itemView.findViewById(R.id.addFriend_cancel);
                addFriend_accept = itemView.findViewById(R.id.addFriend_accept);

                addFriend_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delConfirmFriendData(arrayList.get(getAdapterPosition()).get("userid"));
                        removeItem(getAdapterPosition());
                    }
                });

                addFriend_accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateNewFriendData(arrayList.get(getAdapterPosition()).get("userid"));
                        removeItem(getAdapterPosition());
                    }
                });
            }
        }

        @NonNull
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_confirm_recycleview_item,parent,false);
            return new MyAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
            String friendname_split = arrayList.get(position).get("name").substring(arrayList.get(position).get("name").length() - 2);
            holder.friend_name.setText(friendname_split);
            holder.friend_allname.setText(arrayList.get(position).get("name"));
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    //刪除好友請求
    private void delConfirmFriendData(String friendid)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                Log.v("ip",ip);

                MysqlCon con = new MysqlCon();
                con.connMql(ip);

                String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                Log.v("id_store",id_store);

                con.delConfirmFriendData(id_store,friendid);
            }
        }).start();
    }

    //接受好友請求
    private void updateNewFriendData(String friendid)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                Log.v("ip",ip);

                MysqlCon con = new MysqlCon();
                con.connMql(ip);

                String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                Log.v("id_store",id_store);

                con.updateNewFriendData(id_store,friendid);
            }
        }).start();
    }

}
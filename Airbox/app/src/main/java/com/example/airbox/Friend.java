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
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Friend extends AppCompatActivity {

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
    private TextView myfriend;

    //待確認好友頁面
    private Button btnconfirm;

    //儲存好友資料
    ArrayList<HashMap<String,String>> arrayList;

    //提示訊息
    private TextView friend_hint;

    //刷新頁面
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        arrayList = new ArrayList<>();

        //刷新頁面
        swipeRefreshLayout = findViewById(R.id.friend_recyclerview_refreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
        swipeRefreshLayout.setOnRefreshListener(()->{
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            /*
            arrayList.clear();
            getFriendData();
            */

            swipeRefreshLayout.setRefreshing(false);
        });

        //切換到新增好友的頁面
        btnAddFriend = findViewById(R.id.friend_add);
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Friend.this, FriendAdd.class);
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
                intent.setClass(Friend.this, MapsActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到好友頁面
        btnFriend = findViewById(R.id.friend);
        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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
                intent.setClass(Friend.this, Device.class);
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
                intent.setClass(Friend.this, Tourism.class);
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
                intent.setClass(Friend.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //將好友文字畫底線
        myfriend = (TextView) findViewById(R.id.myFriend);
        myfriend.setText("好友");
        myfriend.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        //切換到好友頁面(本頁面)
        btnMyFriend = findViewById(R.id.myFriend);
        btnMyFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Friend.this, Friend.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到待確認好友頁面
        btnconfirm = findViewById(R.id.confirm);
        btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Friend.this, FriendConfirm.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);

        friend_hint = findViewById(R.id.friend_hint);
        if(pref.contains("email")==false)
        {
            friend_hint.setText("登入後即可查看好友列表");
        }
        else
        {
            getFriendData();
        }
    }

    //抓取好友資料
    private void getFriendData()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friend_hint = findViewById(R.id.friend_hint);
                        friend_hint.setText("加載中，請稍後...");
                    }
                });

                String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                Log.v("ip",ip);

                MysqlCon con = new MysqlCon();
                con.connMql(ip);

                String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                Log.v("id_store",id_store);

                //有裝置的好友
                String FriendWithDevice = con.getFriendWithDevice(id_store);
                Log.v("FriendWithDevice",FriendWithDevice);

                String[] FriendWithDevice_split = FriendWithDevice.split(",");

                /*
                for(int i=0;i<FriendWithDevice_split.length;i++)
                {
                    Log.v("FriendWithDevice_split",FriendWithDevice_split[i]);
                }
                */

                //沒裝置的好友
                String FriendNoDevice = con.getFriendNoDevice(id_store);
                Log.v("FriendNoDevice",FriendNoDevice);

                String[] FriendNoDevice_split = FriendNoDevice.split(",");

                /*
                for(int i=0;i<FriendNoDevice_split.length;i++)
                {
                    Log.v("FriendNoDevice_split",FriendNoDevice_split[i]);
                }
                */

                if(FriendWithDevice.equals("") && FriendNoDevice.equals(""))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            friend_hint = findViewById(R.id.friend_hint);
                            friend_hint.setText("尚無好友，趕快添加好友吧");
                        }
                    });
                }

                else
                {
                    try {
                        JSONArray jsonArray = new JSONArray();

                        String dataid = "";
                        String userid = "";
                        String name = "";
                        String latitude = "";
                        String longitude = "";
                        String pm25 = "";

                        Log.v("with",String.valueOf(FriendWithDevice_split.length));

                        for(int i=0;i<FriendWithDevice_split.length;i++)
                        {
                            JSONObject object = new JSONObject();
                            if(i%6==0)
                            {
                                Object repeat = object.put("dataid",FriendWithDevice_split[i]);
                                if(repeat!=null)
                                {
                                    Log.v("record0",FriendWithDevice_split[i]);
                                    dataid = FriendWithDevice_split[i];
                                }
                            }
                            else if(i%6==1)
                            {

                                Object repeat = object.put("userid",FriendWithDevice_split[i]);
                                if(repeat!=null)
                                {
                                    Log.v("record1",FriendWithDevice_split[i]);
                                    userid = FriendWithDevice_split[i];
                                }
                            }
                            else if(i%6==2)
                            {

                                Object repeat = object.put("name",FriendWithDevice_split[i]);
                                if(repeat!=null)
                                {
                                    Log.v("record2",FriendWithDevice_split[i]);
                                    name = FriendWithDevice_split[i];
                                }
                            }
                            else if(i%6==3)
                            {

                                Object repeat = object.put("latitude",FriendWithDevice_split[i]);
                                if(repeat!=null)
                                {
                                    Log.v("record3",FriendWithDevice_split[i]);
                                    latitude = FriendWithDevice_split[i];
                                }
                            }
                            else if(i%6==4)
                            {

                                Object repeat = object.put("longitude",FriendWithDevice_split[i]);
                                if(repeat!=null)
                                {
                                    Log.v("record4",FriendWithDevice_split[i]);
                                    longitude = FriendWithDevice_split[i];
                                }
                            }
                            else
                            {

                                Object repeat = object.put("pm25",FriendWithDevice_split[i]);
                                if(repeat!=null)
                                {
                                    Log.v("record5",FriendWithDevice_split[i]);
                                    pm25 = FriendWithDevice_split[i];
                                }

                                object.put("dataid",dataid);
                                object.put("userid",userid);
                                object.put("name",name);
                                object.put("latitude",latitude);
                                object.put("longitude",longitude);
                                object.put("pm25",pm25);

                                jsonArray.put(object);
                            }
                        }

                        if(FriendNoDevice.equals("")==false)
                        {

                            Log.v("with",String.valueOf(FriendNoDevice_split.length));

                            for(int i=0;i<FriendNoDevice_split.length;i++)
                            {
                                JSONObject object = new JSONObject();

                                object.put("dataid","");
                                object.put("userid","");
                                object.put("name",FriendNoDevice_split[i]);
                                object.put("latitude","");
                                object.put("longitude","");
                                object.put("pm25","");

                                jsonArray.put(object);
                            }


                        }

                        Log.v("object",jsonArray.toString());
                        Log.v("size",Integer.toString(jsonArray.length()));

                        for (int i =0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            dataid = jsonObject.getString("dataid");
                            Log.v("dataid",dataid);
                            userid = jsonObject.getString("userid");
                            Log.v("userid",userid);
                            name = jsonObject.getString("name");
                            Log.v("name",name);
                            latitude = jsonObject.getString("latitude");
                            Log.v("latitude",latitude);
                            longitude = jsonObject.getString("longitude");
                            Log.v("longitude",longitude);
                            pm25 = jsonObject.getString("pm25");
                            String pm25_int="";
                            if(pm25.equals("")==false)
                            {
                                pm25_int = Integer.toString((int) Math.round(Double.parseDouble(pm25)));
                            }
                            Log.v("pm25",pm25);

                            HashMap<String,String> hashMap = new HashMap<>();

                            hashMap.put("name",name);
                            hashMap.put("latitude",latitude);
                            hashMap.put("longitude",longitude);
                            hashMap.put("pm25",pm25_int);

                            arrayList.add(hashMap);
                        }

                        Log.v("data","catchData: "+arrayList);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friend_hint = findViewById(R.id.friend_hint);
                                friend_hint.setText("");

                                RecyclerView recyclerView;
                                MyAdapter myAdapter;
                                recyclerView = findViewById(R.id.friend_recyclerview);
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

        //將經緯度轉換成地址
        public String GPSCity(Geocoder gc, double latitude, double longitude){
            String city = "";
            try {
                List<Address> lstAddress = gc.getFromLocation(latitude, longitude, 1);
                String returnAddress = lstAddress.get(0).getAddressLine(0);
                city = lstAddress.get(0).getAdminArea() + lstAddress.get(0).getLocality();
                //System.out.println("GPSCity: " + lstAddress.get(0).getAdminArea());
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

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView friend_name,friend_location,user_pm25;
            ImageView pm25_image;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                friend_name = itemView.findViewById(R.id.user_name_background);
                friend_location = itemView.findViewById(R.id.friend_location);
                user_pm25 = itemView.findViewById(R.id.user_pm25);
                pm25_image = itemView.findViewById(R.id.pm25_image);
            }
        }

        @NonNull
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_recycleview_item,parent,false);
            return new MyAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
            String friendname_split = arrayList.get(position).get("name").substring(arrayList.get(position).get("name").length() - 2);
            holder.friend_name.setText(friendname_split);
            //有連接裝置的好友
            if(arrayList.get(position).get("pm25").equals("")==false)
            {
                holder.user_pm25.setText(arrayList.get(position).get("pm25"));

                Double lat_dou = Double.parseDouble(arrayList.get(position).get("latitude"));
                Double lon_dou = Double.parseDouble(arrayList.get(position).get("longitude"));
                Log.v("lat",Double.toString(lat_dou));
                Log.v("lon",Double.toString(lon_dou));

                Geocoder gc = new Geocoder(Friend.this, Locale.TRADITIONAL_CHINESE);
                String city = "";
                city = GPSCity(gc,lat_dou,lon_dou);

                holder.friend_location.setText(city);

                int air = Integer.parseInt(arrayList.get(position).get("pm25"));
                if(air<=35)
                {
                    int imageResource = getResources().getIdentifier("@drawable/pm25_one", null, getPackageName()); //取得圖片Resource位子
                    holder.pm25_image.setImageResource(imageResource);
                }
                else if(air>35 && air<=41)
                {
                    int imageResource = getResources().getIdentifier("@drawable/pm25_two", null, getPackageName()); //取得圖片Resource位子
                    holder.pm25_image.setImageResource(imageResource);
                }

                else if(air>41 && air<=53)
                {
                    int imageResource = getResources().getIdentifier("@drawable/pm25_three", null, getPackageName()); //取得圖片Resource位子
                    holder.pm25_image.setImageResource(imageResource);
                }
                else if(air>53 && air<=70)
                {
                    int imageResource = getResources().getIdentifier("@drawable/pm25_four", null, getPackageName()); //取得圖片Resource位子
                    holder.pm25_image.setImageResource(imageResource);
                }
                else if(air>70)
                {
                    int imageResource = getResources().getIdentifier("@drawable/pm25_five", null, getPackageName()); //取得圖片Resource位子
                    holder.pm25_image.setImageResource(imageResource);
                }

            }
            //沒有連接裝置的好友
            else
            {
                holder.friend_location.setText("未連接裝置");
                holder.user_pm25.setVisibility(View.INVISIBLE);
                holder.pm25_image.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }
}
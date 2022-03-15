package com.example.airbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class AttractionCollect extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //返回上一頁
    private ImageButton btnBack;

    //定位
    private LocationManager locationManager;
    private String provider;
    List<String> list;
    private Double latitude;
    private Double longitude;
    private String lat_str = "";
    private String lon_str = "";

    //儲存景點資料
    ArrayList<HashMap<String,String>> arrayList;

    //儲存收藏景點經緯度資料
    private String lat_lon_store ="";
    ArrayList<HashMap<String,String>> lat_lon_arrayList;

    //刷新頁面
    SwipeRefreshLayout swipeRefreshLayout;

    //提示訊息
    private TextView attraction_hint;

    //獲取景點收藏資料的網址
    String attraction_url = "http://airbox.servegame.com/google/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attraction_collect);

        arrayList = new ArrayList<>();
        lat_lon_arrayList = new ArrayList<>();

        //刷新頁面
        swipeRefreshLayout = findViewById(R.id.collection_recyclerview_refreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
        swipeRefreshLayout.setOnRefreshListener(()->{
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            /*
            arrayList.clear();
            lat_lon_arrayList.clear();

            //抓取收藏景點的經緯度
            GetAttraction_lat_lon();

            //將經緯度存成arraylist
            PutAttraction_lat_lon_toArrayList();

            //抓取景點的id
            getCollection_attractionid();

            //請求每一組經緯度的景點資料
            Log.v("data", Integer.toString(lat_lon_arrayList.size()));
            for(int i=0;i<lat_lon_arrayList.size();i++)
            {
                sendGET(lat_lon_arrayList.get(i).get("lat"),lat_lon_arrayList.get(i).get("lon"));
            }
            */

            swipeRefreshLayout.setRefreshing(false);

        });

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AttractionCollect.this, Tourism.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        ///切換到地圖頁面
        btnMap = findViewById(R.id.mapButton);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("exit","0");

                Intent intent = new Intent();
                intent.setClass(AttractionCollect.this, MapsActivity.class);
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
                intent.setClass(AttractionCollect.this, Friend.class);
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
                intent.setClass(AttractionCollect.this, Device.class);
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
                intent.setClass(AttractionCollect.this, Tourism.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/tourism_button_click", null, getPackageName()); //取得圖片Resource位子
        btnTourism.setImageResource(imageResource);

        //切換到更多頁面
        btnSet = findViewById(R.id.set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(AttractionCollect.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);

        attraction_hint = findViewById(R.id.attraction_hint);

        //未登入
        if(pref.contains("email")==false)
        {
            attraction_hint.setText("登入後即可查看景點收藏");
        }
        //已登入
        else
        {
            //抓取收藏景點的經緯度
            GetAttraction_lat_lon();

            //將經緯度存成arraylist
            PutAttraction_lat_lon_toArrayList();

            //抓取景點的id
            getCollection_attractionid();

            //請求每一組經緯度的景點資料
            Log.v("data", Integer.toString(lat_lon_arrayList.size()));
            for(int i=0;i<lat_lon_arrayList.size();i++)
            {
                sendGET(lat_lon_arrayList.get(i).get("lat"),lat_lon_arrayList.get(i).get("lon"));
            }
        }
    }

    //從資料庫中抓取所有喜愛點經緯度
    private void GetAttraction_lat_lon()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                Log.v("ip",ip);

                MysqlCon con = new MysqlCon();
                con.connMql(ip);

                String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                Log.v("id_store", id_store);

                String check = "false";

                check = con.getUserCollection(id_store);

                if(check.equals("true"))
                {
                    //抓取使用者收藏景點的所有經緯度
                    lat_lon_store = con.getAttraction_lat_lon(id_store);

                    //已收藏景點
                    //將lat,lon儲存進userAttraction
                    SharedPreferences pref=getSharedPreferences("userAttraction",MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor= pref.edit();
                    editor.putString("lat_lon",lat_lon_store);
                    editor.commit();
                }
                else
                {
                    //尚未收藏景點
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            attraction_hint = findViewById(R.id.attraction_hint);
                            attraction_hint.setText("尚未收藏景點");
                        }
                    });
                }
            }
        }).start();
    }

    //將經緯度存成arraylist
    private void PutAttraction_lat_lon_toArrayList()
    {
        String lat_lon = getSharedPreferences("userAttraction", MODE_MULTI_PROCESS).getString("lat_lon", "");
        Log.v("lat_lon",lat_lon);

        //有收藏景點
        if(lat_lon.equals("")==false)
        {
            String[] record = lat_lon.split(",");

            try {
                JSONArray jsonArray = new JSONArray();

                String lat = "";
                String lon = "";

                for (int i = 0; i < record.length; i++) {
                    JSONObject object = new JSONObject();
                    if (i%2==0)
                    {
                        Object repeat = object.put("lat", record[i]);
                        if (repeat != null) {
                            Log.v("record0", record[i]);
                            lat = record[i];
                        }
                    }
                    else if (i%2==1)
                    {

                        Object repeat = object.put("lon", record[i]);
                        if (repeat != null) {
                            Log.v("record1", record[i]);
                            lon = record[i];
                        }
                        object.put("lat", lat);
                        object.put("lon", lon);

                        jsonArray.put(object);
                    }

                }
                Log.v("object", jsonArray.toString());
                Log.v("size", Integer.toString(jsonArray.length()));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    lat = jsonObject.getString("lat");
                    Log.v("lat", lat);
                    lon = jsonObject.getString("lon");
                    Log.v("lon", lon);

                    HashMap<String, String> hashMap = new HashMap<>();

                    hashMap.put("lat", lat);
                    hashMap.put("lon", lon);

                    lat_lon_arrayList.add(hashMap);
                }

                Log.v("data", "catchData: " + lat_lon_arrayList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //從資料庫中抓取所有喜愛點id
    private void getCollection_attractionid()
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

                //抓取使用者收藏的所有景點id
                String data = con.getCollection_attractionid(id_store);

                //將attractionID儲存進userAttraction
                SharedPreferences pref=getSharedPreferences("userAttraction",MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor= pref.edit();
                editor.putString("attractionID_collection",data);
                editor.commit();

            }
        }).start();
    }

    //獲取該經緯度收藏的景點資料
    private void sendGET(String lat,String lon) {
        ProgressDialog dialog = ProgressDialog.show(AttractionCollect.this,"讀取中","請稍候",true);
        attraction_url = attraction_url + lat + "," + lon;
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(attraction_url)
        //      .header("Cookie","")//有Cookie需求的話則可用此發送
        //      .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                Log.v("error",e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(AttractionCollect.this, "加載失敗，請重新整理", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    /**取得回傳*/
                    //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                    //String  text = responseBodyCopy.string();
                    String text = response.body().string();
                    String time_text = text.substring(10,29);
                    Log.v("time_text",time_text);
                    String record_text = text.substring(43,text.length()-2);
                    Log.v("record_text",record_text);
                    String[] record = record_text.split("\\{'name': '|\\}, \\{'name': '|', 'place_id': '|', 'rating': |, 'user_ratings_total': |, 'vicinity': '|', 's_d0': |, 'lat': |, 'lon': |, 'distance': |\\}, |\\}");

                    String data = getSharedPreferences("userAttraction", MODE_MULTI_PROCESS).getString("attractionID_collection", "");
                    Log.v("all_favoriteID",data);

                    String[] idrecord = data.split(",");
                    int count=0;

                    String name = "";
                    String place_id = "";
                    String rating = "";
                    String user_ratings_total = "";
                    String vicinity = "";
                    String s_d0 = "";
                    String lat = "";
                    String lon = "";
                    String distance = "";

                    JSONArray jsonArray_favorite = new JSONArray();

                    Log.v("array1",jsonArray_favorite.toString());

                    for(int i=1;i<record.length;i++)
                    {
                        JSONObject object_favorite = new JSONObject();
                        if(count%9==0)
                        {
                            Object repeat = object_favorite.put("name",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record0",record[i]);
                                name = record[i];
                            }
                        }
                        else if(count%9==1)
                        {

                            Object repeat = object_favorite.put("place_id",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record1",record[i]);
                                place_id = record[i];
                            }
                        }
                        else if(count%9==2)
                        {

                            Object repeat = object_favorite.put("rating",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record2",record[i]);
                                rating = record[i];
                            }
                        }
                        else if(count%9==3)
                        {

                            Object repeat = object_favorite.put("user_ratings_total",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record3",record[i]);
                                user_ratings_total = record[i];
                            }
                        }
                        else if(count%9==4)
                        {

                            Object repeat = object_favorite.put("vicinity",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record4",record[i]);
                                vicinity = record[i];
                            }
                        }
                        else if(count%9==5)
                        {

                            Object repeat = object_favorite.put("s_d0",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record5",record[i]);
                                s_d0 = record[i];
                            }
                        }
                        else if(count%9==6)
                        {

                            Object repeat = object_favorite.put("lat",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record6",record[i]);
                                lat = record[i];
                            }
                        }
                        else if(count%9==7)
                        {

                            Object repeat = object_favorite.put("lon",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record7",record[i]);
                                lon = record[i];
                            }
                        }
                        else
                        {

                            Object repeat = object_favorite.put("distance",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record5",record[i]);
                                distance = record[i];
                            }

                            if(Arrays.asList(idrecord).contains(place_id))
                            {
                                Log.v("yes",place_id);
                                object_favorite.put("name",name);
                                object_favorite.put("place_id",place_id);
                                object_favorite.put("rating",rating);
                                object_favorite.put("user_ratings_total",user_ratings_total);
                                object_favorite.put("vicinity",vicinity);
                                object_favorite.put("s_d0",s_d0);
                                object_favorite.put("lat",lat);
                                object_favorite.put("lon",lon);
                                object_favorite.put("distance",distance);

                                jsonArray_favorite.put(object_favorite);
                            }
                        }
                        count++;
                    }
                    Log.v("array2",jsonArray_favorite.toString());
                    Log.v("size",Integer.toString(jsonArray_favorite.length()));

                    for (int i =0;i<jsonArray_favorite.length();i++){
                        JSONObject jsonObject = jsonArray_favorite.getJSONObject(i);
                        name = jsonObject.getString("name");
                        Log.v("name",name);
                        place_id = jsonObject.getString("place_id");
                        Log.v("place_id",place_id);
                        rating = jsonObject.getString("rating");
                        Log.v("rating",rating);
                        user_ratings_total = jsonObject.getString("user_ratings_total");
                        Log.v("user_ratings_total",user_ratings_total);
                        vicinity = jsonObject.getString("vicinity");
                        Log.v("vicinity",vicinity);
                        s_d0 = jsonObject.getString("s_d0");
                        String s_d0_int = Integer.toString((int) Math.round(Double.parseDouble(s_d0)));
                        Log.v("s_d0",s_d0);
                        lat = jsonObject.getString("lat");
                        Log.v("lat",lat);
                        lon = jsonObject.getString("lon");
                        Log.v("lon",lon);
                        distance = jsonObject.getString("distance");
                        Log.v("distance",distance);

                        HashMap<String,String> hashMap = new HashMap<>();

                        hashMap.put("name",name);
                        hashMap.put("place_id",place_id);
                        hashMap.put("rating",rating);
                        hashMap.put("user_ratings_total",user_ratings_total);
                        hashMap.put("vicinity",vicinity);
                        hashMap.put("s_d0",s_d0_int);
                        hashMap.put("lat",lat);
                        hashMap.put("lon",lon);
                        hashMap.put("distance",distance);

                        arrayList.add(hashMap);
                    }

                    Log.v("data","catchData: "+arrayList);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            RecyclerView recyclerView;
                            MyAdapter myAdapter;
                            recyclerView = findViewById(R.id.collection_recyclerview);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
                            myAdapter = new MyAdapter();
                            recyclerView.setAdapter(myAdapter);
                        }
                    });

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        public void removeItem(int position){
            arrayList.remove(position);
            notifyItemRemoved(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name,tourism_rating,tourism_comment,vicinity,s_d0,distance;
            ImageView pm25_image;
            ImageButton addattraction_btn;
            View tourism_recycleview;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tourism_name);
                tourism_rating = itemView.findViewById(R.id.tourism_rating);
                tourism_comment = itemView.findViewById(R.id.tourism_comment);
                vicinity = itemView.findViewById(R.id.tourism_location);
                s_d0 = itemView.findViewById(R.id.user_pm25);
                distance = itemView.findViewById(R.id.tourism_distance);
                pm25_image = itemView.findViewById(R.id.pm25_image);
                addattraction_btn = itemView.findViewById(R.id.addattraction_btn);
                tourism_recycleview = itemView;

                addattraction_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String ip = getSharedPreferences("server", MODE_MULTI_PROCESS).getString("ip", "");
                                Log.v("ip",ip);

                                MysqlCon con = new MysqlCon();
                                con.connMql(ip);

                                String id_store = getSharedPreferences("userAccount", MODE_MULTI_PROCESS).getString("userID", "");
                                Log.v("id_store", id_store);

                                //移除景點
                                String check_del = con.delfavoriteData(id_store,arrayList.get(getAdapterPosition()).get("place_id"));

                                //成功移除
                                if (check_del.equals("true")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AttractionCollect.this, "已從收藏景點中移除", Toast.LENGTH_LONG).show();
                                            removeItem(getAdapterPosition());
                                        }
                                    });
                                }
                                //移除失敗
                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AttractionCollect.this, "移除失敗，請重新點擊移除", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });

            }
        }

        @NonNull
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tourism_recycleview_item,parent,false);
            return new MyAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {

            holder.tourism_rating.setText(arrayList.get(position).get("rating")+"星");
            holder.tourism_comment.setText("（"+arrayList.get(position).get("user_ratings_total")+")");
            holder.name.setText(arrayList.get(position).get("name"));
            holder.distance.setText(arrayList.get(position).get("distance").substring(0,3)+"公里");
            holder.vicinity.setText(arrayList.get(position).get("vicinity"));
            holder.s_d0.setText(arrayList.get(position).get("s_d0"));

            int air = Integer.parseInt(arrayList.get(position).get("s_d0"));
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

            int imageResource = getResources().getIdentifier("@drawable/tourism_addfavorite_click", null, getPackageName()); //取得圖片Resource位子
            holder.addattraction_btn.setImageResource(imageResource);

            //連結到google map
            holder.tourism_recycleview.setOnClickListener((v)->{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr="+ lat_str+ "," + lon_str + "&daddr=" + arrayList.get(position).get("lat")+ "," +arrayList.get(position).get("lon") +"&avoid=highway" +"&language=zh-CN") );
                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }
}
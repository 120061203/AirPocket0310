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
import android.provider.Settings;
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

public class Tourism extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //景點收藏頁面的按鈕
    private ImageButton btnFavoriteAttraction;

    //儲存景點資料
    ArrayList<HashMap<String,String>> arrayList;

    //定位
    private LocationManager locationManager;
    private String provider;
    List<String> list;
    private Double latitude;
    private Double longitude;
    private String lat_str = "";
    private String lon_str = "";

    //刷新頁面
    SwipeRefreshLayout swipeRefreshLayout;

    //景點資料的網址
    private String tourism_url = "http://airbox.servegame.com/google/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourism);

        arrayList = new ArrayList<>();

        //獲取手機權限
        getPermission();

        //宣告手機裝置的位置管理
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //獲取使用者位置
        getLocation();

        //刷新頁面
        swipeRefreshLayout = findViewById(R.id.tourism_recyclerview_refreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
        swipeRefreshLayout.setOnRefreshListener(()->{
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            /*
            arrayList.clear();
            sendGET();
            */

            swipeRefreshLayout.setRefreshing(false);
        });

        //切換到景點收藏的頁面
        btnFavoriteAttraction = findViewById(R.id.favoriteattraction_btn);
        btnFavoriteAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Tourism.this, AttractionCollect.class);
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
                intent.setClass(Tourism.this, MapsActivity.class);
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
                intent.setClass(Tourism.this, Friend.class);
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
                intent.setClass(Tourism.this, Device.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //切換到推薦頁面
        btnTourism = findViewById(R.id.tourism);
        btnTourism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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
                intent.setClass(Tourism.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        Log.v("tourism_url",tourism_url + lat_str + "," + lon_str);

        GetAllattraction();

        sendGET();
    }

    //取得當前GPS info
    @SuppressLint("MissingPermission")
    public void getLocation()
    {
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

                                tourism_url = tourism_url + latitude + "," + longitude;
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

            }
            tourism_url = tourism_url + latitude + "," + longitude;

            locationManager.removeUpdates(locationListener);

        }catch(Exception e){
            e.printStackTrace();
        }
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
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(Tourism.this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("提醒")
                        .setMessage("此應用程式，需要位置權限才能正常使用")
                        .setCancelable(false)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(Tourism.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(Tourism.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "被永遠拒絕，只能使用者手動給予權限", Toast.LENGTH_LONG).show();
                    //開啟應用程式資訊，讓使用者手動給予權限
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(Tourism.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                    Toast.makeText(this, "按下拒絕", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "允許權限", Toast.LENGTH_LONG).show();
            }
        }
    }

    //獲取景點資料
    private void sendGET() {
        ProgressDialog dialog = ProgressDialog.show(Tourism.this,"讀取中","請稍候",true);
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(tourism_url)
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
                        Toast.makeText(Tourism.this, "加載失敗，請重新整理", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    /**取得回傳*/
                    //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                    //String text = responseBodyCopy.string();
                    String text = response.body().string();

                    String time_text = text.substring(10,29);
                    Log.v("time_text",time_text);
                    String record_text = text.substring(43,text.length()-2);
                    Log.v("record_text",record_text);
                    String[] record = record_text.split("\\{'name': '|\\}, \\{'name': '|', 'place_id': '|', 'rating': |, 'user_ratings_total': |, 'vicinity': '|', 's_d0': |, 'lat': |, 'lon': |, 'distance': |\\}, |\\}");


                    JSONArray jsonArray = new JSONArray();
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

                    for(int i=1;i<record.length;i++)
                    {
                        JSONObject object = new JSONObject();
                        if(count%9==0)
                        {
                            Object repeat = object.put("name",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record0",record[i]);
                                name = record[i];
                            }
                        }
                        else if(count%9==1)
                        {

                            Object repeat = object.put("place_id",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record1",record[i]);
                                place_id = record[i];
                            }
                        }
                        else if(count%9==2)
                        {

                            Object repeat = object.put("rating",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record2",record[i]);
                                rating = record[i];
                            }
                        }
                        else if(count%9==3)
                        {

                            Object repeat = object.put("user_ratings_total",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record3",record[i]);
                                user_ratings_total = record[i];
                            }
                        }
                        else if(count%9==4)
                        {

                            Object repeat = object.put("vicinity",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record4",record[i]);
                                vicinity = record[i];
                            }
                        }
                        else if(count%9==5)
                        {

                            Object repeat = object.put("s_d0",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record5",record[i]);
                                s_d0 = record[i];
                            }
                        }
                        else if(count%9==6)
                        {

                            Object repeat = object.put("lat",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record6",record[i]);
                                lat = record[i];
                            }
                        }
                        else if(count%9==7)
                        {

                            Object repeat = object.put("lon",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record7",record[i]);
                                lon = record[i];
                            }
                        }
                        else
                        {

                            Object repeat = object.put("distance",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record8",record[i]);
                                distance = record[i];
                            }
                            object.put("name",name);
                            object.put("place_id",place_id);
                            object.put("rating",rating);
                            object.put("user_ratings_total",user_ratings_total);
                            object.put("vicinity",vicinity);
                            object.put("s_d0",s_d0);
                            object.put("lat",lat);
                            object.put("lon",lon);
                            object.put("distance",distance);

                            jsonArray.put(object);
                        }
                        count++;
                    }
                    Log.v("object",jsonArray.toString());
                    Log.v("size",Integer.toString(jsonArray.length()));


                    for (int i =0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
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
                            if(Integer.toString(jsonArray.length()).equals("0"))
                            {
                                Toast.makeText(Tourism.this, "讀取失敗，請重新下拉整理頁面", Toast.LENGTH_LONG).show();
                            }
                            RecyclerView recyclerView;
                            MyAdapter myAdapter;
                            recyclerView = findViewById(R.id.tourism_recyclerview);
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
                        SharedPreferences pref=getSharedPreferences("userAccount",MODE_MULTI_PROCESS);

                        //未登入
                        if(pref.contains("email")==false)
                        {
                            AlertDialog.Builder connalertDialog =
                                    new AlertDialog.Builder(Tourism.this);
                            connalertDialog.setTitle("提示訊息");
                            connalertDialog.setMessage("登入後才可收藏喜愛景點");
                            connalertDialog.setPositiveButton("離開", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            connalertDialog.setCancelable(true);
                            connalertDialog.show();
                        }
                        //已登入
                        //新增喜愛點到資料庫中
                        else
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

                                    //檢查該景點是否有在資料庫中
                                    String check = con.checkfavoriteData(id_store,arrayList.get(getAdapterPosition()).get("place_id"));

                                    //已在資料庫中，需要做移除景點的工作
                                    if(check.equals("true"))
                                    {
                                        String check_del = con.delfavoriteData(id_store,arrayList.get(getAdapterPosition()).get("place_id"));
                                        if(check_del.equals("true"))
                                        {
                                            //重新抓取目前所有收藏的景點
                                            GetAllattraction();

                                            int imageResource = getResources().getIdentifier("@drawable/tourism_addfavorite", null, getPackageName()); //取得圖片Resource位子
                                            addattraction_btn.setImageResource(imageResource);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Tourism.this, "已從收藏景點中移除", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                        else
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Tourism.this, "移除失敗，請重新點擊移除", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                    //新增景點
                                    else
                                    {
                                        //新增收藏到資料庫中
                                        String check_add = con.addfavoriteData(id_store,arrayList.get(getAdapterPosition()).get("place_id"),lat_str,lon_str);
                                        if(check_add.equals("true"))
                                        {
                                            //重新抓取目前所有的收藏景點
                                            GetAllattraction();

                                            int imageResource = getResources().getIdentifier("@drawable/tourism_addfavorite_click", null, getPackageName()); //取得圖片Resource位子
                                            addattraction_btn.setImageResource(imageResource);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Tourism.this, "新增至收藏景點", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                       else
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Tourism.this, "新增失敗，請重新點擊新增", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                }
                            }).start();
                        }
                    }
                });

            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tourism_recycleview_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String data = getSharedPreferences("userAttraction", MODE_MULTI_PROCESS).getString("attractionID_tourism", "");
            Log.v("all_favoriteID",data);

            String[] idrecord = data.split(",");

            if(Arrays.asList(idrecord).contains(arrayList.get(position).get("place_id")))
            {
                int imageResource = getResources().getIdentifier("@drawable/tourism_addfavorite_click", null, getPackageName()); //取得圖片Resource位子
                holder.addattraction_btn.setImageResource(imageResource);
            }
            else
            {
                int imageResource = getResources().getIdentifier("@drawable/tourism_addfavorite", null, getPackageName()); //取得圖片Resource位子
                holder.addattraction_btn.setImageResource(imageResource);
            }

            holder.tourism_rating.setText(arrayList.get(position).get("rating")+"星");
            holder.tourism_comment.setText("（"+arrayList.get(position).get("user_ratings_total")+")");
            holder.name.setText(arrayList.get(position).get("name"));
            holder.distance.setText(arrayList.get(position).get("distance").substring(0,3)+" 公里");
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

    //抓取使用者收藏的所有景點id
    private void GetAllattraction()
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
                Log.v("data",data);

                //將attractionID儲存進userAttraction
                SharedPreferences pref=getSharedPreferences("userAttraction",MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor= pref.edit();
                editor.putString("attractionID_tourism",data);
                editor.commit();
            }
        }).start();
    }
}
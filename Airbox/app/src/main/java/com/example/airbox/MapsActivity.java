package com.example.airbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.airbox.databinding.ActivityMapsBinding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMyLocationClickListener {

    //google map
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private View mapView;

    //??????
    private LocationManager locationManager;
    private String provider;
    List<String> list;
    private String lat;
    private String lon;

    //?????????
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

   //????????????
    private ImageButton refresh_btn;

    //??????????????????
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    //??????????????????
    private ImageButton btn_datamap_select;
    private Button btn_epa_pm25;
    private Button btn_epa_aqi;
    private String epa_data = "PM25";

    //??????server??? ip
    //private String ip_url = "http://airbox.servegame.com/ip";
    private String ip_url = "http://192.168.58.187:8000/";

    //epa???lass???????????????
//    private String epa_url = "http://airbox.servegame.com/epa";
//    private String data_url = "http://airbox.servegame.com/lass";
    private String epa_url = "http://192.168.58.187:8000/epa";
    private String data_url = "http://192.168.58.187:8000/lass";

    //????????????????????????????????????
//    private String device_url = "http://airbox.servegame.com/weather/";
    private String device_url = "http://192.168.58.187:8000/weather/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btn_epa_pm25 = findViewById(R.id.pm25_data);
        btn_epa_aqi = findViewById(R.id.aqi_data);

        //???????????????????????????
        getPermission();

        //?????????????????????????????????
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //???????????????????????????
        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapView = mapFragment.getView();
            mapFragment.getMapAsync(this);
        }

        //????????????
        refresh_btn = findViewById(R.id.refresh);
        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(epa_data.equals("PM25"))
                {
                    arrayList.clear();
                    mMap.clear();

                    sendGetIP();

                    sendGET();

                }
                else if(epa_data.equals("AQI"))
                {
                    arrayList.clear();
                    mMap.clear();

                    sendGetIP();

                    sendGET();
                }
                else
                {
                    btn_epa_pm25.setVisibility(View.INVISIBLE);
                    btn_epa_aqi.setVisibility(View.INVISIBLE);

                    arrayList.clear();
                    mMap.clear();

                    sendGetIP();

                    sendLassGET();
                }
            }
        });

        //epa???pm2.5??????
        btn_epa_pm25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable_pm25 = getResources().getDrawable(R.drawable.epadata_select_click_background);
                btn_epa_pm25.setBackgroundDrawable(drawable_pm25);

                Drawable drawable_aqi = getResources().getDrawable(R.drawable.epadata_select_background);
                btn_epa_aqi.setBackgroundDrawable(drawable_aqi);

                btn_epa_pm25.setVisibility(View.VISIBLE);
                btn_epa_aqi.setVisibility(View.VISIBLE);
                mMap.clear();
                epa_data = "PM25";
                sendGET();
            }
        });

        //epa???aqi??????
        btn_epa_aqi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable_aqi = getResources().getDrawable(R.drawable.epadata_select_click_background);
                btn_epa_aqi.setBackgroundDrawable(drawable_aqi);

                Drawable drawable_pm25 = getResources().getDrawable(R.drawable.epadata_select_background);
                btn_epa_pm25.setBackgroundDrawable(drawable_pm25);

                btn_epa_pm25.setVisibility(View.VISIBLE);
                btn_epa_aqi.setVisibility(View.VISIBLE);
                mMap.clear();
                epa_data = "AQI";
                sendGET();
            }
        });

        //???????????????????????????
        btn_datamap_select = findViewById(R.id.datamap_select);
        btn_datamap_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.datamap_select, null);
                alertDialog.setView(view);
                Button btnepa = view.findViewById(R.id.epa_btn);
                Button btnlass = view.findViewById(R.id.lass_btn);
                if(epa_data.equals(""))
                {
                    Drawable drawable_aqi = getResources().getDrawable(R.drawable.epadata_select_click_background);
                    btnlass.setBackgroundDrawable(drawable_aqi);

                    Drawable drawable_pm25 = getResources().getDrawable(R.drawable.epadata_select_background);
                    btnepa.setBackgroundDrawable(drawable_pm25);
                }
                else
                {
                    Drawable drawable_aqi = getResources().getDrawable(R.drawable.epadata_select_click_background);
                    btnepa.setBackgroundDrawable(drawable_aqi);

                    Drawable drawable_pm25 = getResources().getDrawable(R.drawable.epadata_select_background);
                    btnlass.setBackgroundDrawable(drawable_pm25);
                }
                AlertDialog dialog = alertDialog.create();

                Window window = dialog.getWindow();
                WindowManager.LayoutParams params = window.getAttributes();
                params.x = 10;
                params.y = 420;
                window.setAttributes(params);
                dialog.show();
                window.setGravity(Gravity.BOTTOM | Gravity.RIGHT);

                WindowManager m = getWindowManager();
                Display d = m.getDefaultDisplay(); //??????????????????
                params.height = (int) (d.getHeight() * 0.19); //????????????????????????0.19
                params.width = (int) (d.getWidth() * 0.6); //???????????????0.6
                dialog.getWindow().setAttributes(params);

                btnepa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable_pm25 = getResources().getDrawable(R.drawable.epadata_select_click_background);
                        btn_epa_pm25.setBackgroundDrawable(drawable_pm25);

                        Drawable drawable_aqi = getResources().getDrawable(R.drawable.epadata_select_background);
                        btn_epa_aqi.setBackgroundDrawable(drawable_aqi);

                        btn_epa_pm25.setVisibility(View.VISIBLE);
                        btn_epa_aqi.setVisibility(View.VISIBLE);
                        mMap.clear();
                        epa_data = "PM25";
                        sendGET();
                        dialog.cancel();
                    }
                });
                btnlass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        epa_data = "";
                        btn_epa_pm25.setVisibility(View.INVISIBLE);
                        btn_epa_aqi.setVisibility(View.INVISIBLE);
                        mMap.clear();
                        sendLassGET();
                        dialog.cancel();
                    }
                });
            }
        });

        //?????????????????????
        btnMap = findViewById(R.id.mapButton);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/map_button_click", null, getPackageName()); //????????????Resource??????
        btnMap.setImageResource(imageResource);

        //?????????????????????
        btnFriend = findViewById(R.id.friend);
        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, Friend.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //?????????????????????
        btnDevice = findViewById(R.id.device);
        btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, Device.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //?????????????????????
        btnTourism = findViewById(R.id.tourism);
        btnTourism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, Tourism.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //?????????????????????
        btnSet = findViewById(R.id.set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap=googleMap;

            //?????????????????????????????????
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

            locationManager.requestLocationUpdates(provider,2000,2,locationListener);

            //?????????????????????????????????
            if(provider==null)
            {
                new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage("?????????????????????????????????????????????????????????")
                        .setCancelable(false)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LatLng user = new LatLng(22.7344107 , 120.2849883);
                                mMap.addMarker(new MarkerOptions().position(user));
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(user)
                                        .zoom(15)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                device_url = device_url +22.7344107+","+120.2849883;
                                sendAirGET();
                            }
                        })
                        .show();
                locationManager.removeUpdates(locationListener);
            }
            else
            {
                Location location = locationManager.getLastKnownLocation(provider);

                while(location==null)
                {
                    location = locationManager.getLastKnownLocation(provider);
                }
                if(location!=null)
                {
                    //??????????????????????????????????????????????????????
                    googleMap.setMyLocationEnabled(true);
                    //googleMap.setPadding(380,170,5,180);
                    googleMap.setOnMyLocationButtonClickListener(this);
                    googleMap.setOnMyLocationClickListener(this);

                    if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null)
                    {
                        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                                locationButton.getLayoutParams();

                        // position on right bottom
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        layoutParams.addRule(RelativeLayout.ABOVE,R.id.fun_btn);
                        layoutParams.setMargins(0, 0, 30, 200);
                    }

                    LatLng user = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(user).title("Marker in User"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(user));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

                    lat = Double.toString(location.getLatitude());
                    lon = Double.toString(location.getLongitude());

                    device_url = device_url +lat+","+lon;

                    sendAirGET();
                    locationManager.removeUpdates(locationListener);
                }
            }

            sendGetIP();

            sendFirstGET();
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    //??????????????????
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            lat = Double.toString(location.getLatitude());
            lon = Double.toString(location.getLongitude());
            Log.v("lat_update",lat);
            Log.v("lon_update",lon);

        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //????????????????????????????????????(???????????????GPS??????GPS??????????????????????????????)
        }

        @Override
        public void onProviderEnabled(String provider) {
            //GPS??????
        }

        @Override
        public void onProviderDisabled(String provider) {
            // GPS ??????
        }
    };

    //??????????????????
    public void getPermission()
    {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage("??????????????????????????????????????????????????????")
                        .setCancelable(false)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
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

    //????????????????????????????????????callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "???????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                    //?????????????????????????????????????????????????????????
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                    Toast.makeText(this, "????????????", Toast.LENGTH_LONG).show();
                }
            } else {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapView = mapFragment.getView();
                mapFragment.getMapAsync(this);
                Toast.makeText(this, "????????????", Toast.LENGTH_LONG).show();
            }
        }
    }

    //??????????????????
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        //Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    //??????????????????
    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    //??????server???ip
    private void sendGetIP() {
        /**????????????*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**??????????????????*/

        Request request = new Request.Builder()
                .url(ip_url)
                //               .header("Cookie","")//???Cookie??????????????????????????????
                //               .addHeader("","")//??????API?????????header????????????????????????
                .build();
        /**????????????*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**?????????????????????????????????*/
                Log.v("error",e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**????????????*/
                //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                //String ip = responseBodyCopy.string();
                String ip = response.body().string();
                Log.v("ip",ip);

                //???IP??????server
                SharedPreferences pref=getSharedPreferences("server",MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor= pref.edit();
                editor.putString("ip",ip);
                editor.commit();
            }

        });
    }

    //?????????????????????pm2.5??????
    private void sendAirGET() {
        /**????????????*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**??????????????????*/
        Request request = new Request.Builder()
                .url(device_url)
        //      .header("Cookie","")//???Cookie???????????????????????????
        //      .addHeader("","")//??????API?????????header????????????????????????
                .build();
        /**????????????*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**?????????????????????????????????*/
                Log.v("error", e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**????????????*/
                //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                //String text = responseBodyCopy.string();
                String text = response.body().string();
                String[] record = text.split("\\{'time': '|', 'SiteName': '|', 's_t0': |, 's_h0': |, 's_d0': |\\}");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("air_len", String.valueOf(record.length));
                        if (record.length == 6) {
                            Double air_dou = Double.parseDouble(record[5]);
                            Integer air_int = (int) Math.round(air_dou);

                            SharedPreferences pref = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS);

                            if (pref.contains("Airpollution")) {
                                String data = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("Airpollution", "");
                                Log.v("Airpollution", data);

                                String pollution = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("PollutionDone", "");

                                if (air_int > Integer.parseInt(data) && pollution.equals("?????????") == false) {

                                    //???check_del?????????mysqlcheck
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("PollutionDone", "?????????");
                                    editor.commit();

                                    new AlertDialog.Builder(MapsActivity.this)
                                            .setTitle("????????????")
                                            .setMessage("??????PM2.5????????????????????????")
                                            .setCancelable(false)
                                            .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .show();
                                }
                            } else {
                                String pollution = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("PollutionDone", "");

                                int data = 35;
                                if (air_int > data && pollution.equals("?????????") == false) {

                                    //???check_del?????????mysqlcheck
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("PollutionDone", "?????????");
                                    editor.commit();

                                    new AlertDialog.Builder(MapsActivity.this)
                                            .setTitle("??????")
                                            .setMessage("??????PM2.5????????????????????????")
                                            .setCancelable(false)
                                            .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .show();
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    //??????????????????epa??????
    private void sendFirstGET() {
        ProgressDialog dialog = ProgressDialog.show(MapsActivity.this,"?????????","?????????",true);
        /**????????????*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**??????????????????*/
        Request request = new Request.Builder()
                .url(epa_url)
                //               .header("Cookie","")//???Cookie??????????????????????????????
                //               .addHeader("","")//??????API?????????header????????????????????????
                .build();
        /**????????????*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**?????????????????????????????????*/
                Log.v("error", e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(MapsActivity.this, "??????????????????????????????", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**????????????*/
                //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                //String text = responseBodyCopy.string();
                String text = response.body().string();
                String time_text = text.substring(10, 29);
                Log.v("time_text", time_text);
                String record_text = text.substring(43, text.length() - 2);
                Log.v("record_text", record_text);
                String[] record = record_text.split("\\{'SiteName': '|', 'AQI': '|': '|', 'PM2.5': '|', 'PM2.5': |, 'Longitude': '|', 'Longitude': '|', 'Latitude': '|'\\}, \\{'SiteName': '|'\\}");
                Log.v("record_size", Integer.toString(record.length));

                try {
                    JSONArray jsonArray = new JSONArray();

                    int count = 0;
                    String SiteName = "";
                    String AQI = "";
                    String PM25 = "";
                    String Longitude = "";
                    String Latitude = "";

                    for (int i = 1; i < record.length; i++) {
                        Log.v("i", Integer.toString(i));
                        JSONObject object = new JSONObject();
                        if (count % 5 == 0) {
                            Object repeat = object.put("SiteName", record[i]);
                            if (repeat != null) {
                                Log.v("record0", record[i]);
                                SiteName = record[i];
                            }
                        } else if (count % 5 == 1) {

                            Object repeat = object.put("AQI", record[i]);
                            if (repeat != null) {
                                Log.v("record1", record[i]);
                                if (record[i].equals("")) {
                                    record[i] = "?????????";
                                }
                                AQI = record[i];
                            }
                        } else if (count % 5 == 2) {

                            Object repeat = object.put("PM25", record[i]);
                            if (repeat != null) {
                                Log.v("record2", record[i]);
                                if (record[i].equals("")) {
                                    record[i] = "?????????";
                                }
                                PM25 = record[i];
                            }
                        } else if (count % 5 == 3) {

                            Object repeat = object.put("Longitude", record[i]);
                            if (repeat != null) {
                                Log.v("record3", record[i]);
                                Longitude = record[i];
                            }
                        } else if (count % 5 == 4) {

                            Object repeat = object.put("Latitude", record[i]);
                            if (repeat != null) {
                                Log.v("record4", record[i]);
                                Latitude = record[i];
                            }
                            object.put("SiteName", SiteName);
                            object.put("AQI", AQI);
                            object.put("PM25", PM25);
                            object.put("Longitude", Longitude);
                            object.put("Latitude", Latitude);

                            jsonArray.put(object);
                        }
                        count++;
                    }
                    Log.v("object", jsonArray.toString());
                    Log.v("size", Integer.toString(jsonArray.length()));

                    arrayList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        SiteName = jsonObject.getString("SiteName");
                        //Log.v("SiteName",SiteName);
                        AQI = jsonObject.getString("AQI");
                        //Log.v("AQI",AQI);
                        PM25 = jsonObject.getString("PM25");
                        //Log.v("PM25",PM25);
                        Longitude = jsonObject.getString("Longitude");
                        //Log.v("Longitude",Longitude);
                        Latitude = jsonObject.getString("Latitude");
                        //Log.v("Latitude",Latitude);

                        HashMap<String, String> hashMap = new HashMap<>();

                        hashMap.put("SiteName", SiteName);
                        hashMap.put("AQI", AQI);
                        hashMap.put("PM25", PM25);
                        hashMap.put("Longitude", Longitude);
                        hashMap.put("Latitude", Latitude);

                        arrayList.add(hashMap);
                    }
                    Log.v("data", "catchData: " + arrayList);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("getdata", "Data: " + arrayList);
                            for (int i = 0; i < arrayList.size(); i++) {
                                Log.v("name_mark", arrayList.get(i).get("SiteName"));
                                Log.v("AQI_mark", arrayList.get(i).get("AQI"));
                                Log.v("PM25_mark", arrayList.get(i).get("PM25"));
                                Log.v("Longitude_mark", arrayList.get(i).get("Longitude"));
                                Log.v("Latitude_mark", arrayList.get(i).get("Latitude"));

                                //??????marker??????
                                int height = 90;
                                int width = 90;

                                if (arrayList.get(i).get("PM25").equals("?????????") == false && arrayList.get(i).get("PM25").equals("0")==false) {
                                    Integer air = Integer.parseInt(arrayList.get(i).get("PM25"));
                                    BitmapDrawable bitmapdraw = new BitmapDrawable();

                                    if (air <= 35) {
                                        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.airdata_one);
                                    } else if (air > 35 && air <= 41) {
                                        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.airdata_two);
                                    } else if (air > 41 && air <= 53) {
                                        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.airdata_three);
                                    } else if (air > 53 && air <= 70) {
                                        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.airdata_four);
                                    } else if (air > 70) {
                                        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.airdata_five);
                                    }

                                    Bitmap b = bitmapdraw.getBitmap();
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false).copy(Bitmap.Config.ARGB_8888, true);
                                    Canvas canvas = new Canvas(smallMarker);
                                    Paint paint = new Paint();
                                    paint.setColor(Color.WHITE);
                                    paint.setTextSize(35);

                                    Double air_dou = Double.parseDouble(arrayList.get(i).get("PM25"));
                                    int air_int = (int) Math.round(air_dou);
                                    if (air_int >= 100) {
                                        canvas.drawText(Integer.toString(air_int), 18, 55, paint);
                                    } else if (air_int < 10) {
                                        canvas.drawText(Integer.toString(air_int), 35, 55, paint);
                                    } else {
                                        canvas.drawText(Integer.toString(air_int), 25, 55, paint);
                                    }

                                    LatLng epa = new LatLng(Double.valueOf(arrayList.get(i).get("Latitude")), Double.valueOf(arrayList.get(i).get("Longitude")));
                                    mMap.addMarker(new MarkerOptions()
                                            .position(epa)
                                            .title("?????????" + arrayList.get(i).get("SiteName"))
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                            .snippet("PM2.5???" + arrayList.get(i).get("PM25"))
                                    );
                                }
                            }
                            dialog.dismiss();
                        }

                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //???epa??????(PM2.5/AQI)
    private void sendGET() {
        ProgressDialog dialog = ProgressDialog.show(MapsActivity.this,"?????????","?????????",true);
        /**????????????*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**??????????????????*/
        Request request = new Request.Builder()
                .url(epa_url)
                //               .header("Cookie","")//???Cookie??????????????????????????????
                //               .addHeader("","")//??????API?????????header????????????????????????
                .build();
        /**????????????*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**?????????????????????????????????*/
                Log.v("error",e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(MapsActivity.this, "??????????????????????????????", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //Log.v("epa",response.body().string());
                /**????????????*/
                //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                //String text = responseBodyCopy.string();
                String text = response.body().string();
                String time_text = text.substring(10,29);
                Log.v("time_text",time_text);
                String record_text = text.substring(43,text.length()-2);
                Log.v("record_text",record_text);
                String[] record = record_text.split("\\{'SiteName': '|', 'AQI': '|': '|', 'PM2.5': '|', 'Longitude': '|', 'Latitude': '|'\\}, \\{'SiteName': '|'\\}");
                Log.v("record_size",Integer.toString(record.length));

                try {
                    JSONArray jsonArray = new JSONArray();

                    int count = 0;
                    String SiteName = "";
                    String AQI = "";
                    String PM25 = "";
                    String Longitude = "";
                    String Latitude = "";

                    for(int i=1;i<record.length;i++)
                    {
                        Log.v("i",Integer.toString(i));
                        JSONObject object = new JSONObject();
                        if(count%5==0)
                        {
                            Object repeat = object.put("SiteName",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record0",record[i]);
                                SiteName = record[i];
                            }
                        }
                        else if(count%5==1)
                        {

                            Object repeat = object.put("AQI",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record1",record[i]);
                                if(record[i].equals(""))
                                {
                                    record[i] = "?????????";
                                }
                                AQI = record[i];
                            }
                        }
                        else if(count%5==2)
                        {

                            Object repeat = object.put("PM25",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record2",record[i]);
                                if(record[i].equals(""))
                                {
                                    record[i] = "?????????";
                                }
                                PM25 = record[i];
                            }
                        }
                        else if(count%5==3)
                        {

                            Object repeat = object.put("Longitude",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record3",record[i]);
                                Longitude = record[i];
                            }
                        }
                        else if(count%5==4)
                        {

                            Object repeat = object.put("Latitude",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record4",record[i]);
                                Latitude = record[i];
                            }
                            object.put("SiteName",SiteName);
                            object.put("AQI",AQI);
                            object.put("PM25",PM25);
                            object.put("Longitude",Longitude);
                            object.put("Latitude",Latitude);

                            jsonArray.put(object);
                        }
                        count++;
                    }
                    Log.v("object",jsonArray.toString());
                    Log.v("size",Integer.toString(jsonArray.length()));

                    arrayList.clear();
                    for (int i =0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        SiteName = jsonObject.getString("SiteName");
                        //Log.v("SiteName",SiteName);
                        AQI = jsonObject.getString("AQI");
                        //Log.v("AQI",AQI);
                        PM25 = jsonObject.getString("PM25");
                        //Log.v("PM25",PM25);
                        Longitude = jsonObject.getString("Longitude");
                        //Log.v("Longitude",Longitude);
                        Latitude = jsonObject.getString("Latitude");
                        //Log.v("Latitude",Latitude);

                        HashMap<String,String> hashMap = new HashMap<>();

                        hashMap.put("SiteName",SiteName);
                        hashMap.put("AQI",AQI);
                        hashMap.put("PM25",PM25);
                        hashMap.put("Longitude",Longitude);
                        hashMap.put("Latitude",Latitude);

                        arrayList.add(hashMap);
                    }
                    Log.v("data","catchData: "+arrayList);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("getdata","Data: "+arrayList);
                            for(int i=0;i<arrayList.size();i++)
                            {
                                Log.v("name_mark",arrayList.get(i).get("SiteName"));
                                Log.v("AQI_mark",arrayList.get(i).get("AQI"));
                                Log.v("PM25_mark",arrayList.get(i).get("PM25"));
                                Log.v("Longitude_mark",arrayList.get(i).get("Longitude"));
                                Log.v("Latitude_mark",arrayList.get(i).get("Latitude"));

                                //??????marker??????
                                int height = 90;
                                int width = 90;

                                Integer air;

                                if(epa_data.equals("PM25"))
                                {
                                    BitmapDrawable bitmapdraw = new BitmapDrawable();
                                    if(arrayList.get(i).get("PM25").equals("?????????")==false && arrayList.get(i).get("PM25").equals("0")==false)
                                    {
                                        air = Integer.parseInt(arrayList.get(i).get("PM25"));

                                        if(air<=35)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_one);
                                        }
                                        else if(air>35 && air<=41)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_two);
                                        }

                                        else if(air>41 && air<=53)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_three);
                                        }
                                        else if(air>53 && air<=70)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_four);
                                        }
                                        else if(air>70)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_five);
                                        }
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                        Canvas canvas = new Canvas(smallMarker);
                                        Paint paint = new Paint();
                                        paint.setColor(Color.WHITE);
                                        paint.setTextSize(35);

                                        Double air_dou = Double.parseDouble(arrayList.get(i).get("PM25"));
                                        int air_int = (int) Math.round(air_dou);
                                        if(air_int>=100)
                                        {
                                            canvas.drawText(Integer.toString(air_int),18,55,paint);
                                        }
                                        else if(air_int<10)
                                        {
                                            canvas.drawText(Integer.toString(air_int),35,55,paint);
                                        }
                                        else
                                        {
                                            canvas.drawText(Integer.toString(air_int),25,55,paint);
                                        }

                                        String data="";
                                        data = "PM2.5???"+arrayList.get(i).get("PM25");

                                        LatLng epa = new LatLng(Double.valueOf(arrayList.get(i).get("Latitude")), Double.valueOf(arrayList.get(i).get("Longitude")));
                                        mMap.addMarker(new MarkerOptions()
                                                .position(epa)
                                                .title("?????????"+arrayList.get(i).get("SiteName"))
                                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                                .snippet(data)
                                        );
                                    }
                                }
                                else if(epa_data.equals("AQI"))
                                {

                                    BitmapDrawable bitmapdraw = new BitmapDrawable();
                                    if(arrayList.get(i).get("AQI").equals("?????????")==false && arrayList.get(i).get("AQI").equals("0")==false)
                                    {
                                        air = Integer.parseInt(arrayList.get(i).get("AQI"));

                                        if(air<=50)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_one);
                                        }
                                        else if(air>50 && air<=100)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_two);
                                        }

                                        else if(air>100 && air<=150)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_three);
                                        }
                                        else if(air>150 && air<=200)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_four);
                                        }
                                        else if(air>200)
                                        {
                                            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_five);
                                        }

                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false).copy(Bitmap.Config.ARGB_8888,true);
                                        Canvas canvas = new Canvas(smallMarker);
                                        Paint paint = new Paint();
                                        paint.setColor(Color.WHITE);
                                        paint.setTextSize(35);

                                        Double air_dou = Double.parseDouble(arrayList.get(i).get("AQI"));
                                        int air_int = (int) Math.round(air_dou);
                                        if(air_int>=100)
                                        {
                                            canvas.drawText(Integer.toString(air_int),18,55,paint);
                                        }
                                        else if(air_int<10)
                                        {
                                            canvas.drawText(Integer.toString(air_int),35,55,paint);
                                        }
                                        else
                                        {
                                            canvas.drawText(Integer.toString(air_int),25,55,paint);
                                        }

                                        String data="";
                                        data = "AQI???"+arrayList.get(i).get("AQI");

                                        LatLng epa = new LatLng(Double.valueOf(arrayList.get(i).get("Latitude")), Double.valueOf(arrayList.get(i).get("Longitude")));
                                        mMap.addMarker(new MarkerOptions()
                                                .position(epa)
                                                .title("?????????"+arrayList.get(i).get("SiteName"))
                                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                                .snippet(data)
                                        );
                                    }
                                }
                            }
                            dialog.dismiss();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });
    }

    //???lass????????????
    private void sendLassGET() {
        ProgressDialog dialog = ProgressDialog.show(MapsActivity.this,"?????????","?????????",true);
        /**????????????*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**??????????????????*/
        Request request = new Request.Builder()
                .url(data_url)
                //               .header("Cookie","")//???Cookie??????????????????????????????
                //               .addHeader("","")//??????API?????????header????????????????????????
                .build();
        /**????????????*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**?????????????????????????????????*/
                Log.v("error",e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(MapsActivity.this, "??????????????????????????????", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //Log.v("epa",response.body().string());
                /**????????????*/

                //ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                //String text = responseBodyCopy.string();
                String text = response.body().string();
                String time_text = text.substring(10,29);
                Log.v("time_text",time_text);
                String record_text = text.substring(43,text.length()-2);
                Log.v("record_text",record_text);
                String[] record = record_text.split("\\{'SiteName': '|', 'gps_lat': |, 'gps_lon': |, 's_d0': |\\}, \\{'SiteName': '|\\}");
                Log.v("record_size",Integer.toString(record.length));

                for(int i=0;i<record.length;i++) {
                    Log.v("record", record[i]);
                }

                try {
                    JSONArray jsonArray = new JSONArray();

                    int count = 0;
                    String SiteName = "";
                    String gps_lat = "";
                    String gps_lon = "";
                    String s_d0 = "";

                    for(int i=1;i<record.length;i++)
                    {
                        Log.v("i",Integer.toString(i));
                        JSONObject object = new JSONObject();
                        if(count%4==0)
                        {
                            Object repeat = object.put("SiteName",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record0",record[i]);
                                SiteName = record[i];
                            }
                        }
                        else if(count%4==1)
                        {

                            Object repeat = object.put("gps_lat",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record1",record[i]);
                                gps_lat = record[i];
                            }
                        }
                        else if(count%4==2)
                        {

                            Object repeat = object.put("gps_lon",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record2",record[i]);
                                gps_lon = record[i];
                            }
                        }
                        else if(count%4==3)
                        {

                            Object repeat = object.put("s_d0",record[i]);
                            if(repeat!=null)
                            {
                                Log.v("record3",record[i]);
                                if(record[i].equals(""))
                                {
                                    record[i] = "?????????";
                                }
                                s_d0 = record[i];
                            }
                            object.put("SiteName",SiteName);
                            object.put("gps_lat",gps_lat);
                            object.put("gps_lon",gps_lon);
                            object.put("s_d0",s_d0);

                            jsonArray.put(object);
                        }
                        count++;
                    }
                    Log.v("object",jsonArray.toString());
                    Log.v("size",Integer.toString(jsonArray.length()));


                    arrayList.clear();
                    for (int i =0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        SiteName = jsonObject.getString("SiteName");
                        //Log.v("SiteName",SiteName);
                        gps_lat = jsonObject.getString("gps_lat");
                        //Log.v("AQI",AQI);
                        gps_lon = jsonObject.getString("gps_lon");
                        //Log.v("PM25",PM25);
                        s_d0 = jsonObject.getString("s_d0");
                        String s_d0_int = Integer.toString((int) Math.round(Double.parseDouble(s_d0)));

                        HashMap<String,String> hashMap = new HashMap<>();

                        hashMap.put("SiteName",SiteName);
                        hashMap.put("gps_lat",gps_lat);
                        hashMap.put("gps_lon",gps_lon);
                        hashMap.put("s_d0",s_d0_int);

                        arrayList.add(hashMap);
                    }
                    Log.v("data","catchData: "+arrayList);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("getdata","Data: "+arrayList);
                            for(int i=0;i<arrayList.size();i++)
                            {
                                Log.v("name_mark",arrayList.get(i).get("SiteName"));
                                Log.v("gps_lat_mark",arrayList.get(i).get("gps_lat"));
                                Log.v("gps_lon_mark",arrayList.get(i).get("gps_lon"));
                                Log.v("s_d0_mark",arrayList.get(i).get("s_d0"));

                                //??????marker??????
                                int height = 90;
                                int width = 90;

                                if(arrayList.get(i).get("s_d0").equals("?????????")==false && arrayList.get(i).get("s_d0").equals("0")==false)
                                {
                                    Double air = Double.parseDouble(arrayList.get(i).get("s_d0"));
                                    BitmapDrawable bitmapdraw = new BitmapDrawable();

                                    if(air<=35)
                                    {
                                        bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_one);
                                    }
                                    else if(air>35 && air<=41)
                                    {
                                        bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_two);
                                    }

                                    else if(air>41 && air<=53)
                                    {
                                        bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_three);
                                    }
                                    else if(air>53 && air<=70)
                                    {
                                        bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_four);
                                    }
                                    else if(air>70)
                                    {
                                        bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.airdata_five);
                                    }

                                    Bitmap b = bitmapdraw.getBitmap();
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false).copy(Bitmap.Config.ARGB_8888,true);
                                    Canvas canvas = new Canvas(smallMarker);
                                    Paint paint = new Paint();
                                    paint.setColor(Color.WHITE);
                                    paint.setTextSize(35);

                                    Integer air_int = Integer.parseInt(arrayList.get(i).get("s_d0"));
                                    if(air_int>=100)
                                    {
                                        canvas.drawText(Integer.toString(air_int),18,55,paint);
                                    }
                                    else if(air_int<10)
                                    {
                                        canvas.drawText(Integer.toString(air_int),35,55,paint);
                                    }
                                    else
                                    {
                                        canvas.drawText(Integer.toString(air_int),25,55,paint);
                                    }

                                    LatLng epa = new LatLng(Double.valueOf(arrayList.get(i).get("gps_lat")), Double.valueOf(arrayList.get(i).get("gps_lon")));
                                    mMap.addMarker(new MarkerOptions()
                                            .position(epa)
                                            .title("?????????"+arrayList.get(i).get("SiteName"))
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                            .snippet("PM2.5???"+arrayList.get(i).get("s_d0"))
                                    );
                                }
                            }
                            dialog.dismiss();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });
    }
}


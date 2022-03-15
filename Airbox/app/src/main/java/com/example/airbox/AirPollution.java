package com.example.airbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AirPollution extends AppCompatActivity {

    //功能列
    private ImageButton btnMap;
    private ImageButton btnFriend;
    private ImageButton btnDevice;
    private ImageButton btnTourism;
    private ImageButton btnSet;

    //返回上一頁
    private ImageButton btnBack;

    //PM2.5濃度上限設定
    private SeekBar seekbar;
    private TextView seektext;

    //尚未設定濃度上限的初始值
    private int init_value = 35;

    //確定按鈕
    private Button airPollution_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_pollution);

        //切換到上一頁
        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AirPollution.this, UserSet.class);
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
                intent.setClass(AirPollution.this, MapsActivity.class);
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
                intent.setClass(AirPollution.this, Friend.class);
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
                intent.setClass(AirPollution.this, Device.class);
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
                intent.setClass(AirPollution.this, Tourism.class);
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
                intent.setClass(AirPollution.this, UserSet.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        int imageResource = getResources().getIdentifier("@drawable/set_button_click", null, getPackageName()); //取得圖片Resource位子
        btnSet.setImageResource(imageResource);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seektext = (TextView) findViewById(R.id.seektext);

        //設定完上限後，將值存起來
        airPollution_btn = findViewById(R.id.airPollution_btn);
        airPollution_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init_value = seekbar.getProgress();
                Log.v("Airpollution",Integer.toString(init_value));

                SharedPreferences pref=getSharedPreferences("userAirpollution",MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor= pref.edit();
                editor.putString("Airpollution",Integer.toString(init_value));
                editor.putString("PollutionDone","");
                editor.commit();

                Toast.makeText(AirPollution.this, "儲存成功", Toast.LENGTH_LONG).show();
            }
        });

        SharedPreferences pref=getSharedPreferences("userAirpollution",MODE_MULTI_PROCESS);

        //使用者設定過上限
        if(pref.contains("Airpollution"))
        {
            String data = getSharedPreferences("userAirpollution", MODE_MULTI_PROCESS).getString("Airpollution", "");
            Log.v("Airpollution",data);

            seekbar.setMax(100);//設定SeekBar最大值
            seekbar.setProgress(Integer.parseInt(data));//設定SeekBar拖移初始值
        }

        //使用者尚未設定上限，初始值給定35
        else
        {
            seekbar.setMax(100);//設定SeekBar最大值
            seekbar.setProgress(init_value);//設定SeekBar拖移初始值
        }

        //滑動seekbar的設定
        seektext.setText("濃度上限：" +seekbar.getProgress() + "  /  最大值："+seekbar.getMax());
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seektext.setText("濃度上限：" + progress + "  /  最大值："+seekbar.getMax());
            }

            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(AirPollution.this, "觸碰SeekBar", Toast.LENGTH_SHORT).show();
            }

            @Override

            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(AirPollution.this, "放開SeekBar", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
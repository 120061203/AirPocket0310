package com.example.airbox;

import static android.content.Context.MODE_MULTI_PROCESS;
import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class MysqlCon {
    //JDBC連接資料庫
    //資料庫定義
    String mysql_ip = "";
    int mysql_port = 3306; //Port 預設為 3306
    String db_name = "airbox";
    String url = "jdbc:mysql://"+mysql_ip+":"+mysql_port+"/"+db_name;
    String db_user = "root";
    String db_password = "12345678";

    //建構子
    public MysqlCon()
    {

    }

    //連接資料庫
    public void connMql(String ip) {
        boolean Driverflag = false;
        boolean Connflag = false;
        while(Driverflag==false)
        {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Log.v("DB","加載驅動成功");
                Driverflag = true;
            }catch( ClassNotFoundException e) {
                Log.e("DB","加載驅動失敗");
                return;
            }
        }
        while(Connflag==false)
        {
            mysql_ip = ip;
            url = "jdbc:mysql://"+mysql_ip+":"+mysql_port+"/"+db_name;
            // 連接資料庫
            try {
                Connection con = DriverManager.getConnection(url,db_user,db_password);
                Log.v("DB","遠端連接成功");
                Connflag = true;
            }catch(SQLException e) {
                Log.e("DB","遠端連接失敗");
                sendGetIP();
                Log.e("DB", e.toString());
            }
        }
    }

    //抓取server端ip
    private void sendGetIP() {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/

        Request request = new Request.Builder()
                .url("http://192.168.58.187:8000/ip")
                //               .header("Cookie","")//有Cookie需求的話則可用此發送
                //               .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                Log.v("error",e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                String ip = responseBodyCopy.string();
                Log.v("ip",ip);
                mysql_ip = ip;
            }

        });
    }

    //好友
    //SELECT MAX(dataID),userID,name,latitude,longitude,pm25 FROM data NATURAL JOIN (SELECT userID,boxID,name FROM `user` WHERE userID IN (SELECT friendID FROM `friend` WHERE friendConfirm='1' AND userID='1831')) AS two GROUP BY userID
    //抓取好友列表資料(id,name,lat,lon,pm25)
    public String getFriendWithDevice(String userid) {
        Log.v("DB",userid);
        String data = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT dataID,boxID,longitude,latitude,pm25 FROM data as t1 WHERE EXISTS (SELECT boxID,max(dataID) as B from data GROUP BY boxID HAVING t1.boxID = boxID and t1.dataID = max(dataID))";
            //String sql = "SELECT MAX(dataID),userID,name,latitude,longitude,pm25 FROM `data` NATURAL JOIN (SELECT userID,boxID,name FROM `user` WHERE userID IN (SELECT friendID FROM `friend` WHERE friendConfirm='1' AND userID LIKE '" + userid + "')) AS two GROUP BY userID";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                String dataID = rs.getString("MAX(dataID)");
                String userID = rs.getString("userID");
                String name = rs.getString("name");
                String latitude = rs.getString("latitude");
                String longitude = rs.getString("longitude");
                String pm25 = rs.getString("pm25");
                data += dataID + "," + userID + "," + name + "," + latitude + "," + longitude + "," + pm25 + ",";
                Log.v("DB",dataID);
                Log.v("DB",userID);
                Log.v("DB",name);
                Log.v("DB",latitude);
                Log.v("DB",longitude);
                Log.v("DB",pm25);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    //SELECT name FROM (SELECT * FROM `user` WHERE userID IN (SELECT friendID FROM `friend` WHERE friendConfirm='1' AND userID='3262')) AS one WHERE boxID ='deviceName'
    //抓取沒有裝置的好友名單
    public String getFriendNoDevice(String userid) {
        Log.v("DB",userid);
        String data ="";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT name FROM (SELECT * FROM `user` WHERE userID IN (SELECT friendID FROM `friend` WHERE friendConfirm LIKE '" + 1 + "' AND userID LIKE '" + userid + "')) AS one WHERE boxID LIKE 'deviceName'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                String name = rs.getString("name");
                data += name + ",";
                Log.v("DB",name);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.v("data","1"+data);
        return data;
    }

    //SELECT name FROM `user` WHERE userID IN (SELECT friendID FROM `friend` WHERE friendConfirm='0' AND userID='3262');
    //抓取使用者的好友確認名單
    public String getConfirmFriendData(String myid){
        Log.v("DB",myid);
        String data ="";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT name,userID FROM `user` WHERE userID IN (SELECT userID FROM `friend` WHERE friendConfirm LIKE '" + 0 + "' AND friendID LIKE '" + myid + "')";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                String userID = rs.getString("userID");
                String name = rs.getString("name");
                data += userID + "," + name + ",";
                Log.v("DB",userID);
                Log.v("DB",name);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    //DELETE FROM customers WHERE Name='王二';
    //拒絕好友請求，刪除資料庫資料
    public String delConfirmFriendData(String myid,String friendid)
    {
        String check = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql_friend = "DELETE FROM friend WHERE userID LIKE '" + friendid + "' AND friendID LIKE '" + myid +  "'";
            Statement st_friend = con.createStatement();
            st_friend.executeUpdate(sql_friend);
            st_friend.close();

            check = "true";
            Log.v("DB", "刪除成功");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return check;
    }

    //接受好友請求，更新好友的資料庫資料
    public String updateNewFriendData(String myid,String friendid)
    {
        String check = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql_my = "INSERT INTO `friend` (`userID`,`friendID`,`friendConfirm`) VALUES ('" + myid + "','" + friendid + "','" + 1 +"')";
            Statement st_my = con.createStatement();
            st_my.executeUpdate(sql_my);
            st_my.close();

            String sql_friend = "UPDATE friend SET friendConfirm = '" + 1 + "' WHERE userID LIKE '" + friendid + "' AND friendID LIKE '" + myid +  "'";
            Statement st_friend = con.createStatement();
            st_friend.executeUpdate(sql_friend);
            st_friend.close();

            check = "true";
            Log.v("DB", "新增好友成功");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return check;
    }

    //搜尋是否有該名使用者
    public String searchFriend(String userID)
    {
        Log.v("DB",userID);
        String username="";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT * FROM `user` WHERE userID LIKE '" + userID + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if(rs.next())
            {
                username = rs.getString("name");
                Log.v("DB",username);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return username;
    }

    //確認是否已是好友
    public String checkFriend(String myid,String friendid)
    {
        Log.v("DB",myid);
        Log.v("DB",friendid);
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "SELECT * FROM `friend` WHERE friendConfirm LIKE '" + 1 + "' AND userID LIKE '" + myid + "' AND friendID LIKE '" + friendid + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            check = "false";
            if(rs.next())
            {
                Log.v("DB","已為好友,ID:"+rs.getString("friendID"));
                check = "true";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    //確認是否已送出好友申請
    public String checkSendFriend(String myid,String friendid)
    {
        Log.v("DB",myid);
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT * FROM `friend` WHERE friendConfirm LIKE '" + 0 + "' AND userID LIKE '" + myid + "' AND friendID LIKE '" + friendid + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            check = "false";
            if(rs.next())
            {
                Log.v("DB","已送出好友申請");
                check = "true";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    //送出好友申請，新增資料庫資料
    public String insertFriendData(String myid,String friendid)
    {
        Log.v("DB",myid);
        Log.v("DB",friendid);

        String check = "false";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql_my = "INSERT INTO `friend` (`userID`,`friendID`,`friendConfirm`) VALUES ('" + myid + "','" + friendid + "','" + 0 +"')";
            Statement st_my = con.createStatement();
            st_my.executeUpdate(sql_my);
            st_my.close();
            Log.v("DB", "寫入第一筆資料完成");

            check = "true";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;

    }

    //裝置
    //新增裝置資料到資料庫中
    public void insertDeviceData(String dataID,String boxID,String year,String month,String day,String hour,String minute,String second,String temperature,String humidity,String pm100,
                                 String pm25,String pm1,String lat_str,String lon_str)
    {
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "INSERT INTO `data` (`dataID`,`year`,`month`,`day`,`hour`,`minute`,`second`,`boxID`,`temp`,`humid`,`longitude`,`latitude`,`pm10`,`pm25`,`pm1`) VALUES " +
                    "('" + dataID + "','" + year + "','" + month + "','" + day + "','" + hour + "','" + minute +  "','" + second + "','" + boxID + "','" + temperature
                    +  "','" + humidity +  "','" + lon_str + "','"  + lat_str +  "','" + pm100 + "','" + pm25 + "','" + pm1 +"')";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "寫入資料完成");
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("DB", "寫入資料失敗");
            Log.e("DB", e.toString());
        }
    }

    //景點
    //檢查該景點是否有在資料庫中
    public String checkfavoriteData(String myid,String placeid)
    {
        Log.v("DB",myid);
        Log.v("DB",placeid);
        String check = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT * FROM `mycollection` WHERE userID LIKE '" + myid + "' AND attractionID LIKE '" + placeid + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            check = "false";
            if(rs.next())
            {
                Log.v("DB","找到資料");
                check = "true";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    //新增收藏資料到資料庫中
    public String addfavoriteData(String myid,String placeid,String latitude,String longitude)
    {
        Log.v("DB",myid);
        Log.v("DB",placeid);
        Log.v("DB",latitude);
        Log.v("DB",longitude);

        String check ="";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "INSERT INTO `mycollection` (`userID`,`attractionID`,`latitude`,`longitude`) VALUES ('" + myid + "','" + placeid + "','" + latitude + "','" + longitude +"')";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "寫入資料完成");

            check = "true";
        } catch (SQLException e) {
            check = "false";
            e.printStackTrace();
            Log.e("DB", "寫入資料失敗");
            Log.e("DB", e.toString());
        }
        return check;

    }

    //刪除資料庫中的收藏資料
    public String delfavoriteData(String myid,String placeid)
    {
        Log.v("DB",myid);
        Log.v("DB",placeid);
        String check = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "DELETE FROM mycollection WHERE userID LIKE '" + myid + "' AND attractionID LIKE '" + placeid +  "'";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();

            check = "true";
            Log.v("DB", "刪除成功");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return check;
    }

    //確認是否有收藏資料
    public String getUserCollection(String userid) {
        Log.v("DB",userid);
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "SELECT * FROM `myCollection` WHERE userID LIKE '" + userid + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            check = "false";
            if(rs.next())
            {
                Log.v("DB","有收藏資料");
                check = "true";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    //SELECT DISTINCT latitude,longitude FROM `mycollection` WHERE userID LIKE '3262'
    //抓取使用者收藏景點的所有經緯度
    public String getAttraction_lat_lon(String userid) {
        Log.v("DB",userid);
        String data ="";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT DISTINCT latitude,longitude FROM `mycollection` WHERE userID LIKE '" + userid + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                String latitude = rs.getString("latitude");
                String longitude = rs.getString("longitude");

                data += latitude + "," + longitude + ",";
                Log.v("DB",latitude);
                Log.v("DB",longitude);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.v("DB",data);
        return data;
    }

    //抓取使用者收藏的所有景點id
    public String getCollection_attractionid(String userid) {
        Log.v("DB",userid);
        String data ="";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT attractionid FROM `mycollection` WHERE userID LIKE '" + userid + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                String attractionid = rs.getString("attractionid");

                data += attractionid + ",";
                Log.v("DB",attractionid);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    //更多
    //檢查資料庫中是否有存入使用者的裝置資料
    public String checkUserDevice(String userID,String address) {
        Log.v("DB",userID);
        Log.v("DB",address);
        String check = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT * FROM `user` WHERE userID LIKE '" + userID + "' AND boxID LIKE '" + address + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            check = "false";
            if(rs.next())
            {
                Log.v("DB","已有裝置");
                check = "true";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    //使用者新增裝置，將裝置id資料新增進資料庫中
    public void insertDevice(String userID,String address) {
        Log.v("DB",userID);
        Log.v("DB",address);

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "UPDATE user SET boxID = '" + address + "' WHERE userID LIKE '" + userID + "'";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "新增裝置資料成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //使用者解除配對，將裝置id資料移除
    public void delDevice(String userID,String address) {
        Log.v("DB",userID);
        Log.v("DB",address);

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "UPDATE user SET boxID = 'deviceName' WHERE userID LIKE '" + userID + "'";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "刪除裝置資料成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //使用者註冊帳號，將資料新增進資料庫中
    public void insertData(String name,String email,String password) {
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            //user001 user002 user003
            //隨機生成四位數(1-9999)，代表使用者編號
            Random ran = new Random(); //宣告一個Random，變數名稱為ran
            int userId;
            userId = ran.nextInt(9999-1000+1)+1000;
            boolean SameFlag = false;
            String id = String.valueOf(userId);
            //確認編號是否重複
            String samesql = "SELECT * FROM user WHERE userID LIKE '" + id +"'";
            Statement samest = con.createStatement();
            ResultSet samers = samest.executeQuery(samesql);
            while(samers.next())
            {
                SameFlag = true;
            }

            //編號重複時，重新給定一個新的編號
            while(SameFlag==true)
            {
                userId = ran.nextInt(9999-1000+1)+1000;
                SameFlag = false;
                id = String.valueOf(userId);
                //確認編號是否重複
                samesql = "SELECT * FROM user WHERE userID LIKE '" + id +"'";
                samest = con.createStatement();
                samers = samest.executeQuery(samesql);
                while(samers.next())
                {
                    SameFlag = true;
                }
            }

            String sql = "INSERT INTO `user` (`userID`,`email`,`password`,`name`,`sumPoint`,`boxID`) VALUES ('" + id + "','" + email + "','" + password + "','" + name + "','" + 0 + "','"+ "deviceName" +"')";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "寫入資料完成");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //比對使用者輸入的帳號密碼是否與資料庫中一致，若一致會跳到裝置頁面，否則會停留在登入畫面
    public String getUserData(String email,String password) {
        Log.v("DB",email);
        Log.v("DB",password);
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "SELECT * FROM `user` WHERE email LIKE '" + email + "' AND password LIKE '" + password + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            check = "false";
            if(rs.next())
            {
                Log.v("DB","登入成功");
                check = "true";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    //抓取使用者id
    public String getUserID(String email) {
        Log.v("DB",email);
        String userID = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT userID FROM `user` WHERE email LIKE '" + email + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if(rs.next())
            {
                userID = rs.getString("userID");
                Log.v("DB",userID);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userID;
    }

    //抓取使用者名字
    public String getUserName(String email) {
        Log.v("DB",email);
        String name = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT name FROM `user` WHERE email LIKE '" + email + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if(rs.next())
            {
                name = rs.getString("name");
                Log.v("DB",name);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    //UPDATE user SET name="吳如" WHERE name="吳偲如";
    //修改使用者暱稱
    public String modifyName(String email,String new_name){
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "UPDATE user SET name = '" + new_name + "' WHERE email LIKE '" + email + "'";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            check = "true";
            st.close();
            Log.v("DB", "修改名字成功");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return check;
    }

    //修改使用者郵箱
    public String modifyEmail(String last_email,String new_email) {
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "UPDATE user SET email = '" + new_email + "' WHERE email LIKE '" + last_email + "'";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            check = "true";
            st.close();
            Log.v("DB", "修改郵箱成功");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return check;
    }

    //修改使用者密碼
    public String modifyPassword(String email,String new_password) {
        String check = "";

        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);

            String sql = "UPDATE user SET password = '" + new_password + "' WHERE email LIKE '" + email + "'";
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            check = "true";
            st.close();
            Log.v("DB", "修改密碼成功");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return check;
    }
}

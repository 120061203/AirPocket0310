from flask import Flask
import urllib
import gzip
import json
import math
import socket
import requests
import pymysql
from datetime import datetime

db_settings = {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "12345678",
    "db": "airbox",
    "charset": "utf8"
}

#目標網址lass
url_lass = 'https://pm25.lass-net.org/data/last-all-airbox.json.gz'
file_name = url_lass.split("/")[-1]
uneed_lass = ['SiteAddr','time', 'app', 'area', 'date', 'gps_alt', 'gps_fix', 'gps_num', 'name', 's_d1', 's_d2', 's_h0', 's_t0', 'timestamp', 'device_id', 'c_d0', 'c_d0_method']

#目標網址epa
url_epa = "https://data.epa.gov.tw/api/v1/aqx_p_432?offset=0&limit=10000&api_key=a0afbf32-6aaa-48b7-ba97-aacd1f6e7485"
uneed_epa = ['County', 'Pollutant', 'Status', 'SO2', 'CO', 'CO_8hr', 'O3', 'O3_8hr', 'PM10', 'NO2', 'NOx', 'NO', 'WindSpeed', 'WindDirec', 'PublishTime', 'PM2.5_AVG', 'PM10_AVG', 'SO2_AVG','SiteId', 'ImportDate']

key_google = "AIzaSyA11vxDL0OotP-2onXIBHnzkt8Z0SNiYco"

app = Flask(__name__)

def get_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    ip = s.getsockname()[0]
    s.close()
    return ip

def getDistance(latA, lonA, latB, lonB):
    ra = 6378140  # 赤道半徑
    rb = 6356755  # 極半徑
    flatten = (ra - rb) / ra  # Partial rate of the earth
    # change angle to radians
    radLatA = math.radians(latA)
    radLonA = math.radians(lonA)
    radLatB = math.radians(latB)
    radLonB = math.radians(lonB)

    pA = math.atan(rb / ra * math.tan(radLatA))
    pB = math.atan(rb / ra * math.tan(radLatB))
    x = math.acos(math.sin(pA) * math.sin(pB) + math.cos(pA) * math.cos(pB) * math.cos(radLonA - radLonB))
    c1 = (math.sin(x) - x) * (math.sin(pA) + math.sin(pB)) ** 2 / math.cos(x / 2) ** 2
    c2 = (math.sin(x) + x) * (math.sin(pA) - math.sin(pB)) ** 2 / math.sin(x / 2) ** 2
    dr = flatten / 8 * (c1 - c2)
    distance = ra * (x + dr)
    distance = round(distance / 1000, 4)
    return distance

@app.route("/")
@app.route("/lass")
def lass():

    final = []
    try:
        # 建立Connection物件
        conn = pymysql.connect(**db_settings)

        with conn.cursor() as cursor:
            command = "SELECT dataID,boxID,longitude,latitude,pm25 FROM data as t1 WHERE EXISTS (SELECT boxID,max(dataID) as B from data GROUP BY boxID HAVING t1.boxID = boxID and t1.dataID = max(dataID))"

            # 執行指令
            cursor.execute(command)
            # 取得所有資料
            result = cursor.fetchall()

            for i in result:
                if i[1] != "" and i[2] != "" and i[3] != "" and i[4] != 0:
                    try:
                        final.append({'SiteName': i[1],
                                      'gps_lat': float(i[3][:9]),
                                      'gps_lon': float(i[2][:10]),
                                      's_d0': float(i[4])})
                    except:
                        continue
    except:
        print("db Connect fail")

    #請求gz檔
    urllib.request.urlretrieve(url_lass, file_name)
    

    f = gzip.open(file_name,'r')
    jdata = f.read()
    f.close()
    #json
    lass_datas = json.loads(jdata)
    data = lass_datas['feeds']

    for d in data:
        try:
            final.append({'SiteName': d['SiteName'],
                          'gps_lat': d['gps_lat'],
                          'gps_lon': d['gps_lon'],
                          's_d0': d['s_d0']})
        except:
            continue
    result = {"time" : datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
    "record" : final}
    return str(result)

@app.route("/ip")
def ip():
    netip = requests.get('https://api.ipify.org').text
    return str(netip)

@app.route("/epa")
def epa():
    final = []
    try:
        # 建立Connection物件
        conn = pymysql.connect(**db_settings)

        with conn.cursor() as cursor:
            command = "SELECT dataID,boxID,longitude,latitude,pm25 FROM data as t1 WHERE EXISTS (SELECT boxID,max(dataID) as B from data GROUP BY boxID HAVING t1.boxID = boxID and t1.dataID = max(dataID))"

            # 執行指令
            cursor.execute(command)
            # 取得所有資料
            result = cursor.fetchall()

            for i in result:
                if i[1] != "" and i[2] != "" and i[3] != "" and i[4] != 0:
                    try:
                        final.append({'SiteName': i[1],
                                      'AQI': "",
                                      'PM2.5': str(i[4]),
                                      'Longitude': str(i[2][:10]),
                                      'Latitude': str(i[3][:9])})
                    except:
                        continue
    except:
        print("db Connect fail")

    with urllib.request.urlopen(url_epa) as response:
        html = response.read()

    #json   
    json_data = json.loads(html)
    data = json_data['records']
    for d in data:
        try:
            if d['AQI'] != "" or d['PM2.5'] != "":
                final.append({'SiteName': d['SiteName'],
                             'AQI': d['AQI'],
                            'PM2.5': d['PM2.5'],
                            'Longitude': d['Longitude'],
                            'Latitude': d['Latitude']})
        except:
            continue
    result = {"time" : datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
    "record" : final}
    return str(result)
@app.route("/weather/<location>")
def weather(location):
    #lass
    url = 'https://pm25.lass-net.org/data/last-all-airbox.json.gz'

    file_name = url.split("/")[-1]
    #請求gz檔
    urllib.request.urlretrieve(url, file_name)

    f = gzip.open(file_name,'r')
    jdata = f.read()
    f.close()
    #lass_json
    lass_data = json.loads(jdata)
    data = lass_data['feeds']
    dict_value = [float(x) for x in location.split(",")]
    MIN = 99
    target = 0
    for d in data:
            dd = getDistance(dict_value[0], dict_value[1], d['gps_lat'], d['gps_lon'])
            if dd < 5.0:
                if (MIN > dd):
                    MIN = dd
                    target = d
    final = {'time' : target['time'],
            'SiteName' : target['SiteName'],
            's_t0' : target['s_t0'],
            's_h0' : target['s_h0'],
            's_d0' : target['s_d0']}
    return str(final)
@app.route("/google/<location>")
def google(location):
    #location = "22.6417806,120.3232642"
    radius = 5000
    type_target = "tourist_attraction"
    keyword = ''""

    # google
    dict1 = {'location': location, 'radius': radius, 'type': type_target, 'keyword': keyword, 'key': key_google}
    qstr = urllib.parse.urlencode(dict1)

    target_url_APX = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=zh-TW&"
    target_url_APX = target_url_APX + qstr

    with urllib.request.urlopen(target_url_APX) as response:
        html = response.read()

    #google_json
    google_data = json.loads(html)

    #請求gz檔
    try:
        urllib.request.urlretrieve(url_lass, file_name)
    except Exception as e:
        print(e)

    f = gzip.open(file_name,'r')
    jdata = f.read()
    f.close()
    #lass_json
    lass_data = json.loads(jdata)




    #closest lass site
    data = lass_data['feeds']
    final = []
    lat, lon = (float(x) for x in location.split(","))
    for google_results in google_data['results']:
        # fine closest lass site
        dict_value = list(google_results['geometry']['location'].values())
        MIN = 99.0
        target = 0
        for d in data:
            dd = getDistance(dict_value[0], dict_value[1], d['gps_lat'], d['gps_lon'])
            if dd < 1.0:
                if (MIN > dd):
                    MIN = dd
                    target = d
        # json set
        try:
            if target['s_d0'] < 41:
                final.append({'name': google_results['name'],
                          'place_id': google_results['place_id'],
                          'rating': google_results['rating'],
                          'user_ratings_total': google_results['user_ratings_total'],
                          'vicinity': google_results['vicinity'],
                          's_d0': target['s_d0'],
                          'lat' : dict_value[0],
                          'lon' : dict_value[1],
                          'distance': getDistance(dict_value[0], dict_value[1], lat, lon)})
        except:
            continue
    final = sorted(final, key=lambda i: i['distance'])

    result = {"time" : datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
    "record" : final}
    return str(result)


if __name__=="__main__":
    app.debug=True
    app.run(host=str(get_ip()),port=8000)
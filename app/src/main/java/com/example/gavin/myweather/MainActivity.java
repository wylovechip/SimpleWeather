package com.example.gavin.myweather;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import cn.edu.pku.wzc.bean.TodayWeather;
import cn.edu.pku.wzc.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final int UPDATE_TODAY_WEATHER =1;
    private String updateCityCode;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;//定义相关的控制对象
    private  TextView week1T,week2T,week3T,temperature1T,temperature2T,temperature3T,
            wind1T,wind2T,wind3T,climate1T,climate2T,climate3T;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };/*为更新按钮添加事件*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        } /*网络状态*/
        mCitySelect = (ImageView)  findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        initView();//调用initView函数。

        updateCityCode = getIntent().getStringExtra("citycode");//citycode改变更改天气信息
        if(updateCityCode!="-1"&& updateCityCode != null)
        {
            queryWeatherCode(updateCityCode);
        }else {
            SharedPreferences sharedPreferences = getSharedPreferences(
                    "CityCodePreference", Activity.MODE_PRIVATE);
            String defaultCityCode = sharedPreferences.getString("citycode", "");
            if (defaultCityCode != null) {
                Log.d("defaultCityCode", defaultCityCode);
                queryWeatherCode(defaultCityCode);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
            startActivityForResult(i,1);
        };
        if (view.getId() == R.id.title_update_btn) {
            //通过SharedPreferences读取城市id
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }  //获取网络数据
    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);


        //future
        week1T = (TextView)findViewById(R.id.future3_no1_week);
        temperature1T = (TextView)findViewById(R.id.future3_no1_temperature);
        climate1T = (TextView)findViewById(R.id.future3_no1_weatherState);
        wind1T = (TextView)findViewById(R.id.future3_no1_wind);

        week2T = (TextView)findViewById(R.id.future3_no2_week);
        temperature2T = (TextView)findViewById(R.id.future3_no2_temperature);
        climate2T = (TextView)findViewById(R.id.future3_no2_weatherState);
        wind2T = (TextView)findViewById(R.id.future3_no2_wind);

        week3T = (TextView)findViewById(R.id.future3_no3_week);
        temperature3T = (TextView)findViewById(R.id.future3_no3_temperature);
        climate3T = (TextView)findViewById(R.id.future3_no3_weatherState);
        wind3T = (TextView)findViewById(R.id.future3_no3_wind);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }//初始化控件内容

    private void queryWeatherCode(String cityCode) {

        //通过城市id获得天气数据，xml文件数据
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;//调用parseXML，并返回TodayWeather对象
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    //GET请求可以获取静态页面，也可以把参数放在URL字串后面，传递给servlet
                    con.setRequestMethod("GET");
                    /*设置超时时间为8秒，如果8秒内不能连接就被认为是有错误发生，
                    从而有效地避免了由于ip不通导致连接不上的问题
                     */
                    con.setConnectTimeout(8000);
                    //连接后等待响应超时,8秒
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);//调用解析函数
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        week1T.setText(todayWeather.getDate1());
        week2T.setText(todayWeather.getDate2());
        week3T.setText(todayWeather.getDate3());
        temperature1T.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
        temperature2T.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
        temperature3T.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
        climate1T.setText(todayWeather.getType1());
        climate2T.setText(todayWeather.getType2());
        climate3T.setText(todayWeather.getType3());
        wind1T.setText(todayWeather.getFengli1());
        wind2T.setText(todayWeather.getFengli2());
        wind3T.setText(todayWeather.getFengli3());

        if(todayWeather.getType()!=null) {
            Log.d("type", todayWeather.getType());
            switch (todayWeather.getType()) {
                case "晴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }//通过消息机制，将解析的天气对象，通过消息发送给主线程，
    // 主线程接收到消息数据后，调用updateTodayWeather函数，更新UI界面上的数据

    //XML解析函数，将获得的XML天气文件解析
    private TodayWeather parseXML(String xmlData)
    {
        TodayWeather todayWeather = null;

        int fengliCount = 0;
        int fengxiangCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType = xmlPullParser.getEventType();
            Log.d("MWeater","start parse xml");

            while(eventType!=xmlPullParser.END_DOCUMENT)
            {
                switch (eventType)
                {
                    //文档开始位置
                    case XmlPullParser.START_DOCUMENT:
                        Log.d("parse","start doc");
                        break;
                    //标签元素开始位置
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp"))
                        {
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather!=null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                Log.d("city", xmlPullParser.getText());
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                Log.d("updatetime", xmlPullParser.getText());
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                Log.d("wendu", xmlPullParser.getText());
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("fengli", xmlPullParser.getText());
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                Log.d("shidu", xmlPullParser.getText());
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("fengxiang", xmlPullParser.getText());
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                Log.d("pm25", xmlPullParser.getText());
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                Log.d("quelity", xmlPullParser.getText());
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("date", xmlPullParser.getText());
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("high", xmlPullParser.getText());
                                todayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("low", xmlPullParser.getText());
                                todayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("type", xmlPullParser.getText());
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            } else if( xmlPullParser.getName().equals("date") && dateCount == 1){
                                eventType = xmlPullParser.next();
                                Log.d("future1 date",xmlPullParser.getText());
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            } else if(xmlPullParser.getName().equals("low") && lowCount == 1 ){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText());
                                Log.d("future1 low",xmlPullParser.getText());
                                lowCount++;
                            } else if(xmlPullParser.getName().equals("high") && highCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText());
                                Log.d("future1 high",xmlPullParser.getText());
                                highCount++;
                            } else if(xmlPullParser.getName().equals("type") && typeCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                Log.d("future1 type",xmlPullParser.getText());
                                typeCount++;
                            } else if(xmlPullParser.getName().equals("fengli") && fengliCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                Log.d("future1 fengli",xmlPullParser.getText());
                                fengliCount++;
                            } else if( xmlPullParser.getName().equals("date") && dateCount == 2){
                                eventType = xmlPullParser.next();
                                Log.d("future2 date",xmlPullParser.getText());
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            } else if(xmlPullParser.getName().equals("low") && lowCount == 2 ){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText());
                                Log.d("future2 low",xmlPullParser.getText());
                                lowCount++;
                            } else if(xmlPullParser.getName().equals("high") && highCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText());
                                Log.d("future2 high",xmlPullParser.getText());
                                highCount++;
                            } else if(xmlPullParser.getName().equals("type") && typeCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                Log.d("future2 type",xmlPullParser.getText());
                                typeCount++;
                            } else if(xmlPullParser.getName().equals("fengli") && fengliCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                Log.d("future2 fengli",xmlPullParser.getText());
                                fengliCount++;
                            } else if( xmlPullParser.getName().equals("date") && dateCount == 3){
                                eventType = xmlPullParser.next();
                                Log.d("future3 date",xmlPullParser.getText());
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            } else if(xmlPullParser.getName().equals("low") && lowCount == 3 ){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText());
                                Log.d("future3 low",xmlPullParser.getText());
                                lowCount++;
                            } else if(xmlPullParser.getName().equals("high") && highCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText());
                                Log.d("future3 high",xmlPullParser.getText());
                                highCount++;
                            } else if(xmlPullParser.getName().equals("type") && typeCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                Log.d("future3 type",xmlPullParser.getText());
                                typeCount++;
                            } else if(xmlPullParser.getName().equals("fengli") && fengliCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                Log.d("future3 fengli",xmlPullParser.getText());
                                fengliCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return todayWeather;
    }

}

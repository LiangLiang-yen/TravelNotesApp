package com.TravelNotes.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.TravelNotes.app.DataBase.CountyItemDAO;
import com.TravelNotes.app.Item.CountyItem;
import com.TravelNotes.app.ui.VideoStream.VideoStream;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class testMainActivity extends AppCompatActivity {
    public static final String INTENT_FLAG = "PLAN";
    public static final String TAG = "local";
    private Document doc = null; //網頁原始碼
    private LatLng latLng;
    private String cityName; //英文城市名
    private String chineseName; //中文城市名
    private String searchDate; //天氣搜尋的日期
    private Geocoder geocoder; //找尋城市座標
    private CountyItemDAO itemDAO; //資料庫功能
    private List<CountyItem> citylist; //把找尋到城市存成list
    final String url = "http://www.ting.com.tw/tour-info/air-name.htm"; //查詢地區級城市的網址
    private Event loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Event.debug = true; //開啟debug模式
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_main);
        Button check_listButton = (Button) findViewById(R.id.buttonCheck_list);
        /*check_listButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(testMainActivity.this, CheckListActivity.class);
                intent.putExtra(INTENT_FLAG, cityName);
                startActivity(intent);
            }
        });*/
        Button streamButton = (Button) findViewById(R.id.buttonStream);
        streamButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(testMainActivity.this, VideoStream.class);
                startActivity(intent);
            }
        });
        initialization();
    }

    //初始化
    void initialization(){
        loadingDialog = new Event(this);
        loadingDialog.showloadingDialog(this);
        if(!isNetworkConnected()){
            AlertDialog.Builder builder = new AlertDialog.Builder(testMainActivity.this);
            builder.setMessage("無法連上網路")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(-1);
                        }
                    }).create();
            AlertDialog dialog = builder.show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().
                    build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Log.d(TAG, "Connected!");
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Log.d(TAG, "Not Connected!");
                    AlertDialog.Builder builder = new AlertDialog.Builder(testMainActivity.this);
                    builder.setMessage("無法連上網路")
                            .setPositiveButton("確認", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                    System.exit(-1);
                                }
                            }).create();
                    AlertDialog dialog = builder.show();
                }
            });
        }else{
            if(!isNetworkConnected())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(testMainActivity.this);
                builder.setMessage("無法連上網路")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(-1);
                            }
                        }).create();
                AlertDialog dialog = builder.show();
            }
        }

        //this.deleteDatabase(MyDBHelper.DATABASE_NAME);
        itemDAO = new CountyItemDAO(getApplicationContext());
        //資料庫是空的，執行networktask去撈資料
        if (itemDAO.isEmpty()) {
            Log.e("loacl", "Database is empty");
            new Thread(networkTask).start();
        }
        //將資料庫內的資料轉成Area的資料
        SqldataGet();

        geocoder = new Geocoder(this);
        final Spinner AreaSpinner = (Spinner) findViewById(R.id.SpinnerArea);
        final Spinner CitySpinner = (Spinner) findViewById(R.id.SpinnerCity);
        final EditText etSt = (EditText) findViewById(R.id.editTextStartDate);
        final EditText etEn = (EditText) findViewById(R.id.editTextEndDate);

        //監聽Spinner被選擇時
        AreaSpinner.setOnItemSelectedListener(AreaSpinnerSelected);
        CitySpinner.setOnItemSelectedListener(CitySpinnerSelected);

        //監聽日期edixtext被選擇時
        etSt.setOnClickListener(EditTextDateClicked);
        etEn.setOnClickListener(EditTextDateClicked);

        //初始顯示台北天氣
        latLng = new LatLng(25.0342218,121.5622723);
        chineseName = "台北";
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd"); //日期格式化
        searchDate = sd.format(Calendar.getInstance().getTime());
        Log.d(TAG, searchDate);
        weatherSearchAPITask weather = new weatherSearchAPITask(chineseName, searchDate, latLng.latitude, latLng.longitude);
        new Thread(weather).start();
    }

    Handler handler = new Handler();
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e(TAG,"Thread is called.");
                final String html = NetworkClass.getHtml(url);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        doc = Jsoup.parse(html);
                        SqldataSet();
                        //findAreaName();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //抓取區域名稱存到sql
    private void SqldataSet() {
        int index = 0;
        Elements AreaElements = doc.select("body > table > tbody > tr > td > table > tbody > tr >td > table > tbody"); //找出class = word的table
        for(Element element : AreaElements)
        {
            String Area = element.select("tr").get(0).select("td").text();
            Log.d(TAG,"Index:" + index + " " + Area + '\n');
            Elements CityElements = doc.select("body > table > tbody > tr > td > table > tbody > tr >td > table").get(index++).select("tr").next().next();
            for(Element cityelement : CityElements)
            {
                String cityName = cityelement.select("td").get(1).text();
                String chineseName = cityelement.select("td").get(0).text();
                Log.d(TAG,"Area:" + Area + " City:" + cityName + " Chinese:" + chineseName + '\n');

                CountyItem item = new CountyItem(0, Area, cityName, chineseName);
                itemDAO.insert(item);
            }
        }
        SqldataGet();
    }

    //從資料庫中撈取區域
    private void SqldataGet() {
        final Spinner AreaSpinner = (Spinner)findViewById(R.id.SpinnerArea);
        ArrayAdapter<String> areaArr = new ArrayAdapter<String>(testMainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, itemDAO.getArea());
        AreaSpinner.setAdapter(areaArr);
        AreaSpinner.setSelection(1);
    }

    //監聽AreaSpinner選擇----查詢地區new
    private AdapterView.OnItemSelectedListener AreaSpinnerSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Spinner CitySpinner = (Spinner)findViewById(R.id.SpinnerCity);
            ArrayAdapter<String> city_arr = new ArrayAdapter<String>(testMainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,0);
            citylist = itemDAO.getCity(parent.getItemAtPosition(position).toString());
            for(CountyItem item: citylist)
            {
                city_arr.add(item.getCityName() + item.getChineseName());
            }
            CitySpinner.setAdapter(city_arr);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    //監聽CitySpinner選擇----天氣查詢
    private AdapterView.OnItemSelectedListener CitySpinnerSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            //取得地區座標
            final EditText et = (EditText) findViewById(R.id.editTextStartDate);
            cityName = citylist.get(position).getCityName();
            chineseName = citylist.get(position).getChineseName();
            Log.e(TAG, "address:" + cityName);
            List<Address> addressList = null;
            try {
                //if java.io.IOException: grpc failed try Change maxResults number or Reopen wifi
                addressList = geocoder.getFromLocationName(cityName, 6);
                Address useraddress = addressList.get(0);
                latLng = new LatLng(useraddress.getLatitude(), useraddress.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(testMainActivity.this, "Geocoder Error", Toast.LENGTH_LONG).show();
                Toast.makeText(testMainActivity.this, "Please Restart Application", Toast.LENGTH_LONG).show();
            }
            if(!et.getText().toString().matches("")){
                weatherSearchAPITask weather = new weatherSearchAPITask(chineseName, searchDate, latLng.latitude, latLng.longitude);
                loadingDialog.showloadingDialog(getParent());
                new Thread(weather).start();
            }
        }
        @Override
        public void onNothingSelected (AdapterView < ? > parent){

        }
    };

    //查找天氣狀況--DarkSkyAPI
    public class weatherSearchAPITask implements Runnable{
        String searchDate;
        String searchCity;
        LatLng latLng;
        weatherSearchAPITask(String searchCity, String searchDate, double lat, double lon)
        {
            this.searchCity = searchCity;
            this.searchDate = searchDate;
            this.latLng = new LatLng(lat, lon);
        }
        @Override
        public void run() {
            try {
                Log.d(TAG, Double.toString(latLng.latitude) + " " + Double.toString(latLng.longitude));

                if(searchDate.matches(""))
                {
                    Future weatherSearchAPIFuture = Executors.newSingleThreadExecutor().submit(weatherSearchAPITask.this);
                    weatherSearchAPIFuture.cancel(true);
                }

                //取得Unix 時間戳記
                long InputTime = toUnixTime(searchDate);

                final String weatherUrl = "https://api.darksky.net/forecast/2ba39795402afeaddbb95afdd5b1d1cc/" + latLng.latitude + "," + latLng.longitude + "," + InputTime + "?lang=zh-tw&exclude=minutely,hourly,currently,alerts&units=auto";
                final String weatherHtm = Jsoup.connect(weatherUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36")
                        .ignoreContentType(true)
                        .execute()
                        .body();
                Log.e(TAG, weatherUrl);
                Log.e(TAG, weatherHtm);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.weatherLayout);
                            ImageView iconImage = (ImageView) findViewById(R.id.imageView);
                            TextView alertsText = (TextView) findViewById(R.id.textViewAlerts);
                            TextView cityName = (TextView) findViewById(R.id.textViewCityName);
                            TextView tempHigh = (TextView) findViewById(R.id.textViewTempHigh);
                            TextView tempMin = (TextView) findViewById(R.id.textViewTempMin);
                            TextView date = (TextView) findViewById(R.id.textViewDate);

                            JSONObject resJson = new JSONObject(weatherHtm);
                            String high = String.valueOf(resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("temperatureHigh"));
                            String min = String.valueOf(resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("temperatureMin"));
                            String icon, alerts;

                            try {
                                icon = resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getString("icon");
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                icon = "clear-day";
                            }
                            try {
                                alerts =  "預估" + resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getString("summary");
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                alerts = "";
                            }

                            Log.d(TAG,  alerts);
                            Log.d(TAG,  "最高溫度:"+ high);
                            Log.d(TAG,  "最低溫度:"+ min);
                            Log.d(TAG,  "icon:"+ icon);

                            alertsText.setText(alerts);
                            cityName.setText(searchCity);
                            tempHigh.setText("最高溫:" + high);
                            tempMin.setText("最低溫:" + min);
                            date.setText(searchDate);

                            switch (icon){
                                case "partly-cloudy-day":
                                    iconImage.setImageResource(R.drawable.sunnycloud);
                                    break;
                                case "clear-day":
                                    iconImage.setImageResource(R.drawable.sunny);
                                    break;
                                case "cloudy":
                                    iconImage.setImageResource(R.drawable.cloudy);
                                    break;
                                case "rain":
                                    iconImage.setImageResource(R.drawable.rain);
                                    break;
                            }
                            layout.setVisibility(View.VISIBLE);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadingDialog.dismiss();
        }
    }

    //取得Weather.com的網址
    /*public String getWeatherUrl(double Lat, double Lon) {
        return "https://weather.com/zh-TW/weather/today/l/" + Lat + "," + Lon + "?par=google&temp=c";
    }*/

    //抓取區域名稱old
    /*private void findAreaName () {
        final Spinner AreaSpinner = (Spinner)findViewById(R.id.SpinnerArea);

        ArrayList<String> list;//裝排序前的資料
        ArrayAdapter<String> country_arr;//裝排序後的資料

        //找出地區名稱
        Elements elements = doc.select("body > table > tbody > tr > td > table > tbody > tr >td > table > tbody"); //找出class = word的table
        list = new ArrayList<String>();
        for(Element element : elements)
        {
            list.add(element.select("tr").get(0).select("td").text());
        }
        country_arr = new ArrayAdapter<String>(testMainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,list); // 建一個給spinner放元素的ArrayAdapter
        AreaSpinner.setAdapter(country_arr);
        AreaSpinner.setSelection(1);
    }*/

    //監聽AreaSpinner選擇----查詢地區old
    /*private AdapterView.OnItemSelectedListener AreaSpinnerSelected = new AdapterView.OnItemSelectedListener() { // 監聽是否有選擇Spinner
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            final Spinner CitySpinner = (Spinner)findViewById(R.id.SpinnerCity);
            ArrayList<String> list = new ArrayList<>();
            Log.e(TAG, "Selected " + Long.toString(parentView.getItemIdAtPosition(position)));
            Elements elements = doc.select("body > table > tbody > tr > td > table > tbody > tr >td > table").get(position).select("tr").next().next();
            //Log.e(TAG, elements.get(position).select("tr").get(2).text());
            list.clear();
            for(Element element : elements)
            {
                list.add(element.select("td").get(1).text() + " " + element.select("td").get(0).text());
            }
            Collections.sort(list); // 排序
            ArrayAdapter<String> city_arr = new ArrayAdapter<String>(testMainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,list); // 建一個給spinner放元素的ArrayAdapter
            CitySpinner.setAdapter(city_arr);
            CitySpinner.setSelection(3);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {

        }
    };*/

    //查找天氣狀況--weather.com
    /*Runnable weatherSearchTask = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e(TAG, Double.toString(latLng.latitude) + " " + Double.toString(latLng.longitude));
                final String weatherUrl = getWeatherUrl(latLng.latitude, latLng.longitude);
                Log.e(TAG, weatherUrl);
                //final Document weatherHtm = Jsoup.parse(getWeatherUrl(latLng.latitude, latLng.longitude));
                final Document weatherHtm = Jsoup.connect(weatherUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36")
                        .get();
                Log.e(TAG, weatherHtm.select("div[class=today_nowcard-temp]").text());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, weatherHtm.select("div[class=today_nowcard-temp]").text());
                        TextView tv = (TextView) findViewById(R.id.content);
                        tv.setText("(" + weatherHtm.select("p[class=today_nowcard-timestamp] > span").text() + "發布)" +
                                "\n地區溫度：" + weatherHtm.select("div[class=today_nowcard-temp] > span").text() +
                                "\n體感溫度：" + weatherHtm.select("span[class=deg-feels]").text() +
                                "\n    濕度：" + weatherHtm.select("div[class=today_nowcard-sidecar component panel] > table > tbody > tr").get(1).select("td").text() +
                                "\n\n" + weatherHtm.select("div[id=dp0-details] > span").text());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };*/

    //監聽EditTextDate點擊
    private EditText.OnClickListener EditTextDateClicked = new EditText.OnClickListener() {
        @Override
        public void onClick(View v) {
            int year, month, day;
            final EditText et = (EditText)findViewById(v.getId());
            final EditText sted = (EditText)findViewById(R.id.editTextStartDate);
            final EditText eded = (EditText)findViewById(R.id.editTextEndDate);
            Calendar calendar = Calendar.getInstance(Locale.CHINESE);
            if(v.getId() == R.id.editTextEndDate && !sted.getText().toString().matches(""))
            {
                year = Integer.parseInt(sted.getText().toString().split("-")[0]);
                month = Integer.parseInt(sted.getText().toString().split("-")[1]) - 1;
                day = Integer.parseInt(sted.getText().toString().split("-")[2]);
            }

            else if(!et.getText().toString().matches(""))
            {
                year = Integer.parseInt(et.getText().toString().split("-")[0]);
                month = Integer.parseInt(et.getText().toString().split("-")[1]) - 1;
                day = Integer.parseInt(et.getText().toString().split("-")[2]);
            }
            else
            {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }
            new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String datetime = String.valueOf(year)+'-'+String.valueOf(month+1)+'-'+String.valueOf(dayOfMonth);
                    et.setText(datetime);
                    if((!sted.getText().toString().matches("") && !eded.getText().toString().matches("")) && (toUnixTime(eded.getText().toString()) < toUnixTime(sted.getText().toString())))
                        if(et.getId() == R.id.editTextStartDate)
                            eded.setText(datetime);
                        else
                            sted.setText(datetime);
                    searchDate = sted.getText().toString();
                    weatherSearchAPITask weather = new weatherSearchAPITask(chineseName, searchDate, latLng.latitude, latLng.longitude);
                    loadingDialog.showloadingDialog(getParent());
                    new Thread(weather).start();

                }
            }, year, month, day).show();
        }
    };

    private long toUnixTime(String date){
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd"); //日期格式化
            return sd.parse(date).getTime() / 1000L;
        }catch (ParseException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}


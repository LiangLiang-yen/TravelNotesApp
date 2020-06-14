package com.TravelNotes.app.ui.TravelNotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.TravelNotes.app.DataBase.CountyItemDAO;
import com.TravelNotes.app.Event;
import com.TravelNotes.app.Item.CountyItem;
import com.TravelNotes.app.NetworkClass;
import com.TravelNotes.app.R;
import com.TravelNotes.app.ui.TravelNotes.main.PlannerFragment;
import com.TravelNotes.app.ui.TravelNotes.page1.Page1Fragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TravelNotesFragment extends Fragment implements LocationListener {
    private static final int DO_TASK = 0;
    private static String TAG = "main";
    private static Boolean isConnect = false;
    public static Boolean dataBaseIsCurrent = false;
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final String HANDLER_THREAD_ID = "1011";
    private static Boolean getServices = false;

    private View view;

    private LocationManager locationManager;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private Handler mHandler;
    private HandlerThread mThread;
    private Event loadingDialog;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private CountyItemDAO countyItemDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        Event.debug = true; //開啟debug模式

        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //初始化執行緒
        mThread = new HandlerThread(HANDLER_THREAD_ID);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());

        //設定初始Fragment
        manager = getChildFragmentManager();
        transaction = manager.beginTransaction();
        PlannerFragment pFragment = PlannerFragment.newInstance();
        transaction.replace(R.id.fragmentView, pFragment,"mainFragment");
        transaction.commit();

        //初始化DataBase
        countyItemDAO = new CountyItemDAO(view.getContext());
        if (countyItemDAO.isEmpty()) {
            Log.e("loacl", "Database is empty");
            dataBaseIsCurrent = false;
            citySqlSet();
        }else{
            dataBaseIsCurrent = true;
        }

        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        initialization();
        return view;
    }

    /**
     * initialization onCreate
     */
    private void initialization() {
        /**
         *Action Bar transparent
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0
            setImageView();
        }

        //網路確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // API >=5.0
            ConnectivityManager connectivityManager = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().
                    build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Event.info(TAG, "Connected!");
                    isConnect = true;
                    headerTASK();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    Event.info(TAG, "Not Connected!");
                    isConnect = false;
                    headerTASK();
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Event.info(TAG, "Not Connected!");
                    isConnect = false;
                    headerTASK();
                }
            });
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // API < 5.0
            if (isNetworkConnected())
                isConnect = true;
            else
                isConnect = false;
            headerTASK();
        }

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(view.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    /**
     * Set Hearder
     * if Network not connect set ImageView is invisable
     * else run a new thread to get weather data
     */
    private void header() {
        if (isConnect) {
            if(loadingDialog != null)
                loadingDialog.dismiss();
            loadingDialog = new Event(view.getContext());
            loadingDialog.showloadingDialog(requireActivity());
            LatLng latLng = new LatLng(25.0342218,121.5622723);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd"); //日期格式化
            String today = sd.format(Calendar.getInstance().getTime());
            weatherSearchAPITask weather = new weatherSearchAPITask("台北市", today, new LatLng(25.0342218,121.5622723));
            mHandler.post(weather);
            locationInit();
        }else{
            TextView textView = (TextView)view.findViewById(R.id.textViewHeaderText);
            ConstraintLayout layout = (ConstraintLayout) view.findViewById(R.id.weatherLayout);
            TextView headerText = (TextView) view.findViewById(R.id.textViewHeaderText);
            textView.setText("網路連接失敗");
            layout.setVisibility(View.INVISIBLE);
            headerText.setVisibility(View.VISIBLE);
        }
    }

    /**
     *Web Crawler City data to SQL
     */
    private void citySqlSet() {
        final String url = "http://www.ting.com.tw/tour-info/air-name.htm"; //查詢地區級城市的網址
        Log.e(TAG,"Thread is called.");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final String html = NetworkClass.getHtml(url);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            addTaiwanCity();
                            Document doc = Jsoup.parse(html);
                            int index = 0;
                            Elements AreaElements = doc.select("body > table > tbody > tr > td > table > tbody > tr >td > table > tbody"); //找出class = word的table
                            for(Element element : AreaElements) {
                                String Area = element.select("tr").get(0).select("td").text();
                                Log.d(TAG, "Index:" + index + " " + Area + '\n');
                                if (index == 1)
                                    Area = "港澳地區";
                                Elements CityElements = doc.select("body > table > tbody > tr > td > table > tbody > tr >td > table").get(index++).select("tr").next().next();
                                for (Element cityelement : CityElements) {
                                    String cityName = cityelement.select("td").get(1).text();
                                    String chineseName = cityelement.select("td").get(0).text();
                                    Log.d(TAG, "Area:" + Area + " City:" + cityName + " Chinese:" + chineseName + '\n');

                                    CountyItem item = new CountyItem(0, Area, cityName, chineseName);
                                    countyItemDAO.insert(item);
                                }
                                dataBaseIsCurrent = true;
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addTaiwanCity(){
        final String Area = "台灣地區";
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("Keelung","基隆市");
        dict.put("Taipei","臺北市");
        dict.put("New Taipei","新北市");
        dict.put("Taoyuan","桃園市");
        dict.put("Hsinchu City","新竹市");
        dict.put("Hsinchu County","新竹縣");
        dict.put("Miaoli","苗栗縣");
        dict.put("Taichung","臺中市");
        dict.put("Changhua","彰化縣");
        dict.put("Nantou","南投縣");
        dict.put("Yunlin","雲林縣");
        dict.put("Chiayi City","嘉義市");
        dict.put("Chiayi County","嘉義縣");
        dict.put("Tainan","臺南市");
        dict.put("Kaohsiung","高雄市");
        dict.put("Pingtung","屏東縣");
        dict.put("Taitung","臺東縣");
        dict.put("Hualien","花蓮縣");
        dict.put("Yilan","宜蘭縣");
        dict.put("Penghu","澎湖縣");
        dict.put("Kinmen","金門縣");
        dict.put("Lienchiang","連江縣");

        for(Map.Entry<String, String> entry : dict.entrySet()){
            String cityName = entry.getKey();
            String chineseName = entry.getValue();

            Log.d(TAG,"Area:" + Area + " City:" + cityName + " Chinese:" + chineseName + '\n');
            CountyItem item = new CountyItem(0, Area, cityName, chineseName);
            countyItemDAO.insert(item);
        }
    }

    /**
     * Let imageview to extend to Action bar.
     * @notice this method have to be used after setContentView.
     */
    private void setImageView() {
        // Set the padding to match the Status Bar height
        ImageView imageView = (ImageView) view.findViewById(R.id.imageViewBG);
        Guideline guideline = (Guideline) view.findViewById(R.id.guideline);
        imageView.getLayoutParams().height = imageView.getLayoutParams().height + getActionBarHeight();
        guideline.setGuidelineBegin(getActionBarHeight());
    }

    /**
     *
     * @return height of action bar
     */
    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (view.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return -1;
    }

    /**
     * API level < 23 NetworkCheck
     * @return Ture or False
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * SendMessage to Handler
     */
    private void headerTASK(){
        Message msg = new Message();
        msg.what = DO_TASK;
        messageHandler.sendMessage(msg);
    }

    Handler messageHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DO_TASK:
                    header();
                    break;
            }
        }
    };

    /**
     * Location Manager Set
     */
    private void locationInit(){
        getServices = false;
        String provider;
        locationManager = (LocationManager) view.getContext().getSystemService(view.getContext().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
            provider = LocationManager.GPS_PROVIDER;
        }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
            provider = LocationManager.NETWORK_PROVIDER;
        }else{
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        getLocation(location);
    }

    private void getLocation(Location location){
        if(location != null){
            String cityName = "NULL";
            try {
                Geocoder geocoder = new Geocoder(view.getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if(addresses.size() == 0)
                    return;
                if(addresses.get(0).getAdminArea() != null && addresses.get(0).getLocality() != null)
                    cityName = addresses.get(0).getAdminArea() + " " + addresses.get(0).getLocality();
                else if(addresses.get(0).getAdminArea() != null)
                    cityName = addresses.get(0).getAdminArea();
                else if(addresses.get(0).getCountryName() != null)
                    cityName = addresses.get(0).getCountryName();
                else
                    cityName = "NULL";
            }catch (IOException e){
                cityName = "NULL";
                e.printStackTrace();
            }
            Event.info(TAG, "Location changed call " + location.getLatitude() + "," + location.getLongitude());
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd"); //日期格式化
            String today = sd.format(Calendar.getInstance().getTime());
            weatherSearchAPITask weather = new weatherSearchAPITask(cityName, today, new LatLng(location.getLatitude(),location.getLongitude()));
            mHandler.post(weather);
        }
    }

    /**
     * Ues Dark Sky API Search to Search weather
     */
    public class weatherSearchAPITask implements Runnable{
        private String searchDate;
        private String searchCity;
        private LatLng latLng;
        private Handler handler;

        /**
         * constructor
         * @param searchCity 城市名稱
         * @param searchDate 搜尋日期
         * @param latLng 經緯度
         */
        weatherSearchAPITask(@NonNull String searchCity,@NonNull String searchDate,@NonNull final LatLng latLng)
        {
            this.searchCity = searchCity;
            this.searchDate = searchDate;
            this.latLng = latLng;
            this.handler = new Handler();
        }
        @Override
        public void run() {
            try {
                Event.info(TAG, Double.toString(latLng.latitude) + " " + Double.toString(latLng.longitude));

                //取得Unix 時間戳記
                long InputTime = toUnixTime(searchDate);

                final String weatherUrl = "https://api.darksky.net/forecast/2ba39795402afeaddbb95afdd5b1d1cc/" + latLng.latitude + "," + latLng.longitude + "," + InputTime + "?lang=zh-tw&exclude=minutely,hourly,currently,alerts&units=auto";
                final String weatherHtm = Jsoup.connect(weatherUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36")
                        .ignoreContentType(true)
                        .execute()
                        .body();
                Event.info(TAG, weatherUrl);
                Event.info(TAG, weatherHtm);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConstraintLayout layout = (ConstraintLayout) view.findViewById(R.id.weatherLayout);
                            TextView headerText = (TextView) view.findViewById(R.id.textViewHeaderText);

                            ImageView imageBG = (ImageView) view.findViewById(R.id.imageViewBG);
                            ImageView imageIcon = (ImageView) view.findViewById(R.id.imageViewIcon);
                            TextView Temp = (TextView) view.findViewById(R.id.textViewTemp);
                            TextView alertsText = (TextView) view.findViewById(R.id.textViewAlerts);
                            TextView humidity = (TextView) view.findViewById(R.id.textViewHumidity);
                            TextView cityName = (TextView) view.findViewById(R.id.textViewName);
                            TextView date = (TextView) view.findViewById(R.id.textViewDate);
                            TextView link = (TextView) view.findViewById(R.id.textViewLink);

                            JSONObject resJson = new JSONObject(weatherHtm);
                            String high = resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("temperatureHigh") + "˚c";
                            String min = resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("temperatureMin") + "˚c";
                            String humidityPercent = "濕度: " + (int)(resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("humidity")*100) + "%";
                            String linkURL = "https://darksky.net/forecast/" + latLng.latitude + "," + latLng.longitude + "ca12/zh-tw";
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
                                alerts =  resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getString("summary");
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                alerts = "";
                            }

                            Event.info(TAG,  alerts);
                            Event.info(TAG,  "溫度:"+ high + "/" + min);
                            Event.info(TAG,  "icon:"+ icon);
                            Event.info(TAG, linkURL);

                            alertsText.setText(alerts);
                            humidity.setText(humidityPercent);
                            cityName.setText(searchCity);
                            Temp.setText(high + "/" + min);
                            date.setText(searchDate);
                            link.setText(Html.fromHtml("<a href='"+ linkURL +"'>DarkSky</a>"));
                            link.setMovementMethod(LinkMovementMethod.getInstance());

                            setTextColorToWhite(false);

                            switch (icon){
                                case "partly-cloudy-day":
                                    imageIcon.setImageResource(R.drawable.sunnycloud);
                                    imageBG.setImageResource(R.drawable.bg);
                                    break;
                                case "clear-day":
                                    imageIcon.setImageResource(R.drawable.sunny);
                                    imageBG.setImageResource(R.drawable.bg);
                                    break;
                                case "cloudy":
                                    imageIcon.setImageResource(R.drawable.cloudy);
                                    imageBG.setImageResource(R.drawable.bg);
                                    break;
                                case "rain":
                                    imageIcon.setImageResource(R.drawable.rain);
                                    imageBG.setImageResource(R.drawable.bg_rain);
                                    setTextColorToWhite(true);
                                    break;
                                case "thunderstorm":
                                    imageIcon.setImageResource(R.drawable.thunder);
                                    imageBG.setImageResource(R.drawable.bg_rain);
                                    setTextColorToWhite(true);
                                    break;
                            }
                            layout.setVisibility(View.VISIBLE);
                            headerText.setVisibility(View.INVISIBLE);
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
            if(loadingDialog != null)
                loadingDialog.dismiss();
        }

        private void setTextColorToWhite(Boolean isWhite){
            TextView Temp = (TextView) view.findViewById(R.id.textViewTemp);
            TextView alertsText = (TextView) view.findViewById(R.id.textViewAlerts);
            TextView humidity = (TextView) view.findViewById(R.id.textViewHumidity);
            TextView cityName = (TextView) view.findViewById(R.id.textViewName);
            TextView date = (TextView) view.findViewById(R.id.textViewDate);
            TextView link = (TextView) view.findViewById(R.id.textViewLink);
            if(isWhite){
                Temp.setTextAppearance(R.style.imageText_White);
                alertsText.setTextAppearance(R.style.imageText_White);
                humidity.setTextAppearance(R.style.imageText_White);
                cityName.setTextAppearance(R.style.imageTitle_White);
                date.setTextAppearance(R.style.imageBanner_White);
                link.setTextAppearance(R.style.imageBanner_White);
            }else{
                Temp.setTextAppearance(R.style.imageText);
                alertsText.setTextAppearance(R.style.imageText);
                humidity.setTextAppearance(R.style.imageText);
                cityName.setTextAppearance(R.style.imageTitle);
                date.setTextAppearance(R.style.imageBanner);
                link.setTextAppearance(R.style.imageBanner);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {//定位狀態改變

    }

    @Override
    public void onProviderEnabled(String provider) {//當GPS或網路定位功能開啟

    }

    @Override
    public void onProviderDisabled(String provider) {//當GPS或網路定位功能關閉時
        Event.info(TAG, "Location Listener Closed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(view.getContext()).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                }
                break;
        }
    }

    public static long toUnixTime(String date){
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd"); //日期格式化
            return sd.parse(date).getTime() / 1000L;
        }catch (ParseException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    public void changeFragment(long key, String planName, String city, String date){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment;
        fragment = Page1Fragment.newInstance(key, planName, city, date);
        transaction.addToBackStack(fragment.getClass().getName());
        if (!fragment.isAdded()) {	// 先判断是否被add過 如果沒有Add過 代表是第一次呼叫 則需要先add 其餘時候都直接使用show進行顯示
            // hide裡面放的是自己當前所在的Fragment頁面，後面一定要.this才能夠指向現在要隱藏的頁面，否則會直接幫你生成一個新的頁面
            transaction.hide(TravelNotesFragment.this).add(R.id.fragmentView, fragment).commitAllowingStateLoss(); // 隱藏當前頁面 並新增明細頁面
        } else {
            transaction.hide(TravelNotesFragment.this).show(fragment).commit(); //隱藏當前頁面 呼叫明細頁面
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(loadingDialog != null)
            if (loadingDialog.isShowing())
                loadingDialog.dismiss();
    }
}

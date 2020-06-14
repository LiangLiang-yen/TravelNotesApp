package com.TravelNotes.app.ui.TravelNotes.page1;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.TravelNotes.app.Event;
import com.TravelNotes.app.R;
import com.TravelNotes.app.ui.TravelNotes.TravelNotesFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;

public class Page1Fragment extends Fragment {
    private static final String KEY_ID = "keyId";
    private static final String PLAN_NAME = "planName";
    private static final String CITY_NAME = "cityName";
    private static final String DATE = "date";
    private static String TAG = "main2";
    private long key = 0;
    private String city = "";
    private String date = "";

    private View view;

    private TabLayout tabLayout;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private Event loadingDialog;

    public static Page1Fragment newInstance(long key, String planName, String city, String date){
        Page1Fragment fragment = new Page1Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(PLAN_NAME, planName);
        bundle.putLong(KEY_ID, key);
        bundle.putString(CITY_NAME, city);
        bundle.putString(DATE, date);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main2, container, false);
        key = getArguments().getLong(KEY_ID, 0);
        city = getArguments().getString(CITY_NAME);
        date =  getArguments().getString(DATE);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getArguments().getString(PLAN_NAME));

        setImageView();
        initScreen();
        loadingDialog.showloadingDialog(requireActivity());
        getLatlng(city, date);

        return view;
    }

    private void initScreen() {
        // Creating the ViewPager container fragment
        loadingDialog = new Event(view.getContext());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        pager = (ViewPager)view.findViewById(R.id.pages);
        adapter = new ViewPagerAdapter(getChildFragmentManager(), key);
        ConstraintLayout layout = (ConstraintLayout) view.findViewById(R.id.weatherLayout);
        layout.setVisibility(View.INVISIBLE);
        pager.setVisibility(View.INVISIBLE);

        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
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

    private void getLatlng(String city, String date){
        LatLng latLng = null;
        List<Address> addressList = null;
        try {
            //if java.io.IOException: grpc failed try Change maxResults number or Reopen wifi
            addressList =new Geocoder(view.getContext()).getFromLocationName(city, 6);
            Address useraddress = addressList.get(0);
            latLng = new LatLng(useraddress.getLatitude(), useraddress.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            pager.setVisibility(View.VISIBLE);
        }
        weatherSearchAPITask weather = new weatherSearchAPITask(city, date, latLng);
        new Thread(weather).start();
    }

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
        weatherSearchAPITask(@NonNull String searchCity, @NonNull String searchDate, @NonNull final LatLng latLng)
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
                long InputTime = TravelNotesFragment.toUnixTime(searchDate);

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
                            TextView Temp = (TextView) view.findViewById(R.id.textViewTemp);
                            TextView humidity = (TextView) view.findViewById(R.id.textViewHumidity);
                            TextView cityName = (TextView) view.findViewById(R.id.textViewName);
                            TextView date = (TextView) view.findViewById(R.id.textViewDate);

                            JSONObject resJson = new JSONObject(weatherHtm);
                            String high = resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("temperatureHigh") + "˚c";
                            String min = resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("temperatureMin") + "˚c";
                            String humidityPercent = "濕度: " + (int)(resJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0).getDouble("humidity")*100) + "%";

                            Event.info(TAG,  "溫度:"+ high + "/" + min);

                            humidity.setText(humidityPercent);
                            cityName.setText(searchCity);
                            Temp.setText(high + "/" + min);
                            date.setText(searchDate);

                            setTextColorToWhite(false);

                            layout.setVisibility(View.VISIBLE);
                            pager.setVisibility(View.VISIBLE);
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

        private void setTextColorToWhite(Boolean isWhite){
            TextView Temp = (TextView) view.findViewById(R.id.textViewTemp);
            TextView humidity = (TextView) view.findViewById(R.id.textViewHumidity);
            TextView cityName = (TextView) view.findViewById(R.id.textViewName);
            TextView date = (TextView) view.findViewById(R.id.textViewDate);
            if(isWhite){
                Temp.setTextAppearance(R.style.imageText_White);
                humidity.setTextAppearance(R.style.imageText_White);
                cityName.setTextAppearance(R.style.imageTitle_White);
                date.setTextAppearance(R.style.imageBanner_White);
            }else{
                Temp.setTextAppearance(R.style.imageText);
                humidity.setTextAppearance(R.style.imageText);
                cityName.setTextAppearance(R.style.imageTitle);
                date.setTextAppearance(R.style.imageBanner);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.menu_travelnotes));
    }
}
package com.finalproject.app.Item;

public class CountyItem implements java.io.Serializable {
    private long id;
    private String Area;
    private String cityName;
    private String chineseName;
    public CountyItem(){

    }
    public CountyItem(long id, String Area, String cityName, String chineseName){
        this.id = id;
        this.Area = Area;
        this.cityName = cityName;
        this.chineseName = chineseName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setArea(String area) {
        Area = area;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public long getId() {
        return id;
    }

    public String getArea() {
        return Area;
    }

    public String getCityName() {
        return cityName;
    }

    public String getChineseName() {
        return chineseName;
    }
}

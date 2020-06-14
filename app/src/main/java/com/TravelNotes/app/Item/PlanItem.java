package com.TravelNotes.app.Item;

import android.graphics.Color;

import java.util.Random;

public class PlanItem implements java.io.Serializable,Cloneable {
    private long id;
    private long key;
    private int color;
    private String planName;
    private String continent;
    private String cityName;
    private String startDate;
    private String endDate;
    private int continentpPosition;
    private int cityNamePosition;
    public PlanItem(){
        this.id = 0;
        Random rnd = new Random();
        String colorStr[] = {"#1F98D9", "#20C781", "#87DB2C", "#ACFA00", "#22FFCE", "#BB9C7F", "#BB9C7F", "#ABD916", "#ABD916", "#ABD916", "#B1B5C2"};
        this.key = 0;
        this.color = Color.parseColor(colorStr[rnd.nextInt(colorStr.length)]);
        this.continent = "";
        this.planName = "";
        this.cityName = "";
        this.startDate = "";
        this.endDate = "";
        this.continentpPosition = 0;
        this.cityNamePosition = 0;
    }
    public PlanItem(long id, long key, String planName, String continent, String cityName, String startDate, String endDate, int continentpPosition, int cityNamePosition){
        this.id = id;
        Random rnd = new Random();
        String colorStr[] = {"#1F98D9", "#20C781", "#87DB2C", "#ACFA00", "#22FFCE", "#BB9C7F", "#BB9C7F", "#ABD916", "#ABD916", "#ABD916", "#B1B5C2"};
        this.key = key;
        this.color = Color.parseColor(colorStr[rnd.nextInt(colorStr.length)]);
        this.continent = continent;
        this.planName = planName;
        this.cityName = cityName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.continentpPosition = continentpPosition;
        this.cityNamePosition = cityNamePosition;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public void setContinentpPosition(int continentpPosition) {
        this.continentpPosition = continentpPosition;
    }

    public void setCityNamePosition(int cityNamePosition) {
        this.cityNamePosition = cityNamePosition;
    }

    public long getId() {
        return id;
    }

    public String getPlanName() {
        return planName;
    }

    public String getCityName() {
        return cityName;
    }

    public int getColor() {
        return color;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getContinent() {
        return continent;
    }

    public int getContinentpPosition() {
        return continentpPosition;
    }

    public int getCityNamePosition() {
        return cityNamePosition;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public void swap(PlanItem item) throws CloneNotSupportedException {
        PlanItem cache = (PlanItem) this.clone();
        this.key = item.key;
        this.color = item.color;
        this.continent = item.continent;
        this.planName = item.planName;
        this.cityName = item.cityName;
        this.startDate = item.startDate;
        this.endDate = item.endDate;
        this.continentpPosition = item.continentpPosition;
        this.cityNamePosition = item.cityNamePosition;

        item.key = cache.key;
        item.color = cache.color;
        item.continent = cache.continent;
        item.planName = cache.planName;
        item.cityName = cache.cityName;
        item.startDate = cache.startDate;
        item.endDate = cache.endDate;
        item.continentpPosition = cache.continentpPosition;
        item.cityNamePosition = cache.cityNamePosition;
    }
}

package com.TravelNotes.app.Item;

public class ListItem implements java.io.Serializable {

    private long id;
    private long key;
    private String name;
    private int count;
    private String unit;
    private boolean selected = false;

    public ListItem(long key){
        this.id = 0;
        this.key = key;
        this.name = "";
        this.count = 1;
        this.unit = "個";
        this.selected = false;
    }

    public ListItem(long key, long id, String name, int count, String unit){
        this.id = id;
        this.key = key;
        this.name = name;
        this.count = count;
        this.unit = unit;
        this.selected = false;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public ListItem clone(){
        ListItem newItem = new ListItem(key);
        newItem.id = id;
        newItem.name = name;
        newItem.count = count;
        newItem.selected = selected;
        newItem.key = key;
        newItem.unit = unit;
        return newItem;
    }
}

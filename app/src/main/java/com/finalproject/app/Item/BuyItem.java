package com.finalproject.app.Item;

public class BuyItem implements java.io.Serializable {

    private long id;
    private long key;
    private String name;
    private int price;
    private int count;
    private int money;

    public BuyItem(long key){
        this.id = 0;
        this.key = key;
        this.name = "";
        this.price = 0;
        this.count = 1;
        this.money = 0;
    }

    public BuyItem(long key, long id, String name, int price, int count){
        this.id = id;
        this.key = key;
        this.name = name;
        this.price = price;
        this.count = count;
        this.money = price*count;
    }

    public int updateMoney(){
        money = price*count;
        return money;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public BuyItem clone(){
        BuyItem newItem = new BuyItem(key);
        newItem.id = id;
        newItem.key = key;
        newItem.name = name;
        newItem.price = price;
        newItem.count = count;
        newItem.money = money;
        return newItem;
    }
}

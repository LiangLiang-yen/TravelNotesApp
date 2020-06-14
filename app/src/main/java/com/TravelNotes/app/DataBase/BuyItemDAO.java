package com.TravelNotes.app.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.TravelNotes.app.Item.BuyItem;

import java.util.ArrayList;
import java.util.List;

public class BuyItemDAO {
    Context context = null;
    private long key = 0;

    // 表格名稱
    private static final String TABLE_NAME = "buylist";

    // 編號表格欄位名稱，固定不變
    private static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    private static final String KEY_COLUMN = "key";
    private static final String NAME_COLUMN = "name";
    private static final String PRICE_COLUMN = "price";
    private static final String COUNT_COLUMN = "count";
    private static final String MONEY_COLUMN = "money";

    // 使用上面宣告的變數建立表格的SQL指令
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_COLUMN + " INTEGER NOT NULL, " +
                    NAME_COLUMN + " TEXT NOT NULL, " +
                    PRICE_COLUMN + " INTEGER NOT NULL, " +
                    COUNT_COLUMN  + " INTEGER NOT NULL, " +
                    MONEY_COLUMN + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public BuyItemDAO(Context context, long key) {
        this.context = context;
        this.key = key;
        db = MyDBHelper.getDatabase(context);
        if(checkTableNotEXIST())
            createTable();
    }

    private void createTable(){
        db.execSQL(CREATE_TABLE);
    }

    private boolean checkTableNotEXIST(){
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE name ='"+ TABLE_NAME +"' and type='table'", null);
        if (cursor.getCount() == 0)
            return true;
        else
            return false;
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    public BuyItem insert(BuyItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_COLUMN, item.getKey());
        cv.put(NAME_COLUMN, item.getName());
        cv.put(PRICE_COLUMN, item.getPrice());
        cv.put(COUNT_COLUMN, item.getCount());
        cv.put(MONEY_COLUMN, item.getMoney());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }

    public void update(List<BuyItem> items){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_COLUMN + "='" + key + "'";

        for(BuyItem item: items){
            if(!update(item))
                insert(item);
        }
    }

    public boolean update(BuyItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_COLUMN, item.getKey());
        cv.put(NAME_COLUMN, item.getName());
        cv.put(PRICE_COLUMN, item.getPrice());
        cv.put(COUNT_COLUMN, item.getCount());
        cv.put(MONEY_COLUMN, item.getMoney());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定key的資料
    public boolean delete(){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_COLUMN + "=" + key;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(BuyItem item){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + item.getId();
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    public List<BuyItem> getData(){
        List<BuyItem> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_COLUMN +"='" + key + "'", null);
        while (cursor.moveToNext())
        {
            result.add(getRecord(cursor));
        }
        // 關閉Cursor物件
        cursor.close();
        // 回傳結果
        return result;
    }

    public int getTotalMoney(){
        int total = 0;
        List<BuyItem> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_COLUMN +"='" + key + "'", null);
        while (cursor.moveToNext())
        {
            result.add(getRecord(cursor));
        }
        // 關閉Cursor物件
        cursor.close();
        for(BuyItem item : result)
            total += item.getMoney();
        return total;
    }

    // 讀取所有記事資料
    public List<BuyItem> getAll() {
        List<BuyItem> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 把Cursor目前的資料包裝為物件
    public BuyItem getRecord(Cursor cursor) {
        BuyItem result = new BuyItem(key);

        result.setId(cursor.getLong(0));
        result.setKey(cursor.getLong(1));
        result.setName(cursor.getString(2));
        result.setPrice(cursor.getInt(3));
        result.setCount(cursor.getInt(4));
        result.setMoney(cursor.getInt(5));

        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

    public boolean isEmpty() {
        Cursor cursor;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + KEY_COLUMN + "='" + key + "'", null);
            cursor.moveToFirst();
        }catch (Exception e){
            return true;
        }
        if(cursor.getInt(0) > 0)
            return false;
        else
            return true;
    }

    // 建立範例資料
    public void sample() {
        BuyItem item = new BuyItem(key, 0, "衣服", 5000, 3);
        BuyItem item2 = new BuyItem(key, 1, "手機", 40000, 2);

        insert(item);
        insert(item2);
    }
}

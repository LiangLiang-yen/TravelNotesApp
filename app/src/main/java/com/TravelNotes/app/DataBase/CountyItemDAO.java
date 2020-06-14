package com.TravelNotes.app.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.TravelNotes.app.Item.CountyItem;

import java.util.ArrayList;
import java.util.List;

public class CountyItemDAO {
    // Context
    Context context = null;

    // 表格名稱
    public static final String TABLE_NAME = "city";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String AREA_COLUMN = "Area";
    public static final String CITYNAME_COLUMN = "cityName";
    public static final String CHINESENAME_COLUMN = "chineseName";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
             KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
             AREA_COLUMN + " TEXT NOT NULL, " +
             CITYNAME_COLUMN  + " TEXT NOT NULL, " +
             CHINESENAME_COLUMN + " TEXT NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public CountyItemDAO(Context context) {
        this.context = context;
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

    public CountyItem insert(CountyItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(AREA_COLUMN, item.getArea());
        cv.put(CITYNAME_COLUMN, item.getCityName());
        cv.put(CHINESENAME_COLUMN, item.getChineseName());

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

    public boolean update(CountyItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(AREA_COLUMN, item.getArea());
        cv.put(CITYNAME_COLUMN, item.getCityName());
        cv.put(CHINESENAME_COLUMN, item.getChineseName());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 讀取所有記事資料
    public List<CountyItem> getAll() {
        List<CountyItem> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public CountyItem get(long id) {
        // 準備回傳結果用的物件
        CountyItem item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    //取得AreaColumn的所有的文字(不重複)
    public List<String> getArea() {
        // 準備回傳結果用的物件
        List<String> areaArr = new ArrayList<>();
        // 使用編號為查詢條件
        String arg = "DISTINCT";
        // 執行查詢
        Cursor result = db.rawQuery("SELECT DISTINCT " + AREA_COLUMN + " FROM " + TABLE_NAME, null);
        while (result.moveToNext())
        {
                areaArr.add(result.getString(0));
        }
        // 關閉Cursor物件
        result.close();

        return areaArr;
    }

    //取得指定AreaColumn的資料(找城市)
    public List<CountyItem> getCity(String area) {
        // 準備回傳結果用的物件
        List<CountyItem> result = new ArrayList<>();
        // 執行查詢
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE Area='" + area + "' ORDER BY cityName ASC", null);
        while (cursor.moveToNext())
        {
            result.add(getRecord(cursor));
        }
        // 關閉Cursor物件
        cursor.close();
        // 回傳結果
        return result;
    }

    // 把Cursor目前的資料包裝為物件
    public CountyItem getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        CountyItem result = new CountyItem();

        result.setId(cursor.getLong(0));
        result.setArea(cursor.getString(1));
        result.setCityName(cursor.getString(2));
        result.setChineseName(cursor.getString(3));

        // 回傳結果
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
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
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
        CountyItem item = new CountyItem(0,"台港澳", "TAIPEI", "台北");
        CountyItem item2 = new CountyItem(0,"美洲", "NEWYORK", "紐約");
        CountyItem item3 = new CountyItem(0,"大陸", "CHENGTU", "成都");

        insert(item);
        insert(item2);
        insert(item3);
    }
}

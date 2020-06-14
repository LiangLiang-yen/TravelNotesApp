package com.TravelNotes.app.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.TravelNotes.app.Item.PlanItem;

import java.util.ArrayList;
import java.util.List;

public class PlanItemDAO {
    // Context
    Context context = null;

    // 表格名稱
    private static final String TABLE_NAME = "planner";

    // 編號表格欄位名稱，固定不變
    private static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    private static final String KEY_COLUMN = "key";
    private static final String COLOR_COLUMN = "color";
    private static final String PLANNAME_COLUMN = "planName";
    private static final String CONTINENT_COLUMN = "continent";
    private static final String CITYNAME_COLUMN = "cityName";
    private static final String STARTDATE_COLUMN = "startDate";
    private static final String ENDDATE_COLUMN = "endDate";
    private static final String CONTINENT_POSITION_COLUMN = "continentPosition";
    private static final String CITYNAME_POSITION_COLUMN = "cityNamePosition";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
             KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
             KEY_COLUMN + " INTEGER NOT NULL, " +
             COLOR_COLUMN + " INTEGER NOT NULL, " +
             PLANNAME_COLUMN + " TEXT NOT NULL, " +
             CONTINENT_COLUMN + " TEXT NOT NULL, " +
             CITYNAME_COLUMN  + " TEXT NOT NULL, " +
             STARTDATE_COLUMN + " TEXT NOT NULL, " +
             ENDDATE_COLUMN + " TEXT NOT NULL, " +
             CONTINENT_POSITION_COLUMN + " INTEGER NOT NULL, " +
             CITYNAME_POSITION_COLUMN + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public PlanItemDAO(Context context) {
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
        return cursor.getCount() == 0;
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    public PlanItem insert(PlanItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_COLUMN, 0);
        cv.put(COLOR_COLUMN, item.getColor());
        cv.put(PLANNAME_COLUMN, item.getPlanName());
        cv.put(CONTINENT_COLUMN, item.getContinent());
        cv.put(CITYNAME_COLUMN, item.getCityName());
        cv.put(STARTDATE_COLUMN, item.getStartDate());
        cv.put(ENDDATE_COLUMN, item.getEndDate());
        cv.put(CONTINENT_POSITION_COLUMN, item.getContinentpPosition());
        cv.put(CITYNAME_POSITION_COLUMN, item.getCityNamePosition());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);
        item.setKey(id);

        cv.put(KEY_COLUMN, item.getKey());
        String where = KEY_ID + "=" + item.getId();

        db.update(TABLE_NAME, cv, where, null);
        // 回傳結果
        return item;
    }

    public boolean update(PlanItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_COLUMN, item.getKey());
        cv.put(COLOR_COLUMN, item.getColor());
        cv.put(PLANNAME_COLUMN, item.getPlanName());
        cv.put(CONTINENT_COLUMN, item.getContinent());
        cv.put(CITYNAME_COLUMN, item.getCityName());
        cv.put(STARTDATE_COLUMN, item.getStartDate());
        cv.put(ENDDATE_COLUMN, item.getEndDate());
        cv.put(CONTINENT_POSITION_COLUMN, item.getContinentpPosition());
        cv.put(CITYNAME_POSITION_COLUMN, item.getCityNamePosition());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean update(List<PlanItem> items){
        // 刪除指定編號資料並回傳刪除是否成功
        for(PlanItem item: items){
            update(item);
        }
        return true;
    }

    // 刪除參數指定編號的資料
    public boolean delete(PlanItem item){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + item.getId();
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 取得指定編號的資料物件
    public PlanItem get(long id) {
        // 準備回傳結果用的物件
        PlanItem item = null;
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

    public List<PlanItem> getData(){
        List<PlanItem> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext())
        {
            result.add(getRecord(cursor));
        }
        // 關閉Cursor物件
        cursor.close();
        // 回傳結果
        return result;
    }

    public PlanItem getData(long itemId){
        PlanItem result = new PlanItem();
        String where = KEY_COLUMN + "=" + itemId;
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);
        while (cursor.moveToNext())
        {
            result = getRecord(cursor);
        }
        // 關閉Cursor物件
        cursor.close();
        // 回傳結果
        return result;
    }

    // 把Cursor目前的資料包裝為物件
    public PlanItem getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        PlanItem result = new PlanItem();

        result.setId(cursor.getLong(0));
        result.setKey(cursor.getLong(1));
        result.setColor(cursor.getInt(2));
        result.setPlanName(cursor.getString(3));
        result.setContinent(cursor.getString(4));
        result.setCityName(cursor.getString(5));
        result.setStartDate(cursor.getString(6));
        result.setEndDate(cursor.getString(7));
        result.setContinentpPosition(cursor.getInt(8));
        result.setCityNamePosition(cursor.getInt(9));

        // 回傳結果
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
}

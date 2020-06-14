package com.TravelNotes.app.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.TravelNotes.app.Item.ListItem;

import java.util.ArrayList;
import java.util.List;

public class ListItemDAO {
    Context context = null;
    private long key = 0;

    // 表格名稱
    public static final String TABLE_NAME = "baggage";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String KEY_COLUMN = "key";
    public static final String NAME_COLUMN = "name";
    public static final String COUNT_COLUMN = "count";
    public static final String UNIT_COLUMN = "unit";
    public static final String SELECTED_COLUMN = "selected";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_COLUMN + " TEXT NOT NULL, " +
                    NAME_COLUMN + " TEXT NOT NULL, " +
                    COUNT_COLUMN  + " TEXT NOT NULL, " +
                    UNIT_COLUMN + " TEXT NOT NULL, " +
                    SELECTED_COLUMN + " BOOLEAN NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public ListItemDAO(Context context, long key) {
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

    public ListItem insert(ListItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_COLUMN, item.getKey());
        cv.put(NAME_COLUMN, item.getName());
        cv.put(COUNT_COLUMN, item.getCount());
        cv.put(UNIT_COLUMN, item.getUnit());
        cv.put(SELECTED_COLUMN, item.isSelected());

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

    public void update(List<ListItem> items){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_COLUMN + "='" + key + "'";

        for(ListItem item: items){
            if(!update(item))
                insert(item);
        }
    }

    public boolean update(ListItem item){
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_COLUMN, item.getKey());
        cv.put(NAME_COLUMN, item.getName());
        cv.put(COUNT_COLUMN, item.getCount());
        cv.put(UNIT_COLUMN, item.getUnit());
        cv.put(SELECTED_COLUMN, item.isSelected());

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
    public boolean delete(ListItem item){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + item.getId();
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    public List<ListItem> getData(){
        List<ListItem> result = new ArrayList<>();
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

    // 讀取所有記事資料
    public List<ListItem> getAll() {
        List<ListItem> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 把Cursor目前的資料包裝為物件
    public ListItem getRecord(Cursor cursor) {
        ListItem result = new ListItem(key);

        result.setId(cursor.getLong(0));
        result.setKey(cursor.getLong(1));
        result.setName(cursor.getString(2));
        result.setCount(cursor.getInt(3));
        result.setUnit(cursor.getString(4));
        result.setSelected(cursor.getInt(5) > 0);

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
        ListItem item = new ListItem(key, 0, "背包", 6, "個");
        ListItem item2 = new ListItem(key, 1, "衣服", 3, "件");
        ListItem item3 = new ListItem(key, 2, "褲子", 3, "件");

        insert(item);
        insert(item2);
        insert(item3);
    }
}

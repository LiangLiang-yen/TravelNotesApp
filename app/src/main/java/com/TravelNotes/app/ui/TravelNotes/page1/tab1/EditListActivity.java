package com.TravelNotes.app.ui.TravelNotes.page1.tab1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.TravelNotes.app.Adapter.EditItemAdapter;
import com.TravelNotes.app.DataBase.ListItemDAO;
import com.TravelNotes.app.Item.ListItem;
import com.TravelNotes.app.R;
import com.TravelNotes.app.MoveItemHelper.SimpleItemTouchHelperCallback;

import java.util.List;

public class EditListActivity extends AppCompatActivity implements EditItemAdapter.OnStartDragListener {
    public static String ID_KEY = "planIdKey";
    private long key = 0;

    private CheckBox ckb;
    private Button cancelBtn;
    private Button submitBtn;
    private ImageButton addBtn;

    private RecyclerView recyclerView;
    private EditItemAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<ListItem> items;
    private ItemTouchHelper itemTouchHelper;
    private ListItemDAO listitemDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        Intent intent = getIntent();
        key = intent.getLongExtra(ID_KEY, 0);

        setListener();
        createItemList();
        buildRecyclerview();
    }

    private void setListener(){
        ckb = (CheckBox)findViewById(R.id.checkFastDelete);
        addBtn = (ImageButton) findViewById(R.id.buttonAdd);
        cancelBtn = (Button)findViewById(R.id.buttonCancel);
        submitBtn = (Button)findViewById(R.id.buttonSubmit);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditListActivity.this.finish();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add(new ListItem(key));
                mAdapter.notifyDataSetChanged();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listitemDB.update(items);
                EditListActivity.this.finish();
            }
        });
    }

    private void createItemList(){
        listitemDB = new ListItemDAO(this, key);
        if(listitemDB.isEmpty())
            listitemDB.sample();
        items = listitemDB.getData();
    }

    private void buildRecyclerview(){
        //設定RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mAdapter = new EditItemAdapter(this, items, this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // 建立自定Adapter物件
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    public void myDialog(final int position) {
        Log.d("allenj", "myDialog = " + position);

        if(ckb.isChecked()){
            listitemDB.delete(items.get(position));
            items.remove(position);
            mAdapter.notifyDataSetChanged();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("確定刪除(刪除後不可返回)")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("allenj", "刪除成功 " + position);
                            listitemDB.delete(items.get(position));
                            items.remove(position);
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("allenj", "不要刪除 " + position);
                        }
                    });

            AlertDialog ad = builder.create();
            ad.show();
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}

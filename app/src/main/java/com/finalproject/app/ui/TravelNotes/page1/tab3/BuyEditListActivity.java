package com.finalproject.app.ui.TravelNotes.page1.tab3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finalproject.app.Adapter.BuyEditItemAdapter;
import com.finalproject.app.DataBase.BuyItemDAO;
import com.finalproject.app.Item.BuyItem;
import com.finalproject.app.MoveItemHelper.SimpleItemTouchHelperCallback;
import com.finalproject.app.R;

import java.util.List;

public class BuyEditListActivity extends AppCompatActivity implements BuyEditItemAdapter.OnStartDragListener {
    public static String ID_KEY = "planIdKey";
    private long key = 0;

    private CheckBox ckb;
    private Button cancelBtn;
    private Button submitBtn;
    private ImageButton addBtn;

    private RecyclerView recyclerView;
    private BuyEditItemAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<BuyItem> items;
    private ItemTouchHelper itemTouchHelper;
    private BuyItemDAO buyItemDAO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_list_edit);

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
                BuyEditListActivity.this.finish();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add(new BuyItem(key));
                mAdapter.notifyDataSetChanged();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItemDAO.update(items);
                BuyEditListActivity.this.finish();
            }
        });
    }

    private void createItemList(){
        buyItemDAO = new BuyItemDAO(this, key);
        if(buyItemDAO.isEmpty())
            buyItemDAO.sample();
        items = buyItemDAO.getData();
    }

    private void buildRecyclerview(){
        //設定RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mAdapter = new BuyEditItemAdapter(this, items, this);
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
            buyItemDAO.delete(items.get(position));
            items.remove(position);
            mAdapter.notifyDataSetChanged();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("確定刪除(刪除後不可返回)")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("allenj", "刪除成功 " + position);
                            buyItemDAO.delete(items.get(position));
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

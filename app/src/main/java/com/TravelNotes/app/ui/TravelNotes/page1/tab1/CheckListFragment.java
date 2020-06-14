package com.TravelNotes.app.ui.TravelNotes.page1.tab1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.TravelNotes.app.Adapter.CheckItemAdapter;
import com.TravelNotes.app.DataBase.ListItemDAO;
import com.TravelNotes.app.Item.ListItem;
import com.TravelNotes.app.R;

import java.util.List;

public class CheckListFragment extends Fragment{
    private static String ID_KEY = "planIdKey";
    private long key = 0;

    private View view;

    private RecyclerView mRecyclerView;
    private CheckItemAdapter checkItemAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<ListItem> items;
    private ListItemDAO listitemDB;

    public static CheckListFragment newInstance(long key){
        CheckListFragment fragment = new CheckListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ID_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_check_list, container, false);
        key = getArguments().getLong(ID_KEY, 0);

        // 建立自定Adapter物件
        setListener();
        createItemList();
        buildRecyclerview();
        return view;
    }

    private void createItemList(){
        listitemDB = new ListItemDAO(view.getContext() , key);
        if(listitemDB.isEmpty())
            items = null;
        else
            items = listitemDB.getData();
    }

    private void buildRecyclerview(){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        checkItemAdapter = new CheckItemAdapter(this, items);

        mRecyclerView.setLayoutManager(layoutManager);

        if(items == null){
            TextView tv = (TextView)view.findViewById(R.id.empty);
            mRecyclerView.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
        }
        else {
            TextView tv = (TextView)view.findViewById(R.id.empty);
            mRecyclerView.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
            mRecyclerView.setAdapter(checkItemAdapter);
        }
    }

    private void setListener(){
        Button btn = (Button)view.findViewById(R.id.buttonEdit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextActivity();
            }
        });
    }

    public void itemOnClick(ListItem item){
        item.setSelected(!item.isSelected());
        updateDB(item);
        checkItemAdapter.notifyDataSetChanged();
    }

    private void updateDB(ListItem item){
        listitemDB.update(item);
    }

    private void nextActivity(){
        Intent intent = new Intent(view.getContext(), EditListActivity.class);
        intent.putExtra(EditListActivity.ID_KEY, key);
        view.getContext().startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        createItemList();
        buildRecyclerview();
    }
}

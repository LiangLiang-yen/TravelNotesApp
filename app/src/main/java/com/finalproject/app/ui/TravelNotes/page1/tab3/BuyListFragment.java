package com.finalproject.app.ui.TravelNotes.page1.tab3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finalproject.app.Adapter.BuyItemAdapter;
import com.finalproject.app.DataBase.BuyItemDAO;
import com.finalproject.app.Item.BuyItem;
import com.finalproject.app.R;

import java.util.List;

public class BuyListFragment extends Fragment {
    private static String ID_KEY = "planIdKey";
    private long key = 0;

    private View view;

    private RecyclerView mRecyclerView;
    private BuyItemAdapter buyItemAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView textViewTotalMoney;

    private List<BuyItem> items;
    private BuyItemDAO buyItemDAO;

    public static BuyListFragment newInstance(long key){
        BuyListFragment fragment = new BuyListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ID_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_buy_list, container, false);
        key = getArguments().getLong(ID_KEY, 0);

        // 建立自定Adapter物件
        setListener();
        createItemList();
        buildRecyclerview();

        return view;
    }

    private void setListener(){
        Button btn = (Button)view.findViewById(R.id.buttonEdit);
        textViewTotalMoney = (TextView) view.findViewById(R.id.textViewTotalMoney);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextActivity();
            }
        });
    }

    private void createItemList(){
        buyItemDAO = new BuyItemDAO(view.getContext() , key);
        if(buyItemDAO.isEmpty()) {
            items = null;
        }
        else
            items = buyItemDAO.getData();
    }

    private void buildRecyclerview(){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        buyItemAdapter = new BuyItemAdapter(this, items);
        mRecyclerView.setLayoutManager(layoutManager);

        if(items == null){
            TextView tv = (TextView)view.findViewById(R.id.empty);
            LinearLayout title = (LinearLayout)view.findViewById(R.id.title);
            title.setVisibility(View.GONE);
            textViewTotalMoney.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
        }
        else {
            TextView tv = (TextView)view.findViewById(R.id.empty);
            LinearLayout title = (LinearLayout)view.findViewById(R.id.title);
            textViewTotalMoney.setText("總金額:" + buyItemDAO.getTotalMoney() + "元");
            title.setVisibility(View.VISIBLE);
            textViewTotalMoney.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
            mRecyclerView.setAdapter(buyItemAdapter);
        }
    }

    private void nextActivity(){
        Intent intent = new Intent(view.getContext(), BuyEditListActivity.class);
        intent.putExtra(BuyEditListActivity.ID_KEY, key);
        view.getContext().startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        createItemList();
        buildRecyclerview();
    }
}

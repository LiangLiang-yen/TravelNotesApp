package com.finalproject.app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finalproject.app.Item.BuyItem;
import com.finalproject.app.R;
import com.finalproject.app.ui.TravelNotes.page1.tab3.BuyListFragment;

import java.util.List;

public class BuyItemAdapter extends RecyclerView.Adapter<BuyItemAdapter.ViewHolder> {

    private BuyListFragment fragment;
    private List<BuyItem> items;
    private BuyItem item;

    public BuyItemAdapter(BuyListFragment fragment, List<BuyItem> items) {
        this.fragment = fragment;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_buy_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BuyItemAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(items.get(position));

        item = items.get(position);

        //設定名稱、數量、單位
        holder.itemName.setText(item.getName());
        holder.itemPrice.setText(String.valueOf(item.getPrice()));
        holder.itemCount.setText(String.valueOf(item.getCount()));
        holder.itemMoney.setText(String.valueOf(item.getMoney()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView itemName;
        TextView itemPrice;
        TextView itemCount;
        TextView itemMoney;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = (TextView) itemView.findViewById(R.id.itemName);
            itemPrice = (TextView) itemView.findViewById(R.id.itemPrice);
            itemCount = (TextView) itemView.findViewById(R.id.itemCount);
            itemMoney = (TextView) itemView.findViewById(R.id.itemMoney);
        }
    }
}
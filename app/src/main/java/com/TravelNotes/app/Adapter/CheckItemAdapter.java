package com.TravelNotes.app.Adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.TravelNotes.app.Item.ListItem;
import com.TravelNotes.app.R;
import com.TravelNotes.app.ui.TravelNotes.page1.tab1.CheckListFragment;

public class CheckItemAdapter extends RecyclerView.Adapter<CheckItemAdapter.ViewHolder> {

    private CheckListFragment fragment;
    private List<ListItem> items;
    private ListItem item;

    public CheckItemAdapter(CheckListFragment fragment, List<ListItem> items) {
        this.fragment = fragment;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CheckItemAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(items.get(position));

        item = items.get(position);

        // 設定是否已選擇
        holder.selectedItem.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);

        //設定名稱、數量、單位
        holder.itemName.setText(item.getName());
        holder.itemCount.setText(String.valueOf(item.getCount()));
        holder.itemUnit.setText(item.getUnit());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.itemOnClick(items.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView selectedItem;
        TextView itemName;
        TextView itemCount;
        TextView itemUnit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            selectedItem = (ImageView) itemView.findViewById(R.id.selectedItem);
            itemName = (TextView) itemView.findViewById(R.id.itemName);
            itemCount = (TextView) itemView.findViewById(R.id.itemCount);
            itemUnit = (TextView) itemView.findViewById(R.id.itemUnit);
        }
    }
}
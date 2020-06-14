package com.TravelNotes.app.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.TravelNotes.app.Item.BuyItem;
import com.TravelNotes.app.MoveItemHelper.ItemTouchHelperAdapter;
import com.TravelNotes.app.R;
import com.TravelNotes.app.ui.TravelNotes.page1.tab3.BuyEditListActivity;

import java.util.List;

public class BuyEditItemAdapter extends RecyclerView.Adapter<BuyEditItemAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private List<BuyItem> items;
    private BuyEditListActivity activity;
    private final OnStartDragListener mDragStartListener;

    public BuyEditItemAdapter(BuyEditListActivity activity, List<BuyItem> items, OnStartDragListener dragStartListener) {
        this.items = items;
        this.activity = activity;
        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public BuyEditItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_buy_item_edit, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BuyEditItemAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(items.get(position));

        BuyItem li = items.get(position);
        holder.itemName.setText(li.getName());
        holder.itemPrice.setText(String.valueOf(li.getPrice()));
        holder.itemCount.setText(String.valueOf(li.getCount()));
        holder.itemMoney.setText(String.valueOf(li.getMoney()));

        holder.removeView.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.myDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        itemSwap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition,toPosition);
    }

    private void itemSwap(List<BuyItem> items, int from, int to){
        BuyItem cache = items.get(from).clone();
        items.get(from).setKey(items.get(to).getKey());
        items.get(from).setName(items.get(to).getName());
        items.get(from).setPrice(items.get(to).getPrice());
        items.get(from).setCount(items.get(to).getCount());
        items.get(from).setMoney(items.get(to).getMoney());

        items.get(to).setKey(cache.getKey());
        items.get(to).setName(cache.getName());
        items.get(to).setPrice(cache.getPrice());
        items.get(to).setCount(cache.getCount());
        items.get(to).setMoney(cache.getMoney());
    }

    @Override
    public void onItemDismiss(int position) {
        activity.myDialog(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private EditText itemName;
        private EditText itemPrice;
        private EditText itemCount;
        private TextView itemMoney;
        private ImageButton removeView;
        private ImageView moveView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = (EditText) itemView.findViewById(R.id.itemName);
            itemPrice = (EditText)  itemView.findViewById(R.id.itemPrice);
            itemCount = (EditText) itemView.findViewById(R.id.itemCount);
            itemMoney = (TextView) itemView.findViewById(R.id.itemMoney);
            removeView = (ImageButton) itemView.findViewById(R.id.removeItem);
            moveView = (ImageView) itemView.findViewById(R.id.moveItem);

            itemName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    items.get(getAdapterPosition()).setName(itemName.getText().toString());
                }
            });
            itemPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(itemPrice.getText().toString().matches(""))
                        items.get(getAdapterPosition()).setPrice(0);
                    else
                        items.get(getAdapterPosition()).setPrice(Integer.parseInt(itemPrice.getText().toString()));
                    int money = items.get(getAdapterPosition()).updateMoney();
                    itemMoney.setText(String.valueOf(money));
                }
            });
            itemCount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(itemCount.getText().toString().matches(""))
                        items.get(getAdapterPosition()).setCount(0);
                    else
                        items.get(getAdapterPosition()).setCount(Integer.parseInt(itemCount.getText().toString()));
                    int money = items.get(getAdapterPosition()).updateMoney();
                    itemMoney.setText(String.valueOf(money));
                }
            });
        }
    }

    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
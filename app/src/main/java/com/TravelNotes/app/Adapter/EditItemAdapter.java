package com.TravelNotes.app.Adapter;

import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.TravelNotes.app.Item.ListItem;
import com.TravelNotes.app.R;
import com.TravelNotes.app.MoveItemHelper.ItemTouchHelperAdapter;
import com.TravelNotes.app.ui.TravelNotes.page1.tab1.EditListActivity;

public class EditItemAdapter extends RecyclerView.Adapter<EditItemAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private List<ListItem> items;
    private EditListActivity activity;

    public EditItemAdapter(EditListActivity activity, List<ListItem> items, OnStartDragListener dragStartListener) {
        this.items = items;
        this.activity = activity;
    }

    @NonNull
    @Override
    public EditItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_edititem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EditItemAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(items.get(position));

        ListItem li = items.get(position);
        holder.itemName.setText(li.getName());
        holder.itemCount.setText(String.valueOf(li.getCount()));
        holder.itemUnit.setText(li.getUnit());

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

    private void itemSwap(List<ListItem> items, int from, int to){
        ListItem cache = items.get(from).clone();
        items.get(from).setName(items.get(to).getName());
        items.get(from).setKey(items.get(to).getKey());
        items.get(from).setCount(items.get(to).getCount());
        items.get(from).setSelected(items.get(to).isSelected());
        items.get(from).setUnit(items.get(to).getUnit());

        items.get(to).setName(cache.getName());
        items.get(to).setKey(cache.getKey());
        items.get(to).setCount(cache.getCount());
        items.get(to).setSelected(cache.isSelected());
        items.get(to).setUnit(cache.getUnit());
    }

    @Override
    public void onItemDismiss(int position) {
        activity.myDialog(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private EditText itemName;
        private EditText itemCount;
        private EditText itemUnit;
        private ImageButton removeView;
        private ImageView moveView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = (EditText) itemView.findViewById(R.id.itemName);
            itemCount = (EditText) itemView.findViewById(R.id.itemCount);
            itemUnit = (EditText) itemView.findViewById(R.id.itemUnit);
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
                }
            });
            itemUnit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    items.get(getAdapterPosition()).setUnit(itemUnit.getText().toString());
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
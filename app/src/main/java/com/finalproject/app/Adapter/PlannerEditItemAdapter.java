package com.finalproject.app.Adapter;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.finalproject.app.Event;
import com.finalproject.app.Item.PlanItem;
import com.finalproject.app.R;
import com.finalproject.app.ui.TravelNotes.main.PlannerEditFragment;
import com.finalproject.app.MoveItemHelper.ItemTouchHelperAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public class PlannerEditItemAdapter extends RecyclerView.Adapter<PlannerEditItemAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private PlannerEditFragment fragment;
    private List<PlanItem> items;
    private PlanItem pi;
    private OnStartDragListener mDragStartListener;

    public PlannerEditItemAdapter(PlannerEditFragment fragment, List<PlanItem> items,  OnStartDragListener dragStartListener){
        this.fragment = fragment;
        this.items = items;
        this.mDragStartListener = dragStartListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_planner_item_edit, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.itemView.setTag(items.get(position));

        pi = items.get(position);
        holder.itemPlanName.setText(pi.getPlanName());
        holder.itemCityName.setText(pi.getCityName());
        holder.itemDate.setText(DateString(pi.getStartDate(), pi.getEndDate()));
        holder.color.setBackground(new ColorDrawable(pi.getColor()));
        holder.itemEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event.info("planner", String.valueOf(pi.getId()));
                fragment.changeFragment(items.get(position).getKey());
            }
        });
        holder.itemRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.removeItem(position);
            }
        });
        holder.itemMoveBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String DateString(String date1, String date2){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //日期格式化
            SimpleDateFormat sd = new SimpleDateFormat("M-dd");
            return sd.format(sdf.parse(date1)) + " - " + sd.format(sdf.parse(date2));
        }catch (ParseException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        itemSwap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition,toPosition);
    }

    private void itemSwap(List<PlanItem> items, int from, int to){
        try {
            items.get(from).swap(items.get(to));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemDismiss(int position) {
        fragment.removeItem(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout color;
        private TextView itemPlanName;
        private TextView itemCityName;
        private TextView itemDate;
        private ImageButton itemRemoveBtn;
        private ImageButton itemEditBtn;
        private ImageView itemMoveBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            color = (ConstraintLayout) itemView.findViewById(R.id.color);
            itemPlanName = (TextView) itemView.findViewById(R.id.textViewPlanName);
            itemCityName = (TextView) itemView.findViewById(R.id.textViewCityName);
            itemDate = (TextView) itemView.findViewById(R.id.textViewDate);
            itemRemoveBtn = (ImageButton) itemView.findViewById(R.id.removeItem);
            itemEditBtn = (ImageButton) itemView.findViewById(R.id.editItem);
            itemMoveBtn = (ImageView) itemView.findViewById(R.id.moveItem);
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

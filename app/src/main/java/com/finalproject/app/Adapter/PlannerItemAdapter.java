package com.finalproject.app.Adapter;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finalproject.app.Item.PlanItem;
import com.finalproject.app.R;
import com.finalproject.app.ui.TravelNotes.main.PlannerFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PlannerItemAdapter extends RecyclerView.Adapter<PlannerItemAdapter.ViewHolder> {

    private PlannerFragment fragment;
    private List<PlanItem> items;
    private PlanItem pi;

    public PlannerItemAdapter(PlannerFragment fragment, List<PlanItem> items){
        this.fragment = fragment;
        this.items = items;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_planner_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.itemView.setTag(items.get(position));

        pi = items.get(position);
        holder.itemPlanName.setText(pi.getPlanName());
        holder.itemCityName.setText(pi.getCityName());
        holder.itemDate.setText(DateString(pi.getStartDate(), pi.getEndDate()));
        holder.color.setBackground(new ColorDrawable(pi.getColor()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.changeFragment(items.get(position).getKey(), items.get(position).getPlanName(), items.get(position).getCityName(), items.get(position).getStartDate());
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout color;
        private TextView itemPlanName;
        private TextView itemCityName;
        private TextView itemDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            color = (LinearLayout) itemView.findViewById(R.id.color);
            itemPlanName = (TextView) itemView.findViewById(R.id.textViewPlanName);
            itemCityName = (TextView) itemView.findViewById(R.id.textViewCityName);
            itemDate = (TextView) itemView.findViewById(R.id.textViewDate);
        }
    }
}

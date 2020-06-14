package com.TravelNotes.app.ui.TravelNotes.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.TravelNotes.app.Adapter.PlannerEditItemAdapter;
import com.TravelNotes.app.DataBase.ListItemDAO;
import com.TravelNotes.app.DataBase.PlanItemDAO;
import com.TravelNotes.app.Item.PlanItem;
import com.TravelNotes.app.R;
import com.TravelNotes.app.MoveItemHelper.SimpleItemTouchHelperCallback;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class PlannerEditFragment extends Fragment implements PlannerEditItemAdapter.OnStartDragListener{
    public static final int ADD_FRAGMENT_FLAG = 0;
    public static final int BACK_TO_MAIN_FRAGMENT_FLAG = 2;
    private View view;

    private RecyclerView mRecyclerView;
    private PlannerEditItemAdapter plannerEditItemAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ItemTouchHelper itemTouchHelper;

    private List<PlanItem> items;
    private PlanItemDAO planItemDAO;

    private Button btnCancel;
    private Button btnSubmit;

    public static PlannerEditFragment newInstance()
    {
        PlannerEditFragment fragment = new PlannerEditFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planner_list_edit, container, false);
        createItemList();
        buildRecyclerview();
        buttonListener();
        return view;
    }

    private void createItemList(){
        planItemDAO = new PlanItemDAO(view.getContext());
        if(planItemDAO.isEmpty())
            items = null;
        else
            items = planItemDAO.getData();
    }

    private void buildRecyclerview(){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        plannerEditItemAdapter = new PlannerEditItemAdapter(this, items, this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(plannerEditItemAdapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(plannerEditItemAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void buttonListener(){
        btnCancel = (Button) view.findViewById(R.id.buttonCancel);
        btnSubmit = (Button) view.findViewById(R.id.buttonSubmit);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(0);
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                planItemDAO.update(items);
                changeFragment(0);
            }
        });
    }

    public void removeItem(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("確定刪除(刪除後不可返回)")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ListItemDAO listItemDAO = new ListItemDAO(view.getContext(), items.get(position).getId());
                            listItemDAO.delete();
                            planItemDAO.delete(items.get(position));
                        }catch (IndexOutOfBoundsException e){

                        }
                        Snackbar.make(view, "刪除成功", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        items.remove(position);
                        plannerEditItemAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog ad = builder.create();
        ad.show();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    public void changeFragment(long itemId){
        Fragment fragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if(itemId != 0) {
            fragment = PlannerAddFragment.newInstance(itemId);
            transaction.addToBackStack(fragment.getClass().getName());
        }else {
            getFragmentManager().popBackStack();
            return;
        }
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                createItemList();
                buildRecyclerview();
            }
        });
        if (!fragment.isAdded()) {	// 先判断是否被add過 如果沒有Add過 代表是第一次呼叫 則需要先add 其餘時候都直接使用show進行顯示
            // hide裡面放的是自己當前所在的Fragment頁面，後面一定要.this才能夠指向現在要隱藏的頁面，否則會直接幫你生成一個新的頁面
            transaction.hide(PlannerEditFragment.this).add(R.id.fragmentView, fragment).commitAllowingStateLoss(); // 隱藏當前頁面 並新增明細頁面
        } else {
            transaction.hide(PlannerEditFragment.this).show(fragment).commitAllowingStateLoss(); //隱藏當前頁面 呼叫明細頁面
        }
    }
}

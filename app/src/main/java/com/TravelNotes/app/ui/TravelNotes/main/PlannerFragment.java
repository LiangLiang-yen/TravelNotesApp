package com.TravelNotes.app.ui.TravelNotes.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.TravelNotes.app.Adapter.PlannerItemAdapter;
import com.TravelNotes.app.DataBase.PlanItemDAO;
import com.TravelNotes.app.Item.PlanItem;
import com.TravelNotes.app.ui.TravelNotes.TravelNotesFragment;
import com.TravelNotes.app.R;

import java.util.List;

public class PlannerFragment extends Fragment{
    private static final int ADD_FRAGMENT_FLAG = 0;
    private static final int EDIT_FRAGMENT_FLAG = 1;

    private View view;

    private RecyclerView mRecyclerView;
    private PlannerItemAdapter plannerItemAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<PlanItem> items;
    private PlanItemDAO planItemDAO;

    private ImageButton btnAdd;
    private Button btnEdit;

    public static PlannerFragment newInstance(){
        PlannerFragment fragment = new PlannerFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planner_list, container, false);
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
        if(items == null){
            btnEdit = (Button) view.findViewById(R.id.buttonEdit);
            btnEdit.setEnabled(false);
            TextView tv = (TextView)view.findViewById(R.id.empty_view);
            mRecyclerView.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
        }else {
            btnEdit = (Button) view.findViewById(R.id.buttonEdit);
            btnEdit.setEnabled(true);
            TextView tv = (TextView)view.findViewById(R.id.empty_view);
            mRecyclerView.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
            plannerItemAdapter = new PlannerItemAdapter(this, items);
        }

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(plannerItemAdapter);
    }

    private void buttonListener(){
        btnAdd = (ImageButton) view.findViewById(R.id.buttonAdd);
        btnEdit = (Button) view.findViewById(R.id.buttonEdit);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TravelNotesFragment.dataBaseIsCurrent){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("第一次執行此程式，請連上網路")
                            .setPositiveButton("確認", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            }).create();
                    AlertDialog dialog = builder.show();
                }else
                    changeFragment(ADD_FRAGMENT_FLAG);
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(EDIT_FRAGMENT_FLAG);
            }
        });
    }

    public void changeFragment(long key, String planName, String city, String date){
        if(getParentFragment() instanceof TravelNotesFragment){
            ((TravelNotesFragment) getParentFragment()).changeFragment(key, planName, city, date);
        }
    }

    private void changeFragment(int fragmentID){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment;
        switch (fragmentID){
            case ADD_FRAGMENT_FLAG:
                fragment = PlannerAddFragment.newInstance(0);
                break;
            case EDIT_FRAGMENT_FLAG:
                fragment = PlannerEditFragment.newInstance();
                break;
                default:
                    fragment = null;
        }
        transaction.addToBackStack(fragment.getClass().getName());
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
            transaction.hide(PlannerFragment.this).add(R.id.fragmentView, fragment).commitAllowingStateLoss(); // 隱藏當前頁面 並新增明細頁面
        } else {
            transaction.hide(PlannerFragment.this).show(fragment).commit(); //隱藏當前頁面 呼叫明細頁面
        }
    }
}

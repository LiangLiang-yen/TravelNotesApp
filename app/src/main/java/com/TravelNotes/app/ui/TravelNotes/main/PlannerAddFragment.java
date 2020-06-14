package com.TravelNotes.app.ui.TravelNotes.main;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.TravelNotes.app.DataBase.CountyItemDAO;
import com.TravelNotes.app.DataBase.PlanItemDAO;
import com.TravelNotes.app.Item.CountyItem;
import com.TravelNotes.app.Item.PlanItem;
import com.TravelNotes.app.ui.TravelNotes.TravelNotesFragment;
import com.TravelNotes.app.R;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.Calendar;
import java.util.List;

public class PlannerAddFragment extends Fragment{
    private static String ITEM_ID_KEY="itemIDkey";
    private Boolean First = true;

    private View view;
    private TravelNotesFragment activity;

    private PlanItem item;
    private PlanItemDAO planItemDAO;
    private CountyItemDAO countyItemDAO;
    private long itemId;

    private EditText editTextPlanName;
    private Spinner spinnerContinent;
    private Spinner spinnerCity;
    private EditText editTextStartDate;
    private EditText editTextEndDate;
    private Button btnCancel;
    private Button btnSubmit;
    private View viewColor;

    public static PlannerAddFragment newInstance(long itemId){
        PlannerAddFragment fragment = new PlannerAddFragment();
        Bundle data = new Bundle();
        data.putLong(ITEM_ID_KEY, itemId);
        fragment.setArguments(data);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planner_list_add, container, false);
        Bundle bundle = getArguments();
        itemId = bundle.getLong(ITEM_ID_KEY);
        setListener();
        createItem();
        setColorPicker();
        setSpinnerData();
        return view;
    }

    private void setListener() {
        btnCancel = (Button) view.findViewById(R.id.buttonCancel);
        btnSubmit = (Button) view.findViewById(R.id.buttonSubmit);
        editTextPlanName = (EditText) view.findViewById(R.id.editTextPlanName);
        spinnerContinent = (Spinner) view.findViewById(R.id.spinnerContinent);
        spinnerCity = (Spinner) view.findViewById(R.id.spinnerCity);
        editTextStartDate = (EditText) view.findViewById(R.id.editTextStartDate);
        editTextEndDate = (EditText) view.findViewById(R.id.editTextEndDate);
        viewColor = (View)view.findViewById(R.id.viewColor);

        InputFilter[] filters = { new InputFilter.LengthFilter(10)};
        editTextPlanName.setFilters(filters);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkEditTextFill()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("請填滿所有的欄位")
                            .setPositiveButton("確認", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            }).create();
                    AlertDialog dialog = builder.show();
                }else {
                    item.setPlanName(editTextPlanName.getText().toString());
                    item.setContinent(spinnerContinent.getSelectedItem().toString());
                    item.setCityName(spinnerCity.getSelectedItem().toString());
                    item.setStartDate(editTextStartDate.getText().toString());
                    item.setEndDate(editTextEndDate.getText().toString());
                    item.setContinentpPosition(spinnerContinent.getSelectedItemPosition());
                    item.setCityNamePosition(spinnerCity.getSelectedItemPosition());
                    if(!planItemDAO.update(item))
                        planItemDAO.insert(item);
                    changeFragment();
                }
            }
        });
        spinnerContinent.setOnItemSelectedListener(SpinnerContinentSelected);
        editTextStartDate.setOnClickListener(EditTextDateClicked);
        editTextEndDate.setOnClickListener(EditTextDateClicked);
    }

    private void createItem(){
        planItemDAO = new PlanItemDAO(view.getContext());
        if(itemId != 0){
            item = planItemDAO.getData(itemId);
            editTextPlanName.setText(item.getPlanName());
            spinnerContinent.setSelection(item.getContinentpPosition());
            editTextStartDate.setText(item.getStartDate());
            editTextEndDate.setText(item.getEndDate());
        }else{
            item = new PlanItem();
        }
    }

    private void setColorPicker(){
        notifyColorSetChanged();
        viewColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(view.getContext())
                        .setTitle("Choose color")
                        .initialColor(item.getColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                item.setColor(selectedColor);
                                notifyColorSetChanged();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });
    }

    private void notifyColorSetChanged(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewColor.getBackground().mutate();
        gradientDrawable.setColor(item.getColor());
    }

    private void setSpinnerData(){
        countyItemDAO = new CountyItemDAO(view.getContext());
        ArrayAdapter<String> areaArr = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, countyItemDAO.getArea());
        spinnerContinent.setAdapter(areaArr);
    }

    /**
     * Listener SpinnerContinent Changeed
     */
    private AdapterView.OnItemSelectedListener SpinnerContinentSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ArrayAdapter<String> city_arr = new ArrayAdapter<String>(view.getContext(),
                    android.R.layout.simple_spinner_dropdown_item,0);
            List<CountyItem> citylist = countyItemDAO.getCity(parent.getItemAtPosition(position).toString());
            for(CountyItem item: citylist)
            {
                city_arr.add(item.getCityName() + item.getChineseName());
            }
            spinnerCity.setAdapter(city_arr);
            if(First){
                spinnerCity.setSelection(item.getCityNamePosition());
                First = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Listener editTextStartDate and editTextEndDate Clicked
     */
    private EditText.OnClickListener EditTextDateClicked = new EditText.OnClickListener() {
        @Override
        public void onClick(View v) {
            int year, month, day;
            final EditText et = (EditText)view.findViewById(v.getId());
            Calendar calendar = Calendar.getInstance();
            if(v.getId() == R.id.editTextEndDate && !editTextStartDate.getText().toString().matches(""))
            {
                year = Integer.parseInt(editTextStartDate.getText().toString().split("-")[0]);
                month = Integer.parseInt(editTextStartDate.getText().toString().split("-")[1]) - 1;
                day = Integer.parseInt(editTextStartDate.getText().toString().split("-")[2]);
            }

            else if(!et.getText().toString().matches(""))
            {
                year = Integer.parseInt(et.getText().toString().split("-")[0]);
                month = Integer.parseInt(et.getText().toString().split("-")[1]) - 1;
                day = Integer.parseInt(et.getText().toString().split("-")[2]);
            }
            else
            {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }
            new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String datetime = String.valueOf(year)+'-'+String.valueOf(month+1)+'-'+String.valueOf(dayOfMonth);
                    et.setText(datetime);
                    if((!editTextStartDate.getText().toString().matches("") && !editTextEndDate.getText().toString().matches("")) && (TravelNotesFragment.toUnixTime(editTextEndDate.getText().toString()) < TravelNotesFragment.toUnixTime(editTextStartDate.getText().toString())))
                        if(et.getId() == R.id.editTextStartDate)
                            editTextEndDate.setText(datetime);
                        else
                            editTextStartDate.setText(datetime);
                }
            }, year, month, day).show();
        }
    };

    private Boolean checkEditTextFill(){
        if(editTextPlanName.getText().toString().matches(""))
            return false;
        if(editTextStartDate.getText().toString().matches(""))
            return false;
        if(editTextEndDate.getText().toString().matches(""))
            return false;
        return true;
    }

    private void changeFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        getFragmentManager().popBackStack();
        closeKeyboard();
    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

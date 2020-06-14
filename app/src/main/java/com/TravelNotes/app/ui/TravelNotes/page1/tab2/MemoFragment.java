package com.TravelNotes.app.ui.TravelNotes.page1.tab2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.TravelNotes.app.R;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class MemoFragment extends Fragment {
    private static String ID_KEY = "planIdKey";
    private static int backgroundColor = 0;
    private String key = "0";
    private int prevBottom;

    private View view;

    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    private Button buttonSetColor;
    private Button buttonFinish;
    private TextView textViewSave;
    private EditText editTextContainer;
    private View viewBackgroundColor;
    private ImageView imageViewCheckMark;

    final Animation in = new AlphaAnimation(0.0f, 1.0f);
    final Animation out = new AlphaAnimation(1.0f, 0.0f);

    public static MemoFragment newInstance(long key){
        MemoFragment fragment = new MemoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ID_KEY, String.valueOf(key));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_memo, container, false);
        backgroundColor = android.graphics.Color.parseColor("#FFF2AB");

        key = getArguments().getString(ID_KEY, "");

        setListener();
        loadData();

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (view.getBottom() > prevBottom) {
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(R.id.line, ConstraintSet.END, R.id.backgroundColor, ConstraintSet.END, 0);
                    constraintSet.applyTo(constraintLayout);
                    editTextContainer.clearFocus();
                    buttonFinish.animate()
                                .alpha(0.0f)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                   public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                        buttonFinish.setVisibility(View.GONE);
                                   }
                            });
                }else if(view.getBottom() < prevBottom){
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(R.id.line, ConstraintSet.END, R.id.buttonFinish, ConstraintSet.END, 0);
                    constraintSet.applyTo(constraintLayout);
                    buttonFinish.animate()
                                .alpha(1.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                        buttonFinish.setVisibility(View.VISIBLE);
                                    }
                                });
                }
                prevBottom = view.getBottom();
            }
        });

        return view;
    }

    private void setListener() {
        constraintLayout = (ConstraintLayout) view.findViewById(R.id.constraintLayout);
        buttonSetColor = (Button) view.findViewById(R.id.buttonSetBackgroundColor);
        buttonFinish = (Button) view.findViewById(R.id.buttonFinish);
        textViewSave = (TextView) view.findViewById(R.id.textViewSave);
        editTextContainer = (EditText) view.findViewById(R.id.editTextContainer);
        viewBackgroundColor = (View) view.findViewById(R.id.backgroundColor);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarLoading);
        imageViewCheckMark = (ImageView) view.findViewById(R.id.imageViewCheckMark);
        buttonSetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(view.getContext())
                        .setTitle("Choose color")
                        .initialColor(backgroundColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                backgroundColor = selectedColor;
                                viewBackgroundColor.setBackgroundColor(backgroundColor);
                                SharedPreferences settings = view.getContext().getSharedPreferences(key, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("Color", selectedColor);
                                editor.apply();
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
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextContainer.clearFocus();
                InputMethodManager imm = (InputMethodManager)view.getContext().getApplicationContext().getSystemService(view.getContext().getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextContainer.requestFocus();
                editTextContainer.setSelection(editTextContainer.getText().toString().length());
                InputMethodManager imm = (InputMethodManager)view.getContext().getApplicationContext().getSystemService(view.getContext().getApplicationContext().INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextContainer, 0);
            }
        });
        editTextContainer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editTextContainer.isFocused()){
                    in.setDuration(3000);
                    out.setDuration(2000);

                    progressBar.setVisibility(View.VISIBLE);
                    saveData();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            imageViewCheckMark.startAnimation(in);
                            imageViewCheckMark.startAnimation(out);
                            textViewSave.startAnimation(in);
                            textViewSave.startAnimation(out);
                        }
                    }, 500);
                }
            }
        });
    }

    private void loadData(){
        editTextContainer = (EditText) view.findViewById(R.id.editTextContainer);
        SharedPreferences settings = view.getContext().getSharedPreferences(key, 0);
        editTextContainer.setText(settings.getString("Text", ""));
        backgroundColor = settings.getInt("Color", backgroundColor);
        viewBackgroundColor.setBackgroundColor(backgroundColor);
    }

    private void saveData(){
        editTextContainer = (EditText) view.findViewById(R.id.editTextContainer);
        SharedPreferences settings = view.getContext().getSharedPreferences(key, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Text", editTextContainer.getText().toString());
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }
}

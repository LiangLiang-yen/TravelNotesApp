package com.TravelNotes.app.ui.TravelNotes.page1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.TravelNotes.app.R;

public class ErrorFragment extends Fragment {

    private View view;

    public static ErrorFragment newInstance(){
        ErrorFragment fragment = new ErrorFragment();
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_error, container, false);
        return view;
    }
}

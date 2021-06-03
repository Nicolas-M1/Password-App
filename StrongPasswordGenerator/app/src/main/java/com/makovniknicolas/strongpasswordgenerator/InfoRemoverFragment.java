package com.makovniknicolas.strongpasswordgenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Dimension;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoRemoverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoRemoverFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    LinearLayout infoColumn;
    private boolean isDarkTheme;
    Button deleteButton;
    Map<CheckBox, LogInInfo> checkToInfoMap;
    final int TEXT_SIZE = 20;

    public InfoRemoverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InfoRemoverFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoRemoverFragment newInstance() {
        InfoRemoverFragment fragment = new InfoRemoverFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_info_remover, container, false);
        isDarkTheme = ((MainActivity)getActivity()).isDarkTheme();
        deleteButton = v.findViewById(R.id.delete_button);
        infoColumn = v.findViewById(R.id.fragmentColumn);
        deleteButton.setOnClickListener((MainActivity)getActivity());
        setTheme(v);

        checkToInfoMap = new HashMap<CheckBox, LogInInfo>();
        PriorityQueue<LogInInfo> infoPQ = ((MainActivity)getActivity()).getInfoPQ();
        for(LogInInfo currentInfo : infoPQ){
            CheckBox currentCheck = addCheckbox(currentInfo.getApplication());
            checkToInfoMap.put(currentCheck, currentInfo);
        }//for

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private CheckBox addCheckbox(String text){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        Resources re = getResources();
        params.setMargins(0,0,0,
                (int)(re.getDimension(R.dimen.view_spacing)*re.getDisplayMetrics().density));
        CheckBox newCheck = new CheckBox(this.getActivity());
        newCheck.setText(text);
        newCheck.setTextSize(Dimension.SP,TEXT_SIZE);
        if(isDarkTheme){
            newCheck.setTextAppearance(R.style.TextDark);
        }//if
        else{
            newCheck.setTextAppearance(R.style.TextLight);
        }//else
        infoColumn.addView(newCheck);
        return newCheck;
    }//addCheckbox

    public Map<CheckBox, LogInInfo> getCheckToInfoMap(){
       return checkToInfoMap;
    }//getCheckToInfoMap

    private void setTheme(View fragment){
        if(isDarkTheme){
            fragment.setBackground(getResources().getDrawable(R.drawable.dark_fragment_background, null));
        }//if
        else{
            fragment.setBackground(getResources().getDrawable(R.drawable.light_fragment_background, null));
        }//else
    }//setTheme
}
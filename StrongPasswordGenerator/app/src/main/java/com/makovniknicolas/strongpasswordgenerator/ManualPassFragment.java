package com.makovniknicolas.strongpasswordgenerator;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManualPassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualPassFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button switchFragments, submitCustom, pasteButton;

    EditText textInput;

    AddInfoActivity parentActivity;

    public ManualPassFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ManualPassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManualPassFragment newInstance() {
        ManualPassFragment fragment = new ManualPassFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_manual_pass, container, false);
        setTheme(v);
        submitCustom = v.findViewById(R.id.submit_custom_button);
        submitCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPassword();
            }
        });
        switchFragments = v.findViewById(R.id.switchToRandom);
        pasteButton = v.findViewById(R.id.paste_button);
        switchFragments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.randomFragment();
            }
        });
        textInput = v.findViewById(R.id.inputCustomPassword);
        parentActivity = ((AddInfoActivity)getActivity());
        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clip = currentClip();
                textInput.setText(clip);
                sendPassword();
            }
        });
        return v;
    }

    private String currentClip(){
        String text = "";
        try{
            ClipboardManager clipboard = (ClipboardManager) parentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            text = String.valueOf(clipboard.getPrimaryClip().getItemAt(0).coerceToText(parentActivity));
        }//try
        catch (Exception e){
            text = "";
        }//catch
        if(text.length()>50){
            text = "";
        }//if
        return text;
    }//currentClip

    private void sendPassword(){
        String inputted= String.valueOf(textInput.getText()).trim();
        if(inputted == null || inputted.length() == 0){
            ((AddInfoActivity)getActivity()).displayToast("That is not a valid password");
        }//if
        else {
            System.out.println("about to send");
            parentActivity.setCurrentPassword(inputted);
        }//else
    }//sendPassword

    public String getPassword(){
        return String.valueOf(textInput.getText());
    }//getPassword

    private void setTheme(View fragment){
        if(((AddInfoActivity)getActivity()).isDarkTheme()){
            fragment.setBackground(getResources().getDrawable(R.drawable.dark_fragment_background, null));
        }//if
        else{
            fragment.setBackground(getResources().getDrawable(R.drawable.light_fragment_background, null));
        }//else
    }//setTheme
}
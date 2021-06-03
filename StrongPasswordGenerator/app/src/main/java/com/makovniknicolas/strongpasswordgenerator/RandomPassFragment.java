package com.makovniknicolas.strongpasswordgenerator;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Stack;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RandomPassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RandomPassFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    CheckBox includeNums, includeLetters, includeUpper, includeSpecial;
    EditText lengthInput;
    Button generateButton, switchButton;
    ProgressBar strengthProgress;
    TextView strengthText;
    final int DEFAULT_LENGTH = 8;
    final int RECOMMENDED_LENGTH = 8;
    BigInteger maxStrength;
    final BigInteger divisor = new BigInteger("1000000000000");

    AddInfoActivity parentActivity;

    TextView displayPassword;

    public RandomPassFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RandomPassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RandomPassFragment newInstance() {
        RandomPassFragment fragment = new RandomPassFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {    }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_random_pass, container, false);
        setTheme(v);
        includeNums = v.findViewById(R.id.numbers_check);
        includeLetters = v.findViewById(R.id.letters_check);
        includeUpper = v.findViewById(R.id.upperCase_check);
        includeSpecial = v.findViewById(R.id.specialChars_check);
        lengthInput = v.findViewById(R.id.length_input);
        displayPassword = v.findViewById(R.id.display_password);
        generateButton = v.findViewById(R.id.submit_params_button);
        switchButton = v.findViewById(R.id.switchToManual);
        strengthProgress = v.findViewById(R.id.strengthProgressBar);
        strengthText = v.findViewById(R.id.strengthTextDisplay);
        parentActivity = ((AddInfoActivity)getActivity());
        setMaxStrength();
        strengthProgress.setMax((maxStrength.divide(divisor).intValue()));
        strengthProgress.setMin(10);
        strengthProgress.setProgress(10);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.manualFragment();
            }
        });
        includeLetters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //when letter's state is changed upper will be off by default
                includeUpper.setChecked(false);
            }
        });
        includeUpper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //when upper's state is changed, letters will be switched on
                includeLetters.setChecked(true);
            }
        });
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRandomPassword(v);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        strengthProgress.setProgress(10);
    }

    public void generateRandomPassword(View view) {
        int length;
        try{
            length = Integer.parseInt(String.valueOf(lengthInput.getText()));
        }//try
        catch (Exception e){
            lengthInput.setText(""+DEFAULT_LENGTH);
            length = DEFAULT_LENGTH;
        }//catch
        int range = 0;
        Stack<Character> toSprinkle = new Stack<Character>();
        //this will ensure that all character types are used at least once
        int stackSize = 0;
        ArrayList<int[]> intervals = new ArrayList<int[]>();
        int specialCount;
        if(includeNums.isChecked()){
            intervals.add(new int[]{'0','9'});
            range+=('9'-'0'+1);
            stackSize+=1;
            toSprinkle.push((char)getRandom('0','9'+1));
        }//if
        if(includeLetters.isChecked()){
            intervals.add(new int[]{'a','z'});
            range+= ('z'-'a'+1);
            stackSize+=1;
            toSprinkle.push((char) getRandom('a','z'+1));
        }//if
        if(includeUpper.isChecked()){
            intervals.add(new int[]{'A','Z'});
            range+=('Z'-'A'+1);
            stackSize+=1;
            toSprinkle.push((char) getRandom('A','Z'+1));
        }//if
        if(includeSpecial.isChecked()){
            intervals.add(new int[]{'!','/'});
            intervals.add(new int[]{':','@'});
            specialCount = ('/'-'!'+1)+('@'-':'+1);
            range+= specialCount; //there are a few more but these are the only ones I'm including
            stackSize+=1;
            int digit = getRandom(0,specialCount+1);
            char toAdd;
            if('!'+digit <= '/'){
                toAdd = (char)('!'+digit);
            }//if
            else{
                toAdd = (char)(digit-('/'-'!') + ':');
            }//else
            toSprinkle.push(toAdd);
        }//if
        if(range == 0){
            intervals.add(new int[]{'0','9'});
            includeNums.setChecked(true);
            range = 10;
            stackSize = 1;
            toSprinkle.push((char)getRandom('0','9'));
        }//if

        String password = "";
        for(int run = 0; run<(length-stackSize); run++){
            password += (char)getRandom(range,intervals);
        }//for

        int count = 0;
        while(!toSprinkle.isEmpty() && count<length){
            count++;
            char current = toSprinkle.pop();
            password = insert(password, getRandom(0,password.length()-1), current);
        }//while

        setStrength(power(range,length));
        parentActivity.setCurrentPassword(password);
        displayPassword.setText("Possible: "+password);
    }//generateRandomPassword

    private int getRandom(int min, int max){ //inclusive
        double numDbl = Math.random()*(max-min);
        return (int)(numDbl) + min;
    }//getRandom

    private int getRandom(int range, ArrayList<int[]> intervals){ //inclusive
        int num = getRandom(0,range);
        for(int index = 0; index<intervals.size(); index++){
            int[] current = intervals.get(index);
            int currentRange = (current[1]-current[0]+1);
            if(num < currentRange){
                return num + current[0];
            }//if
            else{
                num -= currentRange;
            }//else
        }//for
        return ' ';
    }//getRandom

    private String insert(String insertIn, int target, char character){
        return insertIn.substring(0,target) + character + insertIn.substring(target);
    }//insert

    private void setStrength(BigInteger strength){
        if(strength.compareTo(maxStrength) > 0){
            strength = maxStrength;
        }//if
        System.out.println("Strength: "+strength+", Max Strength: "+maxStrength);
        if(strength.compareTo((maxStrength.multiply(new BigInteger("2")).divide(new BigInteger("3")))) >0){ //strong
            int green = getResources().getColor(R.color.lightGreen);
            strengthText.setText("Strong");
            strengthText.setTextColor(green);
            setProgress(strength);
            strengthProgress.setProgressTintList(ColorStateList.valueOf(green));
        }//if
        else if(strength.compareTo(maxStrength.divide(new BigInteger("3"))) >0){
            int yellow = getResources().getColor(R.color.regularYellow);
            strengthText.setText("Medium");
            strengthText.setTextColor(yellow);
            setProgress(strength);
            strengthProgress.setProgressTintList(ColorStateList.valueOf(yellow));
        }//else if
        else{
            int red = getResources().getColor(R.color.brightRed);
            strengthText.setText("Weak");
            strengthText.setTextColor(red);
            setProgress(strength);
            strengthProgress.setProgressTintList(ColorStateList.valueOf(red));
        }//else
    }//setStrength

    private void setMaxStrength(){
        int range = 0;
        //this will ensure that all character types are used at least once
        int specialCount;
        range+=('9'-'0'+1);
        range+= ('z'-'a'+1);
        range+=('Z'-'A'+1);
        specialCount = ('/'-'!'+1)+('@'-':'+1);
        range+= specialCount; //there are a few more but these are the only ones I'm including
        maxStrength = power(range,RECOMMENDED_LENGTH);
    }//setMaxStrength

    private BigInteger power(int base, int exponent){
        BigInteger temp = new BigInteger("1");
        BigInteger baseBI = new BigInteger(base+"");
        for(int i = 1; i <= exponent; i++){
            temp = temp.multiply(baseBI);
        }//for
        return temp;
    }//power

    private void setProgress(BigInteger biValue){
        System.out.println("Max value:"+maxStrength.divide(divisor)+", Current Value:"+biValue.divide(divisor));
        strengthProgress.setProgress((biValue.divide(divisor).intValue()));
    }//setProgress

    private void setTheme(View fragment){
        if(((AddInfoActivity)getActivity()).isDarkTheme()){
            fragment.setBackground(getResources().getDrawable(R.drawable.dark_fragment_background, null));
        }//if
        else{
            fragment.setBackground(getResources().getDrawable(R.drawable.light_fragment_background, null));
        }//else
    }//setTheme
}
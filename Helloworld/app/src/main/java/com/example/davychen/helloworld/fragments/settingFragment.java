package com.example.davychen.helloworld.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.account;
import com.example.davychen.helloworld.password_reset;
import com.example.davychen.helloworld.trans_password_reset;

/**
 * A simple {@link Fragment} subclass.
 */
public class settingFragment extends Fragment {
    private account parentAct;
    private View mView;

    public static settingFragment newInstance(account act) {
        settingFragment f = new settingFragment();
        f.parentAct = act;
        return f;
    }
    public settingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.mView = inflater.inflate(R.layout.fragment_setting, container, false);
        final Button password_reset = mView.findViewById(R.id.password_reset);
        Button trans_pwd_reset = mView.findViewById(R.id.trans_pwd_reset);
        password_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reset = new Intent(parentAct, password_reset.class);
                reset.putExtra("Message",parentAct.nin);
                parentAct.startActivity(reset);
            }
        });

        trans_pwd_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reset = new Intent(parentAct, trans_password_reset.class);
                reset.putExtra("Message",parentAct.nin);
                parentAct.startActivity(reset);
            }
        });

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Settings");
    }

}

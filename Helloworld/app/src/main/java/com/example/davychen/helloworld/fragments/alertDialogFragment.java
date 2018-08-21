package com.example.davychen.helloworld.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.account;
import com.example.davychen.helloworld.adapters.payeeItemAdapter;
import com.example.davychen.helloworld.items.payee_item;
import com.example.davychen.helloworld.services.getRecipients;

import java.util.ArrayList;

import static android.view.View.GONE;

public class alertDialogFragment extends DialogFragment implements payeeMaintenanceInterface{

    private account parentAct;
    private ArrayList<payee_item> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private payeeItemAdapter adapter;
    private SwipeRefreshLayout mRefreshLayout;


    public transfer frag;

    public static alertDialogFragment newInstance(account act, transfer frag){
        alertDialogFragment instance = new alertDialogFragment();
        instance.parentAct = act;
        instance.frag = frag;
        return instance;
    }

    public alertDialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View v = inflater.inflate(R.layout.fragment_payee_maintenance, null);
        this.recyclerView = v.findViewById(R.id.payeeRecyclerView);

        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.adapter = new payeeItemAdapter(this.list, getContext(),alertDialogFragment.this, frag);
        this.recyclerView.setAdapter(adapter);

        this.mRefreshLayout = v.findViewById(R.id.payeeSwipeRefreshLayout);
        this.mRefreshLayout.setEnabled(false);

        Button addNewPayee= v.findViewById(R.id.addNewPayeeButton);
        addNewPayee.setVisibility(GONE);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Thread(new getRecipients(alertDialogFragment.this)).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        window.setLayout(width, height);
        //window.setGravity(Gravity.CENTER);

    }

    public ArrayList<payee_item> getList() {
        return list;
    }

    @Override
    public account getParentAct() {
        return this.parentAct;
    }

    public payeeItemAdapter getAdapter() {
        return this.adapter;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return this.mRefreshLayout;
    }

}

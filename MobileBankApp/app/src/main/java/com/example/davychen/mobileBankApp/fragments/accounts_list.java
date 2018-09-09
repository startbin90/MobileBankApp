package com.example.davychen.mobileBankApp.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.adapters.accountItemAdapter;
import com.example.davychen.mobileBankApp.items.account_item;
import com.example.davychen.mobileBankApp.services.retrieveAccountInfo;

import java.util.List;


/**
 * fragment showing accounts list
 * subclass of BottomSheetDialogFragment since it will be called as a BottomSheetDialog Fragment
 * also a subclass of Fragment
 */
public class accounts_list extends BottomSheetDialogFragment  {
    /**
     * reference to itemLst in account activity
     */
    List<account_item> list;
    account parentAct;
    public accountItemAdapter adapter;
    public SwipeRefreshLayout mRefreshLayout;

    public accounts_list() {
        // Required empty public constructor
    }

    public static accounts_list newInstance(account act) {
        accounts_list f = new accounts_list();
        f.parentAct = act;
        f.list = act.itemLst;
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and set recycler view, recycler list view adapter
        // and refresh layout
        View v = inflater.inflate(R.layout.fragment_accounts_list, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.accounts_recyclerView);

        recyclerView.addItemDecoration(new DividerItemDecoration(parentAct, DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.adapter = new accountItemAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        this.mRefreshLayout = v.findViewById(R.id.accounts_list_swipeRefreshLayout);
        mRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary
                , R.color.green
                , R.color.dividerGrey
                , R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveAccountInfo task= new retrieveAccountInfo(parentAct);
                task.execute();
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // retrieve account info when view is created
        retrieveAccountInfo task= new retrieveAccountInfo(parentAct);
        task.execute();
    }
}

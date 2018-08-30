package com.example.davychen.mobileBankApp.fragments;


import android.os.AsyncTask;
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
import com.example.davychen.mobileBankApp.services.setAccountInfo;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class accounts_list extends BottomSheetDialogFragment  {

    List<account_item> list;
    private RecyclerView recyclerView;
    account parentAct;
    public accountItemAdapter adapter;
    public SwipeRefreshLayout mRefreshLayout;
    //BottomSheetOnItemClickedListener mListener;
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_accounts_list, container, false);
        this.recyclerView = v.findViewById(R.id.accounts_recyclerView);

        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        this.recyclerView.setHasFixedSize(true);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.adapter = new accountItemAdapter(list, getActivity());
        this.recyclerView.setAdapter(adapter);

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
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Accounts");
        retrieveAccountInfo task= new retrieveAccountInfo(parentAct);
        task.execute();
    }
}

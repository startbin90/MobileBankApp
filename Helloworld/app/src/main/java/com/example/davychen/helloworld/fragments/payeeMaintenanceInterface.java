package com.example.davychen.helloworld.fragments;

import android.support.v4.widget.SwipeRefreshLayout;

import com.example.davychen.helloworld.account;
import com.example.davychen.helloworld.adapters.payeeItemAdapter;
import com.example.davychen.helloworld.items.payee_item;

import java.util.ArrayList;

public interface payeeMaintenanceInterface{

    account getParentAct();

    ArrayList<payee_item> getList();

    payeeItemAdapter getAdapter();

    SwipeRefreshLayout getRefreshLayout();
}

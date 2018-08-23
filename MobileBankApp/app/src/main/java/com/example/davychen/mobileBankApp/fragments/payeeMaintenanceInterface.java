package com.example.davychen.mobileBankApp.fragments;

import android.support.v4.widget.SwipeRefreshLayout;

import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.adapters.payeeItemAdapter;
import com.example.davychen.mobileBankApp.items.payee_item;

import java.util.ArrayList;

public interface payeeMaintenanceInterface{

    account getParentAct();

    ArrayList<payee_item> getList();

    payeeItemAdapter getAdapter();

    SwipeRefreshLayout getRefreshLayout();
}

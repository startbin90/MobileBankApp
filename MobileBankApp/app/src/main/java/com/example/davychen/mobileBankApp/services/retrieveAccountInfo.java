package com.example.davychen.mobileBankApp.services;

import android.os.AsyncTask;

import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.fragments.accounts_list;
import com.example.davychen.mobileBankApp.myIO;
import com.example.davychen.mobileBankApp.returnMessage;

import java.lang.ref.WeakReference;

public class retrieveAccountInfo extends AsyncTask<Void, Void ,Integer> {
    private WeakReference<account> wrap;
    private byte[] msg = null;
    private String nin;
    public retrieveAccountInfo(account act) {
        this.wrap = new WeakReference<>(act);
        this.nin = act.nin;
    }

    public retrieveAccountInfo(account act, byte[] msg) {
        this.wrap = new WeakReference<>(act);
        this.msg = msg;
    }

    @Override
    protected void onPostExecute(Integer ret) {
        new errDecode(ret, wrap.get()).run();
        if (wrap.get().current_fragment instanceof accounts_list){
            ((accounts_list)wrap.get().current_fragment).adapter.notifyDataSetChanged();
            ((accounts_list)wrap.get().current_fragment).mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        if (msg == null){
            returnMessage ret = new GeneralRequestService(
                    10, myIO.toBytes(this.nin, 18)).call();
            if (ret.getRet() == 0){
                msg = ret.getMessage();
            }else{
                return ret.getRet();
            }
        }
        return new setAccountInfo(wrap.get(), msg).call();
    }
}

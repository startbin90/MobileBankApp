package com.example.davychen.helloworld.services;

import android.util.Log;

import com.example.davychen.helloworld.account_detail;
import com.example.davychen.helloworld.items.transaction_detail_item;
import com.example.davychen.helloworld.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class transDetailService implements Runnable {

    private String account_num;
    private long start, end;
    private account_detail act;
    private int err;
    private static String TAG = "transDetailService";

    public transDetailService(account_detail act) {
        this.act = act;
        this.start = act.getStart_time();
        this.end = act.getEnd_time();
        this.account_num = act.account_num;
    }

    @Override
    public void run() {
        //set more to be load since this class is going to load some data
        //and set it true will make progressbar layout invisible when loading
        act.adapter.setLoading();
        byte[] account = myIO.toBytes(this.account_num, 8);
        byte[] startLong = myIO.longToBytes(this.start);
        byte[] endLong = myIO.longToBytes(this.end);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ArrayList<transaction_detail_item> received = new ArrayList<>();
        try {
            outputStream.write(account);
            outputStream.write(startLong);
            outputStream.write(endLong);
            byte mes[] = outputStream.toByteArray();
            byte[] ret = myIO.toServer(6, mes);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
            if (err == 0 || err == 1){
                int count = myIO.bytesToInt(Arrays.copyOfRange(ret, 4, 8));
                for (int i = 0; i < count; i++){
                    //trans_id size 10
                    String trans_id = myIO.bytesToString(ret, 8 + 154 * i, 10);
                    //trans_date size 8
                    long trans_date = myIO.bytesToLong(ret, 18 + 154 * i, 8);
                    //trans_from size 8
                    String trans_from = myIO.bytesToString(ret, 26 + 154 * i, 8);
                    //trans_to size 8
                    String trans_to = myIO.bytesToString(ret, 34 + 154 * i, 8);
                    //trans_to_last_name size 10
                    String trans_to_lname = myIO.bytesToString(ret, 42 + 154 * i, 10);
                    //trans_dir size 1
                    char trans_dir = (char)ret[52 + 154 * i];
                    //trans_value size 4
                    float trans_value = myIO.bytesToFloat(ret, 53 + 154 * i, 4);
                    //trans_post_balance size 4
                    float trans_post_balance = myIO.bytesToFloat(ret, 57 + 154 * i, 4);
                    //trans_channel size 1
                    char trans_channel = (char)ret[61 + 154 * i];
                    //trans_meme size 100
                    String trans_memo = myIO.bytesToString(ret, 62 + 154 * i, 100);
                    transaction_detail_item item = new transaction_detail_item(trans_id, trans_date, trans_from, trans_to,
                            trans_to_lname, trans_dir, trans_value, trans_post_balance,
                            trans_channel, trans_memo);
                    if (i != 0){
                        received.add(item);
                    }else if (act.lst.isEmpty()){
                        received.add(item);
                    }else if (!act.lst.get(act.lst.size() - 1).trans_id.equals(trans_id)){
                        received.add(item);
                    }

                }

            }
        } catch (IOException e) {
            err = 7;
        }
        //check if this runnable is the latest runnable which asks for data
        if (transDetailService.this == act.currentRunner){
            act.lst.addAll(received);
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    act.adapter.notifyDataSetChanged();
                    act.adapter.setLoaded();
                    if (err == 1){
                        act.adapter.setMoreToBeLoad(true);
                        act.setEnd_time(act.lst.get(act.lst.size() - 1).trans_date);
                    }else{
                        act.adapter.setMoreToBeLoad(false);
                    }
                    act.mRefreshLayout.setRefreshing(false);

                }
            });
            switch (err) {
                case 0:
                    Log.d(TAG, "no more data for this time range");
                    break;
                case 1:
                    Log.d(TAG, "more data to be retrieved");
                    break;
            }
            act.runOnUiThread(new errDecode(err, act));
        }
    }



    public static void main(String[] args) {
        int i = 0;
        ArrayList lst = new ArrayList();
        String trans_id ="123";
        System.out.println(i != 0 || ((!lst.isEmpty()) && (!lst.get(lst.size() - 1).equals(trans_id))));

    }
}

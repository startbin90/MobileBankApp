package com.example.davychen.mobileBankApp.services;

import android.support.v4.app.Fragment;

import com.example.davychen.mobileBankApp.fragments.payeeMaintenanceInterface;
import com.example.davychen.mobileBankApp.items.payee_item;
import com.example.davychen.mobileBankApp.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class getRecipients implements Runnable {

    private Fragment frag;
    private String email;
    private int err;

    public getRecipients(Fragment frag) {
        this.frag = frag;
        if (frag instanceof payeeMaintenanceInterface) {
            this.email = ((payeeMaintenanceInterface) frag).getParentAct().email;
        }

    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(myIO.toBytes(email,50));
            byte mes[] = outputStream.toByteArray( );
            byte[] ret = myIO.toServer(9, mes);
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
            ret = Arrays.copyOfRange(ret, 4, ret.length);
            if (err == 0){
                ArrayList<payee_item> temp = new ArrayList<>();
                int count = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
                for (int i = 0; i < count; i++){
                    int start = 4;
                    String num = myIO.bytesToString(ret, start + i * 28, 8);
                    String first_name = myIO.bytesToString(ret, start + 8 + i * 28, 10);
                    String last_name = myIO.bytesToString(ret, start + 16 + i * 28, 10);

                    temp.add(new payee_item(num, first_name, last_name));
                }
                if (frag instanceof payeeMaintenanceInterface) {
                    ((payeeMaintenanceInterface) frag).getList().addAll(temp);
                }
            }

        } catch (IOException e) {
            err  = 7;
        }
        if (frag instanceof payeeMaintenanceInterface){
            ((payeeMaintenanceInterface)frag).getParentAct().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((payeeMaintenanceInterface)frag).getAdapter().notifyDataSetChanged();
                    ((payeeMaintenanceInterface)frag).getRefreshLayout().setRefreshing(false);
                }
            });
            ((payeeMaintenanceInterface)frag).getParentAct().runOnUiThread(new errDecode(err, ((payeeMaintenanceInterface) frag).getParentAct()));
        }
    }
}

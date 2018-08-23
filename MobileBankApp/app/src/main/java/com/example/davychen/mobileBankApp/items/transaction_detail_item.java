package com.example.davychen.mobileBankApp.items;


public class transaction_detail_item {

    public String trans_id;
    public long trans_date;
    public String trans_from;
    public String trans_to;
    public String trans_to_lname;
    public char trans_dir;
    public float trans_value;
    public float trans_post_balance;
    public char trans_channel;
    public String trans_memo;


    public transaction_detail_item(String trans_id, long trans_date, String trans_from, String trans_to, String trans_to_lname, char trans_dir, float trans_value, float trans_post_balance, char trans_channel, String trans_memo) {
        this.trans_id = trans_id;
        this.trans_date = trans_date;
        this.trans_from = trans_from;
        this.trans_to = trans_to;
        this.trans_to_lname = trans_to_lname;
        this.trans_dir = trans_dir;
        this.trans_value = trans_value;
        this.trans_post_balance = trans_post_balance;
        this.trans_channel = trans_channel;
        this.trans_memo = trans_memo;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public long getTrans_date() {
        return trans_date;
    }

    public String getTrans_from() {
        return trans_from;
    }

    public String getTrans_to() {
        return trans_to;
    }

    public String getTrans_to_lname() {
        return trans_to_lname;
    }

    public char getTrans_dir() {
        return trans_dir;
    }

    public float getTrans_value() {
        return trans_value;
    }

    public float getTrans_post_balance() {
        return trans_post_balance;
    }

    public char getTrans_channel() {
        return trans_channel;
    }

    public String getTrans_memo() {
        return trans_memo;
    }


}

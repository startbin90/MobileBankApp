package com.example.davychen.mobileBankApp.items;

public class payee_item {
    private String account_num;
    private String first_name;
    private String last_name;
    private float balance;
    private int type;

    public payee_item(String account_num, String first_name, String last_name, float balance) {
        this.account_num = account_num;
        this.first_name = first_name;
        this.last_name = last_name;
        this.balance = balance;
        this.type = 1; // linked account
    }
    public payee_item(String account_num, String first_name, String last_name) {
        this.account_num = account_num;
        this.first_name = first_name;
        this.last_name = last_name;
        this.type = 0; // payee_item
    }

    public payee_item(int type) {
        this.type = type; // 2 linked account title 3 payee_item title
    }

    public String getAccount_num() {
        return account_num;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setAccount_num(String account_num) {
        this.account_num = account_num;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public int getType() {
        return type;
    }

    public float getBalance() {
        return balance;
    }
}

package com.example.davychen.helloworld.items;

public class account_item {
    private String account_num;
    private float balance;
    private String first_name;
    private String last_name;



    public account_item(String account_num, float balance, String first_name, String last_name) {
        this.account_num = account_num;
        this.balance = balance;
        this.first_name = first_name;
        this.last_name = last_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getAccount_num() {
        return account_num;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return account_num + "    Balance: " + String.valueOf(balance);
    }
}


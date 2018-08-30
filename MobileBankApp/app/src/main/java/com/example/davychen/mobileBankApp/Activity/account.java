package com.example.davychen.mobileBankApp.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.adapters.accountItemAdapter;
import com.example.davychen.mobileBankApp.adapters.transDetailItemAdapter;
import com.example.davychen.mobileBankApp.fragments.accountAdditionFragment;
import com.example.davychen.mobileBankApp.fragments.accounts_list;
import com.example.davychen.mobileBankApp.fragments.payeeMaintenance;
import com.example.davychen.mobileBankApp.fragments.personalProfileFragment;
import com.example.davychen.mobileBankApp.fragments.settingFragment;
import com.example.davychen.mobileBankApp.fragments.transfer;
import com.example.davychen.mobileBankApp.items.account_item;
import com.example.davychen.mobileBankApp.items.transaction_detail_item;
import com.example.davychen.mobileBankApp.myIO;
import com.example.davychen.mobileBankApp.services.retrieveAccountInfo;
import com.example.davychen.mobileBankApp.services.setAccountInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Activity started after Log in.
 * This activity is control of the navigation drawer layout
 *
 */
public class account extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        accountItemAdapter.BottomSheetOnItemClickedListener {
    /**
     * attributes documents client's personal info
     */
    public String email;
    public String nick_name;
    public char sex;
    public String nin;
    public String cell;
    public String address;
    /**
     * client's accounts list
     */
    public ArrayList<account_item> itemLst = new ArrayList<>();
    private static String TAG = "account_activity";
    /**
     * current fragment loaded
     */
    public Fragment current_fragment = null;

    /**
     * onCreate method gets the client's personal info and account list from intent object and
     * sets related view control
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        byte[] msg = getIntent().getByteArrayExtra("Message");
        if (msg != null) {
            retrieveAccountInfo task= new retrieveAccountInfo(this, msg);
            task.execute();
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //navigation view header onClickListener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0);
        nav_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySelectedScreen(R.id.nav_header);
            }
        });

        //default screen
        displaySelectedScreen(R.id.accounts);
        navigationView.getMenu().getItem(0).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
        TextView navGreeting = headerView.findViewById(R.id.nav_header_greeting);
        String text = String.format(getResources().getString(R.string.greeting), nick_name);
        navGreeting.setText(text);
    }

    /**
     * show dialog when Back is pressed in this activity
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setMessage("Quit?");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            account.super.onBackPressed();
                        }
                    });
            alertDialog.show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //calling the method displaySelectedScreen and passing the id of selected menu
        displaySelectedScreen(id);
        //make this method blank
        return true;

    }

    /**
     * set current_fragment and commit changes
     */
    public void displaySelectedScreen(int itemId) {
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.accounts:
                current_fragment = accounts_list.newInstance(this);
                this.setTitle("Linked accounts list");
                break;
            case R.id.transfer:
                current_fragment = transfer.newInstance(this);
                this.setTitle("Transfer");
                break;
            case R.id.payeeMaintenance:
                current_fragment = payeeMaintenance.newInstance(this);
                this.setTitle("Payee maintenance");
                break;
            case R.id.moreAccount:
                current_fragment = accountAdditionFragment.newInstance(this);
                this.setTitle("Link more accounts");
                break;
            case R.id.nav_header:
                current_fragment = personalProfileFragment.newInstance(this);
                this.setTitle("Personal profile");
                break;
            case R.id.settings:
                current_fragment = settingFragment.newInstance(this);
                this.setTitle("Settings");
                break;
            case R.id.logout:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setMessage("Log out?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent log_out = new Intent(account.this, MainActivity.class);
                                startActivity(log_out);
                                account.this.finish();
                            }
                        });
                alertDialog.show();
                break;

        }

        //replacing the fragment
        if (current_fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_holder, current_fragment);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onItemClicked(String account, String balance, String first_name, String last_name) {
        if (current_fragment instanceof accounts_list){
            Intent detail = new Intent(account.this, account_detail.class);
            detail.putExtra("ACCOUNT_NUM", account);
            detail.putExtra("BALANCE", balance);
            detail.putExtra("first_name", first_name);
            detail.putExtra("last_name", last_name);
            account.this.startActivityForResult(detail, 1);
        }else if (current_fragment instanceof transfer){
            ((transfer) current_fragment).onPaymentAccountSelected(
                    new account_item(account, Float.parseFloat(balance), first_name, last_name));
        }
    }

    /**
     * account_detail activity will be called from account activity. User can navigate from account_
     * detail activity to transfer fragment of account activity. account_detail will bring data back
     * which will be received by this method.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // called from redo button in transaction detail dialog from account detail activity
        // brings transaction detail data back
        if (requestCode == 1 && resultCode == 10){
            //search for this from_account in itemLst, making sure this account is in the account
            // list
            String from_account = data.getStringExtra("from_account");
            account_item from = null;
            for (account_item each: itemLst){
                if (each.getAccount_num().equals(from_account)){
                    from = each;
                }
            }
            String to_account = data.getStringExtra("to_account");
            String to_first_name = data.getStringExtra("to_first_name");
            String to_last_name = data.getStringExtra("to_last_name");
            float value = data.getFloatExtra("value", 0);
            String memo = data.getStringExtra("memo");

            if (from != null){
                // initialize transfer fragment and set the current_fragment and begin fragment
                // transaction
                current_fragment = transfer.newInstance(this,
                        new transaction_detail_item(to_account, to_first_name, to_last_name, value, memo),
                        from);
                if (current_fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_holder, current_fragment);
                    ft.commit();
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(1).setChecked(true);
            }


        }else if (requestCode == 1 && resultCode == 11){
            // called from transfer button in account detail activity
            // brings account number back
            String from_account = data.getStringExtra("from_account");
            account_item from = null;
            // search for this account number in list
            for (account_item each: itemLst){
                if (each.getAccount_num().equals(from_account)){
                    from = each;
                }
            }

            if (from != null){
                current_fragment = transfer.newInstance(this, null, from);
                if (current_fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_holder, current_fragment);
                    ft.commit();
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(1).setChecked(true);
            }


        }
    }
}

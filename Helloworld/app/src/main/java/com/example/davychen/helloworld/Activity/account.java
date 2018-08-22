package com.example.davychen.helloworld.Activity;

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

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.adapters.accountItemAdapter;
import com.example.davychen.helloworld.fragments.accountAdditionFragment;
import com.example.davychen.helloworld.fragments.accounts_list;
import com.example.davychen.helloworld.fragments.payeeMaintenance;
import com.example.davychen.helloworld.fragments.personalProfileFragment;
import com.example.davychen.helloworld.fragments.settingFragment;
import com.example.davychen.helloworld.fragments.transfer;
import com.example.davychen.helloworld.items.account_item;
import com.example.davychen.helloworld.myIO;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class account extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        accountItemAdapter.BottomSheetOnItemClickedListener {

    public String email;
    public String nick_name;
    public char sex;
    public String nin;
    public String cell;
    public String addr;
    public ArrayList<account_item> itemLst = new ArrayList<>();
    private static String TAG = "account_activity";
    private Fragment current_fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        byte[] msg = getIntent().getByteArrayExtra("Message");
        if (msg != null) {
            ArrayList<byte[]> ret = myIO.bytesArrayDivider(msg, 50, 10, 1, 18, 15, 100, 4);
            if (ret.size() >= 7){
                email = new String(ret.get(0)).trim();
                nick_name = new String(ret.get(1)).trim();
                sex = (char) ret.get(2)[0];
                nin =  new String(ret.get(3)).trim();
                cell = new String(ret.get(4)).trim();
                addr = new String(ret.get(5)).trim();
                int count = myIO.bytesToInt(ret.get(6));
                if (count > 0){
                    byte[] accountLists = ret.get(7);
                    for (int i = 0; i < count; i++){
                        String num = myIO.bytesToString(accountLists,  i * 32, 8);
                        float balance = ByteBuffer.wrap(Arrays.copyOfRange(accountLists, 8 + i * 32, 12 + i * 32)).getFloat();
                        String first_name = myIO.bytesToString(accountLists,  12 + i * 32, 10);
                        String last_name = myIO.bytesToString(accountLists,  22 + i * 32, 10);
                        itemLst.add(new account_item(num, balance, first_name, last_name));
                    }
                }else{
                    Log.i(TAG, "no linked account, which is impossible, error must occurred");
                }
            }else{
                Log.e(TAG, "the size of returned ArrayList by bytesArrayDivider not correct");
            }

        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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
        displaySelectedScreen(R.id.transfer);

        View headerView = navigationView.getHeaderView(0);
        TextView navGreeting = headerView.findViewById(R.id.nav_header_greeting);
        String text = String.format(getResources().getString(R.string.greeting), nick_name);
        navGreeting.setText(text);
    }

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

    private void displaySelectedScreen(int itemId) {
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.accounts:
                current_fragment = accounts_list.newInstance(this);
                break;
            case R.id.transfer:
                current_fragment = transfer.newInstance(this);
                break;
            case R.id.payeeMaintenance:
                current_fragment = payeeMaintenance.newInstance(this);
                break;
            case R.id.moreAccount:
                current_fragment = accountAdditionFragment.newInstance(this);
                break;
            case R.id.nav_header:
                current_fragment = personalProfileFragment.newInstance(this);
                break;
            case R.id.settings:
                current_fragment = settingFragment.newInstance(this);
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
            account.this.startActivity(detail);
        }else if (current_fragment instanceof transfer){
            ((transfer) current_fragment).onPaymentAccountSelected(
                    new account_item(account, Float.parseFloat(balance), first_name, last_name));
        }
    }
}

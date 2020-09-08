package com.example.smsblocker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    TabLayout tabLayout;
    RecyclerView recyclerView;
    SMS_Adapter sms_adapter;
    ArrayList<String> all_sms;
    ArrayList<String> blocked_sms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        tabLayout = findViewById(R.id.tabLayout);
        all_sms = new ArrayList<String>();
        all_sms.addAll(Arrays.asList("abebe","chaltu","lama","lemi","magarsa"));

        blocked_sms = new ArrayList<String>();
        blocked_sms.addAll(Arrays.asList("saksamu","fake","safsaf","tebelah","girma girma"));


        ActionBarDrawerToggle toggler = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggler);
        toggler.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.recycler_layout);
        sms_adapter = new SMS_Adapter(all_sms);
        recyclerView.setAdapter(sms_adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 1) {
                            sms_adapter = new SMS_Adapter(blocked_sms);
                        } else {
                            all_sms.clear();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                final String myPackageName = getPackageName();
                                if (!Telephony.Sms.getDefaultSmsPackage(getApplication()).equals(myPackageName)) {

                                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                                    startActivityForResult(intent, 1);
                                } else {
                                    List<SMS_Model> lst = getAllSms();
                                    for (SMS_Model l :
                                            lst) {
                                        all_sms.add(l.getMsg());
                                    }
                                }
                            } else {
                                List<SMS_Model> lst = getAllSms();
                                for (SMS_Model l :
                                        lst) {
                                    all_sms.add(l.getMsg());
                                }
                            }
//                                    Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"),
//                                            null, null, null, null);
//
//                                    if (cursor.moveToFirst()) { // must check the result to prevent exception
//                                        do {
//                                            String msgData = "";
//                                            for(int idx=0;idx<cursor.getColumnCount();idx++)
//                                            {
//                                                msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
//                                            }
//
//                                            all_sms.add(msgData);
//                                            // use msgData
//                                        } while (cursor.moveToNext());
//                                    } else {
//                                        // empty box, no SMS
//                                    }
                        }
                        sms_adapter = new SMS_Adapter(all_sms);
                        recyclerView.setAdapter(sms_adapter);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                }
        );

    }

    public List<SMS_Model> getAllSms() {
        List<SMS_Model> lstSms = new ArrayList<SMS_Model>();
        SMS_Model objSms = new SMS_Model();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        this.startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new SMS_Model();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.set_folderName("inbox");
                } else {
                    objSms.set_folderName("sent");
                }

                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();

        return lstSms;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final String myPackageName = getPackageName();
                    if (Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                        List<SMS_Model> lst = getAllSms();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_updateDB:
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(this,"Database is Updating", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_share:
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(this,"Share the app with intent", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_blocked:
                drawer.closeDrawer(GravityCompat.START);
                tabLayout.selectTab(tabLayout.getTabAt(1));
                break;
            case R.id.nav_message:
                drawer.closeDrawer(GravityCompat.START);
                tabLayout.selectTab(tabLayout.getTabAt(0));
                break;
            default:
                break;
        }
        return true;
    }


}
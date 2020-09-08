package com.example.smsblocker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static Boolean read_perm_granted = false;
    final int REQUEST_CODE_ASK_PERMISSION = 123;
    final int REQUEST_CODE_ASK_DEFAULT = 3343;

    DrawerLayout drawer;
    TabLayout tabLayout;
    RecyclerView recyclerView;
    SMS_Adapter sms_adapter;
    ArrayList<SMS_Model> all_sms;
    ArrayList<SMS_Model> blocked_sms;
    PhoneStateListener pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        tabLayout = findViewById(R.id.tabLayout);

        askSIMSReadPermissions();

        all_sms = new ArrayList<>();
        SMS_Model d = new SMS_Model();
        d.setAddress("Error");
        if (getAllSms() == null) {
            all_sms.add(d);
        } else {
            all_sms.addAll(getAllSms());
        }


        blocked_sms = new ArrayList<SMS_Model>();
        SMS_Model fake = new SMS_Model();
        fake.setAddress("None");
        fake.setMsg("No blocked messages yet.");
        blocked_sms.add(fake);


        ActionBarDrawerToggle toggler = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggler);
        toggler.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        recyclerView = findViewById(R.id.recycler_layout);
        sms_adapter = new SMS_Adapter(all_sms);
        recyclerView.setAdapter(sms_adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        recyclerView.setAdapter(new SMS_Adapter(all_sms));
                        break;
                    case 1:
                        recyclerView.setAdapter(new SMS_Adapter(blocked_sms));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    public void askSIMSReadPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
                //Store default sms package name
                Intent setSmsAppIntent =
                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        getPackageName());
                startActivityForResult(setSmsAppIntent, REQUEST_CODE_ASK_DEFAULT);
            } else {
                if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS")
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSION);
                } else {
                    read_perm_granted = true;
                }
                Toast.makeText(MainActivity.this, "Already default", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "not default", Toast.LENGTH_LONG).show();
        }

    }

    public List<SMS_Model> getAllSms() {
        List<SMS_Model> lstSms = new ArrayList<>();
        if (read_perm_granted) {
            Uri uri = Uri.parse("content://sms/inbox");
            String[] all = new String[]{"_id", "address", "date", "body"};
            Cursor c = getContentResolver().query(uri, all, null, null, null);
            try {
                if (c.moveToFirst()) {
                    while (c.moveToNext()) {
                        String id = c.getString(0);
                        String address = c.getString(1);
                        String date = c.getString(2);
                        String body = c.getString(3);
                        SMS_Model newSms = new SMS_Model();
                        newSms.setId(id);
                        newSms.setAddress(address);
                        newSms.setMsg(body);
                        String dateFormat = new SimpleDateFormat("MM/dd/yyyy")
                                .format(new Date(Long.parseLong(date)));
                        newSms.setDate(dateFormat);
                        lstSms.add(newSms);
                    }
                }
                c.close();
            } catch (NullPointerException npe) {
                Log.d("No Message", "no message found in the inbox");
            }
            return lstSms;
        } else {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_ASK_DEFAULT:
                Toast.makeText(MainActivity.this, "app become default result", Toast.LENGTH_LONG).show();
                all_sms = new ArrayList<>();
                SMS_Model d = new SMS_Model();
                d.setAddress("Error");
                if (getAllSms() == null) {
                    all_sms.add(d);
                } else {
                    all_sms.addAll(getAllSms());
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!read_perm_granted) {
            askSIMSReadPermissions();
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
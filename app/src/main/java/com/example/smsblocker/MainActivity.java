package com.example.smsblocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Telephony;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.smsblocker.Threading.FetchBlockedSms;
import com.example.smsblocker.Threading.SaveBlockedSms;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks {

    final int REQUEST_CODE_ASK_PERMISSION = 123;
    final int REQUEST_CODE_ASK_DEFAULT = 3343;

    DrawerLayout drawer;
    TabLayout tabLayout;
    RecyclerView recyclerView;
    SMS_Adapter sms_adapter;

    ArrayList<SMS_Model> all_sms;
    ArrayList<SMS_Model> blocked_sms;
    ArrayList<SMS_Model> array_adapter;

    TabLayout.OnTabSelectedListener onTabSelectedListener;

    DatabaseManager db_manager;
    SMS_Model blockedSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db_manager = DatabaseManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        tabLayout = findViewById(R.id.tabLayout);

        ActionBarDrawerToggle toggler = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggler);
        toggler.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        array_adapter = new ArrayList<>();
        all_sms = new ArrayList<>();
        blocked_sms = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_layout);
        sms_adapter = new SMS_Adapter(array_adapter, this);
        recyclerView.setAdapter(sms_adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        addTabListener();

        getAllSms();
        fetchBlockedMessages();


    }

    public void fetchBlockedMessages() {
        int fetchId = 1;
        getSupportLoaderManager().initLoader(fetchId, null, this);
    }

    public void saveBlockedSms(SMS_Model sms) {
        int saveId = 0;
        blockedSms = sms;
        getSupportLoaderManager().initLoader(saveId, null, this);

    }

    public void getAllSms() {
        if (checkPermission()) {
            List<SMS_Model> lstSms = new ArrayList<>();
            Uri uri = Uri.parse("content://sms/inbox");
            String[] all = new String[]{"_id", "address", "date", "body"};
            Cursor c = getContentResolver().query(uri, all, null,
                    null, "_id DESC");
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

            all_sms.clear();
            all_sms.addAll(lstSms);
            updateRecyclerView(all_sms);

        } else {
            requestSmsPermission();
        }
    }

    public void updateRecyclerView(ArrayList<SMS_Model> all) {
        array_adapter.clear();
        array_adapter.addAll(all);
        sms_adapter.notifyDataSetChanged();
    }

    public Boolean checkPermission() {
        if ((ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS")
                == PackageManager.PERMISSION_GRANTED) && (Telephony.Sms.getDefaultSmsPackage(getApplicationContext())
                .equals(getApplicationContext().getPackageName()))) {
            return true;
        } else {
            return false;
        }
    }

    public void requestSmsPermission() {
        Intent setSmsAppIntent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                getPackageName());
        startActivityForResult(setSmsAppIntent, REQUEST_CODE_ASK_DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ASK_DEFAULT) {
            if (checkPermission()) {
                getAllSms();
            } else {
                Toast.makeText(MainActivity.this, "Please set the app as default sms app!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            getAllSms();
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
    protected void onDestroy() {
        super.onDestroy();
        removeTabListener();
        DatabaseManager.getInstance(this).closeDatabase();
    }

    public void addTabListener() {
        onTabSelectedListener = (new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        if (!checkPermission()) {
                            requestSmsPermission();
                        } else {
                            if (all_sms.isEmpty()) {
                                getAllSms();
                            } else {
                                updateRecyclerView(all_sms);
                            }
                        }
                        break;
                    case 1:
                        updateRecyclerView(blocked_sms);
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

        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }

    public void removeTabListener() {
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_updateDB:
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Database is Updating", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_share:
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Share the app with intent", Toast.LENGTH_LONG).show();
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

    public void askSIMSReadPermissions() {
//        if ((ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS")
//                != PackageManager.PERMISSION_GRANTED) || (!Telephony.Sms.getDefaultSmsPackage(getApplicationContext())
//                .equals(getApplicationContext().getPackageName())))  {
//                //Store default sms package name
//                Intent setSmsAppIntent =
//                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
//                setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
//                        getPackageName());
//                startActivityForResult(setSmsAppIntent, REQUEST_CODE_ASK_DEFAULT);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            if (!Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
//                //Store default sms package name
//                Intent setSmsAppIntent =
//                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
//                setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
//                        getPackageName());
//                startActivityForResult(setSmsAppIntent, REQUEST_CODE_ASK_DEFAULT);
//            } else {
//
//            }
//        } else {
//            Toast.makeText(MainActivity.this, "not default", Toast.LENGTH_LONG).show();
//        }

    }


    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 1) {
            Toast.makeText(MainActivity.this, "async started", Toast.LENGTH_LONG).show();
            return new FetchBlockedSms(this);
        } else {
            return new SaveBlockedSms(this, blockedSms);
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        if (data != null) {
            blocked_sms.clear();
            blocked_sms.addAll(((ArrayList<SMS_Model>) data));
        } else {
            blocked_sms.add(blockedSms);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_selected, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        super.onContextItemSelected(item);
        if (item.getItemId() == R.id.sms_menu) {
            Toast.makeText(getApplicationContext(), "Edit selected.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

}
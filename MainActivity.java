package com.nhtthuan.trackingbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.nhtthuan.trackingbus.Fragment.HomeFragment;
import com.nhtthuan.trackingbus.Fragment.ListBusFragment;
import com.nhtthuan.trackingbus.Fragment.ProfileFragment;
import com.nhtthuan.trackingbus.Fragment.SearchFragment;
import com.nhtthuan.trackingbus.Fragment.SocialFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selecterFragment = null;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        fragment = new HomeFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment).commit();

        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS" , MODE_PRIVATE).edit();
            editor.putString("profileid" , publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout , new ProfileFragment()).commit();
        } else {

            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout , new HomeFragment()).commit();

        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.nav_home) {
                        selecterFragment = new HomeFragment();
//                        selecterFragment = null;
//                        startActivity(new Intent(MainActivity.this, FindPathActivity.class));
                    } else if (itemId == R.id.nav_search) {
                        selecterFragment = new ListBusFragment();
                    } else if (itemId == R.id.nav_add) {
                        selecterFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                    } else if (itemId == R.id.nav_social) {
                        selecterFragment = new SocialFragment();
                    } else if (itemId == R.id.nav_profile) {
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selecterFragment = new ProfileFragment();
                    }

                    if (selecterFragment != null){
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout , selecterFragment).commit();
                    }

                    return true;
                }
            };
}

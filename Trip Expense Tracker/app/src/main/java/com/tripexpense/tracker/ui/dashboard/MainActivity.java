package com.tripexpense.tracker.ui.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tripexpense.tracker.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        if (nav != null) {
            nav.setOnItemSelectedListener(item -> {
                Fragment fragment = null;
                int id = item.getItemId();
                if (id == R.id.navigation_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.navigation_trips) {
                    fragment = new TripsFragment();
                } else if (id == R.id.navigation_profile) {
                    fragment = new ProfileFragment();
                }

                return loadFragment(fragment);
            });
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}

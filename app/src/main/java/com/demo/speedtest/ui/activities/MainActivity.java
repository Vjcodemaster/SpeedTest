package com.demo.speedtest.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.demo.speedtest.R;
import com.demo.speedtest.ui.fragments.SpeedTestFragment;
import com.demo.speedtest.databinding.ActivityMainBinding;
import com.demo.speedtest.ui.viewmodels.MainActivityVM;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mActivityMainBind;
    private MainActivityVM mMainActivityVM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMainBind = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        mMainActivityVM = new ViewModelProvider(MainActivity.this).get(MainActivityVM.class);
        mActivityMainBind.setMainVM(mMainActivityVM);
        mActivityMainBind.setLifecycleOwner(MainActivity.this);

        mActivityMainBind.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                openSpeedTestFragment();
                //openSpeedTestActivity();
                //openFragment();
            }
        });


    }

    private void openFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.add(R.id.flContainer, BlankFragment.newInstance("", ""));
        ft.addToBackStack(null);
        ft.commit();
    }

    private void openSpeedTestFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.add(R.id.flContainer, SpeedTestFragment.newInstance());
        ft.addToBackStack(null);
        ft.commit();
    }

    /*private void openSpeedTestActivity() {
        Intent in = new Intent(MainActivity.this, SpeedTestActivity.class);
        startActivity(in);
    }*/

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 1) {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mActivityMainBind.btnGo.setVisibility(View.VISIBLE);
                }
            }, 500);

        } else {
            super.onBackPressed();
        }
    }
}
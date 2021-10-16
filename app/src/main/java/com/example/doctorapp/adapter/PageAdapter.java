package com.example.doctorapp.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.doctorapp.fragments.AppointmentFragment;
import com.example.doctorapp.fragments.HomeFragment;
import com.example.doctorapp.fragments.ProfileFragment;

public class PageAdapter extends FragmentPagerAdapter {

//    private int numOfTabs;

    PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AppointmentFragment();
            case 1:
                return new HomeFragment();
            case 2:
                return new ProfileFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}

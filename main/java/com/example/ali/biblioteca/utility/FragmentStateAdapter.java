package com.example.ali.biblioteca.utility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 06.01.2018.
 */

public class FragmentStateAdapter extends FragmentStatePagerAdapter {
    public static final boolean disableAnimation = true;
    public static final int HOME_SEARCH_BOOK = 0;
    public static final int HOME_ADD_BOOK = 1;
    public static final int ADD_BOOK_COPY = 2;
    public static final int SEARCH_SHOW_BOOK = 3;
    public static final int HOME_ACCOUNT_INFO = 4;
    public static final int HOME_CHANGE_RULES = 5;
    public static final int HOME_MANAGE_ACCOUNTS = 6;
    public static final int BOOK_LOAN_TO = 7;
    public static final int HOME_HISTORY = 8;
    public static final int EDIT_BOOK = 9;
    public static final int HOME_BOOKS_LOANED = 10;

    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> fragmentTitle = new ArrayList<>();

    public FragmentStateAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitle.add(title);
    }

    public void replaceFragment(int fragmentId, Fragment fragment, String title) {
        fragmentList.remove(fragmentId);
        fragmentTitle.remove(fragmentId);
        fragmentList.add(fragmentId, fragment);
        fragmentTitle.add(fragmentId, title);
    }

    public List<Fragment> getFragmentList() {
        return fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}

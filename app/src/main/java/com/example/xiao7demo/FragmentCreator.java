package com.example.xiao7demo;

import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class FragmentCreator {

    public static final int ITEM_COUNT = 3;
    public static final int ITEM_FIRST = 0;
    public static final int ITEM_SECOND = 1;
    public static final int ITEM_THIRD = 2;

    private static final Map<Integer, Fragment> cache = new HashMap<>();

    public static Fragment getFragment(int index) {
        Fragment fragment = cache.get(index);
        if (fragment != null) {
            return fragment;
        }
        switch (index) {
            case ITEM_FIRST:
                fragment = new FirstFragment();
                break;
            case ITEM_SECOND:
                fragment = new SecondFragment();
                break;
            case ITEM_THIRD:
                fragment = new ThirdFragment();
                break;
            default:
        }
        assert fragment != null;
        cache.put(index, fragment);
        return fragment;
    }

}
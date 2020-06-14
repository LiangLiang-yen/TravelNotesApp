package com.TravelNotes.app.ui.TravelNotes.page1;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.TravelNotes.app.ui.TravelNotes.page1.tab1.CheckListFragment;
import com.TravelNotes.app.ui.TravelNotes.page1.tab2.MemoFragment;
import com.TravelNotes.app.ui.TravelNotes.page1.tab3.BuyListFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static int PAGE_NUM = 3;
    private static String pageName[] = {"裝備清單", "備忘錄", "購物紀錄"};
    private final long key;

    private FragmentManager mFragmentManager;
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public ViewPagerAdapter(@NonNull FragmentManager fm, long key) {
        super(fm);
        this.mFragmentManager = fm;
        this.key = key;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        final Fragment fragment;
        switch (position){
            case 0:
                fragment = CheckListFragment.newInstance(key);
                break;
            case 1:
                fragment = MemoFragment.newInstance(key);
                break;
            case 2:
                fragment = BuyListFragment.newInstance(key);
                break;
                default:
                    fragment = ErrorFragment.newInstance();
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageName[position];
    }

    @Override
    public int getCount() {
        return PAGE_NUM;
    }

    /**
     * On each Fragment instantiation we are saving the reference of that Fragment in a Map
     * It will help us to retrieve the Fragment by position
     *
     * @param container
     * @param position
     * @return
     */
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    /**
     * Remove the saved reference from our Map on the Fragment destroy
     *
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public interface FirstPageFragmentListener
    {
        void onSwitchToNextFragment();
    }
}

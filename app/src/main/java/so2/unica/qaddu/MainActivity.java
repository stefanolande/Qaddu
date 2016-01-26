package so2.unica.qaddu;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public class SamplePagerAdapter extends FragmentStatePagerAdapter {
        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment frg;
            switch(i){
                case 0:
                    frg = new Workout();
                    break;
                case 1:
                    frg = new History();
                    break;
                case 2:
                    frg = new Settings();
                    break;
                default:
                    // Vida loca
                    frg = new Workout();
                    break;
            }

            return frg;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";

            switch (position) {
                case 0:
                    title = getResources().getString(R.string.title_fragment_workout);
                    break;
                case 1:
                    title = getResources().getString(R.string.title_fragment_history);
                    break;
                case 2:
                    title = getResources().getString(R.string.title_fragment_settings);
                    break;
            }

            return title;

        }
    }


    SamplePagerAdapter mSamplePagerAdapter;
    ViewPager mViewPager;
    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSamplePagerAdapter = new SamplePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager.setAdapter(mSamplePagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


    }
}


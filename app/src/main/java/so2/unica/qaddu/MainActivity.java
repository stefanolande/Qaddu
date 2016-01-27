package so2.unica.qaddu;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.Helpers.DatabaseHelper;
import so2.unica.qaddu.Models.WorkoutItem;
import so2.unica.qaddu.QuadduFragments.History;
import so2.unica.qaddu.QuadduFragments.Workout;


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
                    frg = Workout.newInstance("fdfgf", "fgdfgdfg");
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

    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.tool_bar)
    Toolbar mToolBar;

    Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);

        mSamplePagerAdapter = new SamplePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSamplePagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        /*WorkoutItem gianni = new WorkoutItem();
        gianni.setTotalTime(54545454l);
        DatabaseHelper.getIstance(this).addData(gianni,WorkoutItem.class);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.mMenu = menu;

        getMenuInflater().inflate(R.menu.quaddu, this.mMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("MainActivity","Settings press");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


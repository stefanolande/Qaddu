package so2.unica.qaddu;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;
import so2.unica.qaddu.quadduFragments.History;
import so2.unica.qaddu.quadduFragments.Workout;
import so2.unica.qaddu.services.GPSService;


public class MainActivity extends AppCompatActivity {

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

      Intent intent = new Intent(getApplicationContext(), GPSService.class);
      startService(intent);

      setSupportActionBar(mToolBar);

      mSamplePagerAdapter = new SamplePagerAdapter(getSupportFragmentManager());
      mViewPager.setAdapter(mSamplePagerAdapter);
      mTabLayout.setupWithViewPager(mViewPager);

      //DatabaseHelper.initialize(this);

      WorkoutItem workout = new WorkoutItem();
      workout.setTotalTime(54545454l);
      workout.setStartDate(new Date());
      workout.setDistance(2300.6);
      workout.setName("Molentargius");

      DatabaseHelper.getIstance().addData(workout, WorkoutItem.class);

      List<WorkoutPoint> entries = new ArrayList<>();
      entries.add(new WorkoutPoint(workout, 3.2, 3.6, 22.0, 26.0, 398473897l, 23.6));
      entries.add(new WorkoutPoint(workout, 2.2, 1.6, 22.0, 26.0, 398473897l, 23.6));

      try {
         workout.setPoints(entries);
      } catch (SQLException e) {
         e.printStackTrace();
      }

      try {
         DatabaseHelper.getIstance().getDao().update(workout);
      } catch (SQLException e) {
         e.printStackTrace();
      }

      entries = new ArrayList<>();
      entries.add(new WorkoutPoint(workout, 3.2, 3.6, 22.0, 26.0, 398473897l, 23.6));
      entries.add(new WorkoutPoint(workout, 2.2, 1.6, 22.0, 26.0, 398473897l, 23.6));

      try {
         workout.setPoints(entries);
      } catch (SQLException e) {
         e.printStackTrace();
      }

      try {
         DatabaseHelper.getIstance().getDao().update(workout);
      } catch (SQLException e) {
         e.printStackTrace();
      }


      List<WorkoutItem> data = DatabaseHelper.getIstance().GetData(WorkoutItem.class);
      List<WorkoutPoint> points = data.get(0).getPoints();

        /*workout.setTotalTime(5345345435l);
        workout.setStart(new Date());
        workout.setDistance(25300.6);
        workout.setName("monte claro");
        DatabaseHelper.getIstance(this).addData(workout,WorkoutItem.class);
        workout.setTotalTime(34434l);
        workout.setStart(new Date());
        workout.setDistance(1300.6);
        workout.setName("cunnix");
        DatabaseHelper.getIstance(this).addData(workout,WorkoutItem.class);*/

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
         Log.d("MainActivity", "Settings press");
         Intent intent = new Intent(this, SettingsActivity.class);
         this.startActivity(intent);
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   public class SamplePagerAdapter extends FragmentStatePagerAdapter {
      public SamplePagerAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public Fragment getItem(int i) {
         Fragment frg = new Fragment();
         switch (i) {
            case 0:
               frg = new Workout();
               break;
            case 1:
               frg = new History();
               break;
         }

         return frg;

      }

      @Override
      public int getCount() {
         return 2;
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
         }

         return title;

      }
   }
}


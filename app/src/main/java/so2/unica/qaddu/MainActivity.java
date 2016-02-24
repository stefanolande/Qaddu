package so2.unica.qaddu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.quadduFragments.HistoryFragment;
import so2.unica.qaddu.quadduFragments.WorkoutFragment;


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
      String lang = changeLang();
      setLanguage(lang);
      Log.d("Dio-> Cane", lang);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      ButterKnife.bind(this);


      setSupportActionBar(mToolBar);

      mSamplePagerAdapter = new SamplePagerAdapter(getSupportFragmentManager());
      mViewPager.setAdapter(mSamplePagerAdapter);
      mTabLayout.setupWithViewPager(mViewPager);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      this.mMenu = menu;

      getMenuInflater().inflate(R.menu.quaddu, this.mMenu);

      return true;
   }

   @Override
   public void onResume() {
      super.onResume();
      String lang = changeLang();
      setLanguage(lang);

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

   public String changeLang() {
      SharedPreferences preferences_language = PreferenceManager.getDefaultSharedPreferences(this);
      String lang = preferences_language.getString("language_preference", "DEFAULT");
      switch (lang) {
         case "Italian":
            lang = "it";
            break;
         default:
            lang = "en";
            break;
      }
      return lang;
   }

   /**
    * This method change the language of the application
    */
   public void setLanguage(String language) {
      Locale myLocale = new Locale(language);
      Resources res = getBaseContext().getResources();
      DisplayMetrics dm = res.getDisplayMetrics();
      Configuration conf = res.getConfiguration();
      conf.locale = myLocale;

      res.updateConfiguration(conf, dm);
      getBaseContext().getResources().updateConfiguration(
            getBaseContext().getResources().getConfiguration(),
            getBaseContext().getResources().getDisplayMetrics());
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
               frg = new WorkoutFragment();
               break;
            case 1:
               frg = new HistoryFragment();
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


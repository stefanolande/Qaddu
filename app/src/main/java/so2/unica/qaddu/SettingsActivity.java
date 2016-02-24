package so2.unica.qaddu;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Locale;


public class SettingsActivity extends PreferenceActivity {
   Locale myLocale;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      SharedPreferences preferences_language = PreferenceManager.getDefaultSharedPreferences(this);
      String lang = preferences_language.getString("language_preference", "DEFAULT");
      addPreferencesFromResource(R.xml.setting);

   }

}

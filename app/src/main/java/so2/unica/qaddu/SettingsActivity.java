package so2.unica.qaddu;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity {
   public static final int MIN_INTERVAL_LENGTH = 1;
   public static final int MAX_INTERVAL_LENGTH = 10000;
   public static final int MIN_TARGET_SPEED = 0;
   public static final int MAX_TARGET_SPEED = 30;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.setting);
      final Preference preference = findPreference("setting_meters");

      if (preference instanceof EditTextPreference) {
         final EditTextPreference editTextPreference = (EditTextPreference) preference;
         editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

               if (!newValue.equals("")) {
                  int val = Integer.parseInt(newValue.toString());
                  if ((val > MIN_INTERVAL_LENGTH) && (val <= MAX_INTERVAL_LENGTH)) {
                     return true;
                  } else {
                     //the user input does not match the criteria
                     Toast.makeText(getApplicationContext(), getString(R.string.settings_interval_wrong_value, MIN_INTERVAL_LENGTH, MAX_INTERVAL_LENGTH), Toast.LENGTH_LONG).show();
                     return false;
                  }
               }
               Toast.makeText(getApplicationContext(), getString(R.string.settings_interval_wrong_value, MIN_INTERVAL_LENGTH, MAX_INTERVAL_LENGTH), Toast.LENGTH_LONG).show();
               return false;
            }
         });

      }

      final Preference preferenceTarget = findPreference("setting_target");
      if (preferenceTarget instanceof EditTextPreference) {
         final EditTextPreference editTargetPreference = (EditTextPreference) preferenceTarget;
         editTargetPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
               if (!newValue.equals("")) {
                  int val1 = Integer.parseInt(newValue.toString());
                  if (val1 != MIN_TARGET_SPEED && val1 <= MAX_TARGET_SPEED) {
                     return true;
                  } else {
                     // invalid you can show invalid message
                     Toast.makeText(getApplicationContext(), getString(R.string.settings_interval_wrong_value, MIN_TARGET_SPEED, MAX_TARGET_SPEED), Toast.LENGTH_LONG).show();
                     return false;
                  }
               }
               Toast.makeText(getApplicationContext(), getString(R.string.settings_interval_wrong_value, MIN_TARGET_SPEED, MAX_TARGET_SPEED), Toast.LENGTH_LONG).show();
               return false;
            }
         });
      }
   }

}


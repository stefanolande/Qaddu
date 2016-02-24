package so2.unica.qaddu;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity {
   String targ = "";

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.setting);
      final Preference preference = (Preference) findPreference("setting_meters");

      if (preference instanceof EditTextPreference) {
         final EditTextPreference editTextPreference = (EditTextPreference) preference;
         editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
               SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


               if (!newValue.equals("")) {
                  int val = Integer.parseInt(newValue.toString());
                  if ((val > 10) && (val <= 1000)) {


                     return true;
                  } else {
                     // invalid you can show invalid message

                     Toast.makeText(getApplicationContext(), "error text", Toast.LENGTH_LONG).show();
                     return false;
                  }
               }
               Toast.makeText(getApplicationContext(), "non hai inserirto un valore", Toast.LENGTH_LONG).show();
               return false;
            }
         });

      }

      final Preference preferencetarg = (Preference) findPreference("setting_target");
      if (preferencetarg instanceof EditTextPreference) {
         final EditTextPreference editTargetPreference = (EditTextPreference) preferencetarg;
         editTargetPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
               targ = editTargetPreference.getText();
               if (!newValue.equals("")) {
                  int val1 = Integer.parseInt(newValue.toString());
                  if (val1 != 0 && val1 <= 30) {
                     return true;
                  } else {
                     // invalid you can show invalid message
                     Toast.makeText(getApplicationContext(), "il valore non può essere 0 o maggiore di 30", Toast.LENGTH_LONG).show();
                     return false;
                  }
               }
               Toast.makeText(getApplicationContext(), "non hai inserirto un valore", Toast.LENGTH_LONG).show();
               return false;
            }
         });
      }
   }

}


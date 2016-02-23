package so2.unica.qaddu;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

       addPreferencesFromResource(R.xml.setting);
       final Preference preference = (Preference) findPreference("setting_meters");

       if (preference instanceof EditTextPreference) {
          EditTextPreference editTextPreference = (EditTextPreference) preference;
          if (editTextPreference.getText().trim().length() > 0) {
             editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                   int val = Integer.parseInt(newValue.toString());
                   if ((val > 10) && (val < 1000)) {

                      return true;
                   } else {
                      // invalid you can show invalid message
                      Toast.makeText(getApplicationContext(), "error text", Toast.LENGTH_LONG).show();
                      return false;
                   }
                }
             });
          }
       }
    }


}

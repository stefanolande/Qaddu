package so2.unica.qaddu;


import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;


public class SettingsActivity extends PreferenceActivity {
   public static final int MIN_INTERVAL_LENGTH = 1;
   public static final int MAX_INTERVAL_LENGTH = 10000;
   public static final int MIN_TARGET_SPEED = 0;
   public static final int MAX_TARGET_SPEED = 30;


   @Override
   @SuppressWarnings("deprecation")
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.setting);

      // Control text input in EditTextPrecefernce

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
      // Control text input in EditTextPrecefernce
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

         final Preference deleteWorkouts = findPreference("delete_all");
         deleteWorkouts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

               //create a dialog to request the confirmation to the user
               AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
               // Add the buttons
               builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     //delete all the workoutItem and workoutPoints from the db
                     DatabaseHelper.getIstance().deleteAll(WorkoutItem.class);
                     DatabaseHelper.getIstance().deleteAll(WorkoutPoint.class);

                     Toast.makeText(getApplicationContext(), R.string.all_workout_deleted, Toast.LENGTH_SHORT).show();
                  }
               });

               builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     // User cancelled the dialog
                  }
               });

               // Create the AlertDialog
               AlertDialog dialog = builder.create();
               dialog.setTitle(getString(R.string.delete_all_workout_confirmation));
               dialog.show();

               return true;
            }
         });
      }
   }

}


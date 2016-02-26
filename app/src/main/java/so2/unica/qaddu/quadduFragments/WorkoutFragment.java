package so2.unica.qaddu.quadduFragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.AppController;
import so2.unica.qaddu.MainActivity;
import so2.unica.qaddu.R;
import so2.unica.qaddu.services.WorkoutService;
import so2.unica.qaddu.services.WorkoutService.LocalBinder;
import so2.unica.qaddu.services.WorkoutService.updateUI;

/**
 * Fragment used to perform a workout. Allow the user to start, pause, resume, and stop a workout and displays the information during the progress.
 */
public class WorkoutFragment extends Fragment implements updateUI {

   public static final int NOTIFICATION_ID = 42;
   @Bind(R.id.tvIntervalLength)
   TextView tvIntervalLength;
   @Bind(R.id.circle_container)
   LinearLayout mCircleContainer;
   @Bind(R.id.min_circle)
   View mCircle;
   @Bind(R.id.tv_instant_speed)
   TextView tvInstantSpeed;
   @Bind(R.id.tv_target)
   TextView tvTargetSpeed;
   @Bind(R.id.tv_total_km)
   TextView tvTotalKm;
   @Bind(R.id.tv_total_speed)
   TextView tvTotalKmH;
   @Bind(R.id.tv_total_time)
   TextView tvTotalTime;
   @Bind(R.id.tv_total_pace)
   TextView tvTotalPace;
   @Bind(R.id.tv_last_x_speed)
   TextView tvLastSpeed;
   @Bind(R.id.tv_last_x_pace)
   TextView tvLastPace;
   @Bind(R.id.nameWorkout)
   EditText etNameWorkout;
   @Bind(R.id.btn_stop)
   ImageButton bStop;
   @Bind(R.id.btn_start)
   ImageButton bStart;

   private WorkoutService mService;
   private boolean mBound = false;


   private BroadcastReceiver mBroadcastReceiver;
   private IntentFilter mIntentFilter;

   private boolean mWorkoutRunning = false;
   private boolean mWorkoutPaused = false;
   private boolean mWorkoutPausedByUser = false;

   private double mTargetSpeed;
   private String mWorkoutName;
   private boolean mGPSEnabled = true;
   private int mContainerWidth;

   /**
    * Defines callbacks for service binding, passed to bindService()
    */
   private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
         LocalBinder binder = (LocalBinder) service;
         mService = binder.getService();
         mBound = true;
         mService.addWorkoutListener(WorkoutFragment.this);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
         mBound = false;
      }
   };

   public WorkoutFragment() {
      // Required empty public constructor
   }

   @Override
   public void onPause() {
      super.onPause();

      //detach the listener from the service to disable UI update
      if (mBound) {
         mService.removeWorkoutListener();
      }
   }

   @Override
   public void onResume() {
      super.onResume();


      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

      //fetch the target speed from the preferences and set it
      setTargetSpeed(Double.parseDouble(preferences.getString("setting_target", getActivity().getString(R.string.default_target_speed))));

      //fetch the length of the interval for the partial from the preferences and set it
      setTvIntervalLength(Integer.parseInt(preferences.getString("setting_meters", getActivity().getString(R.string.default_interval))));


      //attach the listener to the service to resume UI update
      if (mBound) {
         mService.addWorkoutListener(this);
      }
   }

   /**
    * Sets the length of the interval for the partial counters and show it on the UI
    *
    * @param interval int interval length
    */
   private void setTvIntervalLength(int interval) {

      if (interval < 1000) {
         tvIntervalLength.setText(getActivity().getString(R.string.interval_length_m, interval));
      } else {
         interval = interval / 1000;
         tvIntervalLength.setText(getActivity().getString(R.string.interval_length_km, interval));
      }

   }

   /**
    * Sets the position of the floating circle (The Ball) and show it on the UI
    *
    * @param offset double Offset for the position's ball
    */
   private void setCircleOffset(double offset) {
      LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCircle.getLayoutParams();
      int margin = (int) (mContainerWidth / 30 * offset);
      params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
      mCircle.setLayoutParams(params);
   }

   /**
    * Sets the speed into TextView of the instant speed, calculating the offset and calling the setCircleOffset method
    * @param instantSpeed double Instant speed
    */
   private void setInstantSpeed(double instantSpeed) {
      DecimalFormat df = new DecimalFormat("#0.00");
      tvInstantSpeed.setText(df.format(instantSpeed) + " KM/H");

      //calculate percent difference between the current speed and the target
      Double offset = ((instantSpeed / mTargetSpeed) - 1) * 100;

      //correlate the speed difference ratio to the circle position on the screen
      if (offset <= -20) {
         offset = -30.0;
      } else if (offset <= -7.5) {
         offset = -15.0;
      } else if (offset <= 7.5) {
         offset = 0.0;
      } else if (offset <= 20) {
         offset = 15.0;
      } else {
         offset = 30.0;
      }

      setCircleOffset(offset);
   }

   /**
    * Sets the target speed into TextView of the target speed
    * @param targetSpeed double Target Speed
    */
   private void setTargetSpeed(double targetSpeed) {
      mTargetSpeed = targetSpeed;
      DecimalFormat decimalFormat = new DecimalFormat("0.0");
      tvTargetSpeed.setText(getActivity().getString(R.string.target) + decimalFormat.format(targetSpeed) + " KM/H");
   }

   /**
    * Sets the total km traveled into the TextView of the total km
    * @param totalMeters double Total meters
    */
   private void setTotalKm(double totalMeters) {
      double km = totalMeters / 1000;
      DecimalFormat df = new DecimalFormat("#0.00");
      tvTotalKm.setText(df.format(km) + " KM");
   }

   /**
    * Sets the total speed into the TextView of the total speed
    * @param totalKmH double Total speed (The average speed from the start until the current)
    */
   private void setTotalSpeed(double totalKmH) {
      DecimalFormat df = new DecimalFormat("#0.00");
      tvTotalKmH.setText(df.format(totalKmH) + " KM/H");
   }

   /**
    * Sets the total time into the TextView of the total time
    * @param totalTime double Total time (The time from the start until the current)
    */
   private void setTotalTime(float totalTime) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      tvTotalTime.setText(simpleDateFormat.format(totalTime));
   }

   /**
    * Sets the total pace into the TextView of the total pace
    * @param totalPace double Total pace (The average pace from the start until the current)
    */
   private void setTotalPace(double totalPace) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      tvTotalPace.setText(simpleDateFormat.format(totalPace) + " MIN/KM");
   }

   //This method is used to set the last X meter speed into the TextView of the last X meter speed
   private void setIntervalSpeed(double lastSpeed) {
      DecimalFormat df = new DecimalFormat("#0.00");
      tvLastSpeed.setText(df.format(lastSpeed) + " KM/H");
   }

   //This method is used to set the last X meter's pace speed into the TextView of the LastPace
   private void setIntervalPace(double lastPace) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      tvLastPace.setText(simpleDateFormat.format(lastPace * 1000) + " MIN/KM");
   }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_workout, container, false);
      ButterKnife.bind(this, view);

      //default workout name, used when the user does not insert a name
      mWorkoutName = getActivity().getString(R.string.untitled_workout);

      //create intent filter for the gps status change
      mIntentFilter = new IntentFilter(AppController.GPS_TURNED_ON);
      mIntentFilter.addAction(AppController.GPS_TURNED_OFF);


      //Set the action of the button play/pause
      setPlayButtonAction();

      //Set the action of the button stop
      setStopButtonAction();


      return view;
   }

   /**
    * Adds the listener that handles the action og the play/pause button
    */
   private void setPlayButtonAction() {
      bStart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            if ((!mWorkoutRunning || mWorkoutPaused) && mGPSEnabled) {
               //first start of the workout or restart
               //when the workout is play or pause, the user can stop the workout (the stop button in enable, is blu)
               bStop.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_stop));
               bStart.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause));

               mWorkoutPaused = false;
               mWorkoutPausedByUser = false;

               //register the broadcast receiver to handle the gps status change
               mBroadcastReceiver = new WorkoutBroadcastReceiverHelper();
               getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter);

               //if the service is already bound it's a restart, so resume the workout
               if (mBound) {
                  mService.resumeWorkout();
               }
            } else if (mGPSEnabled) {
               //the workout state is changing from running to paused
               bStart.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));

               mWorkoutPaused = true;
               mWorkoutPausedByUser = true;

               if (mBound) {
                  mService.pauseWorkout();
               }
            } else if (!mGPSEnabled) {
               //the user is trying to restart a paused workout when the gps is disabled
               mWorkoutPausedByUser = false;
               Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.gps_disabled_on_resume, Toast.LENGTH_LONG);
               toast.show();
            }

            //the user is starting a workout for the first time
            if (!mWorkoutRunning) {

               //get the workout name from the editText if it is not empty
               if (!etNameWorkout.getText().toString().equals("")) {
                  mWorkoutName = etNameWorkout.getText().toString();
               } else {
                  etNameWorkout.setText(mWorkoutName);
               }

               //reset the UI
               setTotalSpeed(0);
               setTotalPace(0);

               setIntervalPace(0);
               setIntervalSpeed(0);

               setInstantSpeed(0);
               setTotalTime(0);
               setTotalKm(0);

               etNameWorkout.setEnabled(false);

               //Start and bind the workout service
               Log.d("WorkoutFragment", "requested service");
               Intent intent = new Intent(getActivity(), WorkoutService.class);
               intent.putExtra(WorkoutService.WORKOUT_TITLE, mWorkoutName);
               getActivity().startService(intent);
               getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

               //notify the user with a notification
               createNotification();

               mWorkoutRunning = true;
            }
         }
      });
   }

   /**
    * Adds the listener that handles the action of the stop button
    */
   private void setStopButtonAction() {
      bStop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mWorkoutRunning && mService != null) {
               //ask the user the confirmation to end the workout
               AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
               // Add the buttons
               builder.setPositiveButton(getActivity().getString(R.string.workout_stop), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     //Stop the workout service service
                     if (mService.isRunning() || mService.ismPaused()) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), WorkoutService.class);
                        getActivity().stopService(intent);
                        bStart.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
                     }

                     //unbind the workout service
                     if (mBound) {
                        getActivity().unbindService(mConnection);
                        mBound = false;
                     }

                     //unregister the broadcast receiver to handle the gps status change
                     try {
                        getActivity().unregisterReceiver(mBroadcastReceiver);
                     } catch (RuntimeException e) {
                        //the receiver was not registered
                     }

                     removeNotification();

                     //when the workout is stopped, the user can't click stop button again
                     bStop.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_stopgray));
                     //reset the state of the workout
                     mWorkoutRunning = false;
                     mWorkoutPaused = false;
                     mWorkoutPausedByUser = false;
                     mGPSEnabled = true;
                     etNameWorkout.setEnabled(true);
                     etNameWorkout.setText("");
                     mWorkoutName = getActivity().getString(R.string.untitled_workout);

                  }
               });
               builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     // User cancelled the dialog
                  }
               });

               // Create the AlertDialog
               AlertDialog dialog = builder.create();
               dialog.setTitle(getActivity().getString(R.string.workout_stop_confirmation));
               dialog.show();
            }
         }
      });
   }

   /**
    * Creates a permanent notification to inform the user that a workout is running
    */
   private void createNotification() {
      NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(getActivity())
                  .setSmallIcon(R.drawable.qaddu_notification)
                  .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                  .setContentTitle(getString(R.string.app_name))
                  .setOngoing(true)
                  .setContentText(getActivity().getString(R.string.workout_running_notification));
      Intent resultIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
      PendingIntent resultPendingIntent =
            PendingIntent.getActivity(
                  getActivity(),
                  0,
                  resultIntent,
                  PendingIntent.FLAG_UPDATE_CURRENT
            );
      mBuilder.setContentIntent(resultPendingIntent);
      // Gets an instance of the NotificationManager service
      NotificationManager mNotifyMgr = (NotificationManager) getActivity().getSystemService(Activity.NOTIFICATION_SERVICE);
      // Builds the notification and issues it.
      mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
   }

   /**
    * Removes the "workout running" notification
    */
   private void removeNotification() {
      //remove the notification
      NotificationManager mNotifyMgr = (NotificationManager) getActivity().getSystemService(Activity.NOTIFICATION_SERVICE);
      mNotifyMgr.cancel(NOTIFICATION_ID);
   }

   @Override
   public void onStart() {
      super.onStart();

      //bind the value of the variable with the size of the container
      mCircleContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
         @Override
         public void onGlobalLayout() {
            mContainerWidth = mCircleContainer.getWidth();
         }
      });
   }

   public void updateInfo() {
      //update the UI using public service (mBinder) methods
      setInstantSpeed(mService.getSpeed());
      setTotalKm(mService.getDistance());
      setTotalSpeed(mService.getTotalSpeed());
      setTotalPace(mService.getTotalPace());
      setIntervalPace(mService.getIntervalPace());
      setIntervalSpeed(mService.getIntervalSpeed());
   }

   public void updateTime() {
      //update the time shown on screen, called by the service
      getActivity().runOnUiThread(new Runnable() {
         @Override
         public void run() {
            setTotalTime(mService.getTime());
            //Log.d("UpdateTime", mService.getTime() + "");
         }
      });
   }

   /**
    * Broadcast receiver user for handling the GPS status change
    */
   private class WorkoutBroadcastReceiverHelper extends BroadcastReceiver {
      @Override
      public void onReceive(Context context, Intent intent) {
         switch (intent.getAction()) {
            case AppController.GPS_TURNED_OFF:
               mGPSEnabled = false;

               // pause a workout if it is running and the gps is turned off
               if (mWorkoutRunning && !mWorkoutPaused) {
                  bStart.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
                  mWorkoutPaused = true;

                  Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.gps_disabled_during_workout, Toast.LENGTH_LONG);
                  toast.show();
               }
               break;
            case AppController.GPS_TURNED_ON:
               mGPSEnabled = true;

               //resume the workout if the GPS is turned on during the pause
               if (mWorkoutRunning && mWorkoutPaused && !mWorkoutPausedByUser) {
                  bStart.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause));
                  mWorkoutPaused = false;

                  Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.workout_resumed_with_gps_off, Toast.LENGTH_SHORT);
                  toast.show();
               }
               break;
         }
      }
   }
}

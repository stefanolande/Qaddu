package so2.unica.qaddu.quadduFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.R;
import so2.unica.qaddu.services.WorkoutService;
import so2.unica.qaddu.services.WorkoutService.LocalBinder;
import so2.unica.qaddu.services.WorkoutService.updateUI;


public class WorkoutFragment extends Fragment implements updateUI {

   WorkoutService mService;
   boolean mBound = false;

   int mContainerWidth;

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
   @Bind(R.id.tv_total_step)
   TextView tvTotalStep;
   @Bind(R.id.tv_last_x_speed)
   TextView tvLastSpeed;
   @Bind(R.id.tv_last_x_step)
   TextView tvLastStep;
   @Bind(R.id.nameWorkout)
   EditText etNameWorkout;
   @Bind(R.id.btn_stop)
   ImageButton bStop;
   @Bind(R.id.btn_start)
   ImageButton bStart;

   private String mWorkoutName = "Untitled workout";
   private boolean mWorkoutRunning = false;
   private boolean mWorkoutPaused = false;
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

      if (mBound) {
         mService.removeWorkoutListener();
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      if (mBound) {
         mService.addWorkoutListener(this);
      }
   }

   private void setCircleOffset(double offset) {
      LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCircle.getLayoutParams();
      int margin = (int) (mContainerWidth / 100 * offset);
      params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
      mCircle.setLayoutParams(params);
   }

   //This method is used to set the speed into the TextView of the instant speed
   private void setInstantSpeed(double instantSpeed) {
      DecimalFormat df = new DecimalFormat("#0.00");
      tvInstantSpeed.setText(df.format(instantSpeed) + " KM/H");
   }

   //This method is used to set the target speed into the TextView of the target speed
   private void setTargetSpeed(float targetSpeed) {
      tvTargetSpeed.setText(Float.toString(targetSpeed) + " KM/H");
   }

   //This method is used to set the total Km traveled into the TextView of the total km
   private void setTotalKm(double totalMeters) {
      double km = totalMeters / 1000;
      DecimalFormat df = new DecimalFormat("#0.00");
      tvTotalKm.setText(df.format(km) + " KM");
   }

   //This method is used to set the total speed into the TextView of the total speed
   private void setTotalSpeed(double totalKmH) {
      DecimalFormat df = new DecimalFormat("#0.00");
      tvTotalKmH.setText(df.format(totalKmH) + " KM/H");
   }

   //This method is used to set the total time into the TextView of the total time
   private void setTotalTime(float totalTime) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      tvTotalTime.setText(simpleDateFormat.format(totalTime));
   }

   //This method is used to set the total step speed into the TextView of the total step
   private void setTotalStep(float totalStep) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      tvTotalStep.setText(simpleDateFormat.format(totalStep) + " /KM");
   }

   //This method is used to set the last X meter speed into the TextView of the last X meter speed
   private void setIntervalSpeed(float lastSpeed) {
      DecimalFormat df = new DecimalFormat("#0.00");
      tvLastSpeed.setText(df.format(lastSpeed) + " KM/H");
   }

   //This method is used to set the last X meter's step speed into the TextView of the LastStep
   private void setIntervalStep(float lastStep) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      tvLastStep.setText(simpleDateFormat.format(lastStep) + " /KM");
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_workout, container, false);
      ButterKnife.bind(this, view);

      bStart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            //reset the infos on the UI
            if (!mWorkoutRunning && !mWorkoutPaused) {
               //reset the infos
               setTotalSpeed(0);
               setTotalStep(0);

               setIntervalStep(0);
               setIntervalSpeed(0);

               setInstantSpeed(0);
               setTotalTime(0);
               setTotalKm(0);
            }

            if (!mWorkoutRunning || mWorkoutPaused) {
               //when the workout is play or pause, the user can stop the workout (the stop button in enable, is blu)
               bStop.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
               bStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
               mWorkoutPaused = false;
            } else {
               bStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
               mWorkoutPaused = true;
            }

            //start the workout service if needed
            if (!mWorkoutRunning) {
               if (!etNameWorkout.getText().equals("")) {
                  mWorkoutName = etNameWorkout.getText().toString();
               } else {
                  etNameWorkout.setText(mWorkoutName);
               }

               etNameWorkout.setEnabled(false);

               //Start and bind the workout service
               Log.d("WorkoutFragment", "requested service");
               Intent intent = new Intent(getActivity(), WorkoutService.class);
               intent.putExtra(WorkoutService.WORKOUT_TITLE, mWorkoutName);
               getActivity().startService(intent);
               getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
               mWorkoutRunning = true;
            }
         }
      });

      bStop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            //unbind the workout service
            if (mBound) {
               getActivity().unbindService(mConnection);
               mBound = false;
            }

            //Stop the workout service service
            if (WorkoutService.running) {
               Intent intent = new Intent(getActivity().getApplicationContext(), WorkoutService.class);
               getActivity().stopService(intent);
               bStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
            }


            //when the workout is stopped, the user can't click stop button again
            bStop.setImageDrawable(getResources().getDrawable(R.drawable.ic_stopgray));
            mWorkoutRunning = false;
            mWorkoutPaused = false;
            etNameWorkout.setEnabled(true);
         }
      });

      //TODO retrieve the target speed from settings and setTargetSpeed()

      return view;
   }

   @Override
   public void onStart() {
      super.onStart();
      mCircleContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
         @Override
         public void onGlobalLayout() {
            mContainerWidth = mCircleContainer.getWidth();
         }
      });
   }

/*
      new Thread(new Runnable() {
         public void run() {
            int sum = 1;
            while (true) {
               tmpOff += sum;

               getActivity().runOnUiThread(new Runnable() {
                  public void run() {
                     setCircleOffset(tmpOff);
                     setInstantSpeed(((tmpOff / 10 + 9) + 0.1f) + instantSpeed);
                     setTargetSpeed(targetSpeed);
                     setTotalKm(totalKm);
                     setTotalSpeed(totalKmH);
                     setTotalTime(totalTime);
                     setTotalStep(totalStep);
                     setIntervalSpeed(lastKmh);
                     setIntervalStep(lastStep);
                  }
               });


               if (tmpOff == 100) {
                  sum = -1;

               }
               if (tmpOff == -100) {
                  sum = 1;
               }
               try {
                  Thread.sleep(150);
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }
            }
         }
      }).start();

*/


   public void updateInfo() {
      //update the UI using public service (mBinder) methods
      setInstantSpeed(mService.getSpeed());
      setTotalKm(mService.getDistance());
      setTotalSpeed(mService.getTotalSpeed());
   }

   public void updateTime() {
      getActivity().runOnUiThread(new Runnable() {
         @Override
         public void run() {
            setTotalTime(mService.getTime());
            //Log.d("UpdateTime", mService.getTime() + "");
         }
      });
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
   }

   @Override
   public void onDetach() {
      super.onDetach();
   }
}

package so2.unica.qaddu.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import so2.unica.qaddu.AppController;
import so2.unica.qaddu.R;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.GpsPoint;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;


/**
 *
 */

public class WorkoutService extends Service {

   public static final String WORKOUT_TITLE = "QadduWorkout";
   private static final int TIME_UPDATE_INTERVAL = 250; //timer update interval in milliseconds

   // Binder given to clients
   private final IBinder mBinder = new LocalBinder();

   private WorkoutItem mItem; //workout in progress
   private List<WorkoutPoint> mPoints; //points generated during the workout
   private Double mDistance = 0.0; //total distance covered
   private int mIntervalLength; //length of the interval used by the partial counter
   private long mTotalTime = 0; //total duration of the workout

   //state of the workout
   private boolean mWorkoutRunning = false;
   private boolean mPaused = false;
   private boolean mPausedByGPSOff = false;

   private IntentFilter mIntentFilter;
   private BroadcastReceiver mBroadcastReceiver;

   //Reference to the updateUI to update the UI
   private updateUI mObserver;

   //Timer and task used for the time update
   private Timer mTimer;
   private TimerTask mTimeUpdateTask;

   public boolean isRunning() {
      return mWorkoutRunning;
   }


   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }

   public void addWorkoutListener(updateUI updateUI) {
      this.mObserver = updateUI;
   }

   public void removeWorkoutListener() {
      this.mObserver = null;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.d("WorkoutService", "started");
      mWorkoutRunning = true;

      //create a new workout item
      mItem = new WorkoutItem();
      mItem.setStartDate(new Date());
      mItem.setName(intent.getStringExtra(WORKOUT_TITLE));
      mPoints = new ArrayList<>();

      //start the gps service
      Intent gpsIntent = new Intent(getApplicationContext(), GPSService.class);
      startService(gpsIntent);

      //register the intent filters and the receiver for the gps points and status
      mIntentFilter = new IntentFilter(AppController.BROADCAST_NEW_GPS_POSITION);
      mIntentFilter.addAction(AppController.GPS_TURNED_ON);
      mIntentFilter.addAction(AppController.GPS_TURNED_OFF);

      mBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
               case AppController.BROADCAST_NEW_GPS_POSITION:
                  GpsPoint point = intent.getParcelableExtra(GpsPoint.QADDU_GPS_POINT);
                  onReceivePoint(point);
                  break;
               case AppController.GPS_TURNED_OFF:
                  if (isRunning()) {
                     pauseWorkout();
                     mPausedByGPSOff = true;
                  }
                  break;
               case AppController.GPS_TURNED_ON:
                  if (!isRunning() && mPausedByGPSOff) {
                     resumeWorkout();
                     mPausedByGPSOff = false;
                  }
                  break;
            }

         }
      };
      this.registerReceiver(mBroadcastReceiver, mIntentFilter);

      //TODO retrieve the interval length from settings and set mIntervalLength
      mIntervalLength = 100;

      //create a timer for the workout time
      mTimeUpdateTask = new TimerUpdateTask();
      mTimer = new Timer();
      mTimer.scheduleAtFixedRate(mTimeUpdateTask, 0, TIME_UPDATE_INTERVAL);

      return super.onStartCommand(intent, flags, startId);
   }

   public void onReceivePoint(GpsPoint point) {
      if (mPoints.size() == 0) {
         //if its the first point, the distance covered is 0
         mDistance = 0.0;
      } else {
         //otherwise, calculate the distance between the nre point and the last
         float[] distanceArray = new float[1];
         Location.distanceBetween(mPoints.get(mPoints.size() - 1).getLatitude(),
               mPoints.get(mPoints.size() - 1).getLongitude(),
               point.getLatitude(),
               point.getLongitude(),
               distanceArray);

         mDistance += distanceArray[0];

      }

      //add the new point to the list and update the distance in the workout item
      mPoints.add(new WorkoutPoint(mItem, point.getLatitude(), point.getLongitude(), point.getSpeed(), point.getAltitude(), mTotalTime, mDistance));
      mItem.setDistance(mDistance);

      //notify the UI
      if (this.mObserver != null) {
         this.mObserver.updateInfo();
      }
   }

   /**
    * Responsible for ending the workout and saving it into the DB
    */
   @Override
   public void onDestroy() {
      Log.d("WorkoutService", "Stopped");
      mWorkoutRunning = false;

      //Stop the gps service
      if (GPSService.mRunning) {
         Intent intent = new Intent(getApplicationContext(), GPSService.class);
         stopService(intent);
      }

      //stop the timer
      mTimer.cancel();

      //deregister the broadcast receiver
      try {
         this.unregisterReceiver(mBroadcastReceiver);
      } catch (RuntimeException e) {
         //handle the case when the workout is in pause and the broadcastReceiver is not registered
      }

      try {
         //save workout into the db if it has at least one pint
         if (mPoints.size() > 0) {

            DatabaseHelper.getInstance().addData(mItem, WorkoutItem.class);
            mItem.setPoints(mPoints);
            DatabaseHelper.getInstance().getDao().update(mItem);

            //notify the user
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_workout_saved, mItem.getName()), Toast.LENGTH_SHORT);
            toast.show();
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      //send intent to notify the need to update the history
      Intent intent = new Intent();
      intent.setAction(AppController.BROADCAST_NEW_WORKOUT);
      sendBroadcast(intent);
      super.onDestroy();
   }

   /***
    * Puts the workout in pauseWorkout
    */
   public void pauseWorkout() {
      Log.d("WorkoutService", "paused");
      try {
         //stop the timer
         mTimer.cancel();
         mTimeUpdateTask.cancel();

         //remove the intent to receive GPS point
         mIntentFilter = new IntentFilter(AppController.GPS_TURNED_ON);
         mIntentFilter.addAction(AppController.GPS_TURNED_OFF);
         unregisterReceiver(mBroadcastReceiver);
         registerReceiver(mBroadcastReceiver, mIntentFilter);
      } catch (IllegalArgumentException e) {
         //the actions were not necessary
      }

      this.mWorkoutRunning = false;
      this.mPaused = true;
   }

   /**
    * Resume the workout
    */
   public void resumeWorkout() {
      Log.d("WorkoutService", "resumed");

      //resume the timer
      mTimer = new Timer();
      mTimeUpdateTask = new TimerUpdateTask();
      mTimer.scheduleAtFixedRate(mTimeUpdateTask, 0, TIME_UPDATE_INTERVAL);

      //add the intent to receive GPS points
      unregisterReceiver(mBroadcastReceiver);
      mIntentFilter.addAction(AppController.BROADCAST_NEW_GPS_POSITION);
      registerReceiver(mBroadcastReceiver, mIntentFilter);
      this.mWorkoutRunning = true;
      this.mPaused = false;
   }

   /**
    * Returns the last instantaneous speed in km/h
    *
    * @return double speed in km/h
    */
   public double getSpeed() {
      WorkoutPoint lastWorkoutPoint = mPoints.get(mPoints.size() - 1);
      return lastWorkoutPoint.getSpeed();
   }

   /**
    * Returns the average speed for the workout in km/h
    *
    * @return double speed in km/h
    */
   public double getTotalSpeed() {
      double speed = 0;
      if (mTotalTime != 0 && mDistance != 0) {
         speed = msToKmh(mDistance / (mTotalTime / 1000));
      }
      Log.d("WorkoutService", "Total speed " + speed + " " + mTotalTime + " " + mDistance);
      return speed;
   }

   /**
    * Returns the average speed for the last interval in km/h
    *
    * @return double speed in km/h
    */
   public double getIntervalSpeed() {
      //fetch the workout point in the last *interval* meters
      ArrayList<WorkoutPoint> lastPoints = new ArrayList<>();
      for (int i = mPoints.size() - 1; i >= 0; i--) {
         WorkoutPoint point = mPoints.get(i);
         if (this.mDistance - point.getDistance() <= mIntervalLength) {
            lastPoints.add(point);
         }
      }

      double averageSpeed = 0;

      if (lastPoints.size() != 0) {
         //calculate the average speed in the last points
         double speedSum = 0;

         for (WorkoutPoint point : lastPoints) {
            speedSum += point.getSpeed();
         }

         averageSpeed = speedSum / lastPoints.size();
      }
      return averageSpeed;
   }

   /**
    * Returns the average pace for the workout in seconds to km
    *
    * @return double pace in seconds to km
    */
   public double getTotalPace() {
      double timeKm = 0;
      if (mDistance != 0) {
         timeKm = mTotalTime / (mDistance / 1000);
      }
      return timeKm;
   }

   /**
    * Returns the average pace for the last interval in seconds to km
    *
    * @return double seconds to km
    */
   public double getIntervalPace() {
      double intervalSpeed = getIntervalSpeed();
      double sToKm = 0;

      if (intervalSpeed != 0) {
         //convert the speed from km/h to s/km
         sToKm = (60 / intervalSpeed) * 60;
      }


      return sToKm;
   }

   /**
    * Returns the distance covered during the workout in meters
    *
    * @return double distance in meters
    */
   public double getDistance() {
      return mDistance;
   }

   /**
    * Returns the duration of the workout in seconds
    *
    * @return long duration of the workout in seconds
    */
   public long getTime() {
      return mTotalTime;
   }

   /**
    * Converts the speed from m/s to km/h
    *
    * @param ms speed in m/s
    * @return double speed in km/h
    */
   private double msToKmh(double ms) {
      return ms * 3.6; //3.6 is the conversion factor from m/s to km/h
   }

   public boolean ismPaused() {
      return mPaused;
   }

   /**
    * Interface for listener
    */
   public interface updateUI {
      void updateInfo();

      void updateTime();
   }

   /**
    * Class used for the client Binder.
    */
   public class LocalBinder extends Binder {
      public WorkoutService getService() {
         // Return this instance of WorkoutService so clients can call public methods
         return WorkoutService.this;
      }
   }

   public class TimerUpdateTask extends TimerTask {
      @Override
      public void run() {
         mTotalTime += TIME_UPDATE_INTERVAL;
         mItem.setTotalTime(mTotalTime);

         //if the time is multiple of 1000 ms update the UI
         if (mTotalTime % 1000 == 0 && mObserver != null) {
            mObserver.updateTime();
         }
      }
   }

}


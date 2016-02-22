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
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.helpers.ReceiverHelper;
import so2.unica.qaddu.models.GpsPoint;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;


/**
 * Created by Stefano on 14/02/2016.
 */

public class WorkoutService extends Service {

   public static final String WORKOUT_TITLE = "QuadduWorkout";
   private static final int TIME_UPDATE_INTERVAL = 250; //timer update interval in milliseconds

   public static Boolean running = false;
   // Binder given to clients
   private final IBinder mBinder = new LocalBinder();
   WorkoutItem mItem;
   List<WorkoutPoint> mPoints;
   Double mDistance = 0.0;

   BroadcastReceiver mBroadcastReceiver;
   private IntentFilter mIntentFilter;

   //Reference to the updateUI to update the UI
   private updateUI observer;
   private int mIntevalLength;
   private long mTotalTime = 0;

   private Timer mTimer;
   private TimerTask mTimeUpdateTask;


   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }

   public void addWorkoutListener(updateUI updateUI) {
      this.observer = updateUI;
   }

   public void removeWorkoutListener() {
      this.observer = null;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.d("WorkoutService", "started");
      running = true;

      mItem = new WorkoutItem();
      mItem.setStartDate(new Date());
      mItem.setName(intent.getStringExtra(WORKOUT_TITLE));

      mPoints = new ArrayList<>();

      //start the gps service
      Intent gpsIntent = new Intent(getApplicationContext(), GPSService.class);
      startService(gpsIntent);


      mIntentFilter = new IntentFilter(AppController.BROADCAST_NEW_GPS_POSITION);

      mBroadcastReceiver = new ReceiverHelper() {
         @Override
         public void onReceive(Context context, Intent intent) {
            GpsPoint point = intent.getParcelableExtra(GpsPoint.QUADDU_GPS_POINT);
            onReceivePoint(point);
         }
      };
      this.registerReceiver(mBroadcastReceiver, mIntentFilter);

      //TODO retrieve the interval length from settings and set mIntervalLength
      mIntevalLength = 100;

      //create a timer for the workout time
      mTimeUpdateTask = new TimerUpdateTask();
      mTimer = new Timer();
      mTimer.scheduleAtFixedRate(mTimeUpdateTask, 0, TIME_UPDATE_INTERVAL);

      return super.onStartCommand(intent, flags, startId);
   }

   public void onReceivePoint(GpsPoint point) {
      if (mPoints.size() == 0) {
         mDistance = 0.0;
      } else {
         float[] distanceArray = new float[1];

         Location.distanceBetween(mPoints.get(mPoints.size() - 1).getLatitude(),
               mPoints.get(mPoints.size() - 1).getLongitude(),
               point.getLatitude(),
               point.getLongitude(),
               distanceArray);

         mDistance += distanceArray[0];
      }

      mPoints.add(new WorkoutPoint(mItem, point.getLatitude(), point.getLongitude(), point.getSpeed(), point.getAltitude(), mTotalTime, mDistance));
      mItem.setTotalTime(mTotalTime);
      mItem.setDistance(mDistance);

      //notify the UI
      if (this.observer != null) {
         this.observer.updateInfo();
      }
   }

   @Override
   public void onDestroy() {
      Log.d("WorkoutService", "Stopped");

      //Stop the gps service
      if (GPSService.mRunning) {
         Intent intent = new Intent(getApplicationContext(), GPSService.class);
         stopService(intent);
      }

      //stop the timer
      mTimer.cancel();


      running = false;
      try {
         this.unregisterReceiver(mBroadcastReceiver);
      } catch (RuntimeException e) {
         //handle the case when the workout is in pause and the broadcastReceiver is not registered
      }

      try {
         if (mPoints.size() > 0) {
            DatabaseHelper.getIstance().addData(mItem, WorkoutItem.class);
            mItem.setPoints(mPoints);
            DatabaseHelper.getIstance().getDao().update(mItem);
            Toast toast = Toast.makeText(getApplicationContext(), "workout " + mItem.getName() + " saved.", Toast.LENGTH_SHORT);
            toast.show();
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

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
      mTimer.cancel();
      mTimeUpdateTask.cancel();
      unregisterReceiver(mBroadcastReceiver);
   }

   /**
    * Resume the workout
    */
   public void resumeWorkout() {
      Log.d("WorkoutService", "resumed");
      mTimer = new Timer();
      mTimeUpdateTask = new TimerUpdateTask();
      mTimer.scheduleAtFixedRate(mTimeUpdateTask, 0, TIME_UPDATE_INTERVAL);
      registerReceiver(mBroadcastReceiver, mIntentFilter);
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
      if (mTotalTime != 0) {
         speed = msTokmh(mDistance / (mTotalTime / 1000));
      }

      return speed;
   }

   /**
    * Returns the average speed for the last interval in km/h
    *
    * @return double speed in km/h
    */
   public double getIntevalSpeed() {
      //fetch the workout point in the last *interval* meters
      ArrayList<WorkoutPoint> lastPoints = new ArrayList<>();

      for (int i = mPoints.size() - 1; i >= 0; i--) {
         WorkoutPoint point = mPoints.get(i);
         if (this.mDistance - point.getDistance() <= mIntevalLength) {
            lastPoints.add(point);
         }
      }

      //calculate the average speed in the last points
      double speedSum = 0;

      for (WorkoutPoint point : lastPoints) {
         speedSum += point.getSpeed();
      }

      double averageSpeed = speedSum / lastPoints.size();
      return msTokmh(averageSpeed);
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
      //fetch the workout point in the last *interval* meters
      ArrayList<WorkoutPoint> lastPoints = new ArrayList<>();

      for (int i = mPoints.size() - 1; i >= 0; i--) {
         WorkoutPoint point = mPoints.get(i);
         if (this.mDistance - point.getDistance() <= mIntevalLength) {
            lastPoints.add(point);
         }
      }

      //calculate the average speed in the last points
      double speedSum = 0;

      for (WorkoutPoint point : lastPoints) {
         speedSum += point.getSpeed();
      }

      double averageSpeed = speedSum / lastPoints.size();

      //convert the speed from m/s to s/km

      double sToKm = Math.pow(averageSpeed, -1) / 1000;

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
    * @return
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
   private double msTokmh(double ms) {
      return ms * 3.6; //3.6 is the conversion factor from m/s to km/h
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

         //if the time is multiple of 1000 ms update the UI
         if (mTotalTime % 1000 == 0 && observer != null) {
            observer.updateTime();
         }
      }
   }

}


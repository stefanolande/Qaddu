package so2.unica.qaddu.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
   public static Boolean running = false;
   // Binder given to clients
   private final IBinder mBinder = new LocalBinder();
   WorkoutItem mItem;
   List<WorkoutPoint> mPoints;
   Double mDistance;
   BroadcastReceiver mBroadcastReceiver;
   //Reference to the updateUI to update the UI
   private updateUI observer;
   private int mIntevalLength;
   private long mTotalTime;

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
      running = true;

      mItem = new WorkoutItem();
      mItem.setStartDate(new Date());
      mItem.setName(intent.getStringExtra(WORKOUT_TITLE));
      DatabaseHelper.getIstance().addData(mItem, WorkoutItem.class);
      mPoints = new ArrayList<>();

      //start the gps service
      Intent gpsIntent = new Intent(getApplicationContext(), GPSService.class);
      startService(gpsIntent);


      IntentFilter filter = new IntentFilter(AppController.BROADCAST_NEW_GPS_POSITION);

      mBroadcastReceiver = new ReceiverHelper() {
         @Override
         public void onReceive(Context context, Intent intent) {
            GpsPoint point = intent.getParcelableExtra(GpsPoint.QUADDU_GPS_POINT);
            onReceivePoint(point);
         }
      };
      this.registerReceiver(mBroadcastReceiver, filter);

      //TODO retrieve the interval length from settings and set mIntervalLength

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

      mTotalTime = System.currentTimeMillis() - mItem.getStartDate().getTime();

      mPoints.add(new WorkoutPoint(mItem, point.getLatitude(), point.getLongitude(), point.getSpeed(), point.getAltitude(), mTotalTime, mDistance));
      mItem.setTotalTime(mTotalTime);
      mItem.setDistance(mDistance);

      //notify the UI
      if (this.observer != null) {
         this.observer.update();
      }
   }

   @Override
   public void onDestroy() {
      running = false;
      this.unregisterReceiver(mBroadcastReceiver);
      try {
         if (mPoints.size() > 0) {
            mItem.setPoints(mPoints);
            DatabaseHelper.getIstance().getDao().update(mItem);
         } else {
            DatabaseHelper.getIstance().removeData(mItem, WorkoutItem.class);
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      Intent intent = new Intent();
      intent.setAction(AppController.BROADCAST_NEW_WORKOUT);
      sendBroadcast(intent);
      super.onDestroy();
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
    * @return double speed in km/j
    */
   public double getTotalSpeed() {
      return msTokmh(mDistance / mTotalTime);
   }

   /**
    * Returns the average speed for the last interval in km/h
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
    * Returns the average step for the workout in seconds to km
    * @return double step in seconds to km
    */
   public double getTotalStep() {
      double timeKm = mTotalTime / (mDistance / 1000);

      return timeKm;
   }

   /**
    * Returns the average step for the last interval in seconds to km
    * @return double seconds to km
    */
   public double getIntervalStep() {
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
    * @return double distance in meters
    */
   public double getDistance() {
      return mDistance / 1000;
   }

   /**
    * Returns the duration of the workout in seconds
    * @return
    */
   public long getTime() {
      return mTotalTime;
   }

   /**
    * Converts the speed from m/s to km/h
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
      void update();
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

}

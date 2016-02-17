package so2.unica.qaddu.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
   WorkoutItem mItem;
   List<WorkoutPoint> mPoints;

   Double mDistance;

   BroadcastReceiver mBroadcastReceiver;

   private int mIntevalLength;
   private long mTotalTime;


   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return null;
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


      mPoints.add(new WorkoutPoint(mItem, point.getLatitude(), point.getLongitude(), point.getSpeed(), point.getAltitude(), System.currentTimeMillis() - mItem.getStartDate().getTime(), mDistance));
      mItem.setTotalTime(System.currentTimeMillis() - mItem.getStartDate().getTime());
      mItem.setDistance(mDistance);
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

   public void setIntervalLength(int intervalLength) {
      this.mIntevalLength = intervalLength;
   }

   public double getSpeed() {
      WorkoutPoint lastWorkoutPoint = mPoints.get(mPoints.size() - 1);
      return lastWorkoutPoint.getSpeed();
   }

   public double getTotalSpeed() {

      return msTokmh(mDistance / mTotalTime);
   }

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

   public double getTotalStep() {
      double timeKm = mTotalTime / (mDistance / 1000);

      return timeKm;
   }

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

   public double getDistance() {
      return mDistance / 1000;
   }

   public long getTime() {
      return mTotalTime;
   }

   private double msTokmh(double ms) {
      return ms * 3.6; //3.6 is the conversion factor from m/s to km/h
   }

}

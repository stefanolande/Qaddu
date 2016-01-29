package so2.unica.qaddu.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import so2.unica.qaddu.models.WorkoutPoint;

/**
 * Created by stefano on 26/01/16.
 */
public class DataService extends Service {
   private final IBinder mBinder = new LocalBinder();
   Timer timer;

   ArrayList<WorkoutPoint> workoutPoints;

   private double totalDistance;
   private long totalTime; //time in seconds from the beginning of the workout

   private int intevalLength; //length in meters of the window used to calculate the speed and the step
   private long itervalTime; //time gfhuiweio


   // The primary interface we will be calling on the service
   private GPSService mService = null;
   private boolean mIsBound;

   // Intent for the gps service
   private Intent serviceIntent;
   /**
    * Class for interacting with the main interface of the service.
    */
   private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder
            service) {
         // This is called when the connection with the service has been
         // established, giving us the service object we can use to
         // interact with the service.
         GPSService.LocalBinder binder = (GPSService.LocalBinder) service;
         mService = binder.getService();
         mIsBound = true;

         //Instance the listener that will be called when new gps point are available
         GPSService.OnNewGPSPointsListener clientListener = new
               GPSService.OnNewGPSPointsListener() {
                  @Override
                  public void onNewGPSPoint() {
                     getGPSData();
                  }
               };
         //Register the listener
         mService.addOnNewGPSPointsListener(clientListener);
      }

      @Override
      public void onServiceDisconnected(ComponentName className) {
         // This is called when the connection with the service has been
         // unexpectedly disconnected
         mIsBound = false;
         // qui andra' gestita la situazione...
      }
   };

   @Override
   public void onCreate() {
      workoutPoints = new ArrayList<>();
      totalDistance = 0;
      totalTime = 0;

      timer = new Timer();

      //Initialize intent for the service
      serviceIntent = new Intent(this, GPSService.class);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      disconnectLocalService();
   }

   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }

   private double msTokmh(double ms) {
      return ms * 3.6; //3.6 is the conversion factor from m/s to km/h
   }

   public float calculateNewDistance(WorkoutPoint newData, WorkoutPoint lastData) {
      double earthRadius = 6371000; //meters
      double dLat = Math.toRadians(newData.getLatitude() - lastData.getLatitude());
      double dLng = Math.toRadians(newData.getLongitude() - lastData.getLongitude());
      double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lastData.getLatitude())) * Math.cos(Math.toRadians(newData.getLatitude())) *
                  Math.sin(dLng / 2) * Math.sin(dLng / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      float dist = (float) (earthRadius * c);

      return dist;
   }

   private void startTimer() {
      timer.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            totalTime += 1;
         }
      }, 1000, 1000); //execute every second
   }

   private void stopTimer() {
      timer.cancel();
   }

   private void connectLocalService() {
      bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
   }

   private void disconnectLocalService() {
      if (mIsBound) {
         //Deregistro il listener
         mService.removeOnNewGPSPointsListener();
         // Detach our existing connection.
         unbindService(mConnection);
         mIsBound = false;
      }
   }

   private void getGPSData() {
      double latitude = 0;
      double longitude = 0;
      double speed = 0;
      double altitude = 0;

      // fetch the coordinates from the service
      latitude = mService.getLatitude();
      longitude = mService.getLongitude();
      speed = mService.getSpeed();
      altitude = mService.getAltitude();

      WorkoutPoint newWorkoutPoint = new WorkoutPoint(null, latitude, longitude, speed, altitude, totalTime, totalDistance);


      // check if the new data is valid
      if (latitude != 0 && longitude != 0) {
         //check if it is the first point
         if (workoutPoints.isEmpty()) {
            workoutPoints.add(newWorkoutPoint);
         } else {
            //if it is a new point calculate the distance
            WorkoutPoint lastWorkoutPoint = workoutPoints.get(workoutPoints.size() - 1);
            double deltaDistance = calculateNewDistance(newWorkoutPoint, lastWorkoutPoint);
            this.totalDistance += deltaDistance;
         }
      }

   }

   /**
    * Methods for the client
    */

   public void startWorkout() {
      //connect the gps
      connectLocalService();
      //start the timer
      startTimer();
   }

   //Met

   public void pauseWorkout() {
      stopTimer();
   }

   public void endWorkout() {
      //disconnect the gps
      disconnectLocalService();
      //stop the timer
      stopTimer();
      //TODO: Save the workout data in the database
   }

   public void setIntervalLength(int intervalLength) {
      this.intevalLength = intervalLength;
   }

   public double getSpeed() {
      WorkoutPoint lastWorkoutPoint = workoutPoints.get(workoutPoints.size() - 1);
      return lastWorkoutPoint.getSpeed();
   }

   public double getTotalSpeed() {

      return msTokmh(totalDistance / totalTime);
   }

   public double getIntevalSpeed() {
      //fetch the workout point in the last *interval* meters
      ArrayList<WorkoutPoint> lastPoints = new ArrayList<>();

      for (int i = workoutPoints.size() - 1; i >= 0; i--) {
         WorkoutPoint point = workoutPoints.get(i);
         if (this.totalDistance - point.getDistance() <= intevalLength) {
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

   public String getTotalStep() {
      double timeKm = totalTime / (totalDistance / 1000);
      SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

      return dateFormat.format(timeKm);
   }

   public String getIntervalStep() {
      //fetch the workout point in the last *interval* meters
      ArrayList<WorkoutPoint> lastPoints = new ArrayList<>();

      for (int i = workoutPoints.size() - 1; i >= 0; i--) {
         WorkoutPoint point = workoutPoints.get(i);
         if (this.totalDistance - point.getDistance() <= intevalLength) {
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

      SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

      return dateFormat.format(sToKm);
   }

   public double getDistance() {
      return totalDistance / 1000;
   }

   public String getTime() {
      SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

      return dateFormat.format(totalTime);
   }

   public class LocalBinder extends Binder {
      DataService getService() {
         return DataService.this;
      }
   }


}

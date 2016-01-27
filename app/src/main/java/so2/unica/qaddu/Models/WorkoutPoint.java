package so2.unica.qaddu.Models;

import so2.unica.qaddu.Workout;

/**
 * Created by stefano on 27/01/16.
 * This class models an istant of the workout
 */
public class WorkoutPoint {
   private double latitude;
   private double longitude;
   private double speed;
   private double step;
   private double altitude;
   private long time;
   private double distance;

   public WorkoutPoint(double latitude, double longitude, double speed, double altitude, long time, double distance) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.speed = speed;
      this.altitude = altitude;
      this.time = time;
      this.distance = distance;

      //calculate the step from the speed
      double km = distance / 1000;
      double stepInSeconds = time / km;
      this.step = stepInSeconds / 60;
   }

   public double getLongitude() {
      return longitude;
   }

   public void setLongitude(double longitude) {
      this.longitude = longitude;
   }

   public double getSpeed() {
      return speed;
   }

   public void setSpeed(double speed) {
      this.speed = speed;
   }

   public double getLatitude() {
      return latitude;
   }

   public void setLatitude(double latitude) {
      this.latitude = latitude;
   }

   public double getStep() {
      return step;
   }

   public double getAltitude() {
      return altitude;
   }

   public void setAltitude(double altitude) {
      this.altitude = altitude;
   }

   public long getTime() {
      return time;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public double getDistance() {
      return distance;
   }

   public void setDistance(double distance) {
      this.distance = distance;
   }
}

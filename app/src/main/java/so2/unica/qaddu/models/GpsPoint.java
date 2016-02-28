package so2.unica.qaddu.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class used to pass data between the GPSService and the WorkoutService. Models a point received from the gps.
 */
public class GpsPoint implements Parcelable {
   public static final String QADDU_GPS_POINT = "QuadduGpsPoint";
   public static final Creator<GpsPoint> CREATOR = new Creator<GpsPoint>() {
      @Override
      public GpsPoint createFromParcel(Parcel in) {
         return new GpsPoint(in);
      }

      @Override
      public GpsPoint[] newArray(int size) {
         return new GpsPoint[size];
      }
   };
   private double latitude;
   private double longitude;
   private double speed;
   private double altitude;
   private boolean isSpeedCalculated;

   protected GpsPoint(Parcel in) {
      latitude = in.readDouble();
      longitude = in.readDouble();
      speed = in.readDouble();
      altitude = in.readDouble();
      isSpeedCalculated = in.readByte() != 0;
   }

   public GpsPoint(double latitude, double longitude, double speed, double altitude, boolean isSpeedCalculated) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.speed = speed;
      this.altitude = altitude;
      this.isSpeedCalculated = isSpeedCalculated;
   }

   public double getLatitude() {
      return latitude;
   }

   public double getLongitude() {
      return longitude;
   }

   public double getSpeed() {
      return speed;
   }

   public double getAltitude() {
      return altitude;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeDouble(latitude);
      dest.writeDouble(longitude);
      dest.writeDouble(speed);
      dest.writeDouble(altitude);
      dest.writeByte((byte) (isSpeedCalculated ? 1 : 0));
   }
}

package so2.unica.qaddu.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import so2.unica.qaddu.AppController;
import so2.unica.qaddu.R;
import so2.unica.qaddu.models.GpsPoint;

/**
 * Created by stefano on 18/11/15.
 */
public class GPSService extends Service
      implements LocationListener {

   public static Boolean mRunning = false;
   private LocationManager mLocationManager;

   private Long mLastUpdate = null;
   private Double mLastLongitude = null;
   private Double mLastLatitude = null;

   @Override
   public void onCreate() {
      subscribeToLocationUpdates();
   }


   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      mRunning = true;
      return super.onStartCommand(intent, flags, startId);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      mRunning = false;
      mLocationManager.removeUpdates(this);
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   //Notify the clients when a new point is available
   public void onLocationChanged(Location location) {

      Boolean isSpeedCalculated = false;
      if (!location.hasSpeed() && mLastUpdate != null) {
         Double time = (System.currentTimeMillis() - mLastUpdate) / 1000.0;

         float distanceArray[] = new float[1];
         Location.distanceBetween(location.getLatitude(), location.getLongitude(), mLastLatitude, mLastLongitude, distanceArray);
         float distance = distanceArray[0];
         location.setSpeed((float) (distance / time));
         isSpeedCalculated = true;
      }

      mLastUpdate = System.currentTimeMillis();
      mLastLatitude = location.getLatitude();
      mLastLongitude = location.getLongitude();

      Intent intent = new Intent();
      intent.setAction(AppController.BROADCAST_NEW_GPS_POSITION);
      intent.putExtra(GpsPoint.QUADDU_GPS_POINT, new GpsPoint(location.getLatitude(), location.getLongitude(), location.getSpeed() * 3.6, location.getAltitude(), isSpeedCalculated));
      sendBroadcast(intent);
   }

   @Override
   public void onProviderDisabled(String provider) {
   }

   @Override
   public void onProviderEnabled(String provider) {
   }

   @Override
   public void onStatusChanged(String provider, int status, Bundle extras) {
   }

   private void subscribeToLocationUpdates() {
      this.mLocationManager =
            (LocationManager) getSystemService(Context.LOCATION_SERVICE);

      //Notify the user if the GPS is disabled
      if (!mLocationManager.isProviderEnabled("gps")) {
         Toast.makeText(this, R.string.gps_disable,
               Toast.LENGTH_LONG).show();
      }
      this.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
   }

}

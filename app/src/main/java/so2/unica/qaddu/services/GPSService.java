package so2.unica.qaddu.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by stefano on 18/11/15.
 */
public class GPSService extends Service
      implements LocationListener {
   private LocationManager locationManager;
   private double latitude;
   private double longitude;
   private double speed;
   private double altitude;

   private Long lastupdate = null;
   private Double lastlongitude = null;
   private Double lastlatitude = null;

   @Override
   public void onCreate() {
      Toast.makeText(this, "ServiceonCreate()",
            Toast.LENGTH_LONG).show();
      latitude = 0;
      longitude = 0;
      subscribeToLocationUpdates();
   }


   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.d("GPS","Service started");
      return super.onStartCommand(intent, flags, startId);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      locationManager.removeUpdates(this);
      Toast.makeText(this, "ServiceonDestroy()",
            Toast.LENGTH_LONG).show();
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }


   //Metodi usati dai client
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

   public Double calculateNewDistance(Double lat, Double lon) {
      double earthRadius = 6371000; //meters
      double dLat = Math.toRadians(lat - lastlatitude);
      double dLng = Math.toRadians(lon - lastlongitude);
      double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(Math.toRadians(lastlatitude)) * Math.cos(Math.toRadians(lat)) *
                      Math.sin(dLng / 2) * Math.sin(dLng / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

      return (earthRadius * c);
   }

   //Appena è disponibile un nuovo punto notifico il client registrato
   public void onLocationChanged(Location location) {
      if (lastupdate != null){
         Long now = System.currentTimeMillis();
         if(now - lastupdate > 1000){
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Double time = (now-lastupdate)/1000.0;
            Double distance =  calculateNewDistance(latitude,longitude);

            this.speed = distance/time*3.6;
            // Aggiorna le coordinate
            //speed = location.getSpeed();
            altitude = location.getAltitude();
            //Avviso i client (uno in questo caso)
            Intent intent = new Intent();
            intent.setAction("gianni.gianni");
            intent.putExtra("speed",speed);
            sendBroadcast(intent);
         }
      }

      lastupdate = System.currentTimeMillis();
      lastlatitude = location.getLatitude();
      lastlongitude = location.getLongitude();
   }

   @Override
   public void onProviderDisabled(String provider) {
      Toast.makeText(this, "onProviderDisabled " + provider,
            Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onProviderEnabled(String provider) {
      Toast.makeText(this, "onProviderEnabled " + provider, Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onStatusChanged(String provider, int status, Bundle extras) {
      Toast.makeText(this, "onStatusChanged " + provider + " status: " + status, Toast.LENGTH_SHORT).show();
   }

   private void subscribeToLocationUpdates() {
      this.locationManager =
            (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      //Verifica se il GPS e' abilitato altrimenti avvisa l'utente
      if (!locationManager.isProviderEnabled("gps")) {
         Toast.makeText(this, "GPS e' attualmente disabilitato. E' possibile abilitarlo dal menu impostazioni.",
               Toast.LENGTH_LONG).show();
      }
      this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
   }

}

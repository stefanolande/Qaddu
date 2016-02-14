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
import android.util.Log;
import android.widget.Toast;

import so2.unica.qaddu.AppController;
import so2.unica.qaddu.R;
import so2.unica.qaddu.models.GpsPoint;

/**
 * Created by stefano on 18/11/15.
 */
public class GPSService extends Service
        implements LocationListener {

    public static Boolean running = false;
    private LocationManager locationManager;

    private Long lastupdate = null;
    private Double lastlongitude = null;
    private Double lastlatitude = null;

    @Override
    public void onCreate() {
        subscribeToLocationUpdates();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        locationManager.removeUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

        Boolean calculatedSpeed = false;
        if (!location.hasSpeed() && lastupdate != null) {
            Double time = (System.currentTimeMillis() - lastupdate) / 1000.0;
            Double distance = calculateNewDistance(location.getLatitude(), location.getLongitude());
            location.setSpeed((float) (distance / time));
            calculatedSpeed = true;
        }

        lastupdate = System.currentTimeMillis();
        lastlatitude = location.getLatitude();
        lastlongitude = location.getLongitude();

        Intent intent = new Intent();
        intent.setAction(AppController.BROADCAST_NEW_GPS_POSITION);
        intent.putExtra(GpsPoint.QUADDU_GPS_POINT, new GpsPoint(location.getLatitude(), location.getLongitude(), location.getSpeed() * 3.6, location.getAltitude(), calculatedSpeed));
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
        this.locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Verifica se il GPS e' abilitato altrimenti avvisa l'utente
        if (!locationManager.isProviderEnabled("gps")) {
            Toast.makeText(this, R.string.gps_disable,
                    Toast.LENGTH_LONG).show();
        }
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

}

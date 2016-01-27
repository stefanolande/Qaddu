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
import android.widget.Toast;

/**
 * Created by stefano on 18/11/15.
 */
public class GPSService extends Service
        implements LocationListener {
    private final IBinder mBinder = new LocalBinder();
    private OnNewGPSPointsListener clientListener;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private double speed;
    private double altitude;

    @Override
    public void onCreate() {
        Toast.makeText(this, "ServiceonCreate()",
                Toast.LENGTH_LONG).show();
        latitude = 0;
        longitude = 0;
        subscribeToLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        Toast.makeText(this, "ServiceonDestroy()",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        GPSService getService() {
            return GPSService.this;
        }
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

    public void addOnNewGPSListener(OnNewGPSPointsListener
                                            listener) {
        clientListener = listener;
    }

    public void removeOnNewGPSPointsListener() {
        clientListener = null;
    }

    //Appena è disponibile un nuovo punto notifico il client registrato
    public void onLocationChanged(Location location) {
        // Aggiorna le coordinate
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = location.getSpeed();
        altitude = location.getAltitude();
        //Avviso i client (uno in questo caso)
        if (clientListener != null)

        {
            clientListener.onNewGPSPoint();
        }
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

    public void addOnNewGPSPointsListener(OnNewGPSPointsListener listener) {
        clientListener = listener;
    }

    /**
     * Interface for listeners
     */
    public interface OnNewGPSPointsListener {
        public void onNewGPSPoint();
    }
}

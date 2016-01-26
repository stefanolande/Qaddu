package so2.unica.qaddu.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.content.ComponentName;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by stefano on 26/01/16.
 */
public class DataService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private double lastLatitude;
    private double lastLongitude;
    private double totalDistance;
    private long totalTime; //time in seconds from the beginning of the workout

    private int intevalLength; //length in meters of the window used to calculate the speed and the step
    private long itervalTime; //time gfhuiweio


    // The primary interface we will be calling on the service
    private GPSService mService = null;
    private boolean mIsBound;

    // Intent for the gps service
    private Intent serviceIntent;

    @Override
    public void onCreate() {
        lastLatitude = 0;
        lastLongitude = 0;
        totalDistance = 0;
        totalTime = 0;

        //Initialize intent for the service
        serviceIntent = new Intent(this, GPSService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: scollegarsi dal servizio GPS
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        DataService getService() {
            return DataService.this;
        }
    }

    private double msTokmh(double ms) {
        return ms * 3.6; //3.6 is the conversion factor from m/s to km/h
    }

    /**
     * Class and method for the gps service connection
     */

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

            // Istanzio il listener che verra' chiamato dal Service quando saranno
            // disponibili nuovi punti GPS
            GPSService.OnNewGPSPointsListener clientListener = new
                    GPSService.OnNewGPSPointsListener() {
                        @Override
                        public void onNewGPSPoint() {
                            getGPSData();
                        }
                    };
            //Registriamo il listener per ricevere gli aggiornamenti
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

    /**
     * Methods for the client
     */

    public void setIntervalLength(int intervalLength) {
        this.intevalLength = intervalLength;
    }

    public double getSpeed() {
        //prendi velocità dal gps
        return 0;
    }

    public double getTotalSpeed() {
        return msTokmh(totalDistance / totalTime);
    }

    public double getIntevalSpeed() {
        return 0; //attualmente non ho idea di come fare
    }

    public String getTotalStep() {
        double timeKm = totalTime/(totalDistance/1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormat.format(timeKm);
    }

    public String getIntervalStep() {
        return "boh";
    }

    public double getDistance() {
        return totalDistance/1000;
    }

    public String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormat.format(totalTime);
    }


}

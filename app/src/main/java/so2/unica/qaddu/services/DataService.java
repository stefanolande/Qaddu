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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stefano on 26/01/16.
 */
public class DataService extends Service {
    private final IBinder mBinder = new LocalBinder();
    Timer timer;

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

    public class LocalBinder extends Binder {
        DataService getService() {
            return DataService.this;
        }
    }

    private double msTokmh(double ms) {
        return ms * 3.6; //3.6 is the conversion factor from m/s to km/h
    }

    public float calculateNewDistance(double newLat, double newLon) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(newLat - this.lastLatitude);
        double dLng = Math.toRadians(newLon - this.lastLongitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.lastLatitude)) * Math.cos(Math.toRadians(newLat)) *
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

        // fetch the coordinates from the service
        latitude = mService.getLatitude();
        longitude = mService.getLongitude();

        // check if the new data is valid
        if (latitude != 0 && longitude != 0) {
            //check if it is the first point
            if (this.lastLatitude == 0 && this.lastLongitude == 0) {

                //save the new coordinates
                this.lastLatitude = latitude;
                this.lastLongitude = longitude;
            } else {
                //if it is a new point calculate the distance
                double deltaDistance = calculateNewDistance(latitude, longitude);
                this.totalDistance += deltaDistance;
            }
        }

    }

    //Met

    /**
     * Methods for the client
     */

    public void startWorkout() {
        //connect the gps
        connectLocalService();
        //start the timer
        startTimer();
    }

    public void pauseWorkout(){
        stopTimer();
    }

    public void endWorkout(){
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
        double timeKm = totalTime / (totalDistance / 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormat.format(timeKm);
    }

    public String getIntervalStep() {
        return "boh";
    }

    public double getDistance() {
        return totalDistance / 1000;
    }

    public String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormat.format(totalTime);
    }


}

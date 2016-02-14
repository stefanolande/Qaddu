package so2.unica.qaddu.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    public Double calculateNewDistance(Double oldLat, Double oldLon,Double lat, Double lon) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(oldLat - lat);
        double dLng = Math.toRadians(oldLon - lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(oldLat)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (earthRadius * c);
    }

    public void onReceivePoint(GpsPoint point){
        if(mPoints.size() == 0){
            mDistance = 0.0;
        } else {
            mDistance += calculateNewDistance(mPoints.get(mPoints.size()-1).getLatitude(),
                    mPoints.get(mPoints.size()-1).getLongitude(),
                    point.getLatitude(),
                    point.getLongitude());
        }

        mPoints.add(new WorkoutPoint(mItem, point.getLatitude(), point.getLongitude(), point.getSpeed(), point.getAltitude(), System.currentTimeMillis()-mItem.getStartDate().getTime(),mDistance));
        mItem.setTotalTime(System.currentTimeMillis()-mItem.getStartDate().getTime());
        mItem.setDistance(mDistance);
    }

    @Override
    public void onDestroy() {
        running = false;
        this.unregisterReceiver(mBroadcastReceiver);
        try {
            if(mPoints.size() > 0){
            mItem.setPoints(mPoints);
            DatabaseHelper.getIstance().getDao().update(mItem);}
            else {
                DatabaseHelper.getIstance().removeData(mItem,WorkoutItem.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(AppController.BROADCAST_NEW_WORKOUT);
        sendBroadcast(intent);
        super.onDestroy();
    }
}

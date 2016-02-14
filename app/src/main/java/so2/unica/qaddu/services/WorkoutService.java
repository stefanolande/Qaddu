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
 * Created by Riccardo on 14/02/2016.
 */
public class WorkoutService extends Service {
    public static final String WORKOUT_TITLE = "QuadduWorkout";

    WorkoutItem mItem;
    List<WorkoutPoint> mPoints;

    BroadcastReceiver mBroadcastReceiver;

    int coso = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

    public void onReceivePoint(GpsPoint point){
        //double latitude, double longitude, double speed, double altitude, long time, double distance
        mPoints.add(new WorkoutPoint(mItem, point.getLatitude(), point.getLongitude(), point.getSpeed(), point.getAltitude(), System.currentTimeMillis(), coso++));
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(mBroadcastReceiver);
        try {
            mItem.setPoints(mPoints);
            mItem.setDistance(0.0);
            mItem.setTotalTime(0l);
            DatabaseHelper.getIstance().getDao().update(mItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}

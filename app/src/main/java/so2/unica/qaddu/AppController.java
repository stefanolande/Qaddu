package so2.unica.qaddu;

import android.app.Application;

import so2.unica.qaddu.helpers.DatabaseHelper;

/**
 * This class initializes the DatabaseHelper and defines the intents' strings to communicate with the GPS
 */
public class AppController extends Application {
   public static final String BROADCAST_NEW_GPS_POSITION = "QuadduBroadcastNewGpsPosition";
   public static final String BROADCAST_NEW_WORKOUT = "QuadduBroadcastNewWorkout";
   public static final String GPS_TURNED_OFF = "QadduGPSTurnedOff";
   public static final String GPS_TURNED_ON = "QadduGPSTurnedOn";

   @Override
   public void onCreate() {
      super.onCreate();

      DatabaseHelper.initialize(getApplicationContext());
   }
}

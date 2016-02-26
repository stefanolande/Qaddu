package so2.unica.qaddu.helpers;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import so2.unica.qaddu.MainActivity;
import so2.unica.qaddu.R;

/**
 * Helper used to manage the creation and removal of notifications.
 */
public class NotificationHelper {
   public static final int NOTIFICATION_ID = 42;

   /**
    * Creates a permanent notification to inform the user that a workout is running
    */
   public static void createWokrkoutNotification(Activity activity) {
      NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(activity)
                  .setSmallIcon(R.drawable.qaddu_notification)
                  .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher))
                  .setContentTitle(activity.getString(R.string.app_name))
                  .setOngoing(true)
                  .setContentText(activity.getString(R.string.workout_running_notification));
      Intent resultIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
      PendingIntent resultPendingIntent =
            PendingIntent.getActivity(
                  activity,
                  0,
                  resultIntent,
                  PendingIntent.FLAG_UPDATE_CURRENT
            );
      mBuilder.setContentIntent(resultPendingIntent);
      // Gets an instance of the NotificationManager service
      NotificationManager mNotifyMgr = (NotificationManager) activity.getSystemService(Activity.NOTIFICATION_SERVICE);
      // Builds the notification and issues it.
      mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
   }

   /**
    * Removes the "workout running" notification
    */
   public static void removeWorkoutNotification(Activity activity) {
      //remove the notification
      NotificationManager mNotifyMgr = (NotificationManager) activity.getSystemService(Activity.NOTIFICATION_SERVICE);
      mNotifyMgr.cancel(NOTIFICATION_ID);
   }
}

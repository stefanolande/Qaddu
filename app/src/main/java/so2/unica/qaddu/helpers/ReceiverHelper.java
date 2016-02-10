package so2.unica.qaddu.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Riccardo on 10/02/2016.
 */
abstract public class ReceiverHelper extends BroadcastReceiver {
    @Override
    abstract public void onReceive(Context context, Intent intent);
}

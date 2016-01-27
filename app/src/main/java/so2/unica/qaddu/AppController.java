package so2.unica.qaddu;

import android.app.Application;

import so2.unica.qaddu.helpers.DatabaseHelper;

/**
 * Created by Sergio on 28/01/2016.
 */
public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseHelper.initialize(getApplicationContext());
    }
}

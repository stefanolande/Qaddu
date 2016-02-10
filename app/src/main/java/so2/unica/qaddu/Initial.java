package so2.unica.qaddu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import so2.unica.qaddu.services.GPSService;

/*
* This is the initial activity
 */
public class Initial extends AppCompatActivity {
   int time = 2000;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_initial);
      new Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
            final Intent mainIntent = new Intent(Initial.this, MainActivity.class);
            Initial.this.startActivity(mainIntent);
            Initial.this.finish();
         }
      }, time);


   }

}

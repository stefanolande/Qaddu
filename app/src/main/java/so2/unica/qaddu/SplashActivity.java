package so2.unica.qaddu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * This is the initial activity and shows the application logo
 */
public class SplashActivity extends AppCompatActivity {
   int time = 2000;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_initial);
      new Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
            final Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
         }
      }, time);

   }

}

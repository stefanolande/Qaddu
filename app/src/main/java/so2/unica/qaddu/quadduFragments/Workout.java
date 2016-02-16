package so2.unica.qaddu.quadduFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.AppController;
import so2.unica.qaddu.R;
import so2.unica.qaddu.helpers.ReceiverHelper;
import so2.unica.qaddu.models.GpsPoint;
import so2.unica.qaddu.services.GPSService;
import so2.unica.qaddu.services.WorkoutService;


public class Workout extends Fragment {

    @Bind(R.id.circle_container)
    LinearLayout mCircleContainer;

    @Bind(R.id.min_circle)
    View mCircle;

    @Bind(R.id.tv_instant_speed)
    TextView tvInstantSpeed;

    @Bind(R.id.tv_target)
    TextView tvTargetSpeed;

    @Bind(R.id.tv_total_km)
    TextView tvTotalKm;

    @Bind(R.id.tv_total_speed)
    TextView tvTotalKmH;

    @Bind(R.id.tv_total_time)
    TextView tvTotalTime;

    @Bind(R.id.tv_total_step)
    TextView tvTotalStep;

    @Bind(R.id.tv_last_x_speed)
    TextView tvLastSpeed;

    @Bind(R.id.tv_last_x_step)
    TextView tvLastStep;

    @Bind(R.id.nameWorkout)
    EditText etNameWorkout;

    @Bind(R.id.btn_stop)
    ImageButton bStop;

    @Bind(R.id.btn_start)
    ImageButton bStart;

    BroadcastReceiver mBroadcastReceiver;


    int mContainterWidth;
    int tmpOff = -100;

    float totalKm = 0;      //It contains the total kilometers traveled
    float totalKmH = 0;     //It contains the total speed
    float totalTime = 0;    //It contain the time of the workout
    float targetSpeed = 0;  //It contain the target speed
    float lastKmh = 0;      //It contains the speed of the last X meter
    float totalStep = 0;    //It contain the total step
    float lastStep = 0;     //It contain the step of the last X meter
    float instantSpeed = 0; //It contain the instant speed
    String nameWorkout = "";//It contain the workout's name


    public Workout() {
        // Required empty public constructor
    }

    private void setCircleOffset(double o) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCircle.getLayoutParams();
        int margin = (int) (mContainterWidth / 100 * o);
        params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
        mCircle.setLayoutParams(params);
    }


    //This method is used to set the speed into the TextView of the instant speed
    private void setInstantSpeed(double instantSpeed, boolean isCalculated) {
        DecimalFormat df = new DecimalFormat("#0.00");
        tvInstantSpeed.setText(df.format(instantSpeed) + " KM/H");
        if (isCalculated) {
            tvInstantSpeed.setTextColor(ContextCompat.getColor(getActivity(), R.color.QadduRed));
        } else {
            tvInstantSpeed.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        }
    }

    //This method is used to set the target speed into the TextView of the target speed
    private void setTargetSpeed(float targetSpeed) {
        tvTargetSpeed.setText(Float.toString(targetSpeed) + " Km/h");
    }

    //This method is used to set the total Km traveled into the TextView of the total km
    private void setTotalKm(float totalKm) {
        tvTotalKm.setText(Float.toString(totalKm) + " Km");
    }

    //This method is used to set the total speed into the TextView of the total speed
    private void setTotalSpeed(double totalKmH) {
        tvTotalKmH.setText(Double.toString(totalKmH) + " KM/H");
    }

    //This method is used to set the total time into the TextView of the total time
    private void setTotalTime(float totalTime) {
        //tvTotalTime.setText(Float.toString(totalTime));
        tvTotalTime.setText("01:31:12");
    }

    //This method is used to set the total step speed into the TextView of the total step
    private void setTotalStep(float totalStep) {
        tvTotalStep.setText(Float.toString(totalStep) + " /KM");
    }

    //This method is used to set the last X meter speed into the TextView of the last X meter speed
    private void setLastSpeed(float lastSpeed) {
        tvLastSpeed.setText(Float.toString(lastSpeed) + " KM/H");
    }

    //This method is used to set the last X meter's step speed into the TextView of the LastStep
    private void setLastStep(float lastStep) {
        tvLastStep.setText(Float.toString(lastStep) + " /KM");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        ButterKnife.bind(this, view);

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GPSService.running) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), GPSService.class);
                    getActivity().startService(intent);
                    //when the workout is play or pause, the user can stop the workout (the stop button in enable, is blu)
                    bStop.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
                    bStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(), GPSService.class);
                    getActivity().stopService(intent);

                    bStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                    //bStop.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
                }

                if (!WorkoutService.running) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), WorkoutService.class);
                    intent.putExtra(WorkoutService.WORKOUT_TITLE, etNameWorkout.getText().toString());
                    getActivity().startService(intent);
                }
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GPSService.running) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), GPSService.class);
                    getActivity().stopService(intent);
                    bStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                }
                Intent intent = new Intent(getActivity().getApplicationContext(), WorkoutService.class);
                getActivity().stopService(intent);
                //when the Workout is stopped, the user can't click stop button again
                bStop.setImageDrawable(getResources().getDrawable(R.drawable.ic_stopgray));
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mCircleContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContainterWidth = mCircleContainer.getWidth();
            }
        });
        if (mBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter(AppController.BROADCAST_NEW_GPS_POSITION);

            mBroadcastReceiver = new ReceiverHelper() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    GpsPoint point = intent.getParcelableExtra(GpsPoint.QUADDU_GPS_POINT);

                    setInstantSpeed(point.getSpeed(), point.isSpeedCalculated());
                }
            };
            getActivity().registerReceiver(mBroadcastReceiver, filter);

        }

/*
      new Thread(new Runnable() {
         public void run() {
            int sum = 1;
            while (true) {
               tmpOff += sum;

               getActivity().runOnUiThread(new Runnable() {
                  public void run() {
                     setCircleOffset(tmpOff);
                     setInstantSpeed(((tmpOff / 10 + 9) + 0.1f) + instantSpeed);
                     setTargetSpeed(targetSpeed);
                     setTotalKm(totalKm);
                     setTotalSpeed(totalKmH);
                     setTotalTime(totalTime);
                     setTotalStep(totalStep);
                     setLastSpeed(lastKmh);
                     setLastStep(lastStep);
                  }
               });


               if (tmpOff == 100) {
                  sum = -1;

               }
               if (tmpOff == -100) {
                  sum = 1;
               }
               try {
                  Thread.sleep(150);
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }
            }
         }
      }).start();

*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

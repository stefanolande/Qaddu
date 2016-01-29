package so2.unica.qaddu.quadduFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.R;


public class Workout extends Fragment {

    @Bind(R.id.circle_container)
    LinearLayout mCircleContainer;

    @Bind(R.id.min_circle)
    View mCircle;

    @Bind(R.id.tv_instantSpeed)
    TextView tvInstantSpeed;

    @Bind(R.id.tv_target)
    TextView tvTargetSpeed;

    @Bind(R.id.tv_totalKm)
    TextView tvTotalKm;

    int mContainterWidth;
    int tmpOff = -100;

    float totalKm=0;      //It contains the total kilometers traveled
    float totalKmH=0;     //It contains the total speed
    float totalTime=0;    //It contain the time of the workout
    float targetSpeed=0;  //It contain the target speed
    float lastKmh=0;      //It contains the speed of the last X meter
    float totalStep=0;    //It contain the total step
    float lastStep=0;     //It contain the step of the last X meter
    float instantSpeed=0; //It contain the instant speed
    String nameWorkout="";//It contain the workout's name




    public Workout() {
        // Required empty public constructor
    }

    private void setCircleOffset(double o){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mCircle.getLayoutParams();
        int margin = (int) (mContainterWidth/100*o);
        params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
        mCircle.setLayoutParams(params);
    }

    //This method is used to set the speed into the TextView of the instant speed
    private void setInstantSpeed(float instantSpeed){
        tvInstantSpeed.setText(Float.toString(instantSpeed) + " Km/h");
    }
    //This method is used to set the target speed into the TextView of the target speed
    private void setTargetSpeed(float targetSpeed){
        tvTargetSpeed.setText(Float.toString(targetSpeed) + " Km/h");
    }
    //This method is used to set the total Km traveled into the TextView of the total km
    private void setTotalKm(float totalKm){
        tvTotalKm.setText(Float.toString(totalKm) + " Km");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        ButterKnife.bind(this, view);

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




        new Thread(new Runnable() {
            public void run() {
                int sum = 1;
while(true){
                tmpOff+=sum;

    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            setCircleOffset(tmpOff);
            setInstantSpeed(((tmpOff / 10 + 8) + 0.1f)+instantSpeed);
            setTargetSpeed(targetSpeed);
            setTotalKm(totalKm);
        }
    });


    if(tmpOff == 100){
                    sum = -1;

                }
    if(tmpOff == -100){
        sum = 1;
    }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }}
        }).start();


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

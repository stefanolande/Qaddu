package so2.unica.qaddu.quadduFragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.R;
import so2.unica.qaddu.WorkoutDetail;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;


public class Workout extends Fragment {

    @Bind(R.id.circle_container)
    LinearLayout mCircleContainer;

    @Bind(R.id.min_circle)
    View mCircle;

    int mContainterWidth;

    int tmpOff = -100;

    public Workout() {
        // Required empty public constructor
    }

    private void setCircleOffset(double o){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mCircle.getLayoutParams();
        int margin = (int) (mContainterWidth/100*o);
        params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
        mCircle.setLayoutParams(params);
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

package so2.unica.qaddu.quadduFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.R;
import so2.unica.qaddu.WorkoutDetail;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;


public class History extends Fragment {

    @Bind(R.id.lvWorkouts)
    ListView mListView;

    public History() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayAdapter<WorkoutItem> listAdapter;

        final List<WorkoutItem> workouts = DatabaseHelper.getIstance().GetData(WorkoutItem.class);

        if (workouts != null) {
            listAdapter = new ArrayAdapter<WorkoutItem>(getActivity(), R.layout.item_workout_list, workouts) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_workout_list, parent, false);
                    }
                    WorkoutItem workoutItem = getItem(position);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yy");
                    String startDate = dateFormat.format(workoutItem.getStart());

                    dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String startTime = dateFormat.format(workoutItem.getStart());

                    Double km = Math.floor(workoutItem.getDistance() / 1000);

                    ((TextView) convertView.findViewById(R.id.tvWorkoutName)).setText(workoutItem.getName());
                    ((TextView) convertView.findViewById(R.id.tvWorkoutDate)).setText(startDate);
                    ((TextView) convertView.findViewById(R.id.tvWorkoutTime)).setText(startTime);
                    ((TextView) convertView.findViewById(R.id.tvWorkoutDuration)).setText(km + " km");

                    return convertView;
                }
            };

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent mainIntent = new Intent(getActivity(), WorkoutDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("WorkoutID", workouts.get(position).getId());
                    mainIntent.putExtras(bundle);
                    getActivity().startActivity(mainIntent);
                }
            });

            mListView.setAdapter(listAdapter);
        }

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

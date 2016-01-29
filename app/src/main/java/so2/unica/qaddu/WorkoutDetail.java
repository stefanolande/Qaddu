package so2.unica.qaddu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;

public class WorkoutDetail extends AppCompatActivity {
    @Bind(R.id.tool_bar)
    Toolbar mToolBar;

    @Bind(R.id.tvWorkoutName)
    TextView tvWorkoutName;

    @Bind(R.id.spinnerX)
    Spinner spinnerX;

    @Bind(R.id.spinnerY)
    Spinner spinnerY;

    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;

    WorkoutItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (getIntent().getData() != null) {
            String filePath = getIntent().getData().getEncodedPath();


            try {
                StringBuilder text = new StringBuilder();

                BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();

                Gson gson = new Gson();
                mItem = gson.fromJson(text.toString(),WorkoutItem.class);

                for(int i = 0; i <mItem.getPoints().size(); i++){
                    mItem.getPoints().get(i).setWorkout(mItem);
                }

                DatabaseHelper.getIstance().addData(mItem, WorkoutItem.class);
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }


        } else {
            int id = bundle.getInt("WorkoutID");
            mItem = (WorkoutItem) DatabaseHelper.getIstance().getItemById(id, WorkoutItem.class);
        }

        //load the spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.x_array, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerX.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this, R.array.y_array, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerY.setAdapter(adapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Gson gson = new Gson();

                String filename = "workout.qaddu";
                String string = gson.toJson(mItem);

                try {
                    File myFile = new File("/sdcard/" + filename);
                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter =
                            new OutputStreamWriter(fOut);
                    myOutWriter.append(string);
                    myOutWriter.close();
                    fOut.close();
                    Toast.makeText(getBaseContext(),
                            "Done writing SD 'mysdfile.txt'",
                            Toast.LENGTH_SHORT).show();

                    Uri path = Uri.fromFile(myFile);
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("application/octet-stream");
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Guarda il mio allenamento");
                    intent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivityForResult(Intent.createChooser(intent, "Send mail..."), 1222);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
        tvWorkoutName.setText(mItem.getName());

        mToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkoutDetail.this.finish();
            }
        });


        setTitle("Workout detail");

        LineChart lineChart = (LineChart) findViewById(R.id.chart);


        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(2f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        LineDataSet dataset = new LineDataSet(entries, "Workout");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add(" ");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        LineData data = new LineData(labels, dataset);
        //dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);

        lineChart.setData(data);
        lineChart.animateY(5000);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1222) {
            String filename = "workout.qaddu";
            File myFile = new File("/sdcard/" + filename);
            myFile.delete();
        }

    }

}

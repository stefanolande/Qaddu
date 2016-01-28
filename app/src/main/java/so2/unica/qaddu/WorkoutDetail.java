package so2.unica.qaddu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.j256.ormlite.dao.ForeignCollection;


import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;

public class WorkoutDetail extends AppCompatActivity {
    @Bind(R.id.tool_bar)
    Toolbar mToolBar;

    @Bind(R.id.tvWorkoutName)
    TextView tvWorkoutName;

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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
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


        setTitle("Dettaglio allenamento");

        LineChart lineChart = (LineChart) findViewById(R.id.chart);


        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(2f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
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

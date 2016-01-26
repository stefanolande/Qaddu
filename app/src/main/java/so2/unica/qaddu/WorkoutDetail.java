package so2.unica.qaddu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.util.ArrayList;

public class WorkoutDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        LineChart chart = (LineChart) findViewById(R.id.chart);


        chart.setDescription("My Chart");
        chart.animateXY(2000, 2000);
        chart.invalidate();



        // creating list of entry
    /*    ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(2f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        // creating labels
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        LineData data = new LineData(labels, dataset);
        LineChart.setData(data); // set the data and list of lables into chart

        LineChart.setDescription("Description");  // set the description*/

    }



}

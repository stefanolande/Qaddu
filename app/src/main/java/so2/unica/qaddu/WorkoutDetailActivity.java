package so2.unica.qaddu;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;
import so2.unica.qaddu.quadduFragments.History;


public class WorkoutDetailActivity extends AppCompatActivity {

   @Bind(R.id.tool_bar)
   Toolbar mToolBar;
   @Bind(R.id.tvWorkoutName)
   TextView tvWorkoutName;
   @Bind(R.id.spinnerX)
   Spinner spinnerY;
   @Bind(R.id.spinnerY)
   Spinner spinnerX;
   @Bind(R.id.fab)
   FloatingActionButton floatingActionButton;
   @Bind(R.id.chart)
   GraphView graph;
   Menu mMenu;
   WorkoutItem mItem;
   ArrayList<Double> listYAxis;
   xAxisType xAxis;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_workout_detail);
      ButterKnife.bind(this);
      setSupportActionBar(mToolBar);

      //Set the title of the action bar
      setTitle("Workout detail");

      Intent intent = getIntent();
      Bundle bundle = intent.getExtras();

      //Check if the user is opening a json workout file
      //adn read it
      if (getIntent().getData() != null) {
         String filePath = getIntent().getData().getPath();


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
            mItem = gson.fromJson(text.toString(), WorkoutItem.class);

            for (int i = 0; i < mItem.getPoints().size(); i++) {
               mItem.getPoints().get(i).setWorkout(mItem);
            }

            DatabaseHelper.getIstance().addData(mItem, WorkoutItem.class);
         } catch (IOException e) {
            Log.d("E", "EXCEPTION");
         }


      } else {
         //The user is opening a workout from the history
         //get the id and fetch it from the database
         int id = bundle.getInt(History.WORKOUT_ID);
         mItem = (WorkoutItem) DatabaseHelper.getIstance().getItemById(id, WorkoutItem.class);
      }

      //set the time as default x axis
      xAxis = xAxisType.TIME;

      //load the spinners
      ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.y_array, android.R.layout.simple_dropdown_item_1line);
      adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
      spinnerY.setAdapter(adapter);

      //choose the list of Y data to show and call plot to re-draw the graph
      spinnerY.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            listYAxis = new ArrayList<>();
            switch (position) {
               case 0:
                  for (WorkoutPoint point : mItem.getPoints()) {
                     listYAxis.add(point.getSpeed());
                  }
                  break;
               case 1:
                  for (WorkoutPoint point : mItem.getPoints()) {
                     listYAxis.add(point.getStep());
                  }
                  break;
               case 2:
                  for (WorkoutPoint point : mItem.getPoints()) {
                     listYAxis.add(point.getAltitude());
                  }
                  break;
            }
            plot();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            listYAxis = new ArrayList<>();
            for (WorkoutPoint point : mItem.getPoints()) {
               listYAxis.add(point.getSpeed());
            }
         }
      });

      adapter = ArrayAdapter.createFromResource(this, R.array.x_array, android.R.layout.simple_dropdown_item_1line);
      adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
      spinnerX.setAdapter(adapter);

      //chose the value on the x axis using the enum and call plot to re-draw the graph
      spinnerX.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
               case 0:
                  xAxis = xAxisType.TIME;
                  break;
               case 1:
                  xAxis = xAxisType.DISTANCE;
                  break;
            }
            plot();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            xAxis = xAxisType.TIME;
         }
      });

      addFloatingButtonAction();

      tvWorkoutName.setText(mItem.getName());

      mToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
      mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            WorkoutDetailActivity.this.finish();
         }
      });
   }

   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == 1222) {
         String filename = "workout.qaddu";
         File myFile = new File(Environment.getExternalStorageDirectory().getPath() + filename);
         myFile.delete();
      }

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      this.mMenu = menu;

      getMenuInflater().inflate(R.menu.delete, this.mMenu);

      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.action_delete_workout) {
         //Log.d("WorkoutDetail", "Delete press");
         //handle workout delete
         Toast toast = Toast.makeText(getApplicationContext(), "E se poi te ne penti?", Toast.LENGTH_SHORT);
         toast.show();
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   private void addFloatingButtonAction() {
      floatingActionButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {

            Gson gson = new Gson();

            String filename = tvWorkoutName.getText() + ".qaddu";
            String string = gson.toJson(mItem);

            try {
               File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + filename);
               myFile.createNewFile();
               FileOutputStream fOut = new FileOutputStream(myFile);
               OutputStreamWriter myOutWriter =
                     new OutputStreamWriter(fOut);
               myOutWriter.append(string);
               myOutWriter.close();
               fOut.close();

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
   }

   private void plot() {

      DataPoint[] dataArray = new DataPoint[listYAxis.size()];
      List<WorkoutPoint> workoutPoints = mItem.getPoints();

      if (xAxis == xAxisType.TIME) {
         //format the x label as a date
         //getTime() returns the epoch in seconds, it must be converted in milliseconds multiplying by 1000 to create a Date
         for (int i = 0; i < listYAxis.size(); i++) {
            Log.d("XTime", Long.toString(workoutPoints.get(i).getTime()));
            Log.d("XTime", new Date(workoutPoints.get(i).getTime() * 1000).toString());
            dataArray[i] = new DataPoint(new Date(workoutPoints.get(i).getTime() * 1000), listYAxis.get(i));
         }

         LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataArray);
         series.setColor(Color.parseColor(getString(R.string.colorPrimaryHex)));

         graph.getViewport().setXAxisBoundsManual(false);
         graph.removeAllSeries();
         graph.addSeries(series);

         // set date label formatter
         graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("HH:mm:ss")));
         graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

         // set manual x bounds to have nice steps
         graph.getViewport().setMinX(workoutPoints.get(0).getTime() * 1000);
         graph.getViewport().setMaxX(workoutPoints.get(workoutPoints.size() - 1).getTime() * 1000);
         graph.getViewport().setXAxisBoundsManual(true);
         graph.invalidate();

      } else if (xAxis == xAxisType.DISTANCE) {

         //format the x label in km
         for (int i = 0; i < listYAxis.size(); i++) {
            dataArray[i] = new DataPoint(workoutPoints.get(i).getDistance() / 1000.0, listYAxis.get(i));
         }

         LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataArray);
         series.setColor(Color.parseColor(getString(R.string.colorPrimaryHex)));

         graph.getViewport().setXAxisBoundsManual(false);
         graph.removeAllSeries();
         graph.addSeries(series);

         // set date label formatter
         graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(new DecimalFormat("0.#"), new DecimalFormat()));
         graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

         // set manual x bounds to have nice steps
         graph.getViewport().setMinX(workoutPoints.get(0).getDistance() / 1000.0);
         graph.getViewport().setMaxX(workoutPoints.get(workoutPoints.size() - 1).getDistance() / 1000.0);
         graph.getViewport().setXAxisBoundsManual(true);
         graph.invalidate();

      }
   }

   private enum xAxisType {TIME, DISTANCE}
}

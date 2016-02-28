package so2.unica.qaddu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;
import so2.unica.qaddu.quadduFragments.HistoryFragment;

/**
 * WorkoutDetailActivity shows the details of a saved workout
 */

public class WorkoutDetailActivity extends AppCompatActivity {

   public static final int REQUEST_CODE = 1222;

   @Bind(R.id.tool_bar)
   Toolbar mToolBar;
   @Bind(R.id.tvWorkoutName)
   TextView mTvWorkoutName;
   @Bind(R.id.tvWorkoutDistance)
   TextView mTvWorkoutDistance;
   @Bind(R.id.tvWorkoutTime)
   TextView mTvWorkoutTime;
   @Bind(R.id.tvWorkoutAvgSpeed)
   TextView mTvWorkoutAvgSpeed;
   @Bind(R.id.tvWorkoutAvgPace)
   TextView mTvWorkoutAvgPace;
   @Bind(R.id.tvWorkoutDate)
   TextView mTvWorkoutDate;
   @Bind(R.id.spinnerY)
   Spinner mSpinnerY;
   @Bind(R.id.spinnerX)
   Spinner mSpinnerX;
   @Bind(R.id.fab)
   FloatingActionButton mFloatingActionButton;
   @Bind(R.id.chart)
   LineChartView mChart;

   private Menu mMenu;

   private xAxisType mXAxis;
   private yAxisType mYAxis;

   private WorkoutItem mItem;
   private Boolean mImportedWorkout;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_workout_detail);
      ButterKnife.bind(this);
      setSupportActionBar(mToolBar);

      //Set the title of the action bar
      setTitle(getString(R.string.workout_detail));

      //Check if the user is opening a json workout file
      //and read it
      Intent intent = getIntent();
      Bundle bundle = intent.getExtras();

      if (getIntent().getData() != null) {
         String filePath = getIntent().getData().getPath();

         try {
            StringBuilder text = new StringBuilder();

            //read the file passed in the intent
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line;

            while ((line = br.readLine()) != null) {
               text.append(line);
               text.append('\n');
            }
            br.close();

            //convert the json file in a WorkoutItem object
            Gson gson = new Gson();
            mItem = gson.fromJson(text.toString(), WorkoutItem.class);

            for (int i = 0; i < mItem.getPoints().size(); i++) {
               mItem.getPoints().get(i).setWorkout(mItem);
            }

            mImportedWorkout = true;
         } catch (IOException e) {
            Log.d("E", "EXCEPTION");
         }


      } else {
         //The user is opening a workout from the history
         //get the id and fetch it from the database
         int id = bundle.getInt(HistoryFragment.WORKOUT_ID);
         mItem = (WorkoutItem) DatabaseHelper.getInstance().getItemById(id, WorkoutItem.class);
         mImportedWorkout = false;
      }

      //set the time as default x axis
      mXAxis = xAxisType.TIME;

      //load the spinners
      ArrayAdapter<CharSequence> adapterY = ArrayAdapter.createFromResource(this, R.array.y_array, R.layout.spinner_center_item);
      adapterY.setDropDownViewResource(R.layout.spinner_center_item);
      mSpinnerY.setAdapter(adapterY);

      ArrayAdapter<CharSequence> adapterX = ArrayAdapter.createFromResource(this, R.array.x_array, R.layout.spinner_center_item);
      adapterX.setDropDownViewResource(R.layout.spinner_center_item);
      mSpinnerX.setAdapter(adapterX);


      //choose the list of Y data to show and call plot to re-draw the graph
      mSpinnerY.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
               case 0:
                  mYAxis = yAxisType.SPEED;
                  break;
               case 1:
                  mYAxis = yAxisType.PACE;
                  break;
               case 2:
                  mYAxis = yAxisType.ALTITUDE;
                  break;
            }
            plot();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            mYAxis = yAxisType.SPEED;
         }
      });


      //chose the value on the x axis using the enum and call plot to re-draw the graph
      mSpinnerX.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
               case 0:
                  mXAxis = xAxisType.TIME;
                  break;
               case 1:
                  mXAxis = xAxisType.DISTANCE;
                  break;
            }
            plot();
         }


         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            mXAxis = xAxisType.TIME;
         }
      });

      //create the floating action button to share the workout
      addFloatingButtonAction();

      //set the informations on the UI
      mTvWorkoutName.setText(mItem.getName());

      DecimalFormat decimalFormat = new DecimalFormat("0.0");
      String distance = decimalFormat.format(mItem.getDistance() / 1000.0) + " KM";
      mTvWorkoutDistance.setText(distance);

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String duration = simpleDateFormat.format(mItem.getTotalTime());
      mTvWorkoutTime.setText(duration);

      String avgSpeed = decimalFormat.format(mItem.getAverageSpeed()) + " KM/H";
      mTvWorkoutAvgSpeed.setText(avgSpeed);

      simpleDateFormat = new SimpleDateFormat("mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String avgPace = simpleDateFormat.format(mItem.getAveragePaceInSeconds() * 1000) + " MIN/KM";
      Log.d("AVGPace", mItem.getAveragePaceInSeconds() + "");
      mTvWorkoutAvgPace.setText(avgPace);

      simpleDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getDefault());
      String date = simpleDateFormat.format(mItem.getStartDate().getTime());
      mTvWorkoutDate.setText(date);

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
      if (requestCode == REQUEST_CODE) {
         String filename = mTvWorkoutName.getText() + ".qaddu";
         File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + filename);
         myFile.delete();
      }

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if exist.
      this.mMenu = menu;

      //do not create the delete icon if the interface is showing an imported workout
      if (!mImportedWorkout) {
         getMenuInflater().inflate(R.menu.delete, this.mMenu);
      }

      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //handle the press on the delete button
      if (id == R.id.action_delete_workout) {
         AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutDetailActivity.this);
         // Add the buttons
         builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               //end the workout detail activity
               WorkoutDetailActivity.this.finish();

               //remove each workout point associated with the workout item
               for (WorkoutPoint point : mItem.getPoints()) {
                  DatabaseHelper.getInstance().removeData(point, WorkoutPoint.class);
               }
               //remove the workout item
               DatabaseHelper.getInstance().removeData(mItem, WorkoutItem.class);
            }
         });
         builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               // User cancelled the dialog
            }
         });

         // Create the AlertDialog
         AlertDialog dialog = builder.create();
         dialog.setTitle(getString(R.string.workout_delete_confirmation));
         dialog.show();
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   /**
    * The addFloatingButtonAction method manages the send of the workout
    */
   private void addFloatingButtonAction() {
      //share a workout
      mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {

            String filename = mTvWorkoutName.getText() + ".qaddu";

            //convert the workout object in json string
            Gson gson = new Gson();
            String string = gson.toJson(mItem);

            try {
               //save the json string in a file
               File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + filename);
               myFile.createNewFile();
               FileOutputStream fOut = new FileOutputStream(myFile);
               OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
               myOutWriter.append(string);
               myOutWriter.close();
               fOut.close();

               //share the file
               Uri path = Uri.fromFile(myFile);
               Intent intent = new Intent(android.content.Intent.ACTION_SEND);
               intent.setType("application/octet-stream");
               intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.workout_share_text));
               intent.putExtra(Intent.EXTRA_STREAM, path);
               startActivityForResult(Intent.createChooser(intent, getString(R.string.share_workout)), REQUEST_CODE);
            } catch (Exception e) {
               Toast.makeText(getBaseContext(), e.getMessage(),
                     Toast.LENGTH_SHORT).show();
            }


         }
      });
   }

   /**
    * the plot method creates the graph
    */

   private void plot() {
      //plot the graph with the axis determined by the spinners
      List<PointValue> values = new ArrayList<>();
      Axis axisX = new Axis();
      Axis axisY = new Axis().setHasLines(true);

      if (mXAxis == xAxisType.TIME) {

         //if the axis x type is time, format the unix timestamp a date
         axisX.setFormatter(new AxisValueFormatter() {
            @Override
            public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
               simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

               String label = simpleDateFormat.format(value);

               //The library need the label string at the end of the formattedValue array
               label.getChars(0, label.length(), formattedValue, formattedValue.length - label.length());
               //Log.d("Formatter", new String(formattedValue));

               return label.length();
            }

            @Override
            public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {
               //not used-
               return 0;
            }
         });

         //show max 3 date label for space issues
         axisX.setMaxLabelChars(5);
      }

      for (int i = 0; i < mItem.getPoints().size(); i++) {
         Float x, y = 0f;

         if (mXAxis == xAxisType.TIME) {
            x = (float) mItem.getPoints().get(i).getTime();
            axisX.setName(getString(R.string.axisX_time));
         } else {
            x = (float) mItem.getPoints().get(i).getDistance();
            axisX.setName(getString(R.string.axisX_distance));
         }

         switch (mYAxis) {
            case SPEED:
               y = (float) mItem.getPoints().get(i).getSpeed();
               axisY.setName(getString(R.string.axisY_speed));
               break;
            case PACE:
               y = (float) mItem.getPoints().get(i).getPace();
               axisY.setName(getString(R.string.axisY_pace));
               break;
            case ALTITUDE:
               y = (float) mItem.getPoints().get(i).getAltitude();
               axisY.setName(getString(R.string.axisY_altitude));
               break;
         }

         values.add(new PointValue(x, y));
      }

      //create a line and set the proprieties
      Line line = new Line(values);
      line.setColor(ContextCompat.getColor(WorkoutDetailActivity.this, R.color.colorPrimary));
      line.setHasPoints(false);
      line.setFilled(true);
      List<Line> lines = new ArrayList<>();
      lines.add(line);

      axisX.setTextColor(Color.BLACK);
      axisY.setTextColor(Color.BLACK);

      //plot the data
      LineChartData data = new LineChartData();
      data.setLines(lines);
      data.setAxisXBottom(axisX);
      data.setAxisYLeft(axisY);
      mChart.setZoomEnabled(false);
      mChart.setContainerScrollEnabled(false, null);
      mChart.setLineChartData(data);
      mChart.setInteractive(true);
   }

   /**
    * Enum used for memorizing the x axis data type
    */
   private enum xAxisType {
      TIME, DISTANCE
   }

   /**
    * Enum used for memorizing the y axis data type
    */
   private enum yAxisType {
      SPEED, PACE, ALTITUDE
   }
}

package so2.unica.qaddu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import so2.unica.qaddu.helpers.DatabaseHelper;
import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;
import so2.unica.qaddu.quadduFragments.History;


public class WorkoutDetailActivity extends AppCompatActivity {

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
   @Bind(R.id.tvWorkoutAvgStep)
   TextView mTvWorkoutAvgStep;
   @Bind(R.id.tvWorkoutDate)
   TextView mTvWorkoutDate;

   @Bind(R.id.spinnerX)
   Spinner mSpinnerY;
   @Bind(R.id.spinnerY)
   Spinner mSpinnerX;
   @Bind(R.id.fab)
   FloatingActionButton mFloatingActionButton;
   @Bind(R.id.chart)
   LineChartView mChart;
   Menu mMenu;
   WorkoutItem mItem;
   ArrayList<Double> mListYAxis;
   xAxisType mXAxis;
   yAxisType mYAxis;

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
            mListYAxis = new ArrayList<>();
            switch (position) {
               case 0:
                  mYAxis = yAxisType.SPEED;
                  break;
               case 1:
                  mYAxis = yAxisType.STEP;
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


      addFloatingButtonAction();

      mTvWorkoutName.setText(mItem.getName());

      DecimalFormat decimalFormat = new DecimalFormat("0.#");
      String distance = "TOGLIMI"; //decimalFormat.format(mItem.getDistance() / 1000.0) + " KM";
      mTvWorkoutDistance.setText(distance);

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String duration = "toglimi"; //simpleDateFormat.format(mItem.getTotalTime() * 1000);
      mTvWorkoutTime.setText(duration);

      String avgSpeed = decimalFormat.format(mItem.getAverageSpeed()) + " KM/H";
      mTvWorkoutAvgSpeed.setText(avgSpeed);

      simpleDateFormat = new SimpleDateFormat("mm:ss");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String avgStep = simpleDateFormat.format(mItem.getAverageStepInSeconds() * 1000) + " MIN/KM";
      Log.d("AVGStep", mItem.getAverageStepInSeconds() + "");
      mTvWorkoutAvgStep.setText(avgStep);

      simpleDateFormat = new SimpleDateFormat("dd-mm-yy HH:mm:ss");
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
         AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutDetailActivity.this);
// Add the buttons
         builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               WorkoutDetailActivity.this.finish();
               for(WorkoutPoint point : mItem.getPoints()){
                  DatabaseHelper.getIstance().removeData(point,WorkoutPoint.class);
               }
               DatabaseHelper.getIstance().removeData(mItem,WorkoutItem.class);
            }
         });
         builder.setNegativeButton("gianni", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               // User cancelled the dialog
            }
         });

// Create the AlertDialog
         AlertDialog dialog = builder.create();
         dialog.setTitle("Vuoi davvro cancellare il workout?");
         dialog.show();
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   private void addFloatingButtonAction() {
      mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {

            Gson gson = new Gson();

            String filename = mTvWorkoutName.getText() + ".qaddu";
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
      List<PointValue> values = new ArrayList<PointValue>();
      Axis axisX = new Axis();
      Axis axisY = new Axis().setHasLines(true);


      for(int i = 0; i < mItem.getPoints().size(); i++){
         Float x,y = 0f;

         if(mXAxis == xAxisType.TIME){
            x = (float) mItem.getPoints().get(i).getTime()/1000/60;
            axisX.setName("Time");
         } else {
            x = (float) mItem.getPoints().get(i).getDistance();
            axisX.setName("Distance");
         }

         switch(mYAxis){
            case SPEED:
               y = (float) mItem.getPoints().get(i).getSpeed();
               axisY.setName("Speed");
               break;
            case STEP:
               y = (float) mItem.getPoints().get(i).getStep();
               axisY.setName("Step");
               break;
            case ALTITUDE:
               y = (float) mItem.getPoints().get(i).getAltitude();
               axisY.setName("Altitude");
               break;
         }

         values.add(new PointValue(x, y));
      }




      //In most cased you can call data model methods in builder-pattern-like manner.
      Line line = new Line(values).setColor(ContextCompat.getColor(WorkoutDetailActivity.this, R.color.colorPrimaryDark)).setCubic(true);
      List<Line> lines = new ArrayList<Line>();
      lines.add(line);

      LineChartData data = new LineChartData();
      data.setLines(lines);
      data.setAxisXBottom(axisX);
      data.setAxisYLeft(axisY);
      mChart.setZoomEnabled(false);
      mChart.setContainerScrollEnabled(false, null);
      mChart.setLineChartData(data);
   }

   private enum xAxisType {TIME, DISTANCE}
   private enum yAxisType {SPEED, STEP, ALTITUDE}
}

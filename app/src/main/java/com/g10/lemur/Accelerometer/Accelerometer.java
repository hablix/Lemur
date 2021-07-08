package com.g10.lemur.Accelerometer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.g10.lemur.Altimeter.Altimeter;
import com.g10.lemur.Decibel.Decibel;
import com.g10.lemur.Help.Help;
import com.g10.lemur.MainActivity;
import com.g10.lemur.R;
import com.g10.lemur.Settings.Settings;
import com.g10.lemur.Speedometer.Speedometer;
import com.g10.lemur.Vision.Vision;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;


public class Accelerometer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener, View.OnClickListener
{
    NavigationView navigationView;

    //Sensor related declarations
    private TextView  Info_deg, Info_tone ;
    private Sensor accSensor, rotSensor;

    private SensorManager SM;
    private double lessFloatX;

    // buttons
    private Button buttonRecordValues;

    //Graph related declarations
    private double yValueXaxis;


    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    static GraphView graphX;

    static LineGraphSeries<DataPoint> seriesX;

    long activityCreateTime;

    LinearLayout llbase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set the current activity as marked in the menu
        navigationView.setCheckedItem(R.id.menuAcc);

        //Find so we can swap them

        llbase = (LinearLayout)findViewById(R.id.content_accelerometer) ;

        // buttons
        buttonRecordValues = (Button)findViewById((R.id.buttonRecordValues));
        buttonRecordValues.setOnClickListener(this);

        //Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        accSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotSensor = SM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Start Listening to sensor
        //SM.registerListener(this,accSensor,SensorManager.SENSOR_DELAY_UI);
        //SM.registerListener(this,rotSensor,SensorManager.SENSOR_DELAY_UI);

        //Assign TextViews for accelerometer data
        //XaxisText = (TextView)findViewById(R.id.XDataText);
        Info_deg = (TextView)findViewById(R.id.Info1);
        Info_tone = (TextView)findViewById(R.id.Info2);
        //info = (TextView)findViewById(R.id.info);


        //Graph X
        graphX = (GraphView)findViewById(R.id.XGraph);
        seriesX = new LineGraphSeries<>();
        graphX.addSeries(seriesX);

        graphX.getViewport().setXAxisBoundsManual(true);
        graphX.getViewport().setMinX(0);
        graphX.getViewport().setMaxX(10000);
        //graphX.getViewport().setYAxisBoundsManual(true);
        graphX.getViewport().setMinY(-40);
        graphX.getViewport().setMaxY(40);
        graphX.getGridLabelRenderer().setNumHorizontalLabels(4);




        //X Axis Graph Label format
        graphX.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX)
            {
                if (isValueX) {
                    DecimalFormat df = new DecimalFormat("#.#");
                    return df.format(value/1000)+"s";
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        activityCreateTime = System.currentTimeMillis();


    }

    //Values
    double xmin, xmax, ymin, ymax, zmin, zmax;
    public long timestampbegin, lastxmax;

    //Maximum finder
    double x_lastvalue = 0;
    public long  maxf_start;
    double pseudoheading, pseudoheading_deg_whenStarted;

    //Button

    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.buttonRecordValues: {
                // reset timestamp
                timestampbegin = System.currentTimeMillis();
                break;
            }
        }
    }



    public void collectData(float[] values)
    {
//        if(System.currentTimeMillis() < timestampbegin+500)
//        {
//            if(values[0] < xmin) xmin = values[0];
//            if(values[0] > xmax) xmax = values[0];
//            if(values[1] < ymin) ymin = values[1];
//            if(values[1] > ymax) ymax = values[1];
//            if(values[2] < zmin) zmin = values[2];
//            if(values[2] > zmax) zmax = values[2];
//            if(values[0] < xmin*0.9)
//            {
//                info.setText("searching");
//            }
//        }
//        else
//        {
//            collectScan(values);
//        }
//        if(System.currentTimeMillis() > timestampbegin+500 && System.currentTimeMillis() < timestampbegin+600) {
//            info.setText("finished");
//        }

        // if going up, but still below -2m/s2
        if(values[0] < -0 && values[0] > x_lastvalue)
        {
            maxf_start = System.currentTimeMillis();
            pseudoheading_deg_whenStarted = pseudoheading;
        }

        //within the frame, more than 20mss and direction changed
        if(System.currentTimeMillis() < maxf_start + 300)
        {
            if(values[0] > 15 && values[0] < x_lastvalue)
            {
                // Triggerd
                maxf_start = 0;
                playTone();
            }
        }

        x_lastvalue = values[0];
    }




    public void playTone(){
        int resource;
        if(pseudoheading_deg_whenStarted < -131)
        {
            resource = R.raw.l0_c3_cut;
        }
        else
        {
            if (pseudoheading_deg_whenStarted < -82 )
            {
                resource = R.raw.l1_d3_cut;
            }
            else {
                if (pseudoheading_deg_whenStarted < -33)
                {
                    resource = R.raw.l2_ds3_cut;
                }
                else
                {
                    if (pseudoheading_deg_whenStarted < 24 )
                    {
                        resource = R.raw.l3_e3_cut;
                    }
                    else
                    {
                        if (pseudoheading_deg_whenStarted < 75)
                        {
                            resource = R.raw.l4_f3_cut;
                        }
                        else
                        {
                            if (pseudoheading_deg_whenStarted < 126)
                            {
                                resource = R.raw.l5_fs3_cut;
                            } else
                            {
                                resource = R.raw.l6_g3_cut;
                            }
                        }
                    }
                }
            }
        }

        final MediaPlayer mp = MediaPlayer.create(this,resource);
        mp.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                //mp.reset();
                //mp.release();
            }
        });
    }

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        navigationView.setCheckedItem(R.id.menuAcc);
    }
    @Override
    public void onPause(){
        super.onPause(); //always call superclass method first

            mHandler.removeCallbacks(mTimer);

            SM.unregisterListener(this,accSensor);
            SM.unregisterListener(this,rotSensor);

    }
    @Override
    public void onResume(){
        super.onResume();

        navigationView.setCheckedItem(R.id.menuAcc);

        SM.registerListener(this,accSensor,SensorManager.SENSOR_DELAY_UI);
        SM.registerListener(this,rotSensor,SensorManager.SENSOR_DELAY_UI);


        mTimer = new Runnable()
        {
            @Override
            public void run()
            {
                yValueXaxis = lessFloatX;
                seriesX.appendData(newDatapoint(yValueXaxis), true, 100);
                graphX.onDataChanged(true, false);
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.postDelayed(mTimer, 100);
    }

    @Override
    public void onBackPressed()
    {
        // Physical back button pressed

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Right sub-menu.
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Intent intent;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menuHome)
        {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menuVision)
        {
            // Go to Google Vision
            intent = new Intent(this, Vision.class);
            startActivity(intent);
        }
        else if (id == R.id.menuAlti)
        {
            // Go to altimeter
            intent = new Intent(this, Altimeter.class);
            startActivity(intent);
        }
        else if (id == R.id.menuSpeed)
        {
            // Go to speedometer
            intent = new Intent(this, Speedometer.class);
            startActivity(intent);
        }
        else if (id == R.id.menuAcc)
        {
            // Stay here
        }
        else if (id == R.id.menuSound)
        {
            // Go to decibel
            intent = new Intent(this, Decibel.class);
            startActivity(intent);
        }
        else if (id == R.id.menuHelp)
        {
            // Go to help
            intent = new Intent(this, Help.class);
            startActivity(intent);
        }
        else if (id == R.id.menuSettings)
        {
            // Go to Settings
            intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //funktion som rundar av till *int precision* antal decimaler
    //Ex Math.pow(10,1) = 10^1, Math.round(9.83827 * 10) = 98, 98/10 = 9.8
    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lessFloatX = round(sensorEvent.values[0], 1);

            collectData(sensorEvent.values);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //float rotationMatrix[];
            //rotationMatrix=new float[16];
            //SM.getRotationMatrixFromVector(rotationMatrix,sensorEvent.values);

            double valuePsHead = sensorEvent.values[1];
            pseudoheading = round(valuePsHead * 266.26, 1);
            Info_deg.setText(String.valueOf((pseudoheading)));

            ChangeColor();

        }

    }

    private void ChangeColor()
    {
        if(pseudoheading < -131)
        {
            llbase.setBackgroundColor((0xFFffaf7a));
            Info_tone.setText("C³");
        }
        else
        {
            if (pseudoheading < -82 )
            {
                llbase.setBackgroundColor((0xFFff9d5c));
                Info_tone.setText("D³");
            }
            else {
                if (pseudoheading < -33)
                {
                    llbase.setBackgroundColor((0xFFff8b3d));
                    Info_tone.setText("D#³");
                }
                else
                {
                    if (pseudoheading < 24 )
                    {
                        llbase.setBackgroundColor((0xFFff781f));
                        Info_tone.setText("E³");
                    }
                    else
                    {
                        if (pseudoheading < 75)
                        {
                            llbase.setBackgroundColor((0xFFff6600));
                            Info_tone.setText("F³");
                        }
                        else
                        {
                            if (pseudoheading < 126)
                            {
                                llbase.setBackgroundColor((0xFFe15f1a));
                                Info_tone.setText("F#³");
                            } else
                            {
                                llbase.setBackgroundColor((0xFFc85417));
                                Info_tone.setText("G³");
                            }
                        }
                    }
                }
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Not in use
    }
    private DataPoint newDatapoint(double y)
    {
        double timeSince = System.currentTimeMillis() - activityCreateTime;
        return new DataPoint(timeSince, y);
    }

    }


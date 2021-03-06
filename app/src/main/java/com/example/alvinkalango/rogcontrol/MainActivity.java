package com.example.alvinkalango.rogcontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private AlertDialog alerta;
    ProgressDialog progress;
    ImageButton Forward, Backward, Left, Right;
    Button Buzzer;
    TextView BuzzerText;
    Switch Connect, Navigate, Led, Acc;
    ImageView Bt_icon, Nav_icon, Led_icon, Buz_icon, Acc_icon;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    boolean statusConection = false;

    private String dataToSend;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static String address = "98:D3:31:B4:35:0E";
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Connect = (Switch) findViewById(R.id.connect);
        Navigate = (Switch) findViewById(R.id.navigate);
        Led = (Switch) findViewById(R.id.led);
        Acc = (Switch) findViewById(R.id.acc);
        Buzzer = (Button) findViewById(R.id.buzzer);
        BuzzerText = (TextView) findViewById(R.id.textView);

        Forward = (ImageButton) findViewById(R.id.buttonForward);
        Backward = (ImageButton) findViewById(R.id.buttonBackward);
        Left = (ImageButton) findViewById(R.id.buttonLeft);
        Right = (ImageButton) findViewById(R.id.buttonRight);

        Bt_icon = (ImageView) findViewById(R.id.bt_icon);
        Nav_icon = (ImageView) findViewById(R.id.nav_icon);
        Led_icon = (ImageView) findViewById(R.id.led_icon);
        Acc_icon = (ImageView) findViewById(R.id.acc_icon);
        Buz_icon = (ImageView) findViewById(R.id.buz_icon);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setAllOff();

        Connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new ConnectBT().execute();
                    statusConection = true;
                    setAllOn();
                } else {
                    Disconnect();
                    setAllOff();
                }
            }
        });

        Navigate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dataToSend = "N";
                    writeData(dataToSend);
                    Toast.makeText(getApplicationContext(),
                            "Auto mode activated!", Toast.LENGTH_SHORT).show();
                    Acc.setEnabled(false);
                    setDirectionalOff();
                }
                else {
                    dataToSend = "S";
                    writeData(dataToSend);
                    Toast.makeText(getApplicationContext(),
                            "Auto mode disabled", Toast.LENGTH_SHORT).show();
                    Acc.setEnabled(true);
                    setDirectionalOn();
                }
            }
        });

        Led.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dataToSend = "1";
                    writeData(dataToSend);
                } else {
                    dataToSend = "0";
                    writeData(dataToSend);
                }
            }
        });

        Buzzer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dataToSend = "Z";
                writeData(dataToSend);
            }
        });

        Forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "F";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });

        Backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "B";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });
        Left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "L";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });
        Right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "R";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });
    }

    public void Accelerometer(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            setDirectionalOff();
            Navigate.setEnabled(false);
            Toast.makeText(getApplicationContext(),
                    "Accelerometer activated!", Toast.LENGTH_SHORT).show();
        }
        else {
            mSensorManager.unregisterListener(this);
            Navigate.setEnabled(true);
            setDirectionalOn();
            Toast.makeText(getApplicationContext(),
                    "Accelerometer disabled!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setAllOff(){
        Connect.setChecked(false);

        Navigate.setEnabled(false);
        Led.setEnabled(false);
        Acc.setEnabled(false);
        Buzzer.setEnabled(false);
        BuzzerText.setTextColor(Color.parseColor("#C3C3C3"));
        Nav_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Led_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Acc_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Buz_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);

        setDirectionalOff();
    }

    public void setAllOn() {
        Connect.setChecked(true);

        Navigate.setEnabled(true);
        Led.setEnabled(true);
        Acc.setEnabled(true);
        Buzzer.setEnabled(true);
        BuzzerText.setTextColor(Color.parseColor("#000000"));
        Nav_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
        Led_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
        Acc_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
        Buz_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);

        setDirectionalOn();
    }

    public void setDirectionalOff(){
        Forward.setEnabled(false);
        Backward.setEnabled(false);
        Left.setEnabled(false);
        Right.setEnabled(false);

        Forward.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Backward.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Left.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Right.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
    }

    public void setDirectionalOn(){
        Forward.setEnabled(true);
        Backward.setEnabled(true);
        Left.setEnabled(true);
        Right.setEnabled(true);

        Forward.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
        Backward.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
        Left.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
        Right.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
    }

    protected void CheckBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth null!", Toast.LENGTH_SHORT).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    protected void Disconnect(){
        if (outStream != null) {
            try {
                outStream.close();
            }
            catch (Exception e) {}
            outStream = null;
        }

        if (btSocket != null) {
            try {
                btSocket.close();
            }
            catch (Exception e) {}
            btSocket = null;
        }

        statusConection = false;
    }

    protected void writeData(String data) {
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Bug before sending message",
                    Toast.LENGTH_SHORT).show();
        }

        String message = data;
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Bug while sending message",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Disconnect();
    }

    @Override
    protected void onResume(){
        super.onResume();
        CheckBt();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Acc.isChecked()) {
            Acc.setChecked(false);
            Navigate.setEnabled(true);
            setDirectionalOn();
        }

        if (Navigate.isChecked()) {
            Navigate.setChecked(false);
            Acc.setEnabled(true);
            setDirectionalOn();
        }

        mSensorManager.unregisterListener(this);

        if (statusConection) {
            dataToSend = "S";
            writeData(dataToSend);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About");
            builder.setMessage("App by Alvaro Marinho - alvaro.marinho@live.com");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alerta.dismiss();
                }
            });
            alerta = builder.create();
            alerta.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Float x = event.values[0];
        Float y = event.values[1];
        Float z = event.values[2];

        if( x > -3 && x < 3 && z > -4 && z < 4) {         // STOP
            dataToSend = "S";
            writeData(dataToSend);
        }

        if (z > 5 && z <= 9) { // FORWARD
            dataToSend = "F";
            writeData(dataToSend);
        }

        if (z < 0 && z >= -9) { // BACKWARD
            dataToSend = "B";
            writeData(dataToSend);
        }

        if (x > 3 && x <= 9) { // LEFT
            dataToSend = "L";
            writeData(dataToSend);
        }

        if (x < -2 && x >= -9) { // RIGHT
            dataToSend = "R";
            writeData(dataToSend);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {

        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... params) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mBluetoothAdapter.cancelDiscovery();
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                btSocket.connect();
            }
            catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(),"Connection Failed! Try again.", Toast.LENGTH_SHORT).show();
                setAllOff();
            }
            else {
                Toast.makeText(getApplicationContext(), "Connected.", Toast.LENGTH_SHORT).show();
            }
            progress.dismiss();
        }
    }
}

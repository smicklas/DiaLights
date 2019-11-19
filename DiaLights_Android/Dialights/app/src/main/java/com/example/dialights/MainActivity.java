package com.example.dialights;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;


import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //Widgets
    Button btnConnect;
    Button btnUpLeft;
    Button btnLowLeft;
    Button btnRight;
    TextView connectionStatus;
    ListView devices;

    int[] red = {255, 0, 0};
    int[] blue = {0, 255, 0};
    int[] green = {0, 0, 255};

    //Bluetooth
    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    String address = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //Hiding the top bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        //Calling widgets
        btnConnect = (Button) findViewById(R.id.select_device_btn);
        btnUpLeft = (Button) findViewById(R.id.top_left_btn);
        btnRight = (Button) findViewById(R.id.right_btn);
        btnLowLeft = (Button) findViewById(R.id.low_left_btn);
        connectionStatus = (TextView) findViewById(R.id.connection_status);

        //Establish button behavior
        btnUpLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    setUpLeftColor();
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    setLowLeftColor();
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    setRightColor();
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Check if device has bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not available", Toast.LENGTH_LONG).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Please enable Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            connectionStatus.setText("No device connected");
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList() {

        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                //Get each device name and address
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No paired devices found", Toast.LENGTH_LONG).show();
        }

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.device_dialog);
        dialog.setTitle("Devices");
        devices = (ListView) dialog.findViewById(R.id.List);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devices.setAdapter(adapter);
        dialog.show();
        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the device MAC address, the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                if (info.length() > 16) {
                    address = info.substring(info.length() - 17);
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to connect to device", Toast.LENGTH_LONG).show();
                }

                dialog.dismiss();
                connectDevice(devices, address);

            }
        }); //Method called when the device from the list is clicked
    }

    private void connectDevice(ListView devices, String address) {
        new ConnectBT().execute();
    }

    //Frame 1
    private void setUpLeftColor(){
        if(btSocket != null){
                //color picker
                final ColorPicker cp = new ColorPicker(MainActivity.this, red[0], green[0], blue[0]);
                cp.show();
                cp.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(int color) {
                        red[0] = cp.getRed();
                        green[0] = cp.getGreen();
                        blue[0] = cp.getBlue();
                        btnUpLeft.setBackgroundColor(Color.rgb(red[0], green[0], blue[0]));

                        try {
                            //Write the indicator for the panel, in this case "a"
                            btSocket.getOutputStream().write("(".toString().getBytes()); //Initial char
                            btSocket.getOutputStream().write("a".toString().getBytes()); //Panel identification char

                            btSocket.getOutputStream().write("R".toString().getBytes()); //Red value
                            btSocket.getOutputStream().write(Integer.toString(red[0]).getBytes());

                            btSocket.getOutputStream().write("G".toString().getBytes()); //Green value
                            btSocket.getOutputStream().write(Integer.toString(green[0]).getBytes());

                            btSocket.getOutputStream().write("B".toString().getBytes()); //Blue value
                            btSocket.getOutputStream().write(Integer.toString(blue[0]).getBytes());

                            btSocket.getOutputStream().write(")".toString().getBytes()); //Terminating char
                        } catch (IOException e){

                        }
                        cp.dismiss();
                    }
                });
        }
    }

    private void setRightColor(){
        if(btSocket != null){
            //color picker
            final ColorPicker cp = new ColorPicker(MainActivity.this, red[1], green[1], blue[1]);
            cp.show();
            cp.setCallback(new ColorPickerCallback() {
                @Override
                public void onColorChosen(int color) {
                    red[1] = cp.getRed();
                    green[1] = cp.getGreen();
                    blue[1] = cp.getBlue();
                    btnRight.setBackgroundColor(Color.rgb(red[1], green[1], blue[1]));

                    try {
                        //Write the indicator for the panel, in this case "b
                        btSocket.getOutputStream().write("(".toString().getBytes()); //Initial char
                        btSocket.getOutputStream().write("b".toString().getBytes()); //Panel identification char

                        btSocket.getOutputStream().write("R".toString().getBytes()); //Red value
                        btSocket.getOutputStream().write(Integer.toString(red[1]).getBytes());

                        btSocket.getOutputStream().write("G".toString().getBytes()); //Green value
                        btSocket.getOutputStream().write(Integer.toString(green[1]).getBytes());

                        btSocket.getOutputStream().write("B".toString().getBytes()); //Blue value
                        btSocket.getOutputStream().write(Integer.toString(blue[1]).getBytes());

                        btSocket.getOutputStream().write(")".toString().getBytes()); //Terminating char
                    } catch (IOException e){

                    }
                    cp.dismiss();
                }
            });
        }
    }

    private void setLowLeftColor(){
        if(btSocket != null){
            //color picker
            final ColorPicker cp = new ColorPicker(MainActivity.this, red[2], green[2], blue[2]);
            cp.show();
            cp.setCallback(new ColorPickerCallback() {
                @Override
                public void onColorChosen(int color) {
                    red[2] = cp.getRed();
                    green[2] = cp.getGreen();
                    blue[2] = cp.getBlue();
                    btnLowLeft.setBackgroundColor(Color.rgb(red[2], green[2], blue[2]));

                    try {
                        //Write the indicator for the panel, in this case "c"
                        btSocket.getOutputStream().write("(".toString().getBytes()); //Initial char
                        btSocket.getOutputStream().write("c".toString().getBytes()); //Panel identification char

                        btSocket.getOutputStream().write("R".toString().getBytes()); //Red value
                        btSocket.getOutputStream().write(Integer.toString(red[2]).getBytes());

                        btSocket.getOutputStream().write("G".toString().getBytes()); //Green value
                        btSocket.getOutputStream().write(Integer.toString(green[2]).getBytes());

                        btSocket.getOutputStream().write("B".toString().getBytes()); //Blue value
                        btSocket.getOutputStream().write(Integer.toString(blue[2]).getBytes());

                        btSocket.getOutputStream().write(")".toString().getBytes()); //Terminating char
                    } catch (IOException e){

                    }
                    cp.dismiss();
                }
            });
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Sit tight");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Unable to connect to device", Toast.LENGTH_LONG).show();
                //finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                isBtConnected = true;
                connectionStatus.setText(address);
            }
            progress.dismiss();
        }
    }
}
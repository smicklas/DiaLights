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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.CompoundButton;


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
    ImageButton btnSync;
    Switch powerSwitch;
    Switch twinkleSwitch;
    TextView connectionStatus;
    ListView devices;

    //First panel represented with red[0], green[0], blue[0]
    //Last item in each array used for setting color of all panels
    int[] red = {255, 255, 255, 255};
    int[] blue = {0, 0, 0, 0};
    int[] green = {0, 0, 0, 0};

    //Bluetooth
    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean power = false;
    private boolean twinkle = false;
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
        twinkleSwitch = (Switch) findViewById(R.id.twinkle_switch);
        powerSwitch = (Switch) findViewById(R.id.power_switch);
        btnSync = (ImageButton) findViewById(R.id.sync_panels_btn);

        //Set switches off & disable until device is connected
        powerSwitch.setChecked(power);
        twinkleSwitch.setChecked(twinkle);
        powerSwitch.setEnabled(false);
        powerSwitch.setChecked(false);
        twinkleSwitch.setEnabled(false);


        //Establish button behavior
        //If no Bluetooth is connected, show toast and don't allow user to select color
        btnUpLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    setPanelColor(0, btnUpLeft);
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    setPanelColor(1, btnRight);
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnLowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    setPanelColor(2, btnLowLeft);
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }

            }
        });

        powerSwitch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isBtConnected){
                            if(power){
                                btnLowLeft.setBackgroundColor(Color.rgb(0, 0,0));
                                btnRight.setBackgroundColor(Color.rgb(0, 0,0));
                                btnUpLeft.setBackgroundColor(Color.rgb(0, 0,0));
                            }else{
                                btnLowLeft.setBackgroundColor(Color.rgb(red[0], green[0], blue[0]));
                                btnRight.setBackgroundColor(Color.rgb(red[1], green[1], blue[1]));
                                btnUpLeft.setBackgroundColor(Color.rgb(red[2], green[2], blue[2]));
                            }
                            power = !power; //NEED A LISTENER FOR THE SWITCH TOGGLE?
                            sendSingleCommand(4);
                        }
                    }
                });

        twinkleSwitch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isBtConnected){
                            if(!twinkle){
                                //If turning on twinkle, disable all buttons
                                twinkleSwitch.setChecked(true);
                                btnLowLeft.setEnabled(false);
                                btnUpLeft.setEnabled(false);
                                btnRight.setEnabled(false);
                            }else{
                                //If turning off twinkle, enable all buttons
                                twinkleSwitch.setChecked(false);
                                btnLowLeft.setEnabled(true);
                                btnUpLeft.setEnabled(true);
                                btnRight.setEnabled(true);
                            }
                            twinkle = !twinkle;
                            sendSingleCommand(5);
                            //send twinkle signal
                        }
                    }
                });

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected){
                    syncPanelColor();
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_LONG).show();
                }

            }
        });

        //powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                   //@Override
                                                   //public void onCheckedChanged(CompoundButton compoundButton, boolean power) {
                                                       //TO DO - can't turn on until connected
                                                       //TO DO - create method to turn on/off lights
                                              //     }
                                              // });


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
        });
    }

    private void connectDevice(ListView devices, String address) {
        new ConnectBT().execute();
    }

    private void sendSingleCommand(final int command_type){
        if(btSocket != null){
            try {
                btSocket.getOutputStream().write("(".toString().getBytes()); //Initial char
                btSocket.getOutputStream().write(Integer.toString(command_type).getBytes()); //Command indicator
                btSocket.getOutputStream().write(")".toString().getBytes()); //Terminating char
            } catch (IOException e){
                Toast.makeText(getApplicationContext(), "A bluetooth communication error has occurred.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void syncPanelColor(){
        if(btSocket != null){
            //Show color picker
            final ColorPicker cp = new ColorPicker(MainActivity.this, red[3], green[3], blue[3]);
            cp.show();
            cp.setCallback(new ColorPickerCallback() {
                @Override
                public void onColorChosen(int color) {
                    red[3] = cp.getRed();
                    green[3] = cp.getGreen();
                    blue[3] = cp.getBlue();

                    //Try sending color control signals
                    try {
                        btSocket.getOutputStream().write("(".toString().getBytes()); //Initial char
                        btSocket.getOutputStream().write(Integer.toString(3).getBytes()); //Panel indicator

                        btSocket.getOutputStream().write("R".toString().getBytes()); //Red value
                        btSocket.getOutputStream().write(Integer.toString(red[3]).getBytes());

                        btSocket.getOutputStream().write("G".toString().getBytes()); //Green value
                        btSocket.getOutputStream().write(Integer.toString(green[3]).getBytes());

                        btSocket.getOutputStream().write("B".toString().getBytes()); //Blue value
                        btSocket.getOutputStream().write(Integer.toString(blue[3]).getBytes());

                        btSocket.getOutputStream().write(")".toString().getBytes()); //Terminating char

                        //If the signal was sent & bluetooth is connected, assume they changed colors & are on
                        //TODO - Safe assumption?
                        power = true;
                        powerSwitch.setChecked(true);
                    } catch (IOException e){
                        Toast.makeText(getApplicationContext(), "A bluetooth communication error has occurred.", Toast.LENGTH_LONG).show();
                    }
                    btnUpLeft.setBackgroundColor(Color.rgb(red[3], green[3], blue[3]));
                    btnLowLeft.setBackgroundColor(Color.rgb(red[3], green[3], blue[3]));
                    btnRight.setBackgroundColor(Color.rgb(red[3], green[3], blue[3]));
                    cp.dismiss();
                }
            });

        }
    }
    //Send control signals to the panels (selecting static color)
    private void setPanelColor(final int panel_no, final Button btn){
        if(btSocket != null){
            //Show color picker
            final ColorPicker cp = new ColorPicker(MainActivity.this, red[panel_no], green[panel_no], blue[panel_no]);
            cp.show();
            cp.setCallback(new ColorPickerCallback() {
                @Override
                public void onColorChosen(int color) {
                    red[panel_no] = cp.getRed();
                    green[panel_no] = cp.getGreen();
                    blue[panel_no] = cp.getBlue();

                    //Try sending color control signals
                    try {
                        btSocket.getOutputStream().write("(".toString().getBytes()); //Initial char
                        btSocket.getOutputStream().write(Integer.toString(panel_no).getBytes()); //Panel indicator

                        btSocket.getOutputStream().write("R".toString().getBytes()); //Red value
                        btSocket.getOutputStream().write(Integer.toString(red[panel_no]).getBytes());

                        btSocket.getOutputStream().write("G".toString().getBytes()); //Green value
                        btSocket.getOutputStream().write(Integer.toString(green[panel_no]).getBytes());

                        btSocket.getOutputStream().write("B".toString().getBytes()); //Blue value
                        btSocket.getOutputStream().write(Integer.toString(blue[panel_no]).getBytes());

                        btSocket.getOutputStream().write(")".toString().getBytes()); //Terminating char

                        //If the signal was sent & bluetooth is connected, assume they changed colors & are on
                        //TODO - Safe assumption?
                        power = true;
                        powerSwitch.setChecked(true);
                    } catch (IOException e){
                        Toast.makeText(getApplicationContext(), "A bluetooth communication error has occurred.", Toast.LENGTH_LONG).show();
                    }
                    btn.setBackgroundColor(Color.rgb(red[panel_no], green[panel_no], blue[panel_no]));
                    cp.dismiss();
                }
            });

        }
    }

    //Connecting to Bluetooth
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
                    //TODO - Change this name v
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
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went okay
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Unable to connect to device", Toast.LENGTH_LONG).show();
                //finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                isBtConnected = true;
                connectionStatus.setText(address);
                twinkleSwitch.setEnabled(true);
                powerSwitch.setEnabled(true);
            }
            progress.dismiss();
        }
    }
}
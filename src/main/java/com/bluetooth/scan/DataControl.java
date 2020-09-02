package com.bluetooth.scan;


import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class DataControl extends Activity implements  View.OnClickListener {

    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;


    private TextView one, two, three, four, five, six, seven, eight, nine, zero, div, multi, sub, plus, dot, equals, display, clear;
    private ImageButton backDelete;
   // Button btnOn, btnOff, btnDis;
    Button On, Off, Discnt, Abt;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private EditText sendEditText;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the DataControl
        setContentView(R.layout.keyboard);

        //call the widge
       // On = (Button)findViewById(R.id.on_btn);
       // Off = (Button)findViewById(R.id.off_btn);
        Discnt = (Button)findViewById(R.id.dis_btn);
       // Abt = (Button)findViewById(R.id.abt_btn);
        myLabel=findViewById(R.id.my_text_view);
        sendBtn=findViewById(R.id.send_btn);
       // sendEditText=(EditText)findViewById(R.id.sendEditText);

        new ConnectBT().execute(); //Call the class to connect



        //commands to be sent to bluetooth
        sendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendData();      //method to turn on
            }
        });

       // Toast.makeText(getApplicationContext(),""+isBtConnected,Toast.LENGTH_LONG);
/*

        //commands to be sent to bluetooth
        On.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed();      //method to turn on
            }
        });

        Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               // beginListenForData();
                turnOffLed();   //method to turn off


            }
        });
*/
        Discnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

      //  beginListenForData();




          //  receiveData();
        //} catch (IOException e) {
          //  e.printStackTrace();
        //}


        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        six = findViewById(R.id.six);
        seven = findViewById(R.id.seven);
        eight = findViewById(R.id.eight);
        nine = findViewById(R.id.nine);
        zero = findViewById(R.id.zero);
        display =findViewById(R.id.display);
        clear = findViewById(R.id.clear);
        backDelete = findViewById(R.id.backDelete);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);
        eight.setOnClickListener(this);
        nine.setOnClickListener(this);
        zero.setOnClickListener(this);
        display.setOnClickListener(this);
        clear.setOnClickListener(this);
        backDelete.setOnClickListener(this);

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "SevenSegment.ttf");
        display.setTypeface(tf);
    }


    public void receiveData() throws IOException{

//       final Handler handler = new Handler();

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        if (btSocket!=null)
        {
            try
            {
                InputStream socketInputStream =  btSocket.getInputStream();

                byte[] buffer = new byte[1024];
                int bytes;

                // Keep looping to listen for received messages
                while (true) {
                    try {
                        if(mmInputStream!=null) {
                            bytes = mmInputStream.read(buffer);            //read bytes from input buffer
                            final String readMessage = new String(buffer, 0, bytes);
                            // Send the obtained bytes to the UI Activity via handler
                            Log.i("logging", readMessage + "");


                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {

                                    myLabel.setText(readMessage);
                                    //myLabel.append("");
                                } // This is your code
                            };
                            mainHandler.post(myRunnable);
/*
                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                myLabel.setText(readMessage);
                            }
                        });
                        */
                        }

                    } catch (IOException e) {
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                msg("Error");
            }


        }



    }


    void beginListenForData()
    {
      //  final Handler handler = new Handler();
        // Get a handler that can be used to post to the main thread
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final byte delimiter = 10; //This is the ASCII code for a newline character
        Log.d("btSocket",""+btSocket);
        if(btSocket!=null) {
            try {
                mmInputStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        if(mmInputStream!=null) {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;
                                        Log.d("btSocket data", "" + data);
                                        Runnable myRunnable = new Runnable() {
                                            @Override
                                            public void run() {

                                                myLabel.setText(data);
                                                //myLabel.append("");
                                            } // This is your code
                                        };
                                        mainHandler.post(myRunnable);
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }


    private void sendData()
    {
        if (btSocket!=null)
        {
            try
            {


                final Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                       // myLabel.setText(sendEditText.getText().toString());
                        handler.postDelayed(this,100);
                    }
                },100);
                String data="$134"+display.getText().toString()+";";
                btSocket.getOutputStream().write(data.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                mmOutputStream.write("Hello".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }



    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                mmOutputStream.write("Ranojan Kumar".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public  void about(View v)
    {
      //  if(v.getId() == R.id.abt)
        //{
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (v.findViewById(R.id.one) == one) {
            if (!display.getText().equals("")) {
                display.append("1");
                limitDigit(display.getText().toString());
            } else {
                display.setText("1");
            }
        } else if (v.findViewById(R.id.two) == two) {
            if (!display.getText().equals("")) {
                display.append("2");
                limitDigit(display.getText().toString());
            } else {
                display.setText("2");
            }
        } else if (v.findViewById(R.id.three) == three) {
            if (!display.getText().equals("")) {
                display.append("3");
                limitDigit(display.getText().toString());
            } else {
                display.setText("3");
            }
        } else if (v.findViewById(R.id.four) == four) {
            if (!display.getText().equals("")) {
                display.append("4");
                limitDigit(display.getText().toString());
            } else {
                display.setText("4");
            }
        } else if (v.findViewById(R.id.five) == five) {
            if (!display.getText().equals("")) {
                display.append("5");
                limitDigit(display.getText().toString());
            } else {
                display.setText("5");
            }
        } else if (v.findViewById(R.id.six) == six) {
            if (!display.getText().equals("")) {
                display.append("6");
                limitDigit(display.getText().toString());
            } else {
                display.setText("6");
            }
        } else if (v.findViewById(R.id.seven) == seven) {
            if (!display.getText().equals("")) {
                display.append("7");
                limitDigit(display.getText().toString());
            } else {
                display.setText("7");
            }
        } else if (v.findViewById(R.id.eight) == eight) {
            if (!display.getText().equals("")) {
                display.append("8");
                limitDigit(display.getText().toString());
            } else {
                display.setText("8");
            }
        } else if (v.findViewById(R.id.nine) == nine) {
            if (!display.getText().equals("")) {
                display.append("9");
                limitDigit(display.getText().toString());
            } else {
                display.setText("9");
            }
        } else if (v.findViewById(R.id.zero) == zero) {
            if (!display.getText().equals("")) {
                display.append("0");
                limitDigit(display.getText().toString());
            } else {
                display.setText("0");
            }
        } else if (v.findViewById(R.id.display) == display) {

        } else if (v.findViewById(R.id.clear) == clear) {
            display.setText(null);
        } else if (v.findViewById(R.id.backDelete) == backDelete) {
            if (!display.getText().equals("")) {
                String s = display.getText().toString();
                if (s.length() > 0) {
                    display.setText(s.substring(0, s.length() - 1));
                } else {
                    // Toast.makeText(this, "Nothing to remove", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Toast.makeText(this, "nothing to remove", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public  void limitDigit(String input)
    {

        String lastFourDigits = "";     //substring containing last 4 characters

        if (input.length() > 4)
        {
            lastFourDigits = input.substring(input.length() - 4);
        }
        else
        {
            lastFourDigits = input;
        }
        display.setText(lastFourDigits);

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(DataControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                 BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                 btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect();//start connection
                    mmOutputStream = btSocket.getOutputStream();
                    mmInputStream = btSocket.getInputStream();

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;



/*
                try {
                    receiveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            progress.dismiss();

              //beginListenForData();
//To receive data 555
            new Thread(new Runnable() {
                public void run(){
                    try {
                        receiveData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //beginListenForData();
                }
            }).start();
            /*try {
                receiveData();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            /*if(isBtConnected){

                try {
                    receiveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }
    }
}

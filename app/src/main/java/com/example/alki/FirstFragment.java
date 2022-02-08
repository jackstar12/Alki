package com.example.alki;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstFragment#newInstance} factory method to
 * create an instance of this fragment.
 */ //
public class FirstFragment extends Fragment {
    double alkwert = 0;
    int min = 0;
    int hour = 0;

    private final String tag = "Alki";

    private final UUID BLUETOOTH_SSP =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    ReceiveBt receiveBt;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;

    int REQUEST_ENABLE_BLUETOOTH=1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FirstFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FirstFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstFragment newInstance(String param1, String param2) {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        button = (Button) view.findViewById(R.id.reqbutton);
        TextView sober = (TextView) view.findViewById(R.id.sobertime);
        TextView alk = (TextView) view.findViewById(R.id.alkwert);
        EditText name = (EditText) view.findViewById(R.id.namefield);
        TextView status = (TextView) view.findViewById(R.id.status);


        button.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                onButtonClicked(v, sober, alk, status, name);
            }
        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireActivity().registerReceiver(receiver, filter);



        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onButtonClicked(View view, TextView sober, TextView alk, TextView status, EditText name) {
        //BLuetooth Daten werden Empfangen:
        /*Handler handler =new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                switch (msg.what)
                {
                    case STATE_LISTENING:
                        status.setText("Listening");
                        break;
                    case STATE_CONNECTING:
                        status.setText("Connecting");
                        break;
                    case STATE_CONNECTED:
                        status.setText("Connected");
                        break;
                    case STATE_CONNECTION_FAILED:
                        status.setText("Connection Failed");
                        break;
                    case STATE_MESSAGE_RECEIVED:
                        byte[] readBuff= (byte[]) msg.obj;
                        String tempMsg = new String(readBuff,0,msg.arg1);
                        alk.setText(tempMsg);
                        break;

                }
                return true;
            }
        });*/

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        boolean foundPair = false;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if(tryConnectDevice(device)) {
                    foundPair = true;
                    break;
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }


        Log.d(tag, String.valueOf(requireActivity().checkSelfPermission(Context.BLUETOOTH_SERVICE) == PackageManager.PERMISSION_GRANTED));
        if(!foundPair) {
            bluetoothAdapter.startDiscovery();

        }
        // byte[] readBuff= (byte[]) msg.obj;
        // String tempMsg=new String(readBuff,0,msg.arg1);
        // alk.setText(tempMsg);
        alkwert = Double.parseDouble(name.getText().toString());
        hour = (int) (alkwert*10);
        System.out.println(alkwert);
        min = (int) ((alkwert*100)%10);
        DecimalFormat df = new DecimalFormat("00.00");
        alk.setText(df.format(alkwert) + "Promille");
        sober.setText(hour + "h" + min + "min");

    }

    private boolean tryConnectDevice(BluetoothDevice device) {
        Log.d(tag, device.getName());
        boolean connected = false;
        if(device.getName().equals("Galaxy A5 (2017)")) {
            ClientClass client = new ClientClass(device);
            client.start();
            connected = true;
        }
        return connected;
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(tag, "Receiver: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                tryConnectDevice(device);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device=device1;
        }

        public void run()
        {
            try {
                socket=device.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_SSP);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    Toast.makeText(requireActivity(), "Device not paired", Toast.LENGTH_SHORT).show();
                }

                Log.d(tag, "Paired: " + device.getBondState());
                socket.connect();
                Log.d(tag, String.valueOf(socket.isConnected()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ReceiveBt receiveThread = new ReceiveBt(socket);
            receiveThread.start();

        }
    }

    private class ReceiveBt extends Thread {
        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private BluetoothDevice device;

        public ReceiveBt (BluetoothSocket socket) {
            this.socket = socket;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            Log.d(tag, socket.toString());
            InputStream tempIn = null;

            try {
                tempIn= socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                outputStream = socket.getOutputStream();
                outputStream.write(69);
                Log.d(tag, outputStream.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;

            try {
                outputStream.write(69);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while(true) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
/*
            while(true) {
                try {
                    bytes=inputStream.read(buffer);
                    System.out.println(bytes + "\n" + buffer);
                    //handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(receiver);
    }
}
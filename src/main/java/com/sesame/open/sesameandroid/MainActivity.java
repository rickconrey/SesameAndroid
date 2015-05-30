package com.sesame.open.sesameandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private ListView listView;
    private TextView btConnectionStatus;
    private Button btnSend;
    private Button btnConnect;
    private BluetoothAdapter mBluetoothAdapter;
    private InputStream mInputStreamBT;
    private OutputStream mOutputStreamBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        listView = (ListView)findViewById(R.id.listBTDevices);
        btConnectionStatus = (TextView)findViewById(R.id.textView);
        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] buffer = new byte[4];
                buffer[0] = BluetoothAccess.CODE_WORD[0];
                buffer[1] = BluetoothAccess.CODE_WORD[1];
                buffer[2] = BluetoothAccess.CODE_WORD[2];
                buffer[3] = BluetoothAccess.CODE_WORD[3];
                writeBT(buffer);
            }
        });

        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSesame();
            }
        });
    }

    public void runSesame() {
        String LOG_RUNSESAME = "runSesame";
        final UUID SSP_UUID = UUID.fromString(BluetoothAccess.BLUETOOTH_UUID);

        String address = BluetoothAccess.BLUETOOTH_ADDRESS;
        Set<BluetoothDevice> pairedDevices;
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList<String> deviceList = new ArrayList();
        for (BluetoothDevice bt : pairedDevices) {
            deviceList.add(bt.getName());
        }
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, deviceList);

        listView.setAdapter(arrayAdapter);

        BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(address);
        btDevice.fetchUuidsWithSdp();
        ParcelUuid[] uuids = btDevice.getUuids();
        for (int i = 0; i < uuids.length; i++) {
            Log.d(LOG_RUNSESAME, uuids[i].toString());
        }
        mBluetoothAdapter.cancelDiscovery();
        BluetoothSocket btSocket;

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(SSP_UUID);
            btConnectionStatus.setText(R.string.bluetooth_connecting);
            btSocket.connect();
            if (btSocket.isConnected()) {
                btConnectionStatus.setText(R.string.bluetooth_connected);
                Log.d(LOG_RUNSESAME, "Connected");
                mInputStreamBT = btSocket.getInputStream();
                mOutputStreamBT = btSocket.getOutputStream();
            } else {
                btConnectionStatus.setText(R.string.bluetooth_disconnected);
                Log.d(LOG_RUNSESAME, "Failed to connect.");
            }

        } catch (IOException e) {
            CharSequence text = "Error connecting to BT";
            btConnectionStatus.setText(R.string.bluetooth_disconnected);
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    public void writeBT(byte[] buffer) {
        try {
            mOutputStreamBT.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
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
}

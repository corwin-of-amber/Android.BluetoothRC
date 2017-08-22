package amber.corwin.bluetoothrc.androidbluetoothrc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends Activity { //AppCompatActivity {

    private static final String TAG = "BluetoothApp";

    private static String NAME = "Corwin of Amber";
    private static UUID MY_UUID = new UUID(0x000d7b398cfa46a9l, 0xbc70030b09f60869l);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        new AcceptThread().start();

        //new GetEventThread().start();
    }

    // ---------
    // Keys Part
    // ---------

    class GetEventThread extends Thread {
        public void run() {
            // Try to run getevent
            String myStringArray[] = {"su", "-c", "getevent"};
            String line;
            try {
                Process process = new ProcessBuilder().command(myStringArray).redirectErrorStream(true).start();
                InputStreamReader inputstreamreader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputstreamreader);
                while ((line = bufferedReader.readLine()) != null) {
                    Log.i(TAG, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Finished");

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_FOCUS)
        {
            event.startTracking();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) emitVolumeUp();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) emitVolumeDown();
            if (keyCode == KeyEvent.KEYCODE_FOCUS) emitCameraSoft();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            //Log.d(TAG, "Long press KEYCODE_VOLUME_UP");
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //Log.d(TAG, "Long press KEYCODE_VOLUME_DOWN");
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if((event.getFlags() & KeyEvent.FLAG_CANCELED_LONG_PRESS) == 0){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                //Log.e(TAG, "Short press KEYCODE_VOLUME_UP");
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                //Log.e(TAG, "Short press KEYCODE_VOLUME_DOWN");
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void emitVolumeUp() { emit(new byte[] { 61, 62, 63 }); }
    private void emitVolumeDown() {
        emit(new byte[] { 63, 62, 61 });
    }
    private void emitCameraSoft() { emit(new byte[] { 20, 20, 20 }); }

    private void emit(byte[] buf) {
        if (btOut != null) {
            try {
                btOut.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------
    // Bluetooth Server Part
    // ---------------------

    private OutputStream btOut = null;

    private class AcceptThread extends Thread {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    /*if (mBluetoothAdapter == null) {
        // Device does not support Bluetooth
    }*/

        /*
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }
        */

        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            listen();
        }

        private void listen() {
            if (mBluetoothAdapter == null) { abortBluetooth("No Bluetooth device."); return; }

            /*if (mmServerSocket != null) {
                try { mmServerSocket.close(); } catch (IOException e) { e.printStackTrace(); }
                mmServerSocket = null;
            }*/
            try {
                if (mmServerSocket == null)
                    mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                abortBluetooth("Bluetooth access error.");
            }
        }

        public void run() {
            if (mmServerSocket == null) return;  /* TODO wait and retry? */

            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    //mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        socket.close();
                    }
                    catch (IOException e) { e.printStackTrace(); }
                    //break;
                }
                listen(); // go back to listen mode
            }
        }



        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void abortBluetooth(final String msg) {
        final TextView status = (TextView) findViewById(R.id.status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() { status.setText(msg);
            }
        });
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        final TextView status = (TextView) findViewById(R.id.status);
        final String remoteName = socket.getRemoteDevice().getName();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Connected to " + remoteName + ".");
            }
        });

        try {
            InputStream s = socket.getInputStream();
            btOut = socket.getOutputStream();

            while (true) {
                final int byt = s.read();
                if (byt == -1) break;
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Read byte: " + byt, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        } catch (IOException e) {
            // BluetoothInputStream violates the InputStream protocol by not returning -1
            // at end of stream. Instead, an IOException is thrown.
            Log.d(TAG, "connection closed.");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Not connected.");
            }
        });
    }
}
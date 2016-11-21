package amber.corwin.bluetoothrc.androidbluetoothrc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by corwin on 11/20/16.
 */

public class MediaKeysReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.d("BluetoothRC", "Broadcast intent.");

        KeyEvent event = null;
        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            event = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        }

        if (event == null) {
            return;
        }
        Log.d("BluetoothRC", "Media key intent.");
    }
}

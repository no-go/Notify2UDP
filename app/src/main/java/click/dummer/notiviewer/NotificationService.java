package click.dummer.notiviewer;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.Preference;
import android.provider.SyncStateContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NotificationService extends NotificationListenerService {
    public static final String SPLITTOKEN = " || ";
    private String TAG = this.getClass().getSimpleName();
    private SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Intent i = new Intent("click.dummer.notiviewer.NOTIFICATION_LISTENER");
        Notification noti = sbn.getNotification();
        Log.i(TAG, "onNotificationPosted from " + sbn.getPackageName() + "\n");
        Bundle extras = null;
        String title = getString(R.string.app_name);
        String msg = (String) noti.tickerText;
        if (msg != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                extras = noti.extras;
                title = extras.getString(Notification.EXTRA_TITLE);
                String dummy = extras.getString(Notification.EXTRA_TEXT);
                if (dummy != null && dummy.length()> 25) {
                    msg = dummy;
                }
                dummy = extras.getString(Notification.EXTRA_BIG_TEXT);
                if (dummy != null && dummy.length()> 25) {
                    msg = dummy;
                }
            }
            if (msg.startsWith(title)) {
                msg = msg.replaceFirst(title, "").substring(2);
            }
            sendNetBroadcast((title + SPLITTOKEN + msg).trim());
        }
        i.putExtra("notification_event", msg);
        sendBroadcast(i);
    }

    public void sendNetBroadcast(String messageStr) {
        // Hack - should be done using an async task !!
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            InetAddress address = InetAddress.getByName(
                    mPreferences.getString("hostname", "192.168.1.100").trim()
            );

            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, address,
                    Integer.parseInt(mPreferences.getString("port", "58000").trim())
            );
            socket.send(sendPacket);
            Log.i(TAG, getClass().getName() + "Broadcast packet sent to: " + address.getHostAddress());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }
}

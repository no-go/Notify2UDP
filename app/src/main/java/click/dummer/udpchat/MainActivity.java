package click.dummer.udpchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int MTUSIZE = 1500;
    public static final int LOGSIZE = 500;
    public static final String DEFAULTPORT = "58000";
    public static final String BROADCASTADDR = "255.255.255.255";

    private MyDatagramReceiver myDatagramReceiver = null;

    private TextView txtView;
    private EditText hostName;
    private EditText portEdit;
    private EditText message;
    private CheckBox checkBox;

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        hostName = (EditText) findViewById(R.id.hostEdit);
        portEdit = (EditText) findViewById(R.id.portEdit);
        message = (EditText) findViewById(R.id.messageEdit);
        checkBox = (CheckBox) findViewById(R.id.listenBroadcast);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        byte[] ipAddress = BigInteger.valueOf(ip).toByteArray();
        String myip = "";
        for (int i = ipAddress.length-1; i >= 0; i--) {
            myip += Integer.toString(ipAddress[i] & 0xff) + ".";
        }
        myip = myip.substring(0, myip.length() -1);
        getSupportActionBar().setTitle(
                " " + getString(R.string.app_name) + " ("+ myip +")"
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        String host = "";
        String port = "";
        if (mPreferences.contains("hostname")) {
            host = mPreferences.getString("hostname", BROADCASTADDR);
        }
        hostName.setText(host.equals("") ? BROADCASTADDR : host);
        if (mPreferences.contains("port")) {
            port = mPreferences.getString("port", DEFAULTPORT);
        }
        if (mPreferences.contains("listen_Broadcast")) {
            checkBox.setChecked(mPreferences.getBoolean("listen_Broadcast", false));
        }
        portEdit.setText(port.equals("") ? DEFAULTPORT : port);

        if (myDatagramReceiver == null) myDatagramReceiver = new MyDatagramReceiver();
        if (!myDatagramReceiver.isAlive()) myDatagramReceiver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String host = hostName.getText().toString();
        String port = portEdit.getText().toString();
        boolean bc = checkBox.isChecked();
        mPreferences.edit().putString("hostname", host).apply();
        mPreferences.edit().putString("port", port).apply();
        mPreferences.edit().putBoolean("listen_Broadcast", bc).apply();

        if (myDatagramReceiver != null) myDatagramReceiver.kill();
    }

    void log(String str) {
        Date dNow = new Date();
        String dateFormat = "HH:mm:ss";
        SimpleDateFormat ft = new SimpleDateFormat(dateFormat);
        String timeStr = ft.format(dNow);
        str = timeStr + "\n" + str;
        synchronized (txtView) {
            String s = str + txtView.getText().toString();
            if (s.length() < LOGSIZE) {
                txtView.setText(s);
            } else {
                txtView.setText(s.substring(0, LOGSIZE-1));
            }
            txtView.postInvalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void sendButton(View v) {
        String host = hostName.getText().toString().trim();
        if (host.length() == 0) host = BROADCASTADDR;
        String port = portEdit.getText().toString().trim();
        mPreferences.edit().putString("hostname", host).apply();
        mPreferences.edit().putString("port", port).apply();

        // hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            InetAddress address = InetAddress.getByName(host);

            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = message.getText().toString().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length, address,
                    Integer.parseInt(port)
            );
            socket.send(sendPacket);
            log("send to " + host + ": " + message.getText().toString() + "\n");
            message.setText("");
        } catch (IOException e) {
            log("error: " + e.getMessage() + "\n");
        }
    }

    public void bcButton(View v) {
        boolean bc = checkBox.isChecked();
        checkBox.setChecked(bc);
        mPreferences.edit().putBoolean("listen_Broadcast", bc).commit();
        if (myDatagramReceiver != null) myDatagramReceiver.kill();
        // restart app
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(
                getBaseContext().getPackageName()
        );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


    private class MyDatagramReceiver extends Thread {
        private boolean bKeepRunning = true;

        public void run() {
            byte[] lmessage = new byte[MTUSIZE];
            DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);
            DatagramSocket socket = null;
            try {
                if (mPreferences.getBoolean("listen_Broadcast", false) == true) {
                    socket = new DatagramSocket(
                            Integer.parseInt(mPreferences.getString("port", DEFAULTPORT)),
                            InetAddress.getByName(BROADCASTADDR)
                    );
                    socket.setBroadcast(true);
                } else {
                    socket = new DatagramSocket(
                            Integer.parseInt(mPreferences.getString("port", DEFAULTPORT))
                    );
                }
                while(bKeepRunning) {
                    socket.receive(packet);
                    final String message = packet.getAddress().getHostAddress() +
                            ": " + new String(lmessage, 0, packet.getLength()) + "\n";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log(message);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (socket != null) {
                socket.close();
            }
        }

        public void kill() {
            bKeepRunning = false;
        }
    }
}


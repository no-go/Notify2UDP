package click.dummer.udpchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int MTUSIZE = 1480;
    public static final int LOGSIZE = 1000;
    public static final String DEFAULTPORT = "58000";
    public static final String BROADCASTADDR = "255.255.255.255";

    private MyDatagramReceiver myDatagramReceiver = null;
    private TextView txtView;
    private SharedPreferences mPreferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), MessageActivity.class);
                startActivity(intent);
                Snackbar.make(view, getString(R.string.doeswork), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        if (myDatagramReceiver == null) myDatagramReceiver = new MyDatagramReceiver();
        if (!myDatagramReceiver.isAlive()) myDatagramReceiver.start();
    }

    @Override
    protected void onStop() {
        if (myDatagramReceiver != null) myDatagramReceiver.kill();
        super.onStop();
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


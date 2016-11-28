package click.dummer.notiviewer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView txtView;
    private EditText hostName;
    private EditText portEdit;

    private NotificationReceiver nReceiver;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        nReceiver = new NotificationReceiver();
        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        hostName = (EditText) findViewById(R.id.hostEdit);
        portEdit = (EditText) findViewById(R.id.portEdit);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("click.dummer.notiviewer.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nReceiver, filter);

        String host = "";
        String port = "";
        if (mPreferences.contains("hostname")) {
            host = mPreferences.getString("hostname", "255.255.255.255");
        }
        hostName.setText(host.equals("") ? "255.255.255.255" : host);
        if (mPreferences.contains("port")) {
            port = mPreferences.getString("port", "58000");
        }
        portEdit.setText(port.equals("") ? "58000" : port);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);

        String host = hostName.getText().toString();
        String port = portEdit.getText().toString();
        mPreferences.edit().putString("hostname", host).apply();
        mPreferences.edit().putString("port", port).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void buttonClicked(View v) {
        String host = hostName.getText().toString();
        String port = portEdit.getText().toString();
        mPreferences.edit().putString("hostname", host).apply();
        mPreferences.edit().putString("port", port).apply();

        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
        ncomp.setContentTitle(getString(R.string.app_name));
        ncomp.setContentText("I am the Ticker");
        ncomp.setTicker("I am the Ticker");
        ncomp.setSmallIcon(R.mipmap.ic_launcher);
        ncomp.setAutoCancel(true);
        nManager.notify((int)System.currentTimeMillis(), ncomp.build());
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event") + "\n" + txtView.getText();
            txtView.setText(temp);
        }
    }
}


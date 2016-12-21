package click.dummer.notiviewer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Set;

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

        Set<String> packs = NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext());
        boolean readNotiPermissions = packs.contains(getPackageName());
        if (readNotiPermissions == false) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            alertDialogBuilder.setMessage(getString(R.string.sorry, getString(R.string.app_name)));
            alertDialogBuilder.show();
        }

        IntentFilter filter = new IntentFilter("click.dummer.notiviewer.NOTIFICATION_LISTENER");
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
        ncomp.setContentTitle("[myip]");
        ncomp.setContentText("I am the Text from " + getString(R.string.app_name));
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


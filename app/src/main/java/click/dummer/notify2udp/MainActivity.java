package click.dummer.notify2udp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String PROJECT_LINK = "https://github.com/no-go/Notify2UDP/";
    public static final String PROJECT_LINK2 = "https://github.com/no-go/Notify2UDP/tree/master/udp2notify/";
    public static final String FLATTR_ID = "o6wo7q";
    private String FLATTR_LINK;

    private TextView txtView;
    private EditText hostName;
    private EditText portEdit;

    private NotificationReceiver nReceiver;
    private SharedPreferences mPreferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            FLATTR_LINK = "https://flattr.com/submit/auto?fid="+FLATTR_ID+"&url="+
                    java.net.URLEncoder.encode(PROJECT_LINK, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_flattr:
                Intent intentFlattr = new Intent(Intent.ACTION_VIEW, Uri.parse(FLATTR_LINK));
                startActivity(intentFlattr);
                break;
            case R.id.action_project:
                Intent intentProj= new Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_LINK));
                startActivity(intentProj);
                break;
            case R.id.action_project2:
                Intent intentProj2= new Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_LINK2));
                startActivity(intentProj2);
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

        IntentFilter filter = new IntentFilter("click.dummer.notify2udp.NOTIFICATION_LISTENER");
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


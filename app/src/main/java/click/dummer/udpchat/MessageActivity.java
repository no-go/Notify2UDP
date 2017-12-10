package click.dummer.udpchat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageActivity extends Activity {
    private Button sendButton;
    private EditText toHost;
    private EditText message;

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        sendButton = (Button) findViewById(R.id.btnCreateNotify);
        toHost  = (EditText) findViewById(R.id.to);
        message = (EditText) findViewById(R.id.messageEdit);

        toHost.setText(mPreferences.getString("hostname", MainActivity.BROADCASTADDR));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendMessage().execute(
                        toHost.getText().toString(),
                        message.getText().toString()
                );
            }
        });
    }

    private class SendMessage extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String to = strings[0];
            String msg = strings[1];
            if (to.length() == 0) to = MainActivity.BROADCASTADDR;

            mPreferences.edit().putString("hostname", to.trim()).apply();

            // hack
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                InetAddress address = InetAddress.getByName(to);
                //Open a random port to send the package
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                byte[] sendData = msg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length, address,
                        Integer.parseInt(mPreferences.getString("port", MainActivity.DEFAULTPORT))
                );
                socket.send(sendPacket);
            } catch (IOException e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}

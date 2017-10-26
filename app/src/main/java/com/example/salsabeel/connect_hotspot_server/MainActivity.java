package com.example.salsabeel.connect_hotspot_server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    Button startServer;
    TextView ipView, tv,text;

    WifiManager wifiManager;
    WifiApControl apControl;
    Switch hotspot;

    ServerSocket server;   // 8000
    Socket client;
    InputStream inputStream;
    DataInputStream dataInputStream;
    String message;

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        apControl = WifiApControl.getApControl(wifiManager);

        /* Uncomment this to receive messages from client
        text = (TextView) findViewById(R.id.msgLabelView);
        tv = (TextView) findViewById(R.id.msgView);
        */
        ipView = (TextView) findViewById(R.id.ipView);
        startServer = (Button) findViewById(R.id.servetBtn);

        hotspot = (Switch) findViewById(R.id.switch1);
        if (apControl.isWifiApEnabled())
        {
            hotspot.setChecked(true);

            startServer.setEnabled(true);
        }
        hotspot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    // open settings for user to allow permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if ( ! Settings.System.canWrite(getApplicationContext())) {
                            Intent goToSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            goToSettings.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                            startActivity(goToSettings);
                        }
                    }
                    if (!apControl.isWifiApEnabled())
                        if ( wifiManager.isWifiEnabled() )
                        {
                            Toast.makeText(getApplicationContext(),"Please, turn OFF WIFI",Toast.LENGTH_LONG).show();
                            buttonView.setChecked(false);
                        }
                        else
                        {
                            // turn on hotspot
                            turnOnOffHotspot(getApplicationContext(),true);

                            startServer.setEnabled(true);
                        }

                }
                else
                {
                    // turn off hotspot
                    turnOnOffHotspot(getApplicationContext(),false);

                    startServer.setEnabled(false);

                }
            }
        });

        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ipView.setText(getIpAddress());

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // open tcp sockets at port 8000
                        try {
                            server = new ServerSocket(8000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        // accept client socket
                        try
                        {
                            client = server.accept();
                            Log.i("server","client connected");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        /*  Uncomment this to start receiving messages from client

                        // get input stream
                        try {
                            inputStream = client.getInputStream();
                            Log.i("server","input stream found");
                        } catch (IOException e) {
                            Log.i("server","input stream not found");
                        }

                        // create data input stream
                        dataInputStream = new DataInputStream(inputStream);

                        // start receiving data
                        while (!client.isClosed()) {
                            try {
                                message = dataInputStream.readUTF();
                                Log.i("message",message);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // write message on textView
                                        tv.setText(message);
                                    }
                                });
                            } catch (IOException e) {
                                break;
                            }
                        }
                        */
                    }

                }).start();


            }
        });
    }
    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Connect Controller to ip at: "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
    /**
     * Turn on or off Hotspot.
     *
     * @param context
     * @param isTurnToOn
     */
    public void turnOnOffHotspot(Context context, boolean isTurnToOn) {
        if (apControl != null) {
            apControl.setWifiApEnabled(apControl.getWifiApConfiguration(),
                    isTurnToOn);
        }
    }
}

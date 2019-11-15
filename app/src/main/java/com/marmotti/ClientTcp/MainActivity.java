package com.marmotti.ClientTcp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.acl.Permission;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {


    static Client client;
    Scanner scanner = new Scanner(System.in);

    public static class ConnectTask extends AsyncTask<String, String, Client>{

        @Override
        protected Client doInBackground(String... message) {
            //first we instantiate a client
            client = new Client(new Client.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    publishProgress(message);
                }
            });
            client.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d("test", values[0]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = this.getApplicationContext();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 5);


        //Accessing views
        final TextView status = findViewById(R.id.tvStatus);
        Button start = findViewById(R.id.bStart);
        Button stop = findViewById(R.id.bStop);
        final TextView response = findViewById(R.id.tvResponse);


        try {
            new ConnectTask().execute("");
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (client != null) {
                        client.run();
                    }
                    else {
                        status.setText(R.string.msg_problem);
                    }
                }
            });

            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (client != null) {
                        client.stop();
                    }
                    else {
                        status.setText(R.string.msg_problem);
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

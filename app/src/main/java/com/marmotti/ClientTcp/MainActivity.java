package com.marmotti.ClientTcp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

        //Accessing views
        final EditText message = findViewById(R.id.etMsg);
        final TextView status = findViewById(R.id.tvStatus);
        Button send = findViewById(R.id.bSend);
        final TextView conversation = findViewById(R.id.tvConversation);

        try {
            new ConnectTask().execute("");
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (client != null) {
                        String currentMessage = message.getText().toString();
                        if (!currentMessage.isEmpty()) {
                            if (currentMessage.trim().equals("QUIT")) {
                                client.stopClient();
                                status.setText(R.string.msg_disconnected);
                            } else {
                                client.sendMessage(currentMessage);
                                conversation.append(currentMessage);
                            }
                        }
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

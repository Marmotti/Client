package com.marmotti.ClientTcp;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;

class Client {

    private static final String TAG = Client.class.getSimpleName();
    private static final String SERVER_IP = "10.0.2.2"; //server IP address
    private static final int SERVER_PORT = 6666;
    private MediaRecorder recorder;

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;


    Client(OnMessageReceived mMessageListener){
        this.mMessageListener = mMessageListener;
    }

    /**
     * used for sending messages to the server
     *
     * @param message text entered by client
     */
    void sendMessage(final String message){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null){
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * used for safely closing the app
     */
    void stop(){
        recorder.stop();
        mRun = false;
        //safely closing the buffer
        if (mBufferOut != null){
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
        recorder.reset();
    }

    void run(){
        mRun = true;

        //here we create the connection
        try{
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //creating new socket for establishing connection

            try (Socket socket = new Socket(serverAddress, SERVER_PORT)) {

                //sending message to the server
                ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);

                recorder = new MediaRecorder();
                recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);


                recorder.setOutputFile(pfd.getFileDescriptor());

                try {
                    Log.i(TAG, pfd.getFileDescriptor().toString());
                } catch (Exception e) {
                    Log.d(TAG, "Exception");
                }

                try {
                    recorder.prepare();
                    System.out.println("Prepared");
                } catch (IllegalStateException | IOException e) {

                    e.printStackTrace();
                }
                try {
                    recorder.start();
                } catch (Exception e){
                    System.out.println("Nie jest ok");
                    e.printStackTrace();
                }
                //receiving message from the server
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //loop in which client will listen for the server messages
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    //when we have a message present and we have listener asigned. then we are able to receive message
                    if (mServerMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }

                Log.d("RESPONSE FROM SEVER", "S: Received Message: " + mServerMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}

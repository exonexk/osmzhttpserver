package com.vsb.kru13.osmzhttpserver;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;


public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean booRun;
    private Handler handler;
    private int maxThreads;
    private Semaphore semaphore;

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        booRun = false;
    }

    public SocketServer(Handler handler, String maxThreads) {
        this.handler = handler;
        this.maxThreads = Integer.valueOf(maxThreads);
        this.semaphore = new Semaphore(this.maxThreads);
    }

    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            booRun = true;

            while (booRun) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                if (semaphore.tryAcquire())
                {
                    Thread thread = new cThreads(s,handler,semaphore);
                    thread.start();
                }
                else
                {
                    Log.d("semaphore", "FULL");
                }
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        }
        finally {
            serverSocket = null;
            booRun = false;
        }
    }
}


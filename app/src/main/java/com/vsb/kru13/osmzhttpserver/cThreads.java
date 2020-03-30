package com.vsb.kru13.osmzhttpserver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import static com.vsb.kru13.osmzhttpserver.CamActivity.imageInBytes;
import static com.vsb.kru13.osmzhttpserver.CamActivity.bStream;

public class cThreads extends Thread {

    private Socket s;
    private Handler handler;
    private Bundle bundle;
    private Message msg;
    private Semaphore semaphore;


    public cThreads(Socket s, Handler handler, Semaphore semaphore) {
        this.s = s;
        this.handler = handler;
        this.semaphore = semaphore;
        bundle = new Bundle();
        msg = new Message();
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public void run() {
        try {
            Log.d("SERVER", "Socket Accepted");
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String uri = "";
            String type = "";
            String snapshot = "/camera/snapshot";
            String stream = "/camera/stream";
            String bin = "/cgi-bin";

            String tmp = in.readLine();
            if(tmp !=null && !tmp.isEmpty()){
                type = tmp.split(" ")[0];
                uri = tmp.split(" ")[1];

                if (uri.contains(bin)) {
                    String command = uri.substring(9);
                    String[] commands = command.split("%20");
                    if (commands.length > 0) {
                        List<String> arguments = new ArrayList<String>();
                        arguments.add(commands[0]);

                        for (int i = 1; i < commands.length; i++) {
                            arguments.add(commands[1]);
                        }
                        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
                        Process process = processBuilder.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1);
                        int c = 0;
                        while ((c = reader.read()) != -1) {
                            o.write(c);
                        }
                        o.flush();
                        process.destroy();
                    }
                }
                if(uri.contains(snapshot)){
                    if(imageInBytes != null) {
                        out.flush();
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: image/jpeg\n\n");
                        out.flush();
                        o.write(imageInBytes);
                        o.flush();
                    }
                }
                if (uri.contains(stream)) {
                    if (imageInBytes != null) {
                        out.flush();
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"\n\n");
                        while (true) {
                            out.flush();
                            out.write("--OSMZ_boundary\n" +
                                    "Content-Type: image/jpeg\n\n");
                            out.flush();
                            o.write(imageInBytes);
                            o.flush();
                            if (!bStream) {
                                break;
                            }
                        }
                        out.write("--OSMZ_boundary");
                        out.flush();
                        o.flush();
                    }
                }

            }

            String path = "";
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File f = new File(path + uri);

            bundle.putString("type", type);
            bundle.putString("path", path + uri);
            bundle.putLong("sizeFile", f.length());
            msg.setData(bundle);
            handler.sendMessage(msg);


            if(!f.exists() && !uri.contains("cgi-bin")){
                out.write("HTTP/1.0 404 Not found\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>404 Not found</h1></body></html>");
            }
            else{
                if(f.isFile()){
                        out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: " + getMimeType(f.getAbsolutePath()) + "\n" +
                            "Content-Type: " + f.length() + "\n\n");
                        out.flush();
                        FileInputStream inputStream = new FileInputStream(f);
                        byte[] byteArray = new byte[(int) f.length()];
                        inputStream.read(byteArray);
                        o.write(byteArray);
                        o.flush();
                }
                else{
                    if(!uri.contains("cgi-bin")){
                    File d = new File(path + uri);
                    File[] files = d.listFiles();
                    String vypis = "";
                    StringBuilder builder = new StringBuilder();
                    for (File inFile : files)
                    {
                        builder.append(inFile.getName());
                        builder.append("<br>");
                    }
                    Log.d("text", vypis);
                    out.write("HTTP/1.0 404 Not found\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>Dir</h1>" +
                            "<h3>" + builder + "</h3></body></html>");
                }
                }
        }
            out.flush();
            o.flush();
            s.close();
            Log.d("SERVER", "Socket Closed");
} catch (IOException e)
        {
            if (s != null && s.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
                }
        }
        finally {
                s = null;
                semaphore.release();
                }
    }
}

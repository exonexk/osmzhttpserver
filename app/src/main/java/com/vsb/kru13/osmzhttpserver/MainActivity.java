package com.vsb.kru13.osmzhttpserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;
    private Handler handler;
    private Bundle bundle;
    private long size;
    private long sizeOverall = 0;
    private String path;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);
        Button btn3 = (Button)findViewById(R.id.button3);

        updateTextView1("");
        updateTextView2("");
        updateTextView3("");
        updateTextView4("");

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                bundle = getIntent().getExtras();
                bundle = msg.getData();
                path = bundle.getString("path");
                type = bundle.getString("type");
                size = bundle.getLong("sizeFile");
                sizeOverall += size;

                updateTextView1(path);
                updateTextView2(Long.toString(size));
                updateTextView3(Long.toString(sizeOverall));
                updateTextView4(type);

            }
        };

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), CamActivity.class);
                startActivity(intent);
            }
        });
    }

    public void updateTextView1(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(toThis);
    }

    public void updateTextView2(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText(toThis);
    }

    public void updateTextView3(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView3);
        textView.setText(toThis);
    }

    public void updateTextView4(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView10);
        textView.setText(toThis);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button1) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {
                EditText maxThread = (EditText)findViewById(R.id.editText);
                String temp = maxThread.getText().toString();
                Log.d("test", temp);

                if (temp.isEmpty())
                {
                    Toast.makeText(this, "Zadej počet vláken", Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(this,"Server zapnut s počtem vláken:" + maxThread.getText().toString(), Toast.LENGTH_LONG).show();
                    s = new SocketServer(handler,temp);
                    s.start();
                }
            }
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    EditText maxThread = (EditText)findViewById(R.id.editText);
                    String temp = maxThread.getText().toString();
                    Log.d("test", temp);

                    if (temp.isEmpty())
                    {
                        Toast.makeText(this, "Zadej počet vláken", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(this,"Server zapnut s počtem vláken:" + maxThread.getText().toString(), Toast.LENGTH_LONG).show();
                        s = new SocketServer(handler,temp);
                        s.start();
                    }

                }
                break;

            default:
                break;
        }
    }
}

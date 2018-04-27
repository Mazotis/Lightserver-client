package com.mazotis.lightserverclient;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.os.Handler;
import android.widget.ToggleButton;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Arrays;
import org.json.*;

public class LightSettings extends AppCompatActivity {

    List<String> stateList;
    List<String> requestedState;
    Handler queuedRequest = new Handler();
    boolean queuedRequestLock = false;
    boolean connectLock = true;
    boolean playbulbLinkLock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_settings);
        final Button connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new handshakeThread().execute();
            }
        });
        final Switch plantPbSwitch = findViewById(R.id.plantPbSwitch);
        final Switch tvPbSwitch = findViewById(R.id.tvPbSwitch);
        final Switch sofaPbSwitch = findViewById(R.id.sofaPbSwitch);
        Switch avantMlSwitch = findViewById(R.id.avantMlSwitch);
        Switch arriereMlSwitch = findViewById(R.id.arriereMlSwitch);
        ToggleButton linkPbBtn = findViewById(R.id.linkPbBtn);
        linkPbBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    if (isChecked) {
                        playbulbLinkLock = true;
                        plantPbSwitch.setEnabled(false);
                        plantPbSwitch.setVisibility(View.INVISIBLE);
                        tvPbSwitch.setText("Playbulbs");
                        sofaPbSwitch.setEnabled(false);
                        sofaPbSwitch.setVisibility(View.INVISIBLE);
                    } else {
                        playbulbLinkLock = false;
                        plantPbSwitch.setEnabled(true);
                        plantPbSwitch.setVisibility(View.VISIBLE);
                        sofaPbSwitch.setEnabled(true);
                        sofaPbSwitch.setVisibility(View.VISIBLE);
                        tvPbSwitch.setText("Télévision");
                    }
                }
            }
        });
        plantPbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    setRequestedValue("plantPbValue", isChecked ? "1" : "0");
                }
            }
        });
        tvPbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    if (playbulbLinkLock) {
                        setRequestedValue("plantPbValue", isChecked ? "1" : "0");
                        setRequestedValue("tvPbValue", isChecked ? "1" : "0");
                        setRequestedValue("sofaPbValue", isChecked ? "1" : "0");
                    } else {
                        setRequestedValue("tvPbValue", isChecked ? "1" : "0");
                    }
                }
            }
        });
        sofaPbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    setRequestedValue("sofaPbValue", isChecked ? "1" : "0");
                }
            }
        });
        avantMlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    setRequestedValue("avantMlValue", isChecked ? "1" : "0");
                }
            }
        });
        arriereMlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    setRequestedValue("arriereMlValue", isChecked ? "1" : "0");
                }
            }
        });

        new handshakeThread().execute();
    }

    protected void setRequestedValue(String lightString, String lightValue) {
        System.out.println("Setting " + lightString + " to value " + lightValue);
        switch (lightString) {
            case "sofaPbValue":
                requestedState.set(0, lightValue);
                break;
            case "tvPbValue":
                requestedState.set(1, lightValue);
                break;
            case "plantPbValue":
                requestedState.set(2, lightValue);
                break;
            case "avantMlValue":
                requestedState.set(3, lightValue);
                break;
            case "arriereMlValue":
                requestedState.set(4, lightValue);
                break;
            default:
                System.out.println("Light name " + lightString + " does not exist");
                break;
        }

        System.out.println("Current state: " + stateList.toString() + ". Requested state: " + requestedState.toString());

        if (queuedRequestLock == false) {
            queuedRequest.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final sendDataThread lightDataThread = new sendDataThread();
                    lightDataThread.execute();
                }
            }, 200);
        }

        queuedRequestLock = true;
    }

    private class handshakeThread extends AsyncTask<Void, Void, List> {

        @Override
        protected void onPreExecute() {
            connectLock = true;
            Button connectBtn = findViewById(R.id.connectBtn);
            connectBtn.setVisibility(View.INVISIBLE);
            TextView connectText = findViewById(R.id.connectText);
            connectText.setText("Connection en cours...");
            connectText.setTextColor(getColor(android.R.color.black));
            ProgressBar connectProgressBar = findViewById(R.id.connectProgress);
            connectProgressBar.setVisibility(View.VISIBLE);
            ScrollView bulbsScrollView = findViewById(R.id.bulbsScrollView);
            bulbsScrollView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected List doInBackground(Void ... voids) {
            try {
                Socket soc = new Socket("192.168.1.50", 1111);
                PrintWriter sockWrite = new PrintWriter(soc.getOutputStream(), true);
                sockWrite.print("getstate");
                sockWrite.flush();
                BufferedReader sockRead = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                String lState = sockRead.readLine();
                stateList = Arrays.asList(lState.substring(1,lState.length()-1).split("\\s*,\\s*"));
                requestedState = stateList;
                sockRead.close();
                soc.close();
            } catch (Exception e) {
                System.out.println(e);
                stateList = null;
            }

            return stateList;
        }

        @Override
        protected void onPostExecute(List result) {
            if (result != null) {
                System.out.println (result);
                TextView connectText = findViewById(R.id.connectText);
                connectText.setText("Connecté");
                connectText.setTextColor(getColor(android.R.color.background_light));
                ProgressBar connectProgressBar = findViewById(R.id.connectProgress);
                connectProgressBar.setVisibility(View.INVISIBLE);
                Button connectBtn = findViewById(R.id.connectBtn);
                connectBtn.setVisibility(View.INVISIBLE);
                ScrollView bulbsScrollView = findViewById(R.id.bulbsScrollView);
                bulbsScrollView.setVisibility(View.VISIBLE);

                //Switches
                if (!result.get(0).toString().equals('0')) {
                    Switch sofaPbSwitch = findViewById(R.id.sofaPbSwitch);
                    sofaPbSwitch.setChecked(true);
                }
                if (!result.get(1).toString().equals('0')) {
                    Switch tvPbSwitch = findViewById(R.id.tvPbSwitch);
                    tvPbSwitch.setChecked(true);
                }
                if (!result.get(2).toString().equals('0')) {
                    Switch plantPbSwitch = findViewById(R.id.plantPbSwitch);
                    plantPbSwitch.setChecked(true);
                }
                if (!result.get(3).toString().equals('0')) {
                    Switch avantMlSwitch = findViewById(R.id.avantMlSwitch);
                    avantMlSwitch.setChecked(true);
                }
                if (!result.get(4).toString().equals('0')) {
                    Switch arriereMlSwitch = findViewById(R.id.arriereMlSwitch);
                    arriereMlSwitch.setChecked(true);
                }

                connectLock = false;
            } else {
                TextView connectText = findViewById(R.id.connectText);
                connectText.setText("Connection impossible");
                connectText.setTextColor(getColor(android.R.color.holo_red_dark));
                ProgressBar connectProgressBar = findViewById(R.id.connectProgress);
                connectProgressBar.setVisibility(View.INVISIBLE);
                Button connectBtn = findViewById(R.id.connectBtn);
                connectBtn.setVisibility(View.VISIBLE);
            }

            return;
        }
    }

    private class sendDataThread extends AsyncTask<Void, Void, Void> {

        Switch plantPbSwitch = findViewById(R.id.plantPbSwitch);
        Switch tvPbSwitch = findViewById(R.id.tvPbSwitch);
        Switch sofaPbSwitch = findViewById(R.id.sofaPbSwitch);
        Switch avantMlSwitch = findViewById(R.id.avantMlSwitch);
        Switch arriereMlSwitch = findViewById(R.id.arriereMlSwitch);

        @Override
        protected void onPreExecute() {
            connectLock = true;
            plantPbSwitch.setEnabled(false);
            tvPbSwitch.setEnabled(false);
            sofaPbSwitch.setEnabled(false);
            avantMlSwitch.setEnabled(false);
            arriereMlSwitch.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void...voids) {
            System.out.println("sending query");
            try {
                Socket soc = new Socket("192.168.1.50", 1111);
                PrintWriter sockWrite = new PrintWriter(soc.getOutputStream(), true);
                JSONObject JSONreq = new JSONObject();
                JSONreq.put("playbulb", requestedState.get(0) + ',' + requestedState.get(1) + ',' + requestedState.get(2));
                JSONreq.put("milight", requestedState.get(3) + ',' + requestedState.get(4));
                sockWrite.print(JSONreq);
                sockWrite.flush();
                sockWrite.close();
                soc.close();
            } catch (Exception e) {
                System.out.println("Error in query");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            connectLock = false;
            queuedRequestLock = false;
            plantPbSwitch.setEnabled(true);
            tvPbSwitch.setEnabled(true);
            sofaPbSwitch.setEnabled(true);
            avantMlSwitch.setEnabled(true);
            arriereMlSwitch.setEnabled(true);
        }
    }
}

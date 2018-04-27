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
import android.os.Looper;

import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.List;
import java.util.Arrays;
import org.json.*;

public class LightSettings extends AppCompatActivity {

    List<String> stateList;
    boolean connectLock = true;

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
        Switch plantPbSwitch = findViewById(R.id.plantPbSwitch);
        Switch tvPbSwitch = findViewById(R.id.tvPbSwitch);
        Switch sofaPbSwitch = findViewById(R.id.sofaPbSwitch);
        Switch avantMlSwitch = findViewById(R.id.avantMlSwitch);
        Switch arriereMlSwitch = findViewById(R.id.arriereMlSwitch);
        plantPbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    final sendDataThread lightDataThread = new sendDataThread();
                    lightDataThread.setValue("plantPbValue", isChecked ? "1" : "0");
                }
            }
        });
        tvPbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    final sendDataThread lightDataThread = new sendDataThread();
                    lightDataThread.setValue("tvPbValue", isChecked ? "1" : "0");
                }
            }
        });
        sofaPbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    final sendDataThread lightDataThread = new sendDataThread();
                    lightDataThread.setValue("sofaPbValue", isChecked ? "1" : "0");
                }
            }
        });
        avantMlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    final sendDataThread lightDataThread = new sendDataThread();
                    lightDataThread.setValue("avantMlValue", isChecked ? "1" : "0");
                }
            }
        });
        arriereMlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!connectLock) {
                    final sendDataThread lightDataThread = new sendDataThread();
                    lightDataThread.setValue("arriereMlValue", isChecked ? "1" : "0");
                }
            }
        });

        new handshakeThread().execute();
    }

    private void timedSwitch() {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here
                    }
                },
                5000
        );
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
                BufferedReader sockRead = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                String lState = sockRead.readLine();
                stateList = Arrays.asList(lState.substring(1,lState.length()-1).split("\\s*,\\s*"));
                sockRead.close();
                soc.close();
            } catch (Exception e) {
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
                System.out.println("Comparing result.get: " + result.get(0) + " with '0'");
                if (!result.get(0).toString().equals(0)) {
                    System.out.println("is on");
                    Switch sofaPbSwitch = findViewById(R.id.sofaPbSwitch);
                    sofaPbSwitch.setChecked(true);
                }
                if (!result.get(1).toString().equals(0)) {
                    Switch tvPbSwitch = findViewById(R.id.tvPbSwitch);
                    tvPbSwitch.setChecked(true);
                }
                if (!result.get(2).toString().equals(0)) {
                    Switch plantPbSwitch = findViewById(R.id.plantPbSwitch);
                    plantPbSwitch.setChecked(true);
                }
                if (!result.get(3).toString().equals(0)) {
                    Switch avantMlSwitch = findViewById(R.id.avantMlSwitch);
                    avantMlSwitch.setChecked(true);
                }
                if (!result.get(4).toString().equals(0)) {
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
        String sofaPbValue = stateList.get(0);
        String tvPbValue = stateList.get(1);
        String plantPbValue = stateList.get(2);
        String avantMlValue = stateList.get(3);
        String arriereMlValue = stateList.get(4);

        public void setValue(String lightString, String lightValue) {
            System.out.println("Setting " + lightString + " to value " + lightValue);
            switch (lightString) {
                case "sofaPbValue":
                    sofaPbValue = lightValue;
                    stateList.set(0, lightValue);
                    break;
                case "tvPbValue":
                    tvPbValue = lightValue;
                    stateList.set(1, lightValue);
                    break;
                case "plantPbValue":
                    plantPbValue = lightValue;
                    stateList.set(2, lightValue);
                    break;
                case "avantMlValue":
                    avantMlValue = lightValue;
                    stateList.set(3, lightValue);
                    break;
                case "arriereMlValue":
                    arriereMlValue = lightValue;
                    stateList.set(4, lightValue);
                    break;
                default:
                    System.out.println("Light name " + lightString + " does not exist");
                    break;
            }

            System.out.println("Current state: [" + sofaPbValue + "," + tvPbValue + "," + plantPbValue + "," + avantMlValue + "," + arriereMlValue + "]");
            this.execute();
        }

        @Override
        protected void onPreExecute() {
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
                JSONreq.put("playbulb", sofaPbValue + ',' + tvPbValue + ',' + plantPbValue);
                JSONreq.put("milight", avantMlValue + ',' + arriereMlValue);
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
            plantPbSwitch.setEnabled(true);
            tvPbSwitch.setEnabled(true);
            sofaPbSwitch.setEnabled(true);
            avantMlSwitch.setEnabled(true);
            arriereMlSwitch.setEnabled(true);
        }
    }
}

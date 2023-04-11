package com.example.pozoriste;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Api {
    //za dovlacenje JSONa sa API-ja
    public static void getJSON(String url, ReadDataHandler rdh){
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String response = "";

                try{
                    URL url = new URL(strings[0]);
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String red;
                    while((red = br.readLine()) != null){
                        response += red + "\n";
                    }
                    br.close();
                    //con.disconnect();
                } catch (Exception e){
                    response = "[]";
                }

                return response;
            }

            @Override
            protected void onPostExecute(String response) {
                rdh.setJson(response);
                rdh.sendEmptyMessage(0); //ovim okidamo handleMessage
            }
        };

        task.execute(url);
    }
}

package ua.nure.solodovnik.pomodoro_iot_mobile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JSONArray j = null;
        new DemoTask().execute();

        Pusher pusher = new Pusher("1991c289a458393cc0e0");
        pusher.connect();
        Channel channel = pusher.subscribe("android_channel");

        final Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        message.obj.toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        };


        channel.bind("test_event", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
                Message message = mHandler.obtainMessage(1, data);
                message.sendToTarget();
            }

        });
    }


    class DemoTask extends AsyncTask<Void, Void, Integer> {

        JSONArray data;
        protected Integer doInBackground(Void... arg0)  {
            try {
                data = getJSONObjectFromURL("https://pomodoro-iot-api.herokuapp.com//tasks.json");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(Integer result) {
            ListView lv = (ListView) findViewById(R.id.mainListView);
            List<String> listContents = new ArrayList<String>(data.length());
            for (int i = 0; i < data.length(); i++)
            {
                try {
                    listContents.add(data.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            List<String> list = new ArrayList<String>();
            for(int i = 0; i < data.length(); i++){
                try {
                    list.add(data.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter listAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.raw, list);
            lv.setAdapter(listAdapter);
            Logger.getAnonymousLogger().log(Level.INFO, data.toString());
            return;
        }
    }

    public static JSONArray getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();

        jsonString = sb.toString();

        System.out.println("JSON: " + jsonString);

        return new JSONArray(jsonString);
    }

}

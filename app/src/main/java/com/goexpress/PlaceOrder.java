package com.goexpress;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlaceOrder extends AppCompatActivity {

    EditText name,number,company,country,pincode,address,num_boxes,vol_wt,pay_type,note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);


        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        company = findViewById(R.id.company);
        country = findViewById(R.id.country);
        pincode = findViewById(R.id.pincode);
        address = findViewById(R.id.address);
        num_boxes = findViewById(R.id.num_boxes);
        vol_wt = findViewById(R.id.vol_wt);
        pay_type = findViewById(R.id.pay_type);
        note = findViewById(R.id.note);

    }

    public class ApiSubmit extends AsyncTask<String, String, String> {

        ProgressDialog progress = new ProgressDialog(PlaceOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("Sending Data...\nPlease wait");
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                java.net.URL url = new URL(new GlobalUrl().LINK+"pace/api/bookorderdata.php");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
//set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
// json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
//input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();

//response data

                return JsonResponse;


            } catch (IOException /*| JSONException*/ e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return JsonResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("ResponseJson", "Reached" + s);

            progress.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if(jsonObject.getString("statusCode").equals("01"))
                    {
                        //addNotification("Success","Order has been sent to admin");
                        Intent it = new Intent(getApplicationContext(),OrderCompleted.class);
                        it.putExtra("key",1);
                        it.putExtra("order_id",jsonObject.getString("order_id"));
                        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(it);
                        finishAffinity();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(PlaceOrder.this, "Connection Error", Toast.LENGTH_SHORT).show();
        }
    }

}
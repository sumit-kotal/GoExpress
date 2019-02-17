package com.goexpress;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlaceOrder extends AppCompatActivity {

    EditText name,number,company,address,pincode,num_boxes,vol_wt,note;

    AutoCompleteTextView country;
    Spinner pay_type;

    String selectedCountry = "",selectedPayment = "";

    int uid;
    JSONObject dataReceived;

    ImageView submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        SharedPreferences LOGIN = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        uid = LOGIN.getInt("uid",0);

        try {
            dataReceived = new JSONObject(getIntent().getStringExtra("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Data received ",dataReceived.toString());


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

        String[] countries =getResources().getStringArray(R.array.countries);
        ArrayAdapter autocompletetextAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line, countries);

        country.setAdapter(autocompletetextAdapter);

        country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("selectedCountry++",adapterView.getItemAtPosition(i).toString());
                selectedCountry = adapterView.getItemAtPosition(i).toString();
            }
        });



        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject data = new JSONObject();
                selectedPayment = pay_type.getSelectedItem().toString();
                try {
                    data.put("uid",uid);
                    data.put("consignee_name",name.getText().toString());
                    data.put("consignee_contact",number.getText().toString());
                    data.put("consignee_company",company.getText().toString());
                    data.put("consignee_country",selectedCountry);
                    data.put("consignee_pincode",pincode.getText().toString());
                    data.put("consignee_addr",address.getText().toString());
                    data.put("nobox",num_boxes.getText().toString());
                    data.put("weight",vol_wt.getText().toString());
                    data.put("pay_type",selectedPayment);
                    data.put("note",note.getText().toString());
                    data.put("customer_id",dataReceived.getString("uid"));
                    if (dataReceived.has("city")) data.put("cust_city",dataReceived.getString("city"));
                    if (dataReceived.has("state")) data.put("cust_state",dataReceived.getString("state"));
                    if (dataReceived.has("country")) data.put("cust_country",dataReceived.getString("country"));
                    if (dataReceived.has("name")) data.put("cust_name",dataReceived.getString("name"));
                    if (dataReceived.has("company")) data.put("cust_company",dataReceived.getString("company"));
                    if (dataReceived.has("phone")) data.put("cust_contact",dataReceived.getString("phone"));
                    if (dataReceived.has("email")) data.put("cust_email",dataReceived.getString("email"));
                    if (dataReceived.has("address_line1")) data.put("cust_addr",dataReceived.getString("address_line1"));
                    if (dataReceived.has("pincode")) data.put("$cust_pincode",dataReceived.getString("pincode"));


                    Log.d("Printing Data ",data.toString());

                    new ApiSubmit().execute(data.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

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
                java.net.URL url = new URL(new GlobalUrl().LINK+"book_order.php");
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

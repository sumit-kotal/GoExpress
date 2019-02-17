package com.goexpress;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViewOrderActivity extends AppCompatActivity {

    String order_id;
    TextView cust_name,awb,payment,name,number,company,country,pincode,address,note,num_boxes,weight;

    LinearLayout mainLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        setTitle("AWB Details");

        order_id = getIntent().getStringExtra("order_id");

        mainLinear = findViewById(R.id.mainLinear);
        cust_name = findViewById(R.id.cust_name);
        awb = findViewById(R.id.awb);
        payment = findViewById(R.id.payment);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        company = findViewById(R.id.company);
        country = findViewById(R.id.country);
        pincode = findViewById(R.id.pincode);
        address = findViewById(R.id.address);

        num_boxes = findViewById(R.id.num_boxes);
        weight = findViewById(R.id.weight);

        note = findViewById(R.id.note);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("order_id",order_id);

            new ApiLogin().execute(jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("StaticFieldLeak")
    public class ApiLogin extends AsyncTask<String, String, String> {

        ProgressDialog progress = new ProgressDialog(ViewOrderActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("Checking...\nPlease wait");
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
                java.net.URL url = new URL(new GlobalUrl().LINK+"getsingleorderdetails.php");
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

        @SuppressLint("ApplySharedPref")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("Login", " - " + s);

            progress.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    if(jsonObject.getString("statusCode").equals("01"))
                    {

                        JSONObject data = jsonObject.getJSONObject("data");

                        cust_name.setText(data.getString("consignor_name"));
                        awb.setText(data.getString("awb_no"));
                        payment.setText(data.getString("pay_type"));

                        name.setText(data.getString("name_to"));
                        number.setText(data.getString("contact_to"));
                        company.setText(data.getString("company_to"));

                        country.setText(data.getString("con_country"));
                        pincode.setText(data.getString("con_pincode"));
                        address.setText(data.getString("con_address1"));

                        num_boxes.setText(data.getString("num_boxes"));
                        weight.setText(data.getString("vol_weight"));

                        note.setText(data.getString("note"));

                    }
                    else
                    {
                        Toast.makeText(ViewOrderActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(ViewOrderActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(this,OrdersActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.order_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // display a message when a button was pressed
        if (item.getItemId() == R.id.share) {
            Bitmap bitmap = getBitmapFromView(mainLinear);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(this, bitmap));
            shareIntent.setType("image/jpeg");
            startActivity(Intent.createChooser(shareIntent, "Share"));

        }
        return true;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                    inImage, "", "");
            return Uri.parse(path);
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }


    private Bitmap getBitmapFromView(LinearLayout view) {
        try {

            view.setDrawingCacheEnabled(true);

            view.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            view.buildDrawingCache(true);
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getDrawingCache());

            //Define a bitmap with the same size as the view
            view.setDrawingCacheEnabled(false);

            return returnedBitmap;
        }catch (Exception e){
            Log.e("getBitmapFromView", e.getMessage());
        }
        return null;
    }
}

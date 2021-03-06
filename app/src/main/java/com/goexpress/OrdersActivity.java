package com.goexpress;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OrdersActivity extends AppCompatActivity {

    SharedPreferences LOGIN;
    int uid;
    ArrayList<OrderLogs> orderLogsList;
    ListView listView;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        setTitle("AWB List");

        LOGIN = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        uid = LOGIN.getInt("uid",0);
        orderLogsList = new ArrayList<>();
        listView = findViewById(R.id.listView);
        fab = findViewById(R.id.fab);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid",uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new ApiGetLogs().execute(jsonObject.toString());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OrdersActivity.this,CustomerListActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // display a message when a button was pressed
        if (item.getItemId() == R.id.logout) {
            SharedPreferences.Editor editor = LOGIN.edit();
            editor.clear();
            editor.commit();
            Intent it = new Intent(OrdersActivity.this,LoginActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(it);

        }
        return true;
    }




    public class ApiGetLogs extends AsyncTask<String, String, String> {

        ProgressDialog progress = new ProgressDialog(OrdersActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("Fetching Data...\nPlease wait");
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
                java.net.URL url = new URL(new GlobalUrl().LINK+"getorderbyuser.php");
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
            Log.d("OrderByUser", "Reached" + s);

            progress.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        OrderLogs orderLogs = new OrderLogs();
                        orderLogs.setOrder_id(jsonArray.getJSONObject(i).getString("OrderId"));
                        orderLogs.setAwb_no(jsonArray.getJSONObject(i).getString("awb_no"));
                        orderLogs.setDate(getDatetoLocal(jsonArray.getJSONObject(i).getString("created_at")));
                        orderLogsList.add(orderLogs);
                    }

                    ConfirmationAdapter adapter = new ConfirmationAdapter(OrdersActivity.this,R.layout.order_item, orderLogsList);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(OrdersActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
        }
    }



    public class ConfirmationAdapter extends ArrayAdapter<OrderLogs> {

        LayoutInflater vi;
        ViewHolder holder;
        ArrayList<OrderLogs> postList;

        public ConfirmationAdapter(Context context, int resource, ArrayList<OrderLogs> objects) {
            super(context, resource, objects);
            vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            postList = objects;
            //Log.d("ArraySizePost", objects.size() + " ");
        }

        @NonNull
        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            // convert view = design

            Log.d("pos", String.valueOf(position));
            View v = convertView;
            if (v == null) {

                holder = new ViewHolder();
                //v = vi.inflate(Resource, null);
                v = vi.inflate(R.layout.order_item, null);
                holder.order_no = v.findViewById(R.id.order_no);
                holder.order_date = v.findViewById(R.id.order_time);

                holder.details = v.findViewById(R.id.details);

                holder.indicator = v.findViewById(R.id.indicator);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.order_date.setText(postList.get(position).getDate());

            holder.order_no.setText(postList.get(position).getAwb_no());

           // holder.order_no.setText(postList.get(position).getOrder_id());



            //

            holder.details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent it = new Intent(OrdersActivity.this,ViewOrderActivity.class);
                    it.putExtra("order_id",postList.get(position).getOrder_id());
                    startActivity(it);

                }
            });

            return v;
        }

        class ViewHolder {
            TextView order_no,order_date;
            Button details;
            View indicator;
        }

    }


    class OrderLogs
    {
        String order_id;


        String awb_no;

        public String getAwb_no() {
            return awb_no;
        }

        public void setAwb_no(String awb_no) {
            this.awb_no = awb_no;
        }

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        String date;
    }

    public String getDatetoLocal(String dt){

        String formattedDate = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(dt);
            df.setTimeZone(TimeZone.getDefault());
            formattedDate = df.format(date);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.d("Formatted Date",formattedDate);

        return formattedDate;
    }
}

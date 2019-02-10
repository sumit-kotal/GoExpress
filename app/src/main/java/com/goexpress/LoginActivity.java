package com.goexpress;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText userid,password;
    Button sign_in_button;//,sign_up_button;

    SharedPreferences LOGIN;
    WebView mWebview ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userid = findViewById(R.id.userid);
        password = findViewById(R.id.password);

        sign_in_button = findViewById(R.id.sign_in_button);
        //sign_up_button = findViewById(R.id.sign_up_button);
        LOGIN = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

        if (LOGIN.getInt("uid", 0) != 0 && LOGIN.getString("name", null) != null) {
            startActivity(new Intent(getApplicationContext(),OrdersActivity.class));
            finish();
        }


        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", userid.getText().toString());
                        jsonObject.put("password", password.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("JsonSent", jsonObject.toString());
                    new ApiLogin().execute(jsonObject.toString());
            }
        });

       /* sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                //startActivity(new Intent(LoginActivity.this,SignUp.class));
            }
        });*/

    }



    @SuppressLint("StaticFieldLeak")
    public class ApiLogin extends AsyncTask<String, String, String> {

        ProgressDialog progress = new ProgressDialog(LoginActivity.this);

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
                java.net.URL url = new URL(new GlobalUrl().LINK+"pace/api/login.php");
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

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if(jsonObject.getString("statusCode").equals("01"))
                    {
                        String role = jsonObject.getString("role").toLowerCase();
                        if (!role.equals("admin")) {
                            SharedPreferences.Editor editor = LOGIN.edit();
                            editor.putInt("uid", jsonObject.getInt("uid"));
                            editor.putString("name", jsonObject.getString("name"));
                            editor.putString("email", jsonObject.getString("email"));
                            editor.putString("phone", jsonObject.getString("phone"));
                            editor.putString("company", jsonObject.getString("company"));
                            editor.putString("role", jsonObject.getString("role"));
                            editor.commit();
                            startActivity(new Intent(getApplicationContext(), OrdersActivity.class));
                            finish();
                        }
                        else
                            Toast.makeText(LoginActivity.this, "You are not authorized to login into this application", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Wrong username or password.\nPlease try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(LoginActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
        }
    }

}

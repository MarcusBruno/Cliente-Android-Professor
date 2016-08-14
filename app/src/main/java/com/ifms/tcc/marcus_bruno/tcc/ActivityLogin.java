package com.ifms.tcc.marcus_bruno.tcc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityLogin extends AppCompatActivity {

    Button btnLogin;
    EditText edit_text_rp;
    EditText edit_text_senha;
    // contacts JSONArray
    JSONArray login = null;
    JSONObject status = null;
    ArrayList<HashMap<String, String>> contactList;
    public static Professor professor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnLogin = (Button) findViewById(R.id.button_login);
        edit_text_rp = (EditText) findViewById(R.id.edit_text_login_rp);
        edit_text_senha = (EditText) findViewById(R.id.edit_text_login_senha);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AutenticarLogin().execute();
            }
        });

        contactList = new ArrayList<HashMap<String, String>>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AutenticarLogin extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            //Codigo
        }

        @Override
        protected Integer doInBackground(String... params) {
            //Codigo


            String url = "http://192.168.1.9:8000/todo/login/professor/";
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("rp", "123"));
            pairs.add(new BasicNameValuePair("senha", "123"));

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.POST, pairs);
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                // Getting JSON Array node
                login = jsonObj.getJSONArray("message");

                if (status.toString().equalsIgnoreCase("1")) {

                    // Getting JSON Array node
//                    login = jsonObj.getJSONArray("message");

                    if (!login.getJSONObject(0).toString().equalsIgnoreCase("false")) {

                        // looping through All Contacts
                        for (int i = 0; i < login.length(); i++) {
                            JSONObject c = login.getJSONObject(i);
                            professor = new Professor(c.getString("rp"), c.getString("nome"), c.getString("telefone"), c.getString("email"), c.getString("mac_address"));
                        }
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
                    builder.setMessage("Problemas de comunicação com o servidor! \n Entre em contato com o suporte, obrigado!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                }
                            });
                    builder.create().show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            //Codigo
            if (professor != null) {
                Intent i = new Intent(ActivityLogin.this, ActivityDisciplinas.class);
                startActivity(i);
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
                builder.setMessage("Registro de Professor ou Senha incorretos!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                            }
                        });
                builder.create().show();

            }
        }

        protected void onProgressUpdate(Integer params) {
            //Codigo
        }

    }
}

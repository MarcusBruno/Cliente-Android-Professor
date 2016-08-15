package com.ifms.tcc.marcus_bruno.tcc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogin extends AppCompatActivity {

    Button btnLogin;
    EditText edit_text_rp, edit_text_senha;

    public static Professor PROFESSOR;
    private static String RP, SENHA_PROFESSOR;
    private static final String URL = "http://192.168.1.9:8000/todo/login/professor/";

    private static AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnLogin = (Button) findViewById(R.id.button_login);
        edit_text_rp = (EditText) findViewById(R.id.edit_text_login_rp);
        edit_text_senha = (EditText) findViewById(R.id.edit_text_login_senha);
        builder = new AlertDialog.Builder(ActivityLogin.this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RP = edit_text_rp.getText().toString();
                SENHA_PROFESSOR = edit_text_senha.getText().toString();
                if (!RP.equalsIgnoreCase("") && !SENHA_PROFESSOR.equalsIgnoreCase("")) {
                    new AutenticarLogin().execute();
                } else {
                    builder.setMessage("Por favor, preencha todos os campos corretamente.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }).create().show();
                }
            }
        });
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
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("rp", RP));
            pairs.add(new BasicNameValuePair("senha", SENHA_PROFESSOR));

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            try {
                // Making a request to url and getting response
                JSONObject jsonObj = new JSONObject(sh.makeServiceCall(URL, ServiceHandler.POST, pairs));

                //Get status of response;
                String status = jsonObj.getJSONArray("status").getJSONObject(0).getString("status");
                if (!status.equalsIgnoreCase("0")) {
                    // Getting data teacher of array in position 0.
                    JSONObject c = jsonObj.getJSONArray("message").getJSONObject(0);
                    PROFESSOR = new Professor(c.getString("rp"), c.getString("nome"), c.getString("telefone"), c.getString("email"), c.getString("mac_address"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            if (PROFESSOR != null) {
                Intent i = new Intent(ActivityLogin.this, ActivityDisciplinas.class);
                startActivity(i);
                finish();
            } else {
                builder.setMessage("Registro de Professor ou Senha incorretos!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                            }
                        }).create().show();
            }
        }
        protected void onProgressUpdate(Integer params) {}
    }
}

package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.R;
import com.ifms.tcc.marcus_bruno.tcc.Utils.Routes;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogin extends AppCompatActivity {

    private Button loginBtn,cadastrarBtn, recupararSenha;
    private EditText rpET, passET;
    private boolean CONEXAO;
    private ProgressDialog dialog;
    private String rp, passProfessor;
    private AlertDialog.Builder builder;
    protected static Professor PROFESSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Dexter.initialize(ActivityLogin.this);
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        cadastrarBtn = (Button) findViewById(R.id.button_cadastrar_me);
        recupararSenha = (Button) findViewById(R.id.button_recuperar_senha);
        loginBtn = (Button) findViewById(R.id.button_login);
        rpET = (EditText) findViewById(R.id.edit_text_login_rp);
        passET = (EditText) findViewById(R.id.edit_text_login_senha);
        builder = new AlertDialog.Builder(ActivityLogin.this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rp = rpET.getText().toString();
                passProfessor = passET.getText().toString();
                if (!rp.equalsIgnoreCase("") && !passProfessor.equalsIgnoreCase("")) {
                    new AutenticarLogin().execute();
                } else {
                    builder.setMessage(R.string.message_required_inputs_login)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }).create().show();
                }
            }
        });

        recupararSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(ActivityLogin.this, RecuperarSenhaActivity.class);
                startActivity(i);
            }
        });

        cadastrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent i = new Intent(ActivityLogin.this, CadastrarActivity.class);
                startActivity(i);
            }
        });
    }

    public class AutenticarLogin extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ActivityLogin.this, "", "Carregando...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("rp", rp));
            pairs.add(new BasicNameValuePair("senha", passProfessor));

            ServiceHandler sh = new ServiceHandler();
            try {
                String jsonStr = sh.makeServiceCall(Routes.getUrlLoginProfessor(), ServiceHandler.POST, pairs);
                //Tratamento em caso da conexão falhar
                if (jsonStr != null) {
                    CONEXAO = true;
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    //Tratamento em caso do objeto retornar null;
                    if (!jsonObj.equals("")) {
                        //Get status of response;
                        String status = jsonObj.getJSONArray("status").getJSONObject(0).getString("status");
                        if (!status.equalsIgnoreCase("0")) {
                            // Getting data teacher of array in position 0.
                            JSONObject c = jsonObj.getJSONArray("message").getJSONObject(0);
                            PROFESSOR = new Professor(c.getString("tb_prof_rp"), c.getString("tb_prof_nome"), c.getString("tb_prof_telefone"), c.getString("tb_prof_email"), c.getString("tb_prof_mac_address"));
                        }
                    }
                } else {
                    CONEXAO = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            dialog.dismiss();
            if (PROFESSOR != null) {
                Intent i = new Intent(ActivityLogin.this, ActivityDisciplinas.class);
                startActivity(i);
                finish();

            } else if (!CONEXAO) {
                builder.setMessage(R.string.connection_failure)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            } else {
                builder.setMessage(R.string.incorrect_data_login)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create().show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}

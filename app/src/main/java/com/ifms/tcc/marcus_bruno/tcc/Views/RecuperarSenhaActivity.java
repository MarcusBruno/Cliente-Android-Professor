package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ifms.tcc.marcus_bruno.tcc.R;
import com.ifms.tcc.marcus_bruno.tcc.Utils.Routes;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecuperarSenhaActivity extends AppCompatActivity {

    ServiceHandler sh = new ServiceHandler();
    private AlertDialog.Builder builder;
    EditText etRecuperarSenhaRP;
    Button btnRecuperarSenhaRP, btnCadastrar;
    String codigoSegurancaSistema;
    String senha;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        btnRecuperarSenhaRP = (Button) findViewById(R.id.button_recuperar_senha_rp);
        etRecuperarSenhaRP = (EditText) findViewById(R.id.edit_text_recuperar_senha_rp);

        btnRecuperarSenhaRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etRecuperarSenhaRP.getText().toString() != null && !etRecuperarSenhaRP.getText().toString().equalsIgnoreCase("")) {
                    new recuperarSenha().execute();
                }else{

                }
            }
        });

    }

    public class recuperarSenha extends AsyncTask<String, Integer, Integer> {
        private boolean CONEXAO;
        private String status = "";
        @Override
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("rp", etRecuperarSenhaRP.getText().toString()));

            try {
                JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlRecuperarSenhaProfessor(), ServiceHandler.POST, pairs));
                if (jsonObj != null) {
                    CONEXAO = true;
                    if (jsonObj.getString("status").equalsIgnoreCase("1")) {
                        status = "1";
                        codigoSegurancaSistema = jsonObj.getString("message");
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
            if(CONEXAO && status.equalsIgnoreCase("1")){
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecuperarSenhaActivity.this);
                alertDialog.setTitle("RECUPERAR SENHA");
                alertDialog.setMessage("CÓDIGO:");
                alertDialog.setCancelable(false);
                final EditText input = new EditText(RecuperarSenhaActivity.this);
                input.setSingleLine();

                int maxLength = 5;
                InputFilter[] inputFilter = new InputFilter[1];
                inputFilter[0] = new InputFilter.LengthFilter(maxLength);
                input.setFilters(inputFilter);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                alertDialog.setCancelable(true);
                                if(input.getText().toString().equalsIgnoreCase(codigoSegurancaSistema)){
                                    inserirNovaSenha();
                                }else{

                                }
                            }
                        });

                alertDialog.show();
            }
        }
    }


    public class inserirNovaSenha extends AsyncTask<String, Integer, Integer> {
        private String status = "";
        private boolean CONEXAO;
        @Override
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("rp", etRecuperarSenhaRP.getText().toString()));
            pairs.add(new BasicNameValuePair("senha", senha));
            try {
                JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlInserirSenhaProfessor(), ServiceHandler.PUT, pairs));
                if (jsonObj != null) {
                    CONEXAO = true;
                    if (jsonObj.getString("status").equalsIgnoreCase("1")) {
                        status = "1";
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
            if(CONEXAO && status.equalsIgnoreCase("1")){
                AlertDialog.Builder builder = new AlertDialog.Builder(RecuperarSenhaActivity.this);
                builder.setTitle("Nova Senha");
                builder.setMessage("Sua senha foi alterada com sucesso!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                finish();
                                Intent i = new Intent(RecuperarSenhaActivity.this, ActivityLogin.class);
                                startActivity(i);
                            }
                        }).create().show();
            }
        }
    }


    void inserirNovaSenha(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle("Nova Senha");
        final View view = inflater.inflate(R.layout.dialog_recuperar_senha, null);
        builder.setView(view);

       btnCadastrar = (Button) view.findViewById(R.id.button_cadastrar_nova_senha);
       btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText confirmarNovaSenha = (EditText) view.findViewById(R.id.edit_text_confirmar_nova_senha);
                EditText novaSenha = (EditText) view.findViewById(R.id.edit_text_nova_senha);

                if(novaSenha.getText().toString().equalsIgnoreCase(confirmarNovaSenha.getText().toString())){
                    //Envia ao banco de dados a nova senha do usuário.
                    senha = novaSenha.getText().toString();
                    new inserirNovaSenha().execute();
                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent i = new Intent(this, ActivityLogin.class);
        startActivity(i);
    }

}

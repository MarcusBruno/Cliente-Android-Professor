package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ifms.tcc.marcus_bruno.tcc.R;
import com.ifms.tcc.marcus_bruno.tcc.Utils.MacAddress;
import com.ifms.tcc.marcus_bruno.tcc.Utils.Routes;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CadastrarActivity extends AppCompatActivity {
    private boolean CONEXAO;
    private Button cadastrarBtn;
    private EditText et_nome, et_email, et_telefone, et_login_ra, et_senha, et_confirmar_senha;
    private AlertDialog.Builder builder;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cadastrarBtn = (Button) findViewById(R.id.button_cadastrar);
        et_nome = (EditText) findViewById(R.id.edit_text_nome);
        et_email = (EditText) findViewById(R.id.edit_text_email);
        et_telefone = (EditText) findViewById(R.id.edit_text_telefone);
        et_login_ra = (EditText) findViewById(R.id.edit_text_login_ra);
        et_senha = (EditText) findViewById(R.id.edit_text_senha);
        et_confirmar_senha = (EditText) findViewById(R.id.edit_text_confirmar_senha);

        cadastrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validaCampos();
            }
        });
    }

    private void validaCampos(){
        builder = new AlertDialog.Builder(CadastrarActivity.this);

        if(!et_nome.getText().toString().equalsIgnoreCase("")){
            if(!et_email.getText().toString().equalsIgnoreCase("")){
                if(!et_telefone.getText().toString().equalsIgnoreCase("")){
                    if(!et_login_ra.getText().toString().equalsIgnoreCase("")){
                        if(!et_senha.getText().toString().equalsIgnoreCase("") && !et_confirmar_senha.getText().toString().equalsIgnoreCase("")){
                            if(et_senha.getText().toString().equalsIgnoreCase(et_confirmar_senha.getText().toString())){

                                new AutenticarLogin().execute();
                            }else{
                                //Senhas Diferentes
                                builder.setMessage("As senhas não coincidem")
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {}
                                        }).create().show();
                            }
                        }else{
                            //Campo Senha e Confirmar Senha devem ser iguais
                            builder.setMessage("As senha deve ser preenchida e confirmada.")
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {}
                                    }).create().show();
                        }
                    }else {
                        //O RA não está preenchido
                        builder.setMessage("Registro do Aluno não está preenchido.")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {}
                                }).create().show();
                    }
                }else{
                    //A senha não está preenchida
                    builder.setMessage("Telefone não está preenchido.")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            }).create().show();
                }
            }else{
                // O email não está preenchido
                builder.setMessage("Email não está preenchido corretamente.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        }).create().show();
            }
        }else{
            //O Nome não está preenchido
            builder.setMessage("O nome não está preenchido")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    }).create().show();
        }
    }

    public class AutenticarLogin extends AsyncTask<String, Integer, Integer> {
        String status;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CadastrarActivity.this, "", "Carregando...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("tb_alu_nome", et_nome.getText().toString()));
            pairs.add(new BasicNameValuePair("tb_alu_email", et_email.getText().toString()));
            pairs.add(new BasicNameValuePair("tb_alu_telefone", et_telefone.getText().toString()));
            pairs.add(new BasicNameValuePair("tb_alu_ra", et_login_ra.getText().toString()));
            pairs.add(new BasicNameValuePair("tb_usu_ra", et_login_ra.getText().toString()));
            pairs.add(new BasicNameValuePair("tb_usu_senha", et_senha.getText().toString()));
            pairs.add(new BasicNameValuePair("tb_alu_mac_address", MacAddress.getValueMacAddres()));
            pairs.add(new BasicNameValuePair("tb_usu_situacao","Ativo"));
            pairs.add(new BasicNameValuePair("tb_usu_tipo", "Aluno"));
            pairs.add(new BasicNameValuePair("tb_usu_ultimo_acesso", "CURRENT_TIMESTAMP"));

            ServiceHandler sh = new ServiceHandler();
            try {
                String jsonStr = sh.makeServiceCall(Routes.getUrlCadastrarProfessor(), ServiceHandler.POST, pairs);
                //Tratamento em caso da conexão falhar
                if (jsonStr != null) {
                    CONEXAO = true;
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    //Tratamento em caso do objeto retornar null;
                    if (!jsonObj.equals("")) {
                        //Get status of response;
                        status = jsonObj.getString("status");
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
            dialog.cancel();
            builder = new AlertDialog.Builder(CadastrarActivity.this);
            if (CONEXAO && status.equalsIgnoreCase("1")){
                builder.setMessage("Cadastro Realizado com sucesso!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(CadastrarActivity.this, ActivityLogin.class);
                                startActivity(i);
                            }
                        }).create().show();
            }else if(CONEXAO && status.equalsIgnoreCase("0")){
                builder.setMessage("Falha no cadastro! \n Você não pode se cadastrar com um celular que já está cadastrado com outro usuário.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        }).create().show();
            }else if(CONEXAO && status.equalsIgnoreCase("-1")){
                builder.setMessage("Ocorreu um erro no processo de cadastro, por favor entre em contato com o suporte!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        }).create().show();
            }else if(CONEXAO && status.equalsIgnoreCase("-0")){
                builder.setMessage("Usuário já cadastrado!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        }).create().show();
            }else if(!CONEXAO){
                builder.setMessage("Falha de comunicação com o servidor. Tente novamente!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        }).create().show();
            }
        }
    }

    @Override
    public void onBackPressed() {

        builder = new AlertDialog.Builder(CadastrarActivity.this);
            builder.setMessage("Sair da tela de cadastro?")
                    .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent i = new Intent(CadastrarActivity.this, ActivityLogin.class);
                            startActivity(i);
                        }
                    }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {}
            }).create().show();
    }

}

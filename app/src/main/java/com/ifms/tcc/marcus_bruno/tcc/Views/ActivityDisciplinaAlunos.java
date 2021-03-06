package com.ifms.tcc.marcus_bruno.tcc.Views;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ifms.tcc.marcus_bruno.tcc.Models.Aluno;
import com.ifms.tcc.marcus_bruno.tcc.Models.Disciplina;
import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.R;
import com.ifms.tcc.marcus_bruno.tcc.Utils.DetectaConexao;
import com.ifms.tcc.marcus_bruno.tcc.Utils.Routes;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityDisciplinaAlunos extends AppCompatActivity  {

    private String idFrequency; //Id do diário. Cada chamada aberta recebe um ID do diário.
    private Disciplina disciplina;
    private AlertDialog.Builder builder;
    private ServiceHandler sh = new ServiceHandler();
    private MenuItem closeFrequencyAction, openFrequencyAction;
    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;
    private String concatStrAlunosPresentes ="\"\"";
    private boolean openFrequency;
    private Timer timerBuscarAutenticacao, timerDuracaoChamada;
    private ListView alunosLV;
    private ArrayList<Integer> seletedItems; //Array de alunos selecionados da lista para dar presença de forma manual.
    //alunosAdapter = Lista de alunos do tipo String (ID: Nome).
    ArrayList<String> alunosAdapter, alunosAdapterAux,presentes, ausentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplina_alunos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        disciplina = (Disciplina) i.getSerializableExtra("disciplina");
        alunosLV = (ListView) findViewById(R.id.list_view_lista_alunuos_disciplina);
        presentes = new ArrayList<>();
        ausentes = new ArrayList<>();

        if (!new DetectaConexao(ActivityDisciplinaAlunos.this).existeConexao() || !new DetectaConexao(ActivityDisciplinaAlunos.this).localizacaoAtiva()) {
            builder.setMessage("Você deve ativar sua conexão com a internet e a localização do seu aparelho!")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                            startActivity(i);
                        }
                    }).create().show();
        }else{
            new getAlunosDaDisciplina().execute();
            new abrirChamada().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disciplina_alunos, menu);
        closeFrequencyAction = menu.findItem(R.id.closeFrequency).setVisible(true);
        openFrequencyAction = menu.findItem(R.id.openFrequency).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);
        switch (item.getItemId()) {
            case R.id.closeFrequency:
                builder.setMessage(R.string.message_close_frequency)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new fecharChamada().execute();//1
                                closeFrequencyAction.setVisible(false);
                                openFrequencyAction.setVisible(true);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
                return true;
            case R.id.openFrequency:
                builder.setMessage(R.string.message_open_frequency)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new abrirChamada().execute();
                                openFrequencyAction.setVisible(false);
                                closeFrequencyAction.setVisible(true);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (openFrequency == true) {
            builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);
            builder.setMessage("A chamada será encerrada!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new fecharChamada().execute();
                            finish();
                            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                            startActivity(i);

                        }
                    }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).create().show();
        } else {
            finish();
            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
            startActivity(i);
        }
    }

    private class getAlunosDaDisciplina extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String... params) {
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("id", disciplina.getCodigo()));

                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarAlunos(), ServiceHandler.POST, param));
                alunosAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Aluno a = new Aluno(c.getString("tb_alu_ra"), c.getString("tb_alu_nome"), c.getString("tb_alu_telefone"), c.getString("tb_alu_email"), c.getString("tb_alu_mac_address"));
                    alunosAdapter.add(a.getRa() + ": " + a.getNome());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            //Implements the ArrayAdapter after get the data of Web Service.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityDisciplinaAlunos.this, android.R.layout.simple_expandable_list_item_1, alunosAdapter);
            alunosLV.setAdapter(adapter);
            alunosAdapterAux = alunosAdapter;
        }
    }

    private class abrirChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() { }

        @Override
        protected Integer doInBackground(String... params) {
            if (!openFrequency) {
                try {
                    List<NameValuePair> param = new ArrayList<NameValuePair>();
                    param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));
                    param.add(new BasicNameValuePair("disciplina", disciplina.getCodigo()));
                    param.add(new BasicNameValuePair("situacao", "1"));
                    param.add(new BasicNameValuePair("latitude", PROFESSOR.getLatitude() + ""));
                    param.add(new BasicNameValuePair("longitude", PROFESSOR.getLongitude() + ""));

                    JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlAbrirChamada(), ServiceHandler.POST, param));
                    idFrequency = jsonObj.getJSONArray("return").getJSONObject(0).getString("id");
                    if (!idFrequency.equalsIgnoreCase("0")) {
                        openFrequency = true;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            if (openFrequency == true) {
                tempoChamada();
                buscarAutenticacao();
            }
        }
    }

    private class buscarAutenticaoRealizadas extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String... params) {
            ArrayList<String> autenticados = new ArrayList<>();
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("diario", idFrequency));
            param.add(new BasicNameValuePair("alunos", concatStrAlunosPresentes));

             try {
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarAutenticacoesRealizadas(), ServiceHandler.POST, param));
                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    if(!(concatStrAlunosPresentes == "\"\"") && !c.getString("tb_lista_freq_codigo_ra").equalsIgnoreCase("")){
                        concatStrAlunosPresentes += ","+ c.getString("tb_lista_freq_codigo_ra");
                        autenticados.add(c.getString("tb_lista_freq_codigo_ra"));
                    }else{
                        concatStrAlunosPresentes = c.getString("tb_lista_freq_codigo_ra");
                        autenticados.add(c.getString("tb_lista_freq_codigo_ra"));
                    }
                }

                //Remover alunos que já computaram a presença
                for(int i=0; i<alunosAdapter.size(); i++){
                    for(int j=0; j<autenticados.size(); j++){
                        if(alunosAdapter.get(i).split(":")[0].equalsIgnoreCase(autenticados.get(j))){
                            alunosAdapterAux.remove(i);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityDisciplinaAlunos.this, android.R.layout.simple_expandable_list_item_1, alunosAdapterAux);
            alunosLV.setAdapter(adapter);

        }
    }

    private class fecharChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            concatStrAlunosPresentes = "";
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("situacao", "0"));
            param.add(new BasicNameValuePair("id", idFrequency));

            sh.makeServiceCall(Routes.getUrlFecharChamada(), ServiceHandler.PUT, param);
            openFrequency = false; //Chamada é fechada.
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);
            closeFrequencyAction.setVisible(false);
            openFrequencyAction.setVisible(false);
            builder.setMessage("Chamada concluída com sucessos! Deseja adicionar algum aluno manualmente?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alertAdcPresencaManual();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new adcPresencasManual().execute();
                }
            }).create().show();
        }
    }

    private void alertAdcPresencaManual() {
        final CharSequence[] items = alunosAdapterAux.toArray(new CharSequence[alunosAdapterAux.size()]);
        seletedItems = new ArrayList<>();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Lista de Presença - Adição Manual")
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                }).setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(ActivityDisciplinaAlunos.this, seletedItems.toString(), Toast.LENGTH_SHORT).show();
                        if(seletedItems.size() > 0) {
                            new adcPresencasManual().execute();
                        }else{
                            alertAdcPresencaManual();
                        }
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        builder.setMessage("Chamada está concluída! Deseja adicionar algum aluno manualmente?")
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        alertAdcPresencaManual();
                                    }
                                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new adcPresencasManual().execute();

                            }
                        }).create().show();
                    }
                }).create();
        dialog.show();
    }

    private class adcPresencasManual extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            if(seletedItems == null){
                seletedItems = new ArrayList<>();
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (alunosAdapterAux.size() > 0) {
                if (seletedItems != null && seletedItems.size() > 0) {
                    for (int i = 0; i < seletedItems.size(); i++) {
                        String teste = alunosAdapterAux.get(Integer.parseInt(seletedItems.get(i).toString())).split(":")[0];
                        for (int j = 0; j < alunosAdapterAux.size(); j++) {
                            if (teste.equalsIgnoreCase(alunosAdapterAux.get(j).split(":")[0])) {
                                presentes.add(alunosAdapterAux.get(j).split(":")[0]);
                                break;
                            }
                        }
                    }

                    int lengthItens = (presentes.size() - 1);
                    while (lengthItens >= 0) {
                        List<NameValuePair> param = new ArrayList<NameValuePair>();
                        param.add(new BasicNameValuePair("tb_lista_freq_codigo_ra", presentes.get(lengthItens)));
                        param.add(new BasicNameValuePair("tb_lista_freq_codigo_rp", PROFESSOR.getRp()));
                        param.add(new BasicNameValuePair("tb_lista_freq_codigo_disciplina", disciplina.getCodigo()));
                        param.add(new BasicNameValuePair("tb_lista_freq_id_diario", idFrequency));
                        param.add(new BasicNameValuePair("tb_lista_freq_latitude_aluno", PROFESSOR.getLatitude() + ""));
                        param.add(new BasicNameValuePair("tb_lista_freq_longitude_aluno", PROFESSOR.getLongitude() + ""));
                        param.add(new BasicNameValuePair("tb_lista_freq_presenca", "1"));

                        JSONObject jsonObj;
                        try {
                            jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlAdcPresencaAlunosManual(), ServiceHandler.POST, param));

                            if (!jsonObj.getString("status").equalsIgnoreCase("0")) {
                                System.out.println(jsonObj.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        lengthItens--;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            timerDuracaoChamada.cancel();
            timerBuscarAutenticacao.cancel();
            new adcFaltasManual().execute();
        }
    }

    private class adcFaltasManual extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String... params) {
            if(alunosAdapterAux.size() >0){
                if(seletedItems.size() < alunosAdapterAux.size()){
                    for(int i=0; i<alunosAdapterAux.size(); i++){
                        if(!presentes.contains(alunosAdapterAux.get(i).split(":")[0])){
                            ausentes.add(alunosAdapterAux.get(i).split(":")[0]);
                        }
                    }

                    int loop = (ausentes.size()- 1);
                    while (loop >= 0) {
                        List<NameValuePair> param = new ArrayList<NameValuePair>();
                        param.add(new BasicNameValuePair("tb_lista_freq_codigo_ra", ausentes.get(loop)));
                        param.add(new BasicNameValuePair("tb_lista_freq_codigo_rp", PROFESSOR.getRp()));
                        param.add(new BasicNameValuePair("tb_lista_freq_codigo_disciplina", disciplina.getCodigo()));
                        param.add(new BasicNameValuePair("tb_lista_freq_id_diario", idFrequency));
                        param.add(new BasicNameValuePair("tb_lista_freq_latitude_aluno", PROFESSOR.getLatitude() + ""));
                        param.add(new BasicNameValuePair("tb_lista_freq_longitude_aluno", PROFESSOR.getLongitude() + ""));
                        param.add(new BasicNameValuePair("tb_lista_freq_presenca", "0"));

                        sh.makeServiceCall(Routes.getUrlAdcPresencaAlunosManual(), ServiceHandler.POST, param);
                        loop--;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);
            builder.setMessage("Chamada concluída com sucesso!").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            timerDuracaoChamada.cancel();
                            timerBuscarAutenticacao.cancel();

                            finish();
                            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                            startActivity(i);
                        }
                    }).create().show();
            finish();
            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
            startActivity(i);
        }
    }


    private void tempoChamada(){
        timerDuracaoChamada = new Timer();
        timerDuracaoChamada.schedule(new TimerTask() {
            public void run() {
                if (openFrequency == true) {
                    new fecharChamada().execute();
                }
            }
        }, 78000);
    }

    private void buscarAutenticacao(){
        timerBuscarAutenticacao = new Timer();
        timerBuscarAutenticacao.schedule(new TimerTask() {
            @Override
            public void run() {
                //verificar atualizações.
                new buscarAutenticaoRealizadas().execute();
            }
        },0, 10000);
    }
}

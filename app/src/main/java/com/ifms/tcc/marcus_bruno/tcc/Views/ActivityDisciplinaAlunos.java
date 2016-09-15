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

public class ActivityDisciplinaAlunos extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String idFrequency; //Id do diário. Cada chamada aberta recebe um ID do diário.
    private Disciplina disciplina;
    private Timer timer = new Timer();
    private AlertDialog.Builder builder;
    private ServiceHandler sh = new ServiceHandler();
    private MenuItem closeFrequencyAction, openFrequencyAction;
    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;
    private LocationRequest loc;

    private boolean status, openFrequency;
    private GoogleApiClient mGoogleApiClient;

    private ListView alunosLV;
    private ArrayList<Aluno> alunos; //Lista de objetos do tipo aluno
    private ArrayList<String> alunosAdapter; //Lista de alunos do tipo String (ID: Nome).
    private ArrayList<Integer> seletedItems; //Array de alunos selecionados da lista para dar presença de forma manual.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplina_alunos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        disciplina = (Disciplina) i.getSerializableExtra("disciplina");
        status = (boolean) i.getSerializableExtra("status");
        alunosLV = (ListView) findViewById(R.id.list_view_lista_alunuos_disciplina);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(ActivityDisciplinaAlunos.this)
                .addOnConnectionFailedListener(ActivityDisciplinaAlunos.this).build();

        new getAlunosDaDisciplina().execute();
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
                                new fecharChamada().execute();
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

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        configuracoes();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
    }

    @Override
    public void onBackPressed() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

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

    @Override
    public void onLocationChanged(Location location) {
        PROFESSOR.setLatitude(location.getLatitude() + "");
        PROFESSOR.setLongitude(location.getLongitude() + "");
    }

    public class getAlunosDaDisciplina extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            // Creating service handler class instance
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("id", disciplina.getCodigo()));

                // Making a request to url and getting response
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarAlunos(), ServiceHandler.POST, param));
                alunos = new ArrayList<>();
                alunosAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Aluno a = new Aluno(c.getString("tb_alu_ra"), c.getString("tb_alu_nome"), c.getString("tb_alu_telefone"), c.getString("tb_alu_email"), c.getString("tb_alu_mac_address"));
                    alunos.add(a);
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
        }
    }

    public class abrirChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {

            while (PROFESSOR.getLatitude() == null || PROFESSOR.getLongitude() == null ){
                System.out.println("Carregando...");
            }
            if (!openFrequency) {
                // Creating service handler class instance
                try {
                    List<NameValuePair> param = new ArrayList<NameValuePair>();
                    param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));
                    param.add(new BasicNameValuePair("disciplina", disciplina.getCodigo()));
                    param.add(new BasicNameValuePair("situacao", "1"));
                    param.add(new BasicNameValuePair("latitude", PROFESSOR.getLatitude() + ""));
                    param.add(new BasicNameValuePair("longitude", PROFESSOR.getLongitude() + ""));

                    // Making a request to url and getting response
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
                timer.schedule(new TimerTask() {
                    public void run() {
                        if (openFrequency == true) {
                            new fecharChamada().execute();
                        }
                    }
                }, 120000);
            }
        }
    }

    public class fecharChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            timer.purge();
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
            builder.setMessage("Chamada concluída com sucesso! Deseja adicionar algum aluno manualmente?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alertAdcPresencaManual();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();
                    Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                    startActivity(i);

                }
            }).create().show();
        }
    }

    private void alertAdcPresencaManual() {
        final CharSequence[] items = alunosAdapter.toArray(new CharSequence[alunosAdapter.size()]);
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
                        new adcPresencaManual().execute();
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

                                finish();
                                Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                                startActivity(i);

                            }
                        }).create().show();
                    }
                }).create();
        dialog.show();
    }

    public class adcPresencaManual extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            timer.purge();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int lengthItens = (seletedItems.size() - 1);
            while (lengthItens >= 0) {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("tb_lista_freq_codigo_ra", alunosAdapter.get(seletedItems.get(lengthItens)).split(":")[0]));
                param.add(new BasicNameValuePair("tb_lista_freq_codigo_rp", PROFESSOR.getRp()));
                param.add(new BasicNameValuePair("tb_lista_freq_codigo_disciplina", disciplina.getCodigo()));
                param.add(new BasicNameValuePair("tb_lista_freq_id_diario", idFrequency));
                param.add(new BasicNameValuePair("tb_lista_freq_latitude_aluno", PROFESSOR.getLatitude() + ""));
                param.add(new BasicNameValuePair("tb_lista_freq_longitude_aluno", PROFESSOR.getLongitude() + ""));
                param.add(new BasicNameValuePair("tb_lista_freq_presenca", "1"));

                sh.makeServiceCall(Routes.getUrlAdcPresencaAlunosManual(), ServiceHandler.POST, param);
                lengthItens--;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);
            builder.setMessage("Chamada concluída com sucesso!")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                            startActivity(i);
                        }
                    }).create().show();
        }
    }

    void configuracoes() {

        loc = LocationRequest.create();
        loc.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        loc.setInterval(5 * 1000);
        loc.setFastestInterval(1 * 1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, loc, ActivityDisciplinaAlunos.this);


        if (status) {
            new abrirChamada().execute();
            status = false;
        }

    }
}

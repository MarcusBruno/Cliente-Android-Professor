package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.ifms.tcc.marcus_bruno.tcc.Models.Aluno;
import com.ifms.tcc.marcus_bruno.tcc.Models.Disciplina;
import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.R;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;
import com.ifms.tcc.marcus_bruno.tcc.Utils.Routes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityDisciplinaAlunos extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Location location;
    private String idFrequency;
    private Disciplina disciplina;
    private ArrayList<Aluno> alunos;
    private ArrayList<String> alunosListForAdapter;
    private GoogleApiClient mGoogleApiClient;
    private MenuItem closeFrequencyAction, openFrequencyAction;

    protected static boolean openFrequency;
    private static AlertDialog.Builder builder;
    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;

    ListView alunosListView;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    Date dateTimeInicio, dateTimeFim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplina_alunos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);
        alunosListView = (ListView) findViewById(R.id.list_view_lista_alunuos_disciplina);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();


        Intent i = getIntent();
        disciplina = (Disciplina) i.getSerializableExtra("disciplina");

        Toast.makeText(ActivityDisciplinaAlunos.this, disciplina.getNome(), Toast.LENGTH_SHORT).show();
        new getAlunosDaDisciplina().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disciplina_alunos, menu);
        closeFrequencyAction = menu.findItem(R.id.closeFrequency).setVisible(true);
        openFrequencyAction = menu.findItem(R.id.openFrequency).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.closeFrequency:
                builder.setMessage("Tem certeza que deseja encerrar a chamada ?")
                        .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new fecharChamada().execute();
                                closeFrequencyAction.setVisible(false);
                                openFrequencyAction.setVisible(true);
                            }
                        }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
                return true;
            case R.id.openFrequency:
                builder.setMessage("Tem certeza que deseja abrir a chamada ?")
                        .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new abrirChamada().execute();
                                openFrequencyAction.setVisible(false);
                                closeFrequencyAction.setVisible(true);
                            }
                        }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
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
        super.onBackPressed();  // optional depending on your needs
        Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        habilitarPosicaoAtual();
        new abrirChamada().execute();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        builder.setMessage("Houve uma falha de conexao. Tente novamente!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                        startActivity(i);
                        finish();
                    }
                }).create().show();
    }


    public class getAlunosDaDisciplina extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("id", disciplina.getCodigo()));

                // Making a request to url and getting response
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarAlunos(), ServiceHandler.POST, param));
                alunos = new ArrayList<>();
                alunosListForAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Aluno a = new Aluno(c.getString("ra"), c.getString("nome"), c.getString("telefone"), c.getString("email"), c.getString("mac_address"));
                    alunos.add(a);
                    alunosListForAdapter.add(a.getRa() + ": " + a.getNome());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityDisciplinaAlunos.this, android.R.layout.simple_expandable_list_item_1, alunosListForAdapter);
            alunosListView.setAdapter(adapter);
        }
    }

    public class abrirChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (openFrequency == false) {
                dateTimeInicio = new Date();

                ServiceHandler sh = new ServiceHandler();
                try {
                    List<NameValuePair> param = new ArrayList<NameValuePair>();
                    param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));
                    param.add(new BasicNameValuePair("disciplina", disciplina.getCodigo()));
                    param.add(new BasicNameValuePair("horario_inicio", dateFormat.format(dateTimeInicio) + " " + hourFormat.format(dateTimeInicio)));
                    param.add(new BasicNameValuePair("situacao", "1"));
                    param.add(new BasicNameValuePair("latitude", location.getLatitude() + ""));
                    param.add(new BasicNameValuePair("longitude", location.getLongitude() + ""));

                    // Making a request to url and getting response
                    JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlAbrirChamada(), ServiceHandler.POST, param));
                    idFrequency = jsonObj.getJSONArray("return").getJSONObject(0).getString("id");

                    openFrequency = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            if (openFrequency == true) {
                dateTimeFim = dateTimeInicio;
                dateTimeFim.setMinutes((dateTimeInicio.getMinutes() + 2));
            }
        }
    }

    public class fecharChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (new Date().getMinutes() < dateTimeFim.getMinutes()) {
                dateTimeFim = new Date();
            }
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("horario_fim", dateFormat.format(dateTimeFim) + " " + hourFormat.format(dateTimeFim)));
                param.add(new BasicNameValuePair("situacao", "0"));
                param.add(new BasicNameValuePair("id", idFrequency));

                // Making a request to url and getting response
                JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlFecharChamada(), ServiceHandler.PUT, param));

                openFrequency = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
        }
    }


    private void habilitarPosicaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }
}

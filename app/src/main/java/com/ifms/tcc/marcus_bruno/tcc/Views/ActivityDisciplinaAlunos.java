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
import android.util.Log;
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
import com.ifms.tcc.marcus_bruno.tcc.Utils.Routes;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

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

    private Disciplina disciplina;
    private boolean openFrequency;
    private String idFrequency;
    private ListView alunosLV;
    private ArrayList<Aluno> alunos;
    private ArrayList<String> alunosAdapter;
    private AlertDialog.Builder builder;
    private GoogleApiClient mGoogleApiClient;
    private Date dateTimeInicio, dateTimeFim;
    private MenuItem closeFrequencyAction, openFrequencyAction;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplina_alunos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        disciplina = (Disciplina) i.getSerializableExtra("disciplina");
        alunosLV = (ListView) findViewById(R.id.list_view_lista_alunuos_disciplina);

        // Create the location client to start receiving updates
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(ActivityDisciplinaAlunos.this)
                .addOnConnectionFailedListener(ActivityDisciplinaAlunos.this).build();builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);

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

    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        new abrirChamada().execute();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onBackPressed() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if (openFrequency == true) {
            builder.setMessage("A chamada será encerrada!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new fecharChamada().execute();

                            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
                            startActivity(i);
                            finish();
                        }
                    }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).create().show();
        } else {
            Intent i = new Intent(ActivityDisciplinaAlunos.this, ActivityDisciplinas.class);
            startActivity(i);
            finish();
        }
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
                alunosAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Aluno a = new Aluno(c.getString("ra"), c.getString("nome"), c.getString("telefone"), c.getString("email"), c.getString("mac_address"));
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
            if (!openFrequency) {
                dateTimeInicio = new Date();

                Location mCurrentLocation = null;
                if (ActivityCompat.checkSelfPermission(ActivityDisciplinaAlunos.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityDisciplinaAlunos.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mCurrentLocation != null) {
                    // Print current location if not null
                    Log.d("DEBUG", "current location: " + mCurrentLocation.toString());

                    // Creating service handler class instance
                    ServiceHandler sh = new ServiceHandler();
                    try {
                        List<NameValuePair> param = new ArrayList<NameValuePair>();
                        param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));
                        param.add(new BasicNameValuePair("disciplina", disciplina.getCodigo()));
                        param.add(new BasicNameValuePair("horario_inicio", dateFormat.format(dateTimeInicio) + " " + hourFormat.format(dateTimeInicio)));
                        param.add(new BasicNameValuePair("situacao", "1"));
                        param.add(new BasicNameValuePair("latitude", mCurrentLocation.getLatitude() + ""));
                        param.add(new BasicNameValuePair("longitude", mCurrentLocation.getLongitude() + ""));

                        // Making a request to url and getting response
                        JSONObject jsonObj = new JSONObject(sh.makeServiceCall(Routes.getUrlAbrirChamada(), ServiceHandler.POST, param));
                        idFrequency = jsonObj.getJSONArray("return").getJSONObject(0).getString("id");

                        openFrequency = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            dateTimeFim = dateTimeInicio;
            dateTimeFim.setMinutes((dateTimeInicio.getMinutes() + 10));
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
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("horario_fim", dateFormat.format(dateTimeFim) + " " + hourFormat.format(dateTimeFim)));
            param.add(new BasicNameValuePair("situacao", "0"));
            param.add(new BasicNameValuePair("id", idFrequency));

            // Making a request to url and getting response
            sh.makeServiceCall(Routes.getUrlFecharChamada(), ServiceHandler.PUT, param);
            openFrequency = false;
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
        }
    }

}

package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ifms.tcc.marcus_bruno.tcc.Models.Aluno;
import com.ifms.tcc.marcus_bruno.tcc.Models.Disciplina;
import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.R;
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
import java.util.Timer;

public class ActivityDisciplinaAlunos extends AppCompatActivity {

    private Disciplina DISCIPLINA;
    private boolean CHAMADA_ABERTA;
    private String ID_CHAMADA;
    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;
    private final String URL_BUSCAR_ALUNOS = "http://192.168.1.2:8000/todo/disciplinas/alunos/";
    private final String URL_ABRIR_CHAMADA = "http://192.168.1.2:8000/todo/professor/abrir/chamada/";
    private final String URL_FECHAR_CHAMADA = "http://192.168.1.2:8000/todo/professor/fechar/chamada/";

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    Date dateTimeInicio, dateTimeFim;

    ListView alunosListView;
    private ArrayList<Aluno> alunosList;
    private ArrayList<String> alunosListForAdapter;

    private static AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplina_alunos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        builder = new AlertDialog.Builder(ActivityDisciplinaAlunos.this);


        Intent i = getIntent();
        DISCIPLINA = (Disciplina) i.getSerializableExtra("disciplina");

        alunosListView = (ListView) findViewById(R.id.list_view_lista_alunuos_disciplina);

        Toast.makeText(ActivityDisciplinaAlunos.this, DISCIPLINA.getNome(), Toast.LENGTH_SHORT).show();

        new getAlunosDaDisciplina().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disciplina_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fechar_chamada:
                // User chose the "Settings" item, show the app settings UI...

                builder.setMessage("Tem certeza que deseja encerrar a chamada ?")
                        .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new fecharChamada().execute();
                            }
                        }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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
                param.add(new BasicNameValuePair("id", DISCIPLINA.getCodigo()));

                // Making a request to url and getting response
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(URL_BUSCAR_ALUNOS, ServiceHandler.POST, param));
                alunosList = new ArrayList<>();
                alunosListForAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Aluno a = new Aluno(c.getString("ra"), c.getString("nome"), c.getString("telefone"), c.getString("email"), c.getString("mac_address"));
                    alunosList.add(a);
                    alunosListForAdapter.add(a.getRa() + ": " + a.getNome());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            //Implements the ArrayAdapter after get the data of Web Service.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityDisciplinaAlunos.this, android.R.layout.simple_expandable_list_item_1, alunosListForAdapter);
            alunosListView.setAdapter(adapter);

            //Enviar notificação aos alunos
            new abrirChamada().execute();
        }

        protected void onProgressUpdate(Integer params) {
        }
    }

    public class abrirChamada extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            if(!CHAMADA_ABERTA) {
                dateTimeInicio = new Date();

                // Creating service handler class instance
                ServiceHandler sh = new ServiceHandler();
                try {
                    List<NameValuePair> param = new ArrayList<NameValuePair>();
                    param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));
                    param.add(new BasicNameValuePair("disciplina", DISCIPLINA.getCodigo()));
                    param.add(new BasicNameValuePair("horario_inicio", dateFormat.format(dateTimeInicio) + " " + hourFormat.format(dateTimeInicio)));
                    param.add(new BasicNameValuePair("situacao", "1"));
                    param.add(new BasicNameValuePair("latitude", "0001"));
                    param.add(new BasicNameValuePair("longitude", "0002"));

                    // Making a request to url and getting response
                    JSONObject jsonObj = new JSONObject(sh.makeServiceCall(URL_ABRIR_CHAMADA, ServiceHandler.POST, param));
                    ID_CHAMADA = jsonObj.getJSONArray("return").getJSONObject(0).getString("id");

                    CHAMADA_ABERTA = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            dateTimeFim = dateTimeInicio;
            dateTimeFim.setMinutes((dateTimeInicio.getMinutes() + 2));
        }

        protected void onProgressUpdate(Integer params) {
        }
    }

    public class fecharChamada extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            if(new Date().getMinutes() < dateTimeFim.getMinutes()){
                dateTimeFim = new Date();
            }

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("horario_fim", dateFormat.format(dateTimeFim) + " " + hourFormat.format(dateTimeFim)));
                param.add(new BasicNameValuePair("situacao", "0"));
                param.add(new BasicNameValuePair("id", ID_CHAMADA));

                // Making a request to url and getting response
                JSONObject jsonObj = new JSONObject(sh.makeServiceCall(URL_FECHAR_CHAMADA, ServiceHandler.PUT, param));


                CHAMADA_ABERTA = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
        }

        protected void onProgressUpdate(Integer params) {
        }
    }

}

package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class ActivityDisciplinas extends AppCompatActivity  {

    private int itemSelected;
    private ListView disciplinasLV;
    private AlertDialog.Builder builder;
    private ArrayList<Disciplina> disciplinas;
    private ArrayList<String> disciplinasAdapter;
    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplinas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        disciplinasLV = (ListView) findViewById(R.id.list_view_lista_disciplinas);
        registerForContextMenu(disciplinasLV);
        new getDisciplinas().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disciplinas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        builder = new AlertDialog.Builder(ActivityDisciplinas.this);
        switch (item.getItemId()) {
            case R.id.logout:
                builder.setMessage("Você tem certeza que deseja se desconectar?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent i = new Intent(ActivityDisciplinas.this, ActivityLogin.class);
                                startActivity(i);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
        }
        return true;
    }

    public class getDisciplinas extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));

                // Making a request to url and getting response
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(Routes.getUrlBuscarDisciplinasProfessor(), ServiceHandler.POST, param));
                disciplinas = new ArrayList<>();
                disciplinasAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Disciplina d = new Disciplina(c.getString("codigo"), c.getString("nome"), c.getString("descricao"));
                    disciplinas.add(d);
                    disciplinasAdapter.add(d.getCodigo() + " : " + d.getNome());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            //Implements the ArrayAdapter after get the data of Web Service.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityDisciplinas.this, android.R.layout.simple_expandable_list_item_1, disciplinasAdapter);
            disciplinasLV.setAdapter(adapter);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        itemSelected = acmi.position;

        menu.setHeaderTitle("Opções ");
        //GroupID - ItemId - OrderForId
        menu.add(0, 1, 0, "Realizar Chamada");
        menu.add(0, 2, 1, "Agendar Chamada");
        menu.add(0, 3, 2, "Enviar Notificação aos Alunos");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {

            Intent i = new Intent(ActivityDisciplinas.this, ActivityDisciplinaAlunos.class);
            i.putExtra("disciplina", disciplinas.get(itemSelected));
            startActivity(i);

        } else if (item.getItemId() == 2) {
            Toast.makeText(getApplicationContext(), "Opc 2", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == 3) {
            Toast.makeText(getApplicationContext(), "Opc 3", Toast.LENGTH_LONG).show();
        } else {
            return false;
        }
        return true;
    }
}
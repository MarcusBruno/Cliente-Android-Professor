package com.ifms.tcc.marcus_bruno.tcc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ifms.tcc.marcus_bruno.tcc.Models.Disciplina;
import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;
import com.ifms.tcc.marcus_bruno.tcc.Utils.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityDisciplinas extends AppCompatActivity {

    protected static final Professor PROFESSOR = ActivityLogin.PROFESSOR;
    private static final String URL = "http://192.168.1.9:8000/todo/disciplinas/professor/";
    private ArrayList<Disciplina> disciplinasList;
    private ArrayList<String> disciplinasListForAdapter;

    ListView disciplinasListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplinas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        disciplinasListView = (ListView) findViewById(R.id.list_view_lista_disciplinas);

        registerForContextMenu(disciplinasListView);

        Toast.makeText(ActivityDisciplinas.this, "Olá professor " + PROFESSOR.getNome().split(" ")[0], Toast.LENGTH_LONG).show();

        new getDisciplinas().execute();
    }

    public class getDisciplinas extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {}
        @Override
        protected Integer doInBackground(String... params) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair("rp", PROFESSOR.getRp()));

                // Making a request to url and getting response
                JSONArray jsonObj = new JSONArray(sh.makeServiceCall(URL, ServiceHandler.POST, param));
                disciplinasList = new ArrayList<>();
                disciplinasListForAdapter = new ArrayList<>();

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject c = jsonObj.getJSONObject(i);
                    Disciplina d = new Disciplina(c.getString("codigo"), c.getString("nome"), c.getString("descricao"));
                    disciplinasList.add(d);
                    disciplinasListForAdapter.add(d.getCodigo() + " : " + d.getNome());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            //Implements the ArrayAdapter after get the data of Web Service.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ActivityDisciplinas.this, android.R.layout.simple_expandable_list_item_1, disciplinasListForAdapter);
            disciplinasListView.setAdapter(adapter);
        }

        protected void onProgressUpdate(Integer params) {
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Opções");
        //GroupID - ItemId - OrderForId
        menu.add(0, 1, 0, "Realizar Chamada");
        menu.add(0, 2, 1, "Agendar Chamada");
        menu.add(0, 3, 2, "Enviar Notificação aos Alunos");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Toast.makeText(getApplicationContext(), "Opc 1", Toast.LENGTH_LONG).show();
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
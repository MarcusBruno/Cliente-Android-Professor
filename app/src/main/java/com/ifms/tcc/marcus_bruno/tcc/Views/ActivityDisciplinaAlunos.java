package com.ifms.tcc.marcus_bruno.tcc.Views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.ifms.tcc.marcus_bruno.tcc.Models.Disciplina;
import com.ifms.tcc.marcus_bruno.tcc.R;

public class ActivityDisciplinaAlunos extends AppCompatActivity {

    private Disciplina disciplina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplina_alunos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        disciplina = (Disciplina) i.getSerializableExtra("disciplina");

        Toast.makeText(ActivityDisciplinaAlunos.this, disciplina.getNome(), Toast.LENGTH_SHORT).show();
    }

}

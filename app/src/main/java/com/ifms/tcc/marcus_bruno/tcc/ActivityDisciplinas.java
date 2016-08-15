package com.ifms.tcc.marcus_bruno.tcc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.ifms.tcc.marcus_bruno.tcc.Models.Professor;

public class ActivityDisciplinas extends AppCompatActivity {

    Professor professor = ActivityLogin.PROFESSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplinas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Toast.makeText(ActivityDisciplinas.this, "Olá professor "+ professor.getNome().split(" ")[0], Toast.LENGTH_LONG).show();
    }

}

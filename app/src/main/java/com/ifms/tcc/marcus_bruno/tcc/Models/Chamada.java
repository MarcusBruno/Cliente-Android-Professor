package com.ifms.tcc.marcus_bruno.tcc.Models;

import java.util.Date;

/**
 * Created by marcusbruno on 12/09/16.
 */
public class Chamada {
    private String id;
    private String rp;
    private String professor;
    private String codigoDisciplina;
    private String disciplina;
    private Date timestamp;

    public Chamada(String id, String rp, String professor, String codigoDisciplina, String disciplina, Date timestamp) {
        this.id = id;
        this.rp = rp;
        this.professor = professor;
        this.codigoDisciplina = codigoDisciplina;
        this.disciplina = disciplina;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getRp() {
        return rp;
    }

    public String getProfessor() {
        return professor;
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

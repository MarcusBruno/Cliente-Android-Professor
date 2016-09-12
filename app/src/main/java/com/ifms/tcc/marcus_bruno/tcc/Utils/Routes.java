package com.ifms.tcc.marcus_bruno.tcc.Utils;

/**
 * Created by marcus-bruno on 8/18/16.
 */
public class Routes {
    private static final String URL = "http://192.168.1.12:8000/";
    private static final String URL_LOGIN_PROFESSOR = URL+"todo/login/professor/";
    private static final String URL_BUSCAR_ALUNOS = URL+"todo/disciplinas/alunos/";
    private static final String URL_ABRIR_CHAMADA = URL+"todo/professor/abrir/chamada/";
    private static final String URL_FECHAR_CHAMADA = URL+"todo/professor/fechar/chamada/";
    private static final String URL_BUSCAR_DISCIPLINAS_PROFESSOR = URL+"todo/disciplinas/professor/";
    private static final String URL_ADC_PRESENCA_ALUNOS_MANUAL = URL+"todo/presenca/aluno/manual/";

    public static String getUrlLoginProfessor() {
        return URL_LOGIN_PROFESSOR;
    }

    public static String getUrlBuscarAlunos() {
        return URL_BUSCAR_ALUNOS;
    }

    public static String getUrlAbrirChamada() {
        return URL_ABRIR_CHAMADA;
    }

    public static String getUrlFecharChamada() {
        return URL_FECHAR_CHAMADA;
    }

    public static String getUrlBuscarDisciplinasProfessor() {
        return URL_BUSCAR_DISCIPLINAS_PROFESSOR;
    }

    public static String getUrlAdcPresencaAlunosManual() {
        return URL_ADC_PRESENCA_ALUNOS_MANUAL;
    }
}

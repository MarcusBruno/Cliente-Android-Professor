package com.ifms.tcc.marcus_bruno.tcc.Utils;

/**
 * Created by marcus-bruno on 8/18/16.
 */
public class Routes {

    private static final String URL_LOGIN_PROFESSOR = "http://192.168.1.9:8000/todo/login/professor/";
    private static final String URL_BUSCAR_ALUNOS = "http://192.168.1.9:8000/todo/disciplinas/alunos/";
    private static final String URL_ABRIR_CHAMADA = "http://192.168.1.9:8000/todo/professor/abrir/chamada/";
    private static final String URL_FECHAR_CHAMADA = "http://192.168.1.9:8000/todo/professor/fechar/chamada/";
    private static final String URL_BUSCAR_DISCIPLINAS_PROFESSOR = "http://192.168.1.9:8000/todo/disciplinas/professor/";

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
}

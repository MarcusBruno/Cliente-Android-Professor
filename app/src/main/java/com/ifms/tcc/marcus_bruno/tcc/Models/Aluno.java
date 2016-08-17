package com.ifms.tcc.marcus_bruno.tcc.Models;

/**
 * Created by marcus-bruno on 8/15/16.
 */
public class Aluno {
    private String ra;
    private String nome;
    private String telefone;
    private String email;
    private String mac_address;

    public Aluno(String ra, String nome, String telefone, String email, String mac_address) {
        this.ra = ra;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.mac_address = mac_address;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }
}
